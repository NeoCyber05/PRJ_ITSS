# Request Edit Lock — Design

**Date:** 2026-06-06
**Status:** Approved (pending spec review)

## Problem

When a Sales user opens a request for editing, an International Ordering user
must not be able to process that same request at the same time — otherwise both
sides mutate the same request concurrently and corrupt the data. The reverse
must also hold: while Ordering is processing a request, Sales must not edit it.

This is a **mutual, cross-process lock** on a single `request`.

## Decisions (locked in)

| Decision | Choice |
|---|---|
| Storage | Dedicated DB table `request_edit_lock` (multi-instance: Sales/Ordering run separate app instances against the shared Supabase DB, so an in-memory lock can't work) |
| Direction | Mutual — Sales-edit ⟷ Ordering-process exclude each other |
| Stale handling | `expires_at` TTL + heartbeat renew while the screen is open; expiry auto-recovers after a crash |
| Conflict UX | Badge in the list ("🔒 [name] (role)") **and** a hard check on open |
| Lock owner identity | Current user from `AuthSession` → username + role + display name |
| Clock source | **DB server time (`now()`) only** — never client time, to avoid skew across machines |
| Performance | **Every lock DB call runs off the JavaFX thread** (see Performance section) |

Constants: `LOCK_TTL_SECONDS = 900` (15 min), `HEARTBEAT_SECONDS = 300` (5 min).

## Architecture

Approach A: dedicated table + a new lock sub-module inside the `request`
bounded context, following the existing strict MVC layering enforced by
`MvcDependencyTest`.

### 1. Schema — new migration

`supabase/migrations/20260606xxxxxx_add_request_edit_lock.sql`

```sql
CREATE TABLE IF NOT EXISTS "public"."request_edit_lock" (
    "request_id"     integer NOT NULL,
    "owner_username" varchar(100) NOT NULL,
    "owner_role"     varchar(50)  NOT NULL,
    "owner_display"  varchar(150) NOT NULL,
    "locked_at"      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "expires_at"     timestamp NOT NULL,
    CONSTRAINT request_edit_lock_pkey PRIMARY KEY ("request_id"),
    CONSTRAINT request_edit_lock_request_fk FOREIGN KEY ("request_id")
        REFERENCES "public"."request"("id") ON DELETE CASCADE
);
```

One row per `request_id` (PK) = mutual exclusion for free. FK `ON DELETE
CASCADE` removes a stale lock if the request is deleted. The PK index covers
both single lookups and the `request_id = ANY(?)` badge query, so no extra
index is needed at this data scale.

### 2. Model layer — lock sub-module

`model/request/domain/lock/`
- `RequestLock` (record): `requestId, ownerUsername, ownerRole, ownerDisplay,
  lockedAt, expiresAt`.
- `LockOwner` (record): `username, role, display`. Built by the controller from
  `AuthenticatedUser` — primitives only, so the lock module does **not** import
  `model.auth` (no cross-context coupling).
- `LockResult` (record): `boolean acquired`, `RequestLock holder` (self when
  acquired; the current holder when blocked).

`model/request/application/lock/`
- Port `RequestLockGateway`:
  - `LockResult acquireOrRenew(int requestId, LockOwner owner, int ttlSeconds)`
  - `void release(int requestId, String ownerUsername)`
  - `Map<Integer, RequestLock> findActiveForRequests(Collection<Integer> ids)`
- `RequestLockUseCase`: `acquire(...)`, `renew(...)`, `release(...)`,
  `activeLocks(ids)` + `RequestLockException` wrapping gateway errors.

`model/request/infrastructure/persistence/lock/`
- `JdbcRequestLockRepository implements RequestLockGateway`, extends
  `JdbcRepositorySupport`, obtains connections via `ConnectionProvider`,
  runs the two-statement acquire inside a `TransactionRunner` transaction.

Atomic acquire (server-time authoritative):

```sql
INSERT INTO request_edit_lock(request_id,owner_username,owner_role,owner_display,locked_at,expires_at)
VALUES (?,?,?,?, now(), now() + (?||' seconds')::interval)
ON CONFLICT (request_id) DO UPDATE
   SET owner_username=EXCLUDED.owner_username, owner_role=EXCLUDED.owner_role,
       owner_display=EXCLUDED.owner_display, locked_at=now(), expires_at=EXCLUDED.expires_at
 WHERE request_edit_lock.owner_username=EXCLUDED.owner_username
    OR request_edit_lock.expires_at<=now()
RETURNING owner_username,owner_role,owner_display,locked_at,expires_at;
```

- Row returned → acquired or renewed (the `WHERE` allows the update only when
  the requester already owns the lock, or the existing lock has expired).
- No row returned → blocked by an active lock held by someone else; a follow-up
  `SELECT` in the same transaction reads the holder for the error message.

Release:
```sql
DELETE FROM request_edit_lock WHERE request_id = ? AND owner_username = ?;
```

Badge query (single batched statement, no N+1):
```sql
SELECT request_id, owner_username, owner_role, owner_display, locked_at, expires_at
FROM request_edit_lock
WHERE request_id = ANY(?) AND expires_at > now();
```

### 3. Integration points (controller + view)

**Acquire**
- Sales: `SalesRequestEditSession.start()` acquires with role `SALES`. On block,
  return `StartResult.failed("Yêu cầu đang được [display] ([roleLabel]) cập
  nhật.")` and do not open the popup.
- Ordering: change `RequestProcessingLayoutController.setRequestId()` to return an
  outcome `(ok, message)`, acquiring with role `ORDERING`. On block, the view
  shows the message and returns to the list instead of opening the processing
  screen.

**Heartbeat** — lives only in the **view** layer (`javafx.animation.Timeline`
is allowed there; controller/model stay javafx-free). The Sales edit view and the
Ordering processing view each run a Timeline every `HEARTBEAT_SECONDS` that fires
a background renew (see Performance). Stopped on close.

**Release** (best-effort, swallow + log on failure)
- Sales: on Save success, on Cancel, and on dialog `setOnHidden`.
- Ordering: on submit success and on leaving the processing screen.

**Owner mapping / role labels**: controller maps `AuthenticatedUser` → `LockOwner`.
`roleLabel`: `SALES → "Sales"`, `ORDERING → "Đặt hàng"`.

### 4. Conflict UX — badges

- `ReceivedRequestsController` (Ordering list) and the Sales request list call
  `activeLocks(ids)` as part of their existing list load. Rows locked by someone
  else render a badge `🔒 [display] ([roleLabel])`, reusing
  `view/shared/ui/StatusBadgeFactory` styling.
- Clicking a locked row still goes through the hard acquire check, so a lock that
  was taken between list-load and click is caught race-safely.

### 5. Wiring

`MvcContext` builds `RequestLockUseCase` from
`JdbcRequestLockRepository(connectionProvider)` + the main `TransactionRunner`,
and injects the use case plus `this::currentAuthenticatedUser` into the four
touch points: the Sales edit controller module, `RequestProcessingLayoutController`,
`ReceivedRequestsController`, and the Sales request list controller. No new
FXML-bound view package, so `module-info.java` needs no new `opens`.

## Performance — "no lag"

The hard rule: **no lock DB call ever runs on the JavaFX application thread.**
All of them follow the existing async pattern from `App.warmUpDatabaseConnection`
— a `javafx.concurrent.Task` on a daemon `Thread`, with `setOnSucceeded` /
`setOnFailed` marshalling the result back to the FX thread.

- **Acquire on open**: runs inside the same background Task that loads the
  edit/processing data — one round trip, not a separate blocking call. UI updates
  (open screen vs show "locked" alert) happen in `setOnSucceeded`.
- **Badge query**: folded into the existing list-load background Task as one
  batched `request_id = ANY(?)` query — no extra blocking call, no N+1.
- **Heartbeat**: the Timeline tick only *schedules* a background renew Task; the
  DB write never touches the FX thread. A 5-min cadence with a tiny single-row
  upsert is negligible load.
- **Release**: fire-and-forget on a background Task; failures are logged, never
  surfaced, never block closing the screen.

Net added DB work: one tiny upsert on open, one single-row upsert per 5 min while
open, one batched select per list load, one delete on close. All off-thread.

## Edge cases

- Same user reopens their own lock → re-entrant renew (the `WHERE owner = me`
  branch), never self-blocked.
- Expired lock → the next acquirer steals it via `expires_at <= now()`.
- App crash / kill → lock auto-recovers after TTL.
- Request deleted → FK cascade drops the lock.
- Logout inside an edit/processing screen → screen close fires release first;
  anything missed is covered by TTL expiry.

## Testing

- `RequestLockUseCase` unit tests with a fake `RequestLockGateway`: acquire
  success, blocked-by-other (holder reported), re-entrant renew, release.
- `MvcDependencyTest` must stay green: heartbeat Timeline only in the view layer;
  controller and model remain free of `javafx.*`.

## Out of scope

- Admin "force-unlock" button (TTL handles stale locks).
- Locking entities other than `request`.
- Real-time push of lock changes (badges refresh on list reload, not live).
