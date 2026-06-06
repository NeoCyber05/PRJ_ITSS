# Request Edit Lock — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent concurrent edits: Sales-edit and Ordering-process lock a request exclusively using a dedicated DB table, with heartbeat keep-alive and badges in list views.

**Architecture:** New bounded-context sub-module `model/request/domain/lock` + `model/request/application/lock` + `model/request/infrastructure/persistence/lock`. Controllers acquire/release/renew lock; views run heartbeat via `Timeline` + background thread; list views show lock badge via one batched query folded into existing list reload.

**Tech Stack:** Java 17, JDBC/PostgreSQL (Supabase), JavaFX 17, JUnit 5.

---

## File Map

### New files
| File | Purpose |
|---|---|
| `supabase/migrations/20260606000000_add_request_edit_lock.sql` | Table DDL |
| `model/request/domain/lock/RequestLock.java` | DB row record |
| `model/request/domain/lock/LockOwner.java` | Who holds the lock |
| `model/request/domain/lock/LockResult.java` | acquire outcome |
| `model/request/application/lock/RequestLockGateway.java` | Port interface |
| `model/request/application/lock/RequestLockException.java` | Checked exception |
| `model/request/application/lock/RequestLockUseCase.java` | Use case |
| `model/request/infrastructure/persistence/lock/JdbcRequestLockRepository.java` | JDBC impl |
| `controller/ordering/request/process/state/LockOutcome.java` | acquire outcome for controller |
| `src/test/…/model/request/application/lock/RequestLockUseCaseTest.java` | Unit tests |

### Modified files
| File | Change |
|---|---|
| `model/request/RequestModule.java` | Expose `requestLockUseCase()` |
| `controller/sales/request/update/session/SalesRequestEditSession.java` | Inject lock; acquire on `start()`, release/renew |
| `controller/sales/request/update/SalesRequestEditController.java` | Add `releaseLock()`, `renewLock()`, call `view.startHeartbeat` |
| `controller/sales/request/SalesRequestControllerModule.java` | Accept `Supplier<LockOwner>` |
| `controller/ordering/request/process/session/RequestProcessingSession.java` | Inject lock; `start()` → `LockOutcome`, release/renew |
| `controller/ordering/request/process/RequestProcessingLayoutController.java` | `setRequestId()` → `LockOutcome`, add `releaseLock()`, `renewLock()` |
| `controller/ordering/request/ReceivedRequestsController.java` | Inject lock; expose `activeLocks(ids)` |
| `controller/sales/request/list/SalesRequestListController.java` | Inject lock; expose `activeLocks(ids)` |
| `controller/ordering/request/RequestControllerModule.java` | Accept `Supplier<LockOwner>` |
| `bootstrap/MvcContext.java` | Build lock module; pass `Supplier<LockOwner>` to both controller modules |
| `view/ordering/request/process/layout/RequestProcessingLayoutView.java` | Handle `LockOutcome`, heartbeat, release on back/submit |
| `view/sales/request/update/SalesRequestEditView.java` | Add `startHeartbeat(Runnable)`, stop on close |
| `view/ordering/request/ReceivedRequestsView.java` | Badge in actions column |
| `view/sales/request/list/SalesRequestListView.java` | Badge in actions column |

All paths under `src/main/java/org/itss/prj_itss/` unless stated.

---

## Task 1: DB Migration

**Files:**
- Create: `supabase/migrations/20260606000000_add_request_edit_lock.sql`

- [ ] **Step 1: Write migration file**

```sql
CREATE TABLE IF NOT EXISTS "public"."request_edit_lock" (
    "request_id"     integer      NOT NULL,
    "owner_username" varchar(100) NOT NULL,
    "owner_role"     varchar(150) NOT NULL,
    "owner_display"  varchar(150) NOT NULL,
    "locked_at"      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "expires_at"     timestamp    NOT NULL,
    CONSTRAINT request_edit_lock_pkey PRIMARY KEY ("request_id"),
    CONSTRAINT request_edit_lock_request_fk
        FOREIGN KEY ("request_id") REFERENCES "public"."request"("id") ON DELETE CASCADE
);
```

- [ ] **Step 2: Apply migration**

Run in the Supabase SQL editor or via the Supabase CLI. Verify with:
```sql
SELECT * FROM request_edit_lock LIMIT 1;
```
Expected: empty result, no error.

---

## Task 2: Domain Types

**Files:**
- Create: `src/main/java/org/itss/prj_itss/model/request/domain/lock/LockOwner.java`
- Create: `src/main/java/org/itss/prj_itss/model/request/domain/lock/RequestLock.java`
- Create: `src/main/java/org/itss/prj_itss/model/request/domain/lock/LockResult.java`

- [ ] **Step 1: Create `LockOwner`**

```java
package org.itss.prj_itss.model.request.domain.lock;

public record LockOwner(String username, String role, String display) {
    public LockOwner {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
    }
}
```

- [ ] **Step 2: Create `RequestLock`**

```java
package org.itss.prj_itss.model.request.domain.lock;

import java.time.LocalDateTime;

public record RequestLock(
    int requestId,
    String ownerUsername,
    String ownerRole,
    String ownerDisplay,
    LocalDateTime lockedAt,
    LocalDateTime expiresAt
) {}
```

- [ ] **Step 3: Create `LockResult`**

```java
package org.itss.prj_itss.model.request.domain.lock;

public record LockResult(boolean acquired, RequestLock holder) {
    public static LockResult acquired(RequestLock lock) {
        return new LockResult(true, lock);
    }
    public static LockResult blocked(RequestLock existingHolder) {
        return new LockResult(false, existingHolder);
    }
}
```

- [ ] **Step 4: Compile to verify**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS`.

---

## Task 3: Lock Port, Exception, Use Case

**Files:**
- Create: `src/main/java/org/itss/prj_itss/model/request/application/lock/RequestLockGateway.java`
- Create: `src/main/java/org/itss/prj_itss/model/request/application/lock/RequestLockException.java`
- Create: `src/main/java/org/itss/prj_itss/model/request/application/lock/RequestLockUseCase.java`

- [ ] **Step 1: Create `RequestLockGateway`**

```java
package org.itss.prj_itss.model.request.application.lock;

import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.model.request.domain.lock.LockResult;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;

import java.util.Collection;
import java.util.Map;

public interface RequestLockGateway {
    /**
     * Atomically acquire or renew. Returns the outcome.
     * If the row is owned by someone else and not expired, returns blocked with the current holder.
     */
    LockResult acquireOrRenew(int requestId, LockOwner owner, int ttlSeconds) throws RequestLockException;

    void release(int requestId, String ownerUsername) throws RequestLockException;

    Map<Integer, RequestLock> findActiveForRequests(Collection<Integer> requestIds) throws RequestLockException;
}
```

- [ ] **Step 2: Create `RequestLockException`**

```java
package org.itss.prj_itss.model.request.application.lock;

public class RequestLockException extends Exception {
    public RequestLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 3: Create `RequestLockUseCase`**

```java
package org.itss.prj_itss.model.request.application.lock;

import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.model.request.domain.lock.LockResult;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class RequestLockUseCase {

    public static final int LOCK_TTL_SECONDS = 900;   // 15 min
    public static final int HEARTBEAT_SECONDS = 300;  // 5 min

    private final RequestLockGateway gateway;

    public RequestLockUseCase(RequestLockGateway gateway) {
        this.gateway = Objects.requireNonNull(gateway, "gateway");
    }

    public LockResult acquire(int requestId, LockOwner owner) throws RequestLockException {
        return gateway.acquireOrRenew(requestId, owner, LOCK_TTL_SECONDS);
    }

    public void renew(int requestId, LockOwner owner) throws RequestLockException {
        gateway.acquireOrRenew(requestId, owner, LOCK_TTL_SECONDS);
    }

    public void release(int requestId, String ownerUsername) throws RequestLockException {
        gateway.release(requestId, ownerUsername);
    }

    public Map<Integer, RequestLock> activeLocks(Collection<Integer> requestIds) throws RequestLockException {
        if (requestIds == null || requestIds.isEmpty()) return Map.of();
        return gateway.findActiveForRequests(requestIds);
    }
}
```

- [ ] **Step 4: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS`.

---

## Task 4: JDBC Adapter + RequestModule Wiring

**Files:**
- Create: `src/main/java/org/itss/prj_itss/model/request/infrastructure/persistence/lock/JdbcRequestLockRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/request/RequestModule.java`

- [ ] **Step 1: Create `JdbcRequestLockRepository`**

```java
package org.itss.prj_itss.model.request.infrastructure.persistence.lock;

import org.itss.prj_itss.model.request.application.lock.RequestLockException;
import org.itss.prj_itss.model.request.application.lock.RequestLockGateway;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.model.request.domain.lock.LockResult;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;
import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.JdbcRepositorySupport;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public final class JdbcRequestLockRepository extends JdbcRepositorySupport implements RequestLockGateway {

    public JdbcRequestLockRepository(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public LockResult acquireOrRenew(int requestId, LockOwner owner, int ttlSeconds) throws RequestLockException {
        String upsert = """
            INSERT INTO request_edit_lock(request_id, owner_username, owner_role, owner_display, locked_at, expires_at)
            VALUES (?, ?, ?, ?, now(), now() + (?||' seconds')::interval)
            ON CONFLICT (request_id) DO UPDATE
               SET owner_username = EXCLUDED.owner_username,
                   owner_role     = EXCLUDED.owner_role,
                   owner_display  = EXCLUDED.owner_display,
                   locked_at      = now(),
                   expires_at     = EXCLUDED.expires_at
             WHERE request_edit_lock.owner_username = EXCLUDED.owner_username
                OR request_edit_lock.expires_at <= now()
            RETURNING owner_username, owner_role, owner_display, locked_at, expires_at
            """;

        try (Connection conn = getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);

                RequestLock acquired = null;
                try (PreparedStatement ps = conn.prepareStatement(upsert)) {
                    ps.setInt(1, requestId);
                    ps.setString(2, owner.username());
                    ps.setString(3, owner.role());
                    ps.setString(4, owner.display());
                    ps.setString(5, String.valueOf(ttlSeconds));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            acquired = new RequestLock(
                                requestId,
                                rs.getString("owner_username"),
                                rs.getString("owner_role"),
                                rs.getString("owner_display"),
                                rs.getTimestamp("locked_at").toLocalDateTime(),
                                rs.getTimestamp("expires_at").toLocalDateTime()
                            );
                        }
                    }
                }

                if (acquired != null) {
                    conn.commit();
                    return LockResult.acquired(acquired);
                }

                // Blocked — read current holder
                String select = """
                    SELECT owner_username, owner_role, owner_display, locked_at, expires_at
                    FROM request_edit_lock WHERE request_id = ?
                    """;
                RequestLock holder = null;
                try (PreparedStatement ps = conn.prepareStatement(select)) {
                    ps.setInt(1, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            holder = new RequestLock(
                                requestId,
                                rs.getString("owner_username"),
                                rs.getString("owner_role"),
                                rs.getString("owner_display"),
                                rs.getTimestamp("locked_at").toLocalDateTime(),
                                rs.getTimestamp("expires_at").toLocalDateTime()
                            );
                        }
                    }
                }
                conn.commit();
                return LockResult.blocked(holder);

            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw new RequestLockException("acquireOrRenew failed for requestId=" + requestId, e);
            } finally {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            throw new RequestLockException("Cannot get connection for acquireOrRenew", e);
        }
    }

    @Override
    public void release(int requestId, String ownerUsername) throws RequestLockException {
        String sql = "DELETE FROM request_edit_lock WHERE request_id = ? AND owner_username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setString(2, ownerUsername);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RequestLockException("release failed for requestId=" + requestId, e);
        }
    }

    @Override
    public Map<Integer, RequestLock> findActiveForRequests(Collection<Integer> requestIds) throws RequestLockException {
        if (requestIds.isEmpty()) return Map.of();
        // Build  WHERE request_id = ANY(?) using Array
        String sql = """
            SELECT request_id, owner_username, owner_role, owner_display, locked_at, expires_at
            FROM request_edit_lock
            WHERE request_id = ANY(?) AND expires_at > now()
            """;
        Map<Integer, RequestLock> result = new LinkedHashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Integer[] ids = requestIds.toArray(Integer[]::new);
            ps.setArray(1, conn.createArrayOf("integer", ids));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int reqId = rs.getInt("request_id");
                    result.put(reqId, new RequestLock(
                        reqId,
                        rs.getString("owner_username"),
                        rs.getString("owner_role"),
                        rs.getString("owner_display"),
                        rs.getTimestamp("locked_at").toLocalDateTime(),
                        rs.getTimestamp("expires_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RequestLockException("findActiveForRequests failed", e);
        }
        return result;
    }
}
```

- [ ] **Step 2: Add `requestLockUseCase()` to `RequestModule`**

At the top of `RequestModule.java`, add imports:
```java
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.infrastructure.persistence.lock.JdbcRequestLockRepository;
```

Add field after existing repository fields:
```java
private final RequestLockUseCase requestLockUseCase;
```

At end of the constructor body (after existing initializations):
```java
this.requestLockUseCase = new RequestLockUseCase(
    new JdbcRequestLockRepository(connectionProvider)
);
```

Add accessor at end of class:
```java
public RequestLockUseCase requestLockUseCase() {
    return requestLockUseCase;
}
```

- [ ] **Step 3: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS`.

---

## Task 5: Unit Tests for RequestLockUseCase

**Files:**
- Create: `src/test/java/org/itss/prj_itss/model/request/application/lock/RequestLockUseCaseTest.java`

- [ ] **Step 1: Write tests**

```java
package org.itss.prj_itss.model.request.application.lock;

import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.model.request.domain.lock.LockResult;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RequestLockUseCaseTest {

    private static RequestLock fakeLock(int reqId, String username) {
        return new RequestLock(reqId, username, "Bộ phận bán hàng", username,
            LocalDateTime.now(), LocalDateTime.now().plusMinutes(15));
    }

    @Test
    void acquire_success_returnsAcquired() throws RequestLockException {
        LockOwner owner = new LockOwner("alice", "Bộ phận bán hàng", "Alice");
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            (id, o, ttl) -> LockResult.acquired(fakeLock(id, o.username()))
        ));
        LockResult result = uc.acquire(1, owner);
        assertTrue(result.acquired());
        assertEquals("alice", result.holder().ownerUsername());
    }

    @Test
    void acquire_blocked_returnsBlockedWithHolder() throws RequestLockException {
        LockOwner owner = new LockOwner("bob", "Bộ phận đặt hàng quốc tế", "Bob");
        RequestLock existingHolder = fakeLock(1, "alice");
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            (id, o, ttl) -> LockResult.blocked(existingHolder)
        ));
        LockResult result = uc.acquire(1, owner);
        assertFalse(result.acquired());
        assertEquals("alice", result.holder().ownerUsername());
    }

    @Test
    void acquire_reentrant_sameName_returnsAcquired() throws RequestLockException {
        LockOwner owner = new LockOwner("alice", "Bộ phận bán hàng", "Alice");
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            (id, o, ttl) -> LockResult.acquired(fakeLock(id, o.username()))
        ));
        uc.acquire(1, owner);
        LockResult renew = uc.acquire(1, owner);
        assertTrue(renew.acquired());
    }

    @Test
    void release_callsGateway() throws RequestLockException {
        List<String> released = new ArrayList<>();
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            (id, o, ttl) -> LockResult.acquired(fakeLock(id, o.username())),
            (id, user) -> released.add(user + ":" + id)
        ));
        uc.release(1, "alice");
        assertEquals(List.of("alice:1"), released);
    }

    @Test
    void activeLocks_returnsOnlyActiveEntries() throws RequestLockException {
        RequestLock lock = fakeLock(5, "alice");
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            null, null,
            ids -> ids.contains(5) ? Map.of(5, lock) : Map.of()
        ));
        Map<Integer, RequestLock> active = uc.activeLocks(List.of(5, 6));
        assertTrue(active.containsKey(5));
        assertFalse(active.containsKey(6));
    }

    @Test
    void activeLocks_emptyInput_returnsEmpty() throws RequestLockException {
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(null, null, ids -> Map.of()));
        assertTrue(uc.activeLocks(List.of()).isEmpty());
    }

    // ---- Fake gateway ----

    @FunctionalInterface
    interface AcquireFn {
        LockResult acquire(int id, LockOwner owner, int ttl) throws RequestLockException;
    }

    @FunctionalInterface
    interface ReleaseFn {
        void release(int id, String user) throws RequestLockException;
    }

    @FunctionalInterface
    interface FindFn {
        Map<Integer, RequestLock> find(Collection<Integer> ids) throws RequestLockException;
    }

    static class FakeGateway implements RequestLockGateway {
        private final AcquireFn acquireFn;
        private final ReleaseFn releaseFn;
        private final FindFn findFn;

        FakeGateway(AcquireFn acquireFn) { this(acquireFn, null, null); }

        FakeGateway(AcquireFn acquireFn, ReleaseFn releaseFn) {
            this(acquireFn, releaseFn, null);
        }

        FakeGateway(AcquireFn acquireFn, ReleaseFn releaseFn, FindFn findFn) {
            this.acquireFn = acquireFn;
            this.releaseFn = releaseFn;
            this.findFn = findFn;
        }

        @Override
        public LockResult acquireOrRenew(int requestId, LockOwner owner, int ttlSeconds) throws RequestLockException {
            if (acquireFn == null) return LockResult.acquired(new RequestLock(requestId, owner.username(), owner.role(), owner.display(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(15)));
            return acquireFn.acquire(requestId, owner, ttlSeconds);
        }

        @Override
        public void release(int requestId, String ownerUsername) throws RequestLockException {
            if (releaseFn != null) releaseFn.release(requestId, ownerUsername);
        }

        @Override
        public Map<Integer, RequestLock> findActiveForRequests(Collection<Integer> requestIds) throws RequestLockException {
            if (findFn == null) return Map.of();
            return findFn.find(requestIds);
        }
    }
}
```

- [ ] **Step 2: Run tests**

```bash
.\mvnw.cmd test -Dtest=RequestLockUseCaseTest
```
Expected: `Tests run: 6, Failures: 0, Errors: 0`.

---

## Task 6: LockOutcome Record

**Files:**
- Create: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/state/LockOutcome.java`

- [ ] **Step 1: Create record**

```java
package org.itss.prj_itss.controller.ordering.request.process.state;

public record LockOutcome(boolean acquired, String blockedMessage) {
    public boolean blocked() { return !acquired; }
    public static LockOutcome acquired() { return new LockOutcome(true, null); }
    public static LockOutcome blocked(String message) { return new LockOutcome(false, message); }
}
```

- [ ] **Step 2: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS`.

---

## Task 7: Integrate Lock into SalesRequestEditSession

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/controller/sales/request/update/session/SalesRequestEditSession.java`

- [ ] **Step 1: Add lock imports and fields**

Add at the top of the file (after `package`), alongside existing imports:
```java
import org.itss.prj_itss.model.request.application.lock.RequestLockException;
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import java.util.function.Supplier;
```

Add new fields inside the class after `private final SalesRequestEditUseCase useCase;`:
```java
private final RequestLockUseCase lockUseCase;
private final Supplier<LockOwner> lockOwnerSupplier;
private int lockedRequestId;
private String lockedOwnerUsername;
```

- [ ] **Step 2: Replace constructor**

Replace:
```java
public SalesRequestEditSession(SalesRequestEditUseCase useCase) {
    this.useCase = Objects.requireNonNull(useCase, "useCase");
}
```
With:
```java
public SalesRequestEditSession(
        SalesRequestEditUseCase useCase,
        RequestLockUseCase lockUseCase,
        Supplier<LockOwner> lockOwnerSupplier
) {
    this.useCase = Objects.requireNonNull(useCase, "useCase");
    this.lockUseCase = Objects.requireNonNull(lockUseCase, "lockUseCase");
    this.lockOwnerSupplier = Objects.requireNonNull(lockOwnerSupplier, "lockOwnerSupplier");
}
```

- [ ] **Step 3: Replace `start()` method**

Replace the entire `start()` method with:
```java
public StartResult start(
        SalesRequestEditDialogInput input,
        SalesRequestDialogListener listener
) {
    Objects.requireNonNull(input, "input");
    LockOwner owner = lockOwnerSupplier.get();
    LockResult lockResult;
    try {
        lockResult = lockUseCase.acquire(input.requestId(), owner);
    } catch (RequestLockException e) {
        reset();
        return StartResult.failed("Có lỗi khi kiểm tra khóa yêu cầu.");
    }
    if (!lockResult.acquired()) {
        reset();
        var holder = lockResult.holder();
        String msg = holder != null
            ? "Yêu cầu đang được " + holder.ownerDisplay() + " (" + holder.ownerRole() + ") cập nhật."
            : "Yêu cầu đang bị khóa bởi người dùng khác.";
        return StartResult.failed(msg);
    }
    this.lockedRequestId = input.requestId();
    this.lockedOwnerUsername = owner.username();

    SalesRequestEditData data;
    try {
        data = useCase.loadEditData(input.requestId());
    } catch (SalesRequestEditException exception) {
        releaseLockBackground();
        reset();
        return StartResult.failed("Có lỗi xảy ra khi tải yêu cầu cần cập nhật.");
    }
    if (!data.found()) {
        releaseLockBackground();
        reset();
        return StartResult.failed("Không tìm thấy yêu cầu cần cập nhật.");
    }

    this.listener = listener;
    this.state = useCase.buildEditState(data.form());
    this.merchandiseOptions = data.merchandiseOptions();
    return StartResult.success();
}
```

**Important:** Add this import at the top:
```java
import org.itss.prj_itss.model.request.domain.lock.LockResult;
```

- [ ] **Step 4: Update `handleSave()` — release after save**

In `handleSave()`, AFTER `listener.onSalesRequestSaved(...)` and BEFORE `return SaveResult.saved(...)`, add:
```java
releaseLockBackground();
```

So it becomes:
```java
if (listener != null) {
    listener.onSalesRequestSaved(new SalesRequestSavedEvent(draft.requestId(), draft.requestCode()));
}
releaseLockBackground();
return SaveResult.saved("Cập nhật yêu cầu đặt hàng thành công");
```

- [ ] **Step 5: Update `handleCancel()` — release on cancel**

Replace:
```java
public void handleCancel() {
    if (listener != null && state != null) {
        listener.onSalesRequestEditCancelled(currentDraft().requestId());
    }
}
```
With:
```java
public void handleCancel() {
    releaseLockBackground();
    if (listener != null && state != null) {
        listener.onSalesRequestEditCancelled(currentDraft().requestId());
    }
}
```

- [ ] **Step 6: Add `renewLock()`, `releaseLockBackground()`, update `reset()`**

Add these methods before the existing `reset()` method:
```java
public void renewLock() {
    if (lockedRequestId <= 0 || lockedOwnerUsername == null) return;
    LockOwner owner = lockOwnerSupplier.get();
    try {
        lockUseCase.renew(lockedRequestId, owner);
    } catch (Exception ignored) {}
}

private void releaseLockBackground() {
    if (lockedRequestId <= 0 || lockedOwnerUsername == null) return;
    int id = lockedRequestId;
    String username = lockedOwnerUsername;
    lockedRequestId = 0;
    lockedOwnerUsername = null;
    Thread t = new Thread(() -> {
        try { lockUseCase.release(id, username); } catch (Exception ignored) {}
    }, "lock-release");
    t.setDaemon(true);
    t.start();
}
```

Replace the existing `reset()`:
```java
private void reset() {
    listener = null;
    state = null;
    merchandiseOptions = List.of();
    lockedRequestId = 0;
    lockedOwnerUsername = null;
}
```

- [ ] **Step 7: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS` (there will be a compile error in `SalesRequestEditController` since its constructor changed — fix in Task 8).

---

## Task 8: Update SalesRequestEditController

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/controller/sales/request/update/SalesRequestEditController.java`

- [ ] **Step 1: Add lock imports and update constructor signature**

Add imports:
```java
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import java.util.function.Supplier;
```

Replace the constructor:
```java
public SalesRequestEditController(SalesRequestEditUseCase useCase) {
    this.session = new SalesRequestEditSession(Objects.requireNonNull(useCase, "useCase"));
}
```
With:
```java
public SalesRequestEditController(
        SalesRequestEditUseCase useCase,
        RequestLockUseCase lockUseCase,
        Supplier<LockOwner> lockOwnerSupplier
) {
    this.session = new SalesRequestEditSession(
        Objects.requireNonNull(useCase, "useCase"),
        Objects.requireNonNull(lockUseCase, "lockUseCase"),
        Objects.requireNonNull(lockOwnerSupplier, "lockOwnerSupplier")
    );
}
```

- [ ] **Step 2: Add `renewLock()` method + call `startHeartbeat` on success**

Add method after `cancelRequested()`:
```java
public void renewLock() {
    session.renewLock();
}
```

In `start()`, after `render();` (at end of successful path), add:
```java
view.startHeartbeat(this::renewLock);
```

So the successful path becomes:
```java
render();
view.startHeartbeat(this::renewLock);
```

- [ ] **Step 3: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: compile error in `SalesRequestControllerModule` — will be fixed in Task 11.

---

## Task 9: Integrate Lock into RequestProcessingSession

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/session/RequestProcessingSession.java`

- [ ] **Step 1: Add lock imports and fields**

Add imports:
```java
import org.itss.prj_itss.model.request.application.lock.RequestLockException;
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.model.request.domain.lock.LockResult;
import org.itss.prj_itss.controller.ordering.request.process.state.LockOutcome;
import java.util.function.Supplier;
```

Add fields after `private final RequestProcessingUseCase requestProcessingUseCase;`:
```java
private final RequestLockUseCase lockUseCase;
private final Supplier<LockOwner> lockOwnerSupplier;
private String lockedOwnerUsername;
```

- [ ] **Step 2: Replace constructor**

Replace:
```java
public RequestProcessingSession(RequestProcessingUseCase requestProcessingUseCase) {
    this.requestProcessingUseCase = Objects.requireNonNull(requestProcessingUseCase, "requestProcessingUseCase");
}
```
With:
```java
public RequestProcessingSession(
        RequestProcessingUseCase requestProcessingUseCase,
        RequestLockUseCase lockUseCase,
        Supplier<LockOwner> lockOwnerSupplier
) {
    this.requestProcessingUseCase = Objects.requireNonNull(requestProcessingUseCase, "requestProcessingUseCase");
    this.lockUseCase = Objects.requireNonNull(lockUseCase, "lockUseCase");
    this.lockOwnerSupplier = Objects.requireNonNull(lockOwnerSupplier, "lockOwnerSupplier");
}
```

- [ ] **Step 3: Change `start()` from void to `LockOutcome`**

Replace the entire `start()` method:
```java
public LockOutcome start(int requestId) {
    if (requestId <= 0) {
        return LockOutcome.blocked("ID yêu cầu không hợp lệ.");
    }
    LockOwner owner = lockOwnerSupplier.get();
    LockResult lockResult;
    try {
        lockResult = lockUseCase.acquire(requestId, owner);
    } catch (RequestLockException e) {
        return LockOutcome.blocked("Có lỗi khi kiểm tra khóa yêu cầu.");
    }
    if (!lockResult.acquired()) {
        var holder = lockResult.holder();
        String msg = holder != null
            ? "Yêu cầu đang được " + holder.ownerDisplay() + " (" + holder.ownerRole() + ") cập nhật."
            : "Yêu cầu đang bị khóa.";
        return LockOutcome.blocked(msg);
    }
    this.requestId = requestId;
    this.lockedOwnerUsername = owner.username();
    resetProcessingState();
    loadProcessingData();
    rebuildAllocationSection();
    return LockOutcome.acquired();
}
```

- [ ] **Step 4: Update `resetProcessingState()` to clear lock field**

Inside `resetProcessingState()`, add at the end:
```java
lockedOwnerUsername = null;
```

- [ ] **Step 5: Add `renewLock()` and `releaseLock()`**

Add before `resetProcessingState()`:
```java
public void renewLock() {
    if (requestId <= 0 || lockedOwnerUsername == null) return;
    LockOwner owner = lockOwnerSupplier.get();
    try {
        lockUseCase.renew(requestId, owner);
    } catch (Exception ignored) {}
}

public void releaseLock() {
    if (requestId <= 0 || lockedOwnerUsername == null) return;
    int id = requestId;
    String username = lockedOwnerUsername;
    lockedOwnerUsername = null;
    try {
        lockUseCase.release(id, username);
    } catch (Exception ignored) {}
}
```

Note: `releaseLock()` in the session is called from a background thread (from the view) so it's fine to do the DB call directly there.

- [ ] **Step 6: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: compile error in `RequestProcessingLayoutController` (will be fixed next).

---

## Task 10: Update RequestProcessingLayoutController

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/process/RequestProcessingLayoutController.java`

- [ ] **Step 1: Add lock imports and update constructor**

Add imports:
```java
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.controller.ordering.request.process.state.LockOutcome;
import java.util.function.Supplier;
```

Replace constructor:
```java
public RequestProcessingLayoutController(RequestProcessingUseCase requestProcessingUseCase) {
    this.session = new RequestProcessingSession(Objects.requireNonNull(requestProcessingUseCase, "requestProcessingUseCase"));
}
```
With:
```java
public RequestProcessingLayoutController(
        RequestProcessingUseCase requestProcessingUseCase,
        RequestLockUseCase lockUseCase,
        Supplier<LockOwner> lockOwnerSupplier
) {
    this.session = new RequestProcessingSession(
        Objects.requireNonNull(requestProcessingUseCase, "requestProcessingUseCase"),
        Objects.requireNonNull(lockUseCase, "lockUseCase"),
        Objects.requireNonNull(lockOwnerSupplier, "lockOwnerSupplier")
    );
}
```

- [ ] **Step 2: Change `setRequestId()` return type and add helpers**

Replace:
```java
public void setRequestId(int requestId) {
    session.start(requestId);
}
```
With:
```java
public LockOutcome setRequestId(int requestId) {
    return session.start(requestId);
}

public void renewLock() {
    session.renewLock();
}

public void releaseLock() {
    session.releaseLock();
}
```

- [ ] **Step 3: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: compile error in `RequestControllerModule` and the view — both fixed in upcoming tasks.

---

## Task 11: Inject Lock into ReceivedRequestsController and SalesRequestListController

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/ReceivedRequestsController.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/sales/request/list/SalesRequestListController.java`

- [ ] **Step 1: Update `ReceivedRequestsController`**

Replace the entire file content:
```java
package org.itss.prj_itss.controller.ordering.request;

import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.model.request.application.listing.RequestRow;
import org.itss.prj_itss.model.request.application.lock.RequestLockException;
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ReceivedRequestsController {

    private final ReceivedRequestsApplicationService receivedRequestsService;
    private final RequestLockUseCase lockUseCase;

    public ReceivedRequestsController(
            ReceivedRequestsApplicationService receivedRequestsService,
            RequestLockUseCase lockUseCase
    ) {
        this.receivedRequestsService = receivedRequestsService;
        this.lockUseCase = Objects.requireNonNull(lockUseCase, "lockUseCase");
    }

    public List<RequestRow> findRows() {
        return receivedRequestsService.findRows();
    }

    public Map<Integer, RequestLock> activeLocks(Collection<Integer> requestIds) {
        try {
            return lockUseCase.activeLocks(requestIds);
        } catch (RequestLockException e) {
            return Map.of();
        }
    }
}
```

- [ ] **Step 2: Read the current `SalesRequestListController`**

Read file at `src/main/java/org/itss/prj_itss/controller/sales/request/list/SalesRequestListController.java`.

- [ ] **Step 3: Update `SalesRequestListController`**

Add imports:
```java
import org.itss.prj_itss.model.request.application.lock.RequestLockException;
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
```

Add field:
```java
private final RequestLockUseCase lockUseCase;
```

Update constructor to accept `RequestLockUseCase`:
```java
public SalesRequestListController(
        ReceivedRequestsApplicationService receivedRequestsService,
        RequestLockUseCase lockUseCase
) {
    this.receivedRequestsService = receivedRequestsService;
    this.lockUseCase = Objects.requireNonNull(lockUseCase, "lockUseCase");
}
```

Add method:
```java
public Map<Integer, RequestLock> activeLocks(Collection<Integer> requestIds) {
    try {
        return lockUseCase.activeLocks(requestIds);
    } catch (RequestLockException e) {
        return Map.of();
    }
}
```

- [ ] **Step 4: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: compile errors in both controller modules — fixed next.

---

## Task 12: Update Controller Modules and MvcContext

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/controller/sales/request/SalesRequestControllerModule.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/request/RequestControllerModule.java`
- Modify: `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`

- [ ] **Step 1: Update `SalesRequestControllerModule`**

Add imports:
```java
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import java.util.function.Supplier;
```

Replace constructor signature and body — change:
```java
public SalesRequestControllerModule(RequestModule requestModule) {
    this.salesRequestListController =
        new SalesRequestListController(requestModule.receivedRequestsApplicationService());
    ...
    this.salesRequestEditController =
        new SalesRequestEditController(
            new SalesRequestEditUseCase(
                new SalesRequestEditServiceGateway(
                    requestModule.salesRequestQueryService(),
                    requestModule.salesRequestCommandService()
                ),
                new SalesRequestEditMapper(),
                new SalesRequestEditValidator()
            )
        );
    ...
}
```
To:
```java
public SalesRequestControllerModule(
        RequestModule requestModule,
        Supplier<LockOwner> lockOwnerSupplier
) {
    this.salesRequestListController =
        new SalesRequestListController(
            requestModule.receivedRequestsApplicationService(),
            requestModule.requestLockUseCase()
        );
    this.salesRequestCreationController =
        new SalesRequestCreationController(
            requestModule.salesRequestQueryService(),
            requestModule.createSalesRequestUseCase()
        );
    this.salesRequestEditController =
        new SalesRequestEditController(
            new SalesRequestEditUseCase(
                new SalesRequestEditServiceGateway(
                    requestModule.salesRequestQueryService(),
                    requestModule.salesRequestCommandService()
                ),
                new SalesRequestEditMapper(),
                new SalesRequestEditValidator()
            ),
            requestModule.requestLockUseCase(),
            lockOwnerSupplier
        );
    this.viewOrderRequestController =
        new ViewOrderRequestController(requestModule.salesRequestQueryService());
}
```

- [ ] **Step 2: Update `RequestControllerModule`**

Add imports:
```java
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import java.util.function.Supplier;
```

Replace constructor:
```java
public RequestControllerModule(RequestModule requestModule, OrderModule orderModule) {
    this.requestModule = requestModule;
    this.receivedRequestsController =
        new ReceivedRequestsController(requestModule.receivedRequestsApplicationService());
    ...
}
```
With:
```java
public RequestControllerModule(
        RequestModule requestModule,
        OrderModule orderModule,
        Supplier<LockOwner> lockOwnerSupplier
) {
    this.requestModule = requestModule;
    this.receivedRequestsController =
        new ReceivedRequestsController(
            requestModule.receivedRequestsApplicationService(),
            requestModule.requestLockUseCase()
        );
    this.requestDetailPopupController = new RequestDetailPopupController(
        requestModule.receivedRequestDetailApplicationService(),
        orderModule.orderCancellationApplicationService()
    );
    this.lockOwnerSupplier = lockOwnerSupplier;
}
```

Also add field `private final Supplier<LockOwner> lockOwnerSupplier;` and update `requestProcessingLayoutController()`:
```java
public RequestProcessingLayoutController requestProcessingLayoutController() {
    return new RequestProcessingLayoutController(
        requestModule.requestProcessingUseCase(),
        requestModule.requestLockUseCase(),
        lockOwnerSupplier
    );
}
```

- [ ] **Step 3: Update `MvcContext` — add lock owner supplier helper and update module constructors**

Add import:
```java
import org.itss.prj_itss.model.request.domain.lock.LockOwner;
```

Add private helper method:
```java
private LockOwner currentLockOwner() {
    var user = currentAuthenticatedUser();
    if (user == null) throw new IllegalStateException("No authenticated user");
    return new LockOwner(user.username(), user.role().getName(), user.displayName());
}
```

Find the line creating `salesRequestControllers`:
```java
private final SalesRequestControllerModule salesRequestControllers =
    new SalesRequestControllerModule(requestModule);
```
Change to:
```java
private final SalesRequestControllerModule salesRequestControllers =
    new SalesRequestControllerModule(requestModule, this::currentLockOwner);
```

Find the line creating `requestControllers`:
```java
new RequestControllerModule(requestModule, orderModule)
```
Change to:
```java
new RequestControllerModule(requestModule, orderModule, this::currentLockOwner)
```

- [ ] **Step 4: Compile everything**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS`. If there are residual errors, read the error output and fix import/type mismatches.

---

## Task 13: Update Views — Heartbeat in SalesRequestEditView

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/view/sales/request/update/SalesRequestEditView.java`

- [ ] **Step 1: Add Timeline imports and field**

Add imports:
```java
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
```

Add field after `private Runnable closeHandler;`:
```java
private Timeline heartbeatTimeline;
```

- [ ] **Step 2: Add `startHeartbeat(Runnable)` method**

Add method after `setCloseHandler`:
```java
public void startHeartbeat(Runnable renewTask) {
    heartbeatTimeline = new Timeline(
        new KeyFrame(Duration.seconds(300), e -> {
            Thread t = new Thread(renewTask, "lock-heartbeat");
            t.setDaemon(true);
            t.start();
        })
    );
    heartbeatTimeline.setCycleCount(Animation.INDEFINITE);
    heartbeatTimeline.play();
}
```

- [ ] **Step 3: Modify `close()` to stop heartbeat**

Find the existing `close()` method and prepend heartbeat stop:
```java
public void close() {
    if (heartbeatTimeline != null) {
        heartbeatTimeline.stop();
        heartbeatTimeline = null;
    }
    if (closeHandler != null) {
        closeHandler.run();
    }
}
```

- [ ] **Step 4: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS`.

---

## Task 14: Update Views — LockOutcome Handling + Heartbeat in RequestProcessingLayoutView

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/process/layout/RequestProcessingLayoutView.java`

- [ ] **Step 1: Add imports and heartbeat field**

Add imports:
```java
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.itss.prj_itss.controller.ordering.request.process.state.LockOutcome;
```

Add field:
```java
private Timeline heartbeatTimeline;
```

- [ ] **Step 2: Replace `setRequestId()` to handle lock outcome**

Replace:
```java
public void setRequestId(int requestId) {
    controller.setRequestId(requestId);
    renderProcessingScreen();
}
```
With:
```java
public void setRequestId(int requestId) {
    LockOutcome outcome = controller.setRequestId(requestId);
    if (outcome.blocked()) {
        showLockError(outcome.blockedMessage());
        goBack();
        return;
    }
    renderProcessingScreen();
    startHeartbeat();
}

private void showLockError(String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("Không thể xử lý");
    alert.setHeaderText(null);
    alert.setContentText(message != null ? message : "Yêu cầu đang bị khóa.");
    alert.showAndWait();
}

private void startHeartbeat() {
    heartbeatTimeline = new Timeline(
        new KeyFrame(Duration.seconds(300), e -> {
            Thread t = new Thread(controller::renewLock, "lock-heartbeat");
            t.setDaemon(true);
            t.start();
        })
    );
    heartbeatTimeline.setCycleCount(Animation.INDEFINITE);
    heartbeatTimeline.play();
}

private void stopHeartbeatAndRelease() {
    if (heartbeatTimeline != null) {
        heartbeatTimeline.stop();
        heartbeatTimeline = null;
    }
    Thread t = new Thread(controller::releaseLock, "lock-release");
    t.setDaemon(true);
    t.start();
}
```

- [ ] **Step 3: Update `goBack()` to release lock**

Replace:
```java
@FXML
private void goBack() {
    navigateToView.accept("received-requests");
}
```
With:
```java
@FXML
private void goBack() {
    stopHeartbeatAndRelease();
    navigateToView.accept("received-requests");
}
```

- [ ] **Step 4: Update `showPreviewDialog()` to release on confirm**

Replace:
```java
private void showPreviewDialog(List<ProcessingPreviewOrder> previewOrders) {
    new RequestProcessingPreviewDialog(
        () -> navigateToView.accept("received-requests"),
        new RequestProcessingPreviewDialogController(controller, previewOrders)
    ).show(itemsTableContainer);
}
```
With:
```java
private void showPreviewDialog(List<ProcessingPreviewOrder> previewOrders) {
    new RequestProcessingPreviewDialog(
        () -> {
            stopHeartbeatAndRelease();
            navigateToView.accept("received-requests");
        },
        new RequestProcessingPreviewDialogController(controller, previewOrders)
    ).show(itemsTableContainer);
}
```

- [ ] **Step 5: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS`.

---

## Task 15: Badge in ReceivedRequestsView

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/ReceivedRequestsView.java`

- [ ] **Step 1: Add import and activeLocks field**

Add import:
```java
import org.itss.prj_itss.model.request.domain.lock.RequestLock;
import java.util.Map;
import java.util.stream.Collectors;
```

Add field after `private ReceivedRequestsController controller;`:
```java
private Map<Integer, RequestLock> activeLocks = Map.of();
```

- [ ] **Step 2: Update `reload()` to fetch active locks**

Replace:
```java
private void reload() {
    if (controller == null) return;
    rows.setAll(controller.findRows());
    applyFilters();
}
```
With:
```java
private void reload() {
    if (controller == null) return;
    List<RequestRow> rowList = controller.findRows();
    rows.setAll(rowList);
    if (!rowList.isEmpty()) {
        List<Integer> ids = rowList.stream().map(RequestRow::requestId).toList();
        activeLocks = controller.activeLocks(ids);
    } else {
        activeLocks = Map.of();
    }
    applyFilters();
    requestTable.refresh();
}
```

- [ ] **Step 3: Show lock badge in actions column cell factory**

Inside `actionsColumn.setCellFactory(column -> new TableCell<>() {` block, find the part after `if (empty || row == null)` check. BEFORE adding `detailButton`, add:

```java
RequestLock lock = activeLocks.get(row.requestId());
if (lock != null) {
    Label lockLabel = new Label("🔒 " + lock.ownerDisplay() + " (" + lock.ownerRole() + ")");
    lockLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #c05000; -fx-padding: 2 6 2 2;");
    actions.getChildren().add(lockLabel);
}
```

The full `updateItem` becomes:
```java
@Override
protected void updateItem(RequestRow row, boolean empty) {
    super.updateItem(row, empty);
    if (empty || row == null) {
        setGraphic(null);
        return;
    }

    HBox actions = new HBox(8);
    actions.setAlignment(Pos.CENTER_LEFT);

    RequestLock lock = activeLocks.get(row.requestId());
    if (lock != null) {
        Label lockLabel = new Label("🔒 " + lock.ownerDisplay() + " (" + lock.ownerRole() + ")");
        lockLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #c05000; -fx-padding: 2 6 2 2;");
        actions.getChildren().add(lockLabel);
    }

    Button detailButton = new Button("Chi tiết");
    detailButton.getStyleClass().add("forest-secondary-button");
    detailButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
    detailButton.setOnAction(event -> RequestDetailPopup.show(
        requestTable.getScene() == null ? null : requestTable.getScene().getWindow(),
        row.requestCode(),
        detailContext
    ));
    actions.getChildren().add(detailButton);

    if (OrderingFormatters.STATUS_PENDING.equals(OrderingFormatters.normalizeStatusKey(row.status()))) {
        Button processButton = new Button("Xử lý");
        processButton.getStyleClass().add("forest-dark-button");
        processButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
        processButton.setOnAction(event -> {
            if (navigator != null) {
                navigator.showView("request-processing:" + row.requestId());
            }
        });
        actions.getChildren().add(processButton);
    }

    setGraphic(actions);
    setText(null);
}
```

- [ ] **Step 4: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS`.

---

## Task 16: Badge in SalesRequestListView

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/view/sales/request/list/SalesRequestListView.java`

- [ ] **Step 1: Add import and activeLocks field**

Add imports:
```java
import org.itss.prj_itss.model.request.domain.lock.RequestLock;
```

Add field after `private SalesRequestListController controller;`:
```java
private Map<Integer, RequestLock> activeLocks = Map.of();
```

(`Map` and `List` imports should already be present; verify and add if missing.)

- [ ] **Step 2: Find where rows are loaded and add lock query**

Search for the method that calls `controller.getRequests()` or similar. Look for the reload/refresh logic. Add lock query after loading rows:

```java
// After loading rowList from controller:
if (!rowList.isEmpty()) {
    List<Integer> ids = rowList.stream().map(RequestRow::requestId).toList();
    activeLocks = controller.activeLocks(ids);
} else {
    activeLocks = Map.of();
}
requestTable.refresh();
```

- [ ] **Step 3: Add lock badge in the actionsColumn cell factory**

In the `actionsColumn.setCellFactory` block, after `if (empty || row == null)` check and before adding action buttons, add:

```java
RequestLock lock = activeLocks.get(row.requestId());
if (lock != null) {
    Label lockLabel = new Label("🔒 " + lock.ownerDisplay() + " (" + lock.ownerRole() + ")");
    lockLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #c05000; -fx-padding: 2 6 2 2;");
    // Add lockLabel to the actions HBox before other buttons
}
```

- [ ] **Step 4: Compile**

```bash
.\mvnw.cmd -DskipTests compile
```
Expected: `BUILD SUCCESS`.

---

## Task 17: Run Full Test Suite + Verify MvcDependencyTest

- [ ] **Step 1: Run all tests**

```bash
.\mvnw.cmd test
```
Expected: all tests pass, especially:
- `RequestLockUseCaseTest` — 6 tests
- `MvcDependencyTest` — passes (no javafx in controller, no persistence in view)
- All pre-existing tests unchanged

- [ ] **Step 2: If MvcDependencyTest fails — diagnose**

The test checks:
1. `controller.*` must NOT import `view.*` or `javafx.*`
2. `view.*` must NOT import `*.persistence.*`, `Jdbc*`, or `model.shared.database.*`

If it fails, read the failure message to find which file has the illegal import and remove it.

Common pitfalls:
- Added `javafx.animation.Timeline` in a controller class → move that logic to the view
- Imported `JdbcRequestLockRepository` in a view → views must never import persistence classes

- [ ] **Step 3: Commit when all tests green**

```bash
git add supabase/migrations/ src/
git commit -m "feat(request): mutual edit lock with DB table, heartbeat, and list badges"
```

---

## Self-Review Notes

**Spec coverage check:**
- ✅ DB table with FK cascade — Task 1
- ✅ `LockOwner` as pure value type, no cross-context coupling — Task 2
- ✅ Atomic upsert via `RETURNING`, server-time only — Task 4
- ✅ `acquireOrRenew` re-entrant for same owner — SQL `WHERE owner_username = EXCLUDED.owner_username`
- ✅ Expired lock stolen via `expires_at <= now()` — same SQL
- ✅ Sales acquire on `start()`, blocks with holder message — Task 7
- ✅ Ordering acquire on `setRequestId()`, `LockOutcome` propagates to view — Tasks 9–10
- ✅ Heartbeat Timeline in views, renew via background thread — Tasks 13–14
- ✅ Release on Save/Cancel/goBack/submit — Tasks 7, 8, 14
- ✅ Fire-and-forget background release — Task 7 (`releaseLockBackground()`)
- ✅ Batch badge query folded into list reload, no N+1 — Tasks 15–16
- ✅ `MvcDependencyTest` compliance: Timeline only in view layer — Tasks 13–14
- ✅ No extra round trips: acquire + load in same sync sequence — Tasks 7, 9

**Type consistency check:** All references use `LockOutcome` (not `LockResult`) at the controller/view boundary. `LockResult` stays in model layer. `LockOwner` constructed from `AuthenticatedUser` in `MvcContext.currentLockOwner()` — single mapping point.
