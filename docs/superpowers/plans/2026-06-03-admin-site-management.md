# Admin And Ordering Site Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build account management for Admin and site management plus Site-account provisioning for the International Ordering Department.

**Architecture:** Keep the existing JavaFX MVC shape: business rules stay under `model`, flow coordination stays under `controller`, JavaFX state stays under `view`, and wiring stays in `bootstrap/MvcContext`. Use application services plus narrow ports so login does not depend on account-management methods, and site management does not reach directly into JDBC or JavaFX.

**Tech Stack:** Java 17, JavaFX 17, Maven, JUnit 5, PostgreSQL/Supabase JDBC, manual DI through `MvcContext`.

---

## Current Baseline

- Requirement source: `BussinessLogic.md`.
- Existing ordering site route: `site-management`.
- Existing ordering site screen: `src/main/java/org/itss/prj_itss/view/ordering/site/SiteManagementView.java`.
- Existing add-site behavior is a placeholder alert, so this feature should replace it.
- Existing Admin behavior routes to `role-workspace` and says the Admin module is not implemented.
- Existing auth table supports Site accounts already: `account.site_id` FK to `site.id`.
- Existing MVC guardrail: `src/test/java/org/itss/prj_itss/architecture/MvcDependencyTest.java`.
- Existing composition rule from `CLAUDE.md`: register new modules, controllers, and routes in `MvcContext`; do not instantiate services inside views.

## Business Split

- Admin manages internal system accounts only: create, edit, disable, and cancel/delete accounts, excluding Site accounts.
- International Ordering Department manages Site records and creates accounts for Site users.
- A Site account must have role `SITE` and must be linked to exactly one `site.id`.
- An internal account must not have role `SITE` and should have `site_id = null`.
- Account cancellation should be soft-delete by status, not physical delete, because the username is unique and order/request history may still need a stable audit trail.

## Acceptance Criteria

- Admin users land on `account-management`, see only non-Site accounts, and can create, update, disable, and cancel/delete internal accounts.
- Admin cannot create role `SITE` accounts from the Admin screen.
- Ordering users keep access to `site-management` and can create/update Site records.
- Ordering users can create a Site account for a Site; the created account has role `SITE` and the correct `site_id`.
- Site creation with account provisioning runs inside one main-DB transaction.
- Login still uses the current authentication behavior and rejects disabled/deleted accounts.
- MVC tests pass: model has no JavaFX/controller/view imports, controller has no JavaFX imports, view has no persistence/JDBC imports.

## Non-Goals

- Do not implement Site self-service inventory/order confirmation in this plan.
- Do not implement Warehouse or Sales changes.
- Do not migrate password hashing in this plan. Current `AuthenticationService` compares the stored password directly; changing that requires a separate compatibility migration.
- Do not add a dashboard. `BussinessLogic.md` explicitly says the app has Home, not Dashboard.

---

## File Map

### Auth And Admin Account Management

- Create `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountDraft.java`
- Create `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountManagementResult.java`
- Create `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountManagementSnapshot.java`
- Create `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountRow.java`
- Create `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountManagementService.java`
- Create `src/main/java/org/itss/prj_itss/model/auth/application/port/AccountManagementRepository.java`
- Modify `src/main/java/org/itss/prj_itss/model/auth/AuthModule.java`
- Modify `src/main/java/org/itss/prj_itss/model/auth/domain/RoleType.java`
- Modify `src/main/java/org/itss/prj_itss/model/auth/infrastructure/persistence/JdbcAccountRepository.java`
- Create `src/main/java/org/itss/prj_itss/controller/admin/account/AccountManagementController.java`
- Create `src/main/java/org/itss/prj_itss/controller/admin/account/AdminControllerModule.java`
- Create `src/main/java/org/itss/prj_itss/view/admin/account/AccountManagementView.java`
- Create `src/main/java/org/itss/prj_itss/view/admin/account/account-management-view.fxml`

### Ordering Site Management

- Create `src/main/java/org/itss/prj_itss/model/site/application/SiteDraft.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/SiteManagementResult.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/SiteAccountDraft.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/port/SiteCommandRepository.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/port/SiteAccountProvisioningPort.java`
- Modify `src/main/java/org/itss/prj_itss/model/site/application/SiteManagementApplicationService.java`
- Modify `src/main/java/org/itss/prj_itss/model/site/SiteModule.java`
- Modify `src/main/java/org/itss/prj_itss/model/site/infrastructure/persistence/JdbcSiteRepository.java`
- Modify `src/main/java/org/itss/prj_itss/controller/ordering/site/SiteManagementController.java`
- Modify `src/main/java/org/itss/prj_itss/view/ordering/site/SiteManagementView.java`
- Modify `src/main/java/org/itss/prj_itss/view/ordering/site/site-management-view.fxml`

### Navigation, Access, Resources

- Modify `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`
- Modify `src/main/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicy.java`
- Modify `src/main/java/org/itss/prj_itss/view/auth/RoleWorkspaceContentFactory.java`
- Modify `src/main/java/org/itss/prj_itss/view/layout/MainLayoutView.java`
- Modify `src/main/java/org/itss/prj_itss/view/layout/main-layout.fxml`
- Modify `src/main/java/module-info.java`
- Modify `src/main/resources/org/itss/prj_itss/styles/main-style.css` only if existing styles are not enough.

### Database And Tests

- Create `supabase/migrations/20260603090000_seed_roles_and_account_status.sql`
- Create `src/test/java/org/itss/prj_itss/model/auth/application/management/AccountManagementServiceTest.java`
- Create `src/test/java/org/itss/prj_itss/model/site/application/SiteManagementApplicationServiceTest.java`
- Modify `src/test/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicyTest.java`
- Keep `src/test/java/org/itss/prj_itss/architecture/MvcDependencyTest.java` as the final boundary test.

---

## Task 1: Normalize Roles And Status Values

**Files:**
- Create: `supabase/migrations/20260603090000_seed_roles_and_account_status.sql`
- Modify: none

- [ ] **Step 1: Add the migration**

```sql
INSERT INTO public.role (id, name) VALUES
    (1, 'Quản trị viên'),
    (2, 'Bộ phận bán hàng'),
    (3, 'Bộ phận đặt hàng quốc tế'),
    (4, 'Site'),
    (5, 'Bộ phận quản lý kho')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval(
    'public.role_id_seq',
    GREATEST((SELECT COALESCE(MAX(id), 1) FROM public.role), 1),
    true
);

ALTER TABLE public.account
    ALTER COLUMN status SET DEFAULT 'active';

UPDATE public.account
SET status = 'active'
WHERE status IS NULL OR TRIM(status) = '';
```

- [ ] **Step 2: Do not add a role-name uniqueness constraint yet**

Reason: the remote schema only has `role_pkey`; adding a name constraint can fail if old data already contains duplicate labels.

- [ ] **Step 3: Verify migration syntax by inspection**

Expected: no new tables are required. The current `account`, `role`, `site`, and `site_inventory` tables are enough.

---

## Task 2: Add Account Management Model Slice

**Files:**
- Create: `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountDraft.java`
- Create: `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountManagementResult.java`
- Create: `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountManagementSnapshot.java`
- Create: `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountRow.java`
- Create: `src/main/java/org/itss/prj_itss/model/auth/application/management/AccountManagementService.java`
- Create: `src/main/java/org/itss/prj_itss/model/auth/application/port/AccountManagementRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/auth/domain/RoleType.java`
- Modify: `src/main/java/org/itss/prj_itss/model/auth/AuthModule.java`
- Modify: `src/main/java/org/itss/prj_itss/model/auth/infrastructure/persistence/JdbcAccountRepository.java`
- Test: `src/test/java/org/itss/prj_itss/model/auth/application/management/AccountManagementServiceTest.java`

- [ ] **Step 1: Write failing service tests**

Key cases:

```java
@Test
void adminCreateInternalAccountRejectsSiteRole() {
    FakeAccountManagementRepository repository = new FakeAccountManagementRepository();
    AccountManagementService service = new AccountManagementService(repository);

    AccountDraft draft = new AccountDraft(
        "site-01",
        "secret",
        "Tokyo Site",
        RoleType.SITE.id(),
        null
    );

    AccountManagementResult result = service.createInternalAccount(draft);

    assertFalse(result.success());
    assertEquals("Admin không được tạo tài khoản Site.", result.message());
    assertTrue(repository.createdAccounts.isEmpty());
}

@Test
void adminCreateInternalAccountStoresNullSiteId() {
    FakeAccountManagementRepository repository = new FakeAccountManagementRepository();
    AccountManagementService service = new AccountManagementService(repository);

    AccountManagementResult result = service.createInternalAccount(new AccountDraft(
        "sales01",
        "secret",
        "Nhân viên bán hàng",
        RoleType.SALES.id(),
        9
    ));

    assertTrue(result.success());
    assertNull(repository.createdAccounts.get(0).siteId());
}

@Test
void disabledAccountGetsDisabledStatus() {
    FakeAccountManagementRepository repository = new FakeAccountManagementRepository();
    repository.accounts.add(new Account(7, "ordering01", "secret", "Ordering", "active", RoleType.ORDERING.id(), null));
    AccountManagementService service = new AccountManagementService(repository);

    AccountManagementResult result = service.disableAccount(7);

    assertTrue(result.success());
    assertEquals("disabled", repository.statusByAccountId.get(7));
}
```

- [ ] **Step 2: Add `RoleType` helpers**

Add:

```java
public int id() {
    return id;
}

public boolean isAdminRole() {
    return this == ADMIN;
}

public boolean isSiteRole() {
    return this == SITE;
}

public boolean isInternalUserRole() {
    return this == ADMIN || this == SALES || this == ORDERING || this == WAREHOUSE;
}

public static RoleType fromRoleId(int roleId) {
    for (RoleType roleType : values()) {
        if (roleType.id == roleId) {
            return roleType;
        }
    }
    return UNKNOWN;
}
```

- [ ] **Step 3: Add account management records**

Use immutable records:

```java
public record AccountDraft(
    String username,
    String password,
    String fullName,
    int roleId,
    Integer siteId
) {}
```

```java
public record AccountRow(
    int accountId,
    String username,
    String fullName,
    String roleName,
    String status
) {
    public boolean matchesKeyword(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank()
            || contains(username, normalized)
            || contains(fullName, normalized)
            || contains(roleName, normalized)
            || contains(status, normalized);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
```

```java
public record AccountManagementResult(boolean success, String message, Integer accountId) {
    public static AccountManagementResult success(String message, Integer accountId) {
        return new AccountManagementResult(true, message, accountId);
    }

    public static AccountManagementResult failure(String message) {
        return new AccountManagementResult(false, message, null);
    }
}
```

```java
public record AccountManagementSnapshot(
    List<AccountRow> rows,
    List<Role> assignableRoles,
    int activeCount,
    int disabledCount
) {
    public AccountManagementSnapshot {
        rows = rows == null ? List.of() : List.copyOf(rows);
        assignableRoles = assignableRoles == null ? List.of() : List.copyOf(assignableRoles);
    }
}
```

- [ ] **Step 4: Add a narrow account-management repository port**

Do not add these methods to `AccountRepository`; that would make `AuthenticationService` depend on management methods.

```java
public interface AccountManagementRepository {
    List<AuthenticatedUser> findAllUsers();
    List<Role> findRoles();
    Optional<AuthenticatedUser> findUserById(int accountId);
    Optional<AuthenticatedUser> findUserByUsername(String username);
    int createAccount(AccountDraft draft);
    void updateAccount(int accountId, AccountDraft draft);
    void updateStatus(int accountId, String status);
}
```

- [ ] **Step 5: Implement `AccountManagementService`**

Rules:

- `createInternalAccount` rejects `RoleType.SITE`.
- `createInternalAccount` always passes `siteId = null`.
- `updateInternalAccount` rejects changing an account to `SITE`.
- `disableAccount` writes status `disabled`.
- `deleteAccount` writes status `deleted`.
- `load` hides Site accounts and deleted accounts from the default Admin list.
- `findRoles` for Admin assignment excludes `SITE`.

- [ ] **Step 6: Extend `JdbcAccountRepository`**

Implement `AccountManagementRepository` in the existing adapter. Keep SQL private constants near `AUTHENTICATE_SQL`.

Required SQL shapes:

```sql
SELECT a.id, a.username, a.password, a.full_name, a.status, a.role_id, a.site_id, r.name AS role_name
FROM public.account a
INNER JOIN public.role r ON r.id = a.role_id
ORDER BY a.id DESC
```

```sql
INSERT INTO public.account (username, password, full_name, status, role_id, site_id)
VALUES (?, ?, ?, ?, ?, ?)
RETURNING id
```

```sql
UPDATE public.account
SET username = ?, password = ?, full_name = ?, role_id = ?, site_id = ?
WHERE id = ?
```

```sql
UPDATE public.account
SET status = ?
WHERE id = ?
```

- [ ] **Step 7: Expose the service from `AuthModule`**

Add fields:

```java
private final AccountManagementService accountManagementService;
```

Initialize after repository creation:

```java
this.accountManagementService = new AccountManagementService((AccountManagementRepository) accountRepository);
```

Expose:

```java
public AccountManagementService accountManagementService() {
    return accountManagementService;
}
```

If the cast feels too brittle during implementation, change the field type from `AccountRepository` to `JdbcAccountRepository` and return it through the narrow `AccountRepository` method for auth.

- [ ] **Step 8: Run focused tests**

Run:

```powershell
.\mvnw.cmd -q -Dtest=AccountManagementServiceTest test
```

Expected: PASS.

---

## Task 3: Add Admin Controller, View, Route, And Access Policy

**Files:**
- Create: `src/main/java/org/itss/prj_itss/controller/admin/account/AccountManagementController.java`
- Create: `src/main/java/org/itss/prj_itss/controller/admin/account/AdminControllerModule.java`
- Create: `src/main/java/org/itss/prj_itss/view/admin/account/AccountManagementView.java`
- Create: `src/main/java/org/itss/prj_itss/view/admin/account/account-management-view.fxml`
- Modify: `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`
- Modify: `src/main/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicy.java`
- Modify: `src/main/java/org/itss/prj_itss/view/auth/RoleWorkspaceContentFactory.java`
- Modify: `src/main/java/org/itss/prj_itss/view/layout/MainLayoutView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/layout/main-layout.fxml`
- Modify: `src/main/java/module-info.java`
- Test: `src/test/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicyTest.java`

- [ ] **Step 1: Add failing access-policy tests**

```java
@Test
void adminCanAccessAccountManagementOnly() {
    assertTrue(RoleAccessPolicy.canAccess(RoleType.ADMIN, "account-management"));
    assertFalse(RoleAccessPolicy.canAccess(RoleType.ADMIN, "site-management"));
    assertEquals("account-management", RoleAccessPolicy.defaultViewId(RoleType.ADMIN));
}

@Test
void orderingCannotAccessAccountManagement() {
    assertFalse(RoleAccessPolicy.canAccess(RoleType.ORDERING, "account-management"));
}
```

- [ ] **Step 2: Update `RoleAccessPolicy`**

Add an Admin branch before Ordering:

```java
if (roleType.isAdminRole()) {
    return switch (normalizedViewId) {
        case "account-management" -> true;
        default -> false;
    };
}
```

Set Admin default:

```java
if (roleType.isAdminRole()) {
    return "account-management";
}
```

- [ ] **Step 3: Add `AdminControllerModule`**

```java
public final class AdminControllerModule {

    private final AccountManagementController accountManagementController;

    public AdminControllerModule(AuthModule authModule) {
        this.accountManagementController =
            new AccountManagementController(authModule.accountManagementService());
    }

    public AccountManagementController accountManagementController() {
        return accountManagementController;
    }
}
```

- [ ] **Step 4: Add `AccountManagementController`**

Keep it JavaFX-free:

```java
public final class AccountManagementController {

    private final AccountManagementService accountManagementService;

    public AccountManagementController(AccountManagementService accountManagementService) {
        this.accountManagementService = Objects.requireNonNull(accountManagementService, "accountManagementService");
    }

    public AccountManagementSnapshot load() {
        return accountManagementService.load();
    }

    public List<AccountRow> filterRows(List<AccountRow> rows, String keyword) {
        return accountManagementService.filterRows(rows, keyword);
    }

    public AccountManagementResult create(AccountDraft draft) {
        return accountManagementService.createInternalAccount(draft);
    }

    public AccountManagementResult update(int accountId, AccountDraft draft) {
        return accountManagementService.updateInternalAccount(accountId, draft);
    }

    public AccountManagementResult disable(int accountId) {
        return accountManagementService.disableAccount(accountId);
    }

    public AccountManagementResult delete(int accountId) {
        return accountManagementService.deleteAccount(accountId);
    }
}
```

- [ ] **Step 5: Add Admin navigation in `MainLayoutView`**

FXML: add an `adminNavContainer` with one button:

```xml
<VBox fx:id="adminNavContainer" spacing="10">
    <children>
        <Button fx:id="accountManagementButton"
                text="Quản lý tài khoản"
                alignment="CENTER_LEFT"
                maxWidth="1.7976931348623157E308"
                styleClass="shell-nav-button"/>
    </children>
</VBox>
```

Java:

```java
@FXML
private VBox adminNavContainer;

@FXML
private Button accountManagementButton;
```

In `initialize`:

```java
registerNavButton("account-management", accountManagementButton);
```

In `updateUIForUser`:

```java
boolean adminRole = role.isAdminRole();
adminNavContainer.setVisible(adminRole);
adminNavContainer.setManaged(adminRole);
```

- [ ] **Step 6: Register the route in `MvcContext`**

Add a field:

```java
private final AdminControllerModule adminControllers = new AdminControllerModule(authModule);
```

Add route:

```java
RouteRegistry.fxml(
    "account-management",
    "/org/itss/prj_itss/view/admin/account/account-management-view.fxml",
    (viewId, viewInstance, navigator) ->
        ((AccountManagementView) viewInstance).init(
            navigator,
            adminControllers.accountManagementController()
        )
),
```

- [ ] **Step 7: Open the Admin view package in JPMS**

Add to `module-info.java`:

```java
opens org.itss.prj_itss.view.admin.account to javafx.fxml;
```

- [ ] **Step 8: Build the Admin view**

Use existing `card`, `stat-card`, `search-field`, and `btn-primary` styles before adding new CSS.

Required FXML controls:

- `totalAccountsLabel`
- `activeAccountsLabel`
- `disabledAccountsLabel`
- `searchField`
- `createAccountButton`
- `editAccountButton`
- `disableAccountButton`
- `deleteAccountButton`
- `accountTable`
- columns: username, full name, role, status

Required JavaFX behavior:

- `onViewShown` reloads from controller.
- Search filters through `controller.filterRows(...)`.
- Create/Edit opens a JavaFX `Dialog<AccountDraft>` built inside the view.
- Disable/Delete ask confirmation before calling controller.
- The view shows success/failure alerts, then reloads.
- The view does not import any persistence/JDBC package.

- [ ] **Step 9: Update role workspace content**

Change Admin content from "module chưa triển khai" to the account-management route being implemented. Keep this as display text only; access is still enforced by `RoleAccessPolicy`.

- [ ] **Step 10: Run focused tests**

Run:

```powershell
.\mvnw.cmd -q -Dtest=RoleAccessPolicyTest test
.\mvnw.cmd -q -Dtest=MvcDependencyTest test
```

Expected: PASS.

---

## Task 4: Add Site Command And Site Account Provisioning Model Slice

**Files:**
- Create: `src/main/java/org/itss/prj_itss/model/site/application/SiteDraft.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/SiteManagementResult.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/SiteAccountDraft.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/port/SiteCommandRepository.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/port/SiteAccountProvisioningPort.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/application/SiteManagementApplicationService.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/SiteModule.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/infrastructure/persistence/JdbcSiteRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/auth/AuthModule.java`
- Modify: `src/main/java/org/itss/prj_itss/model/auth/infrastructure/persistence/JdbcAccountRepository.java`
- Test: `src/test/java/org/itss/prj_itss/model/site/application/SiteManagementApplicationServiceTest.java`

- [ ] **Step 1: Write failing site service tests**

```java
@Test
void createSiteRejectsDuplicateSiteCode() {
    FakeSiteCommandRepository siteRepository = new FakeSiteCommandRepository();
    siteRepository.existingSiteCodes.add("TOKYO");
    SiteManagementApplicationService service = newService(siteRepository);

    SiteManagementResult result = service.createSite(new SiteDraft(
        "TOKYO",
        "Tokyo Import Site",
        "Japan partner",
        14,
        3
    ));

    assertFalse(result.success());
    assertEquals("Mã site đã tồn tại.", result.message());
}

@Test
void provisionSiteAccountUsesSiteRoleAndSiteId() {
    FakeSiteCommandRepository siteRepository = new FakeSiteCommandRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo Import Site", "Japan partner", 14, 3));
    FakeSiteAccountProvisioningPort accountPort = new FakeSiteAccountProvisioningPort();
    SiteManagementApplicationService service = newService(siteRepository, accountPort);

    SiteManagementResult result = service.provisionSiteAccount(5, new SiteAccountDraft(
        "tokyo-site",
        "secret",
        "Tokyo Site User"
    ));

    assertTrue(result.success());
    assertEquals(RoleType.SITE.id(), accountPort.createdRoleId);
    assertEquals(5, accountPort.createdSiteId);
}
```

- [ ] **Step 2: Add site command records**

```java
public record SiteDraft(
    String siteCode,
    String name,
    String description,
    Integer shipDeliveryDays,
    Integer airDeliveryDays
) {}
```

```java
public record SiteAccountDraft(
    String username,
    String password,
    String fullName
) {}
```

```java
public record SiteManagementResult(boolean success, String message, Integer siteId) {
    public static SiteManagementResult success(String message, Integer siteId) {
        return new SiteManagementResult(true, message, siteId);
    }

    public static SiteManagementResult failure(String message) {
        return new SiteManagementResult(false, message, null);
    }
}
```

- [ ] **Step 3: Add site command ports**

```java
public interface SiteCommandRepository {
    int createSite(SiteDraft draft);
    void updateSite(int siteId, SiteDraft draft);
    boolean existsBySiteCode(String siteCode);
    boolean existsBySiteCodeExceptId(String siteCode, int siteId);
}
```

```java
public interface SiteAccountProvisioningPort {
    boolean usernameExists(String username);
    int createSiteAccount(SiteAccountDraft draft, int siteId);
}
```

- [ ] **Step 4: Extend `SiteManagementApplicationService`**

Constructor should receive:

- `SiteUseCase siteService`
- `CatalogUseCase merchandiseService`
- `SiteCommandRepository siteCommandRepository`
- `SiteAccountProvisioningPort siteAccountProvisioningPort`
- `TransactionRunner transactionRunner`

Rules:

- Site code and name are required.
- Delivery days are optional, but if present must be `>= 0`.
- Duplicate site code is rejected.
- Provisioning rejects missing site.
- Provisioning rejects duplicate username.
- Provisioning uses `transactionRunner.execute(...)`.
- Provisioning calls `createSiteAccount(..., siteId)` only after the site exists.

- [ ] **Step 5: Implement JDBC site commands**

Add SQL to `JdbcSiteRepository`:

```sql
INSERT INTO public.site (site_code, name, description, ship_delivery_days, air_delivery_days)
VALUES (?, ?, ?, ?, ?)
RETURNING id
```

```sql
UPDATE public.site
SET site_code = ?, name = ?, description = ?, ship_delivery_days = ?, air_delivery_days = ?
WHERE id = ?
```

```sql
SELECT EXISTS(SELECT 1 FROM public.site WHERE LOWER(site_code) = LOWER(?))
```

```sql
SELECT EXISTS(SELECT 1 FROM public.site WHERE LOWER(site_code) = LOWER(?) AND id <> ?)
```

- [ ] **Step 6: Implement site-account provisioning in `JdbcAccountRepository`**

Make `JdbcAccountRepository` implement `SiteAccountProvisioningPort`.

Use:

```sql
SELECT EXISTS(SELECT 1 FROM public.account WHERE LOWER(username) = LOWER(?))
```

```sql
INSERT INTO public.account (username, password, full_name, status, role_id, site_id)
VALUES (?, ?, ?, 'active', ?, ?)
RETURNING id
```

The role id must be `RoleType.SITE.id()`.

- [ ] **Step 7: Rewire modules**

`AuthModule` exposes:

```java
public SiteAccountProvisioningPort siteAccountProvisioningPort() {
    return (SiteAccountProvisioningPort) accountRepository;
}
```

`SiteModule` constructor becomes:

```java
public SiteModule(
    ConnectionProvider connectionProvider,
    TransactionRunner transactionRunner,
    CatalogModule catalogModule,
    SiteAccountProvisioningPort siteAccountProvisioningPort
)
```

`MvcContext` constructs:

```java
private final SiteModule siteModule =
    new SiteModule(
        connectionProvider,
        transactionManager,
        catalogModule,
        authModule.siteAccountProvisioningPort()
    );
```

Check all callers from CodeGraph after implementation; expected changed caller is `MvcContext`.

- [ ] **Step 8: Run focused tests**

Run:

```powershell
.\mvnw.cmd -q -Dtest=SiteManagementApplicationServiceTest test
```

Expected: PASS.

---

## Task 5: Complete Ordering Site Management UI

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/controller/ordering/site/SiteManagementController.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/site/SiteManagementView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/site/site-management-view.fxml`
- Modify: `src/main/resources/org/itss/prj_itss/styles/main-style.css` only if needed

- [ ] **Step 1: Extend `SiteManagementController`**

Add JavaFX-free methods:

```java
public SiteManagementApplicationService.Snapshot load() {
    return siteManagementApplicationService.load();
}

public List<SiteRow> filterRows(List<SiteRow> rows, String keyword) {
    return siteManagementApplicationService.filterRows(rows, keyword);
}

public SiteManagementResult createSite(SiteDraft draft) {
    return siteManagementApplicationService.createSite(draft);
}

public SiteManagementResult updateSite(int siteId, SiteDraft draft) {
    return siteManagementApplicationService.updateSite(siteId, draft);
}

public SiteManagementResult provisionSiteAccount(int siteId, SiteAccountDraft draft) {
    return siteManagementApplicationService.provisionSiteAccount(siteId, draft);
}
```

- [ ] **Step 2: Replace placeholder add-site behavior**

Remove `showAddSiteNotice`. `addSiteButton` should open a create-site dialog and call `controller.createSite(...)`.

- [ ] **Step 3: Add selected-row actions**

FXML toolbar should include:

- `addSiteButton`
- `editSiteButton`
- `createSiteAccountButton`

Using selected-row actions is simpler and less brittle than TableView button cells.

- [ ] **Step 4: Add dialogs in the view only**

`SiteManagementView` may construct JavaFX dialogs because it is the view layer.

Create dialog fields:

- Site code
- Site name
- Description
- Ship delivery days
- Air delivery days

Create Site-account dialog fields:

- Username
- Password
- Full name

The dialog returns `SiteDraft` or `SiteAccountDraft`; it must not call repositories.

- [ ] **Step 5: Fix the misleading active-site stat**

The current schema has no `site.status`. Do not keep pretending all sites are active unless the product explicitly adds site status.

Recommended UI copy:

- `totalSitesLabel`: total rows
- second stat label: "Site đã khai báo"
- `merchandiseCountLabel`: merchandise count

- [ ] **Step 6: Reload after every successful command**

After create/update/provision success:

```java
showResult(result);
reload();
```

After failure:

```java
showResult(result);
```

- [ ] **Step 7: Run MVC boundary test**

Run:

```powershell
.\mvnw.cmd -q -Dtest=MvcDependencyTest test
```

Expected: PASS.

---

## Task 6: Final Verification

**Files:**
- Verify all touched files.

- [ ] **Step 1: Compile**

Run:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: build succeeds.

- [ ] **Step 2: Run focused tests**

Run:

```powershell
.\mvnw.cmd -q -Dtest=AccountManagementServiceTest,SiteManagementApplicationServiceTest,RoleAccessPolicyTest,MvcDependencyTest test
```

Expected: all focused tests pass.

- [ ] **Step 3: Run full tests**

Run:

```powershell
.\mvnw.cmd -q test
```

Expected: all tests pass.

- [ ] **Step 4: Manual smoke through the JavaFX app**

Run:

```powershell
.\mvnw.cmd javafx:run
```

Smoke:

- Login as Admin.
- Confirm default view is `Quản lý tài khoản`.
- Create an internal account with Sales/Ordering/Warehouse role.
- Confirm Site role is not offered in the Admin form.
- Disable the created account and confirm login fails for it.
- Login as Ordering.
- Open `Quản lý site`.
- Create a Site.
- Create a Site account for that Site.
- Login with the Site account and confirm it does not get Ordering/Admin navigation.

---

## Design Review Notes

- Do not move account management into `view/auth`; it is an Admin business screen, not authentication UI.
- Do not let `SiteManagementView` import `JdbcSiteRepository`, `ConnectionProvider`, or `TransactionManager`.
- Do not let `AccountManagementController` import JavaFX.
- Do not add Site account creation to Admin, because `BussinessLogic.md` assigns that responsibility to International Ordering Department.
- Do not add a broad "UserRepository" abstraction. The current bounded context is `auth`, and the table is `account`; keep names aligned with the codebase.
- Prefer soft-delete status `deleted` for account cancellation. Physical delete can remove data that old orders, sessions, or audit trails may still need.
- Keep `RoleType` as the single place for role ids. Do not scatter magic numbers `1`, `2`, `3`, `4`, `5` through services or views.
