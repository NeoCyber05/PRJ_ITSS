# Overseas Import Sites Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the actor workspace for Overseas Import Sites: update the linked Site profile, manage offered merchandise and stock, view Site-owned orders, and confirm supply acceptance.

**Architecture:** Keep the current JavaFX MVC shape. `site-management` remains the International Ordering Department screen for Site records and Site-account provisioning; actor `SITE` gets a separate `site-workspace` route. Model code owns business rules and JDBC ports, controllers stay JavaFX-free, views own JavaFX state/dialogs, and `MvcContext` owns wiring.

**Tech Stack:** Java 17, JavaFX 17, Maven, JUnit 5, PostgreSQL/Supabase JDBC, manual DI through `MvcContext`.

---

## Current Baseline

- Requirement source: `BussinessLogic.md`, section `3. Site nhập khẩu (Overseas Import Sites)`.
- Existing Ordering screen: `src/main/java/org/itss/prj_itss/view/ordering/site/SiteManagementView.java`.
- That existing screen is for Ordering to manage Site records and create Site accounts. Do not repurpose it as the Site user's workspace.
- Existing auth model already has `Account.siteId`, and `JdbcAccountRepository.createSiteAccount(...)` stores `role_id = RoleType.SITE.id()` plus the linked `site_id`.
- Existing `RoleAccessPolicyTest` currently asserts `SITE` falls back to `role-workspace`; this must change.
- Existing `site_inventory(site_id, merchandise_id, stock_quantity)` can support the merchandise and stock requirement. No schema change is needed for inventory.
- Existing `order(site_id, status)` and `order_merchandise` can support Site order list/detail and confirmation. Current order status values are `pending`, `shipping`, `cancelled`, `completed`.
- Business interpretation for this plan: Site confirmation means accepting supply for a pending order, so the order moves from `pending` to `shipping`. If the product later needs a separate accepted-but-not-shipped status, that is a schema/business-status change outside this plan.

## Acceptance Criteria

- Site users land on `site-workspace`, not `role-workspace`.
- Site users cannot access Ordering routes such as `site-management`, `received-requests`, `orders`, or `request-processing`.
- The Site workspace only uses the `site_id` linked to the logged-in Site account.
- A Site user with no linked `site_id` sees an unavailable state and cannot update profile, inventory, or orders.
- Site users can update their own Site name, description, ship delivery days, and air delivery days. Site code remains read-only.
- Site users can add/update/remove merchandise they provide by selecting from the shared merchandise catalog and entering non-negative stock.
- Inventory updates write through `site_inventory`; stock `0` is allowed but does not make the Site available for allocation because current availability queries require `stock_quantity > 0`.
- Site users can view only orders where `order.site_id` equals their linked Site id.
- Site users can view order detail items with merchandise code/name/unit, quantity, and delivery method.
- Site users can confirm only their own `pending` orders; confirmation changes status to `shipping`.
- MVC guardrails stay green: model has no JavaFX/controller/view imports, controllers have no JavaFX imports, views have no persistence/JDBC imports.

## Non-Goals

- Do not change the existing Ordering `site-management` responsibility or remove its Site-account creation flow.
- Do not implement Site rejection/cancellation unless the business explicitly adds that action. Current requested action is accepting supply.
- Do not add new order statuses in this plan.
- Do not add Warehouse receipt behavior here. Warehouse still confirms physical arrival later.
- Do not move FXML to `src/main/resources`; this repo keeps FXML under `src/main/java` beside the view controller package.

---

## File Map

### Site Self-Service Model

- Create `src/main/java/org/itss/prj_itss/model/site/application/self/SiteProfileDraft.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/self/SiteInventoryDraft.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/self/SiteInventoryRow.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/self/SiteOrderItemRow.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/self/SiteOrderRow.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/self/SiteWorkspaceSnapshot.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/self/SiteWorkspaceResult.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/self/OverseasSiteApplicationService.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/port/SiteProfileCommandPort.java`
- Create `src/main/java/org/itss/prj_itss/model/site/application/port/SiteInventoryCommandPort.java`
- Create `src/main/java/org/itss/prj_itss/model/order/application/port/SiteOrderRepository.java`
- Modify `src/main/java/org/itss/prj_itss/model/site/SiteModule.java`
- Modify `src/main/java/org/itss/prj_itss/model/site/infrastructure/persistence/JdbcSiteRepository.java`
- Modify `src/main/java/org/itss/prj_itss/model/order/infrastructure/persistence/JdbcOrderRepository.java`

### Site Controller And View

- Create `src/main/java/org/itss/prj_itss/controller/site/SiteWorkspaceController.java`
- Create `src/main/java/org/itss/prj_itss/controller/site/SiteControllerModule.java`
- Create `src/main/java/org/itss/prj_itss/view/site/workspace/SiteWorkspaceView.java`
- Create `src/main/java/org/itss/prj_itss/view/site/workspace/site-workspace-view.fxml`
- Modify `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`
- Modify `src/main/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicy.java`
- Modify `src/main/java/org/itss/prj_itss/view/auth/RoleWorkspaceContentFactory.java`
- Modify `src/main/java/org/itss/prj_itss/view/layout/MainLayoutView.java`
- Modify `src/main/java/org/itss/prj_itss/view/layout/main-layout.fxml`
- Modify `src/main/java/module-info.java`

### Tests

- Create `src/test/java/org/itss/prj_itss/model/site/application/self/OverseasSiteApplicationServiceTest.java`
- Modify `src/test/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicyTest.java`
- Keep `src/test/java/org/itss/prj_itss/architecture/MvcDependencyTest.java` as the final MVC boundary test.

---

## Task 1: Site Role Access And Navigation

**Files:**
- Modify: `src/test/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicyTest.java`
- Modify: `src/main/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicy.java`
- Modify: `src/main/java/org/itss/prj_itss/view/layout/MainLayoutView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/layout/main-layout.fxml`

- [ ] **Step 1: Write failing access-policy tests**

Add this test to `RoleAccessPolicyTest`:

```java
@Test
void siteRoleUsesSiteWorkspaceOnly() {
    assertEquals("site-workspace", RoleAccessPolicy.defaultViewId(RoleType.SITE));
    assertTrue(RoleAccessPolicy.canAccess(RoleType.SITE, "site-workspace"));
    assertFalse(RoleAccessPolicy.canAccess(RoleType.SITE, "role-workspace"));
    assertFalse(RoleAccessPolicy.canAccess(RoleType.SITE, "site-management"));
    assertFalse(RoleAccessPolicy.canAccess(RoleType.SITE, "orders"));
    assertFalse(RoleAccessPolicy.canAccess(RoleType.SITE, "received-requests"));
}
```

- [ ] **Step 2: Run the test and verify it fails**

Run:

```powershell
.\mvnw.cmd -q -Dtest=RoleAccessPolicyTest test
```

Expected: FAIL because `SITE` currently defaults to `role-workspace`.

- [ ] **Step 3: Update `RoleAccessPolicy`**

Add a Site branch after Warehouse:

```java
if (roleType.isSiteRole()) {
    return switch (normalizedViewId) {
        case "site-workspace" -> true;
        default -> false;
    };
}
```

Update `defaultViewId(RoleType roleType)`:

```java
if (roleType.isSiteRole()) {
    return "site-workspace";
}
```

Do not add `site-management` to the Site branch. That route belongs to Ordering.

- [ ] **Step 4: Add Site navigation container to `main-layout.fxml`**

Place this beside the existing Ordering/Sales/Admin navigation containers:

```xml
<VBox fx:id="siteNavContainer" spacing="10">
    <children>
        <Button fx:id="siteWorkspaceButton"
                text="Không gian Site"
                alignment="CENTER_LEFT"
                maxWidth="1.7976931348623157E308"
                styleClass="shell-nav-button"/>
    </children>
</VBox>
```

- [ ] **Step 5: Wire Site navigation in `MainLayoutView`**

Add fields:

```java
@FXML
private VBox siteNavContainer;

@FXML
private Button siteWorkspaceButton;
```

Register the button in `initialize()`:

```java
registerNavButton("site-workspace", siteWorkspaceButton);
```

Update `updateUIForUser()`:

```java
boolean siteRole = role.isSiteRole();
siteNavContainer.setVisible(siteRole);
siteNavContainer.setManaged(siteRole);
```

- [ ] **Step 6: Run focused access tests**

Run:

```powershell
.\mvnw.cmd -q -Dtest=RoleAccessPolicyTest test
```

Expected: PASS.

---

## Task 2: Site Profile And Inventory Model Slice

**Files:**
- Create: `src/main/java/org/itss/prj_itss/model/site/application/self/SiteProfileDraft.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/self/SiteInventoryDraft.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/self/SiteInventoryRow.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/self/SiteWorkspaceResult.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/port/SiteProfileCommandPort.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/port/SiteInventoryCommandPort.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/self/OverseasSiteApplicationService.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/infrastructure/persistence/JdbcSiteRepository.java`
- Test: `src/test/java/org/itss/prj_itss/model/site/application/self/OverseasSiteApplicationServiceTest.java`

- [ ] **Step 1: Write failing profile and inventory tests**

Create `OverseasSiteApplicationServiceTest` with these cases:

```java
@Test
void updateProfileRejectsMissingSite() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    OverseasSiteApplicationService service = newService(siteRepository);

    SiteWorkspaceResult result = service.updateProfile(
        99,
        new SiteProfileDraft("Tokyo Site", "Updated description", 14, 3)
    );

    assertFalse(result.success());
    assertEquals("Site không tồn tại.", result.message());
}

@Test
void updateProfileKeepsSiteCodeReadOnly() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "Old", 10, 2));
    OverseasSiteApplicationService service = newService(siteRepository);

    SiteWorkspaceResult result = service.updateProfile(
        5,
        new SiteProfileDraft("Tokyo Updated", "New", 12, 4)
    );

    assertTrue(result.success());
    assertEquals("TOKYO", siteRepository.sites.get(5).getSiteCode());
    assertEquals("Tokyo Updated", siteRepository.sites.get(5).getName());
    assertEquals(12, siteRepository.sites.get(5).getShipDeliveryDays());
}

@Test
void updateInventoryRejectsNegativeStock() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
    FakeCatalogRepository catalogRepository = new FakeCatalogRepository();
    catalogRepository.merchandise.put(7, new Merchandise(7, "M-01", "Tea", "box"));
    OverseasSiteApplicationService service = newService(siteRepository, catalogRepository);

    SiteWorkspaceResult result = service.updateInventoryItem(5, new SiteInventoryDraft(7, -1));

    assertFalse(result.success());
    assertEquals("Số lượng tồn kho không được âm.", result.message());
}

@Test
void updateInventoryStoresSelectedMerchandiseStock() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
    FakeCatalogRepository catalogRepository = new FakeCatalogRepository();
    catalogRepository.merchandise.put(7, new Merchandise(7, "M-01", "Tea", "box"));
    OverseasSiteApplicationService service = newService(siteRepository, catalogRepository);

    SiteWorkspaceResult result = service.updateInventoryItem(5, new SiteInventoryDraft(7, 25));

    assertTrue(result.success());
    assertEquals(25, siteRepository.inventory.get(5).get(7));
}

@Test
void removeInventoryItemDeletesOnlyThatSiteMerchandise() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
    siteRepository.inventory.put(5, new LinkedHashMap<>(Map.of(7, 25, 8, 10)));
    OverseasSiteApplicationService service = newService(siteRepository);

    SiteWorkspaceResult result = service.removeInventoryItem(5, 7);

    assertTrue(result.success());
    assertFalse(siteRepository.inventory.get(5).containsKey(7));
    assertEquals(10, siteRepository.inventory.get(5).get(8));
}
```

Add these imports and helpers in the same test file:

```java
import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.catalog.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.order.application.port.SiteOrderRepository;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.site.application.port.SiteInventoryCommandPort;
import org.itss.prj_itss.model.site.application.port.SiteProfileCommandPort;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.itss.prj_itss.model.site.domain.Site;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

private OverseasSiteApplicationService newService(FakeSiteRepository siteRepository) {
    return newService(siteRepository, new FakeCatalogRepository(), new FakeSiteOrderRepository());
}

private OverseasSiteApplicationService newService(
    FakeSiteRepository siteRepository,
    FakeCatalogRepository catalogRepository
) {
    return newService(siteRepository, catalogRepository, new FakeSiteOrderRepository());
}

private OverseasSiteApplicationService newService(
    FakeSiteRepository siteRepository,
    FakeCatalogRepository catalogRepository,
    FakeSiteOrderRepository orderRepository
) {
    return new OverseasSiteApplicationService(
        new SiteUseCase(siteRepository, siteRepository),
        new CatalogUseCase(catalogRepository),
        siteRepository,
        siteRepository,
        orderRepository
    );
}

static final class FakeSiteRepository
    implements SiteRepository, InventoryRepository, SiteProfileCommandPort, SiteInventoryCommandPort {

    final Map<Integer, Site> sites = new LinkedHashMap<>();
    final Map<Integer, Map<Integer, Integer>> inventory = new LinkedHashMap<>();

    @Override
    public List<Site> findAll() {
        return List.copyOf(sites.values());
    }

    @Override
    public List<Site> findAvailableForMerchandiseIds(List<Integer> merchandiseIds) {
        return List.of();
    }

    @Override
    public Site findById(int id) {
        return sites.get(id);
    }

    @Override
    public Site findBySiteCode(String siteCode) {
        return sites.values().stream()
            .filter(site -> site.getSiteCode().equalsIgnoreCase(siteCode))
            .findFirst()
            .orElse(null);
    }

    @Override
    public int countAll() {
        return sites.size();
    }

    @Override
    public Map<Integer, Integer> getInventoryBySiteId(int siteId) {
        return Map.copyOf(inventory.getOrDefault(siteId, Map.of()));
    }

    @Override
    public int getStockQuantity(int siteId, int merchandiseId) {
        return inventory.getOrDefault(siteId, Map.of()).getOrDefault(merchandiseId, 0);
    }

    @Override
    public int getTotalStock(int merchandiseId) {
        return inventory.values().stream()
            .mapToInt(items -> items.getOrDefault(merchandiseId, 0))
            .sum();
    }

    @Override
    public int countMerchandiseAtSite(int siteId) {
        return (int) inventory.getOrDefault(siteId, Map.of()).values().stream()
            .filter(stock -> stock > 0)
            .count();
    }

    @Override
    public void updateProfile(int siteId, SiteProfileDraft draft) {
        Site site = sites.get(siteId);
        site.setName(draft.name());
        site.setDescription(draft.description());
        site.setShipDeliveryDays(draft.shipDeliveryDays());
        site.setAirDeliveryDays(draft.airDeliveryDays());
    }

    @Override
    public void upsertInventoryItem(int siteId, int merchandiseId, int stockQuantity) {
        inventory.computeIfAbsent(siteId, ignored -> new LinkedHashMap<>()).put(merchandiseId, stockQuantity);
    }

    @Override
    public void removeInventoryItem(int siteId, int merchandiseId) {
        inventory.computeIfAbsent(siteId, ignored -> new LinkedHashMap<>()).remove(merchandiseId);
    }
}

static final class FakeCatalogRepository implements MerchandiseRepository {
    final Map<Integer, Merchandise> merchandise = new LinkedHashMap<>();

    @Override
    public List<Merchandise> findAll() {
        return List.copyOf(merchandise.values());
    }

    @Override
    public Merchandise findById(int id) {
        return merchandise.get(id);
    }

    @Override
    public Merchandise findByCode(String code) {
        return merchandise.values().stream()
            .filter(item -> item.getCode().equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }

    @Override
    public int countAll() {
        return merchandise.size();
    }
}

static final class FakeSiteOrderRepository implements SiteOrderRepository {
    final Map<Integer, Order> orders = new LinkedHashMap<>();
    final Map<Integer, List<OrderMerchandise>> items = new LinkedHashMap<>();
    int updatedOrderId;

    FakeSiteOrderRepository(Order... sourceOrders) {
        for (Order order : sourceOrders) {
            orders.put(order.getId(), order);
        }
    }

    @Override
    public List<Order> findBySiteId(int siteId) {
        return orders.values().stream()
            .filter(order -> order.getSiteId() == siteId)
            .toList();
    }

    @Override
    public Order findByIdForSite(int orderId, int siteId) {
        Order order = orders.get(orderId);
        return order != null && order.getSiteId() == siteId ? order : null;
    }

    @Override
    public List<OrderMerchandise> findItemsByOrderId(int orderId) {
        return items.getOrDefault(orderId, List.of());
    }

    @Override
    public boolean updateStatusForSite(int orderId, int siteId, String newStatus) {
        Order order = findByIdForSite(orderId, siteId);
        if (order == null) {
            return false;
        }
        updatedOrderId = orderId;
        order.setStatus(newStatus);
        return true;
    }
}
```

- [ ] **Step 2: Run the new tests and verify they fail**

Run:

```powershell
.\mvnw.cmd -q -Dtest=OverseasSiteApplicationServiceTest test
```

Expected: FAIL because the service and records do not exist yet.

- [ ] **Step 3: Add records and command ports**

Create:

```java
package org.itss.prj_itss.model.site.application.self;

public record SiteProfileDraft(
    String name,
    String description,
    Integer shipDeliveryDays,
    Integer airDeliveryDays
) {}
```

```java
package org.itss.prj_itss.model.site.application.self;

public record SiteInventoryDraft(int merchandiseId, int stockQuantity) {}
```

```java
package org.itss.prj_itss.model.site.application.self;

public record SiteWorkspaceResult(boolean success, String message) {
    public static SiteWorkspaceResult success(String message) {
        return new SiteWorkspaceResult(true, message);
    }

    public static SiteWorkspaceResult failure(String message) {
        return new SiteWorkspaceResult(false, message);
    }
}
```

```java
package org.itss.prj_itss.model.site.application.self;

public record SiteInventoryRow(
    int merchandiseId,
    String merchandiseCode,
    String merchandiseName,
    String unit,
    int stockQuantity
) {}
```

```java
package org.itss.prj_itss.model.site.application.port;

import org.itss.prj_itss.model.site.application.self.SiteProfileDraft;

public interface SiteProfileCommandPort {
    void updateProfile(int siteId, SiteProfileDraft draft);
}
```

```java
package org.itss.prj_itss.model.site.application.port;

public interface SiteInventoryCommandPort {
    void upsertInventoryItem(int siteId, int merchandiseId, int stockQuantity);
    void removeInventoryItem(int siteId, int merchandiseId);
}
```

- [ ] **Step 4: Implement profile and inventory methods in `OverseasSiteApplicationService`**

Constructor dependencies for this step:

```java
private final SiteUseCase siteUseCase;
private final CatalogUseCase catalogUseCase;
private final SiteProfileCommandPort profileCommandPort;
private final SiteInventoryCommandPort inventoryCommandPort;
```

Required methods:

```java
public SiteWorkspaceResult updateProfile(int siteId, SiteProfileDraft draft) {
    Site site = siteUseCase.findById(siteId);
    if (site == null) {
        return SiteWorkspaceResult.failure("Site không tồn tại.");
    }
    if (draft.name() == null || draft.name().isBlank()) {
        return SiteWorkspaceResult.failure("Tên site không được để trống.");
    }
    if (draft.shipDeliveryDays() != null && draft.shipDeliveryDays() < 0) {
        return SiteWorkspaceResult.failure("Ngày vận chuyển đường biển không hợp lệ.");
    }
    if (draft.airDeliveryDays() != null && draft.airDeliveryDays() < 0) {
        return SiteWorkspaceResult.failure("Ngày vận chuyển đường hàng không không hợp lệ.");
    }
    profileCommandPort.updateProfile(siteId, draft);
    return SiteWorkspaceResult.success("Cập nhật thông tin site thành công.");
}

public SiteWorkspaceResult updateInventoryItem(int siteId, SiteInventoryDraft draft) {
    if (siteUseCase.findById(siteId) == null) {
        return SiteWorkspaceResult.failure("Site không tồn tại.");
    }
    if (catalogUseCase.findById(draft.merchandiseId()) == null) {
        return SiteWorkspaceResult.failure("Mặt hàng không tồn tại.");
    }
    if (draft.stockQuantity() < 0) {
        return SiteWorkspaceResult.failure("Số lượng tồn kho không được âm.");
    }
    inventoryCommandPort.upsertInventoryItem(siteId, draft.merchandiseId(), draft.stockQuantity());
    return SiteWorkspaceResult.success("Cập nhật tồn kho thành công.");
}

public SiteWorkspaceResult removeInventoryItem(int siteId, int merchandiseId) {
    if (siteUseCase.findById(siteId) == null) {
        return SiteWorkspaceResult.failure("Site không tồn tại.");
    }
    inventoryCommandPort.removeInventoryItem(siteId, merchandiseId);
    return SiteWorkspaceResult.success("Đã bỏ mặt hàng khỏi danh sách kinh doanh.");
}
```

- [ ] **Step 5: Implement JDBC site commands**

Make `JdbcSiteRepository` implement `SiteProfileCommandPort` and `SiteInventoryCommandPort`.

Add SQL:

```java
private static final String UPDATE_PROFILE_SQL = """
    UPDATE public.site
    SET name = ?, description = ?, ship_delivery_days = ?, air_delivery_days = ?
    WHERE id = ?
    """;

private static final String UPSERT_INVENTORY_SQL = """
    INSERT INTO public.site_inventory (site_id, merchandise_id, stock_quantity)
    VALUES (?, ?, ?)
    ON CONFLICT (site_id, merchandise_id)
    DO UPDATE SET stock_quantity = EXCLUDED.stock_quantity
    """;

private static final String REMOVE_INVENTORY_SQL = """
    DELETE FROM public.site_inventory
    WHERE site_id = ? AND merchandise_id = ?
    """;
```

Method bodies:

```java
@Override
public void updateProfile(int siteId, SiteProfileDraft draft) {
    try (PreparedStatement ps = getConnection().prepareStatement(UPDATE_PROFILE_SQL)) {
        ps.setString(1, draft.name().trim());
        ps.setString(2, draft.description() == null || draft.description().isBlank() ? null : draft.description().trim());
        setNullableInteger(ps, 3, draft.shipDeliveryDays());
        setNullableInteger(ps, 4, draft.airDeliveryDays());
        ps.setInt(5, siteId);
        ps.executeUpdate();
    } catch (SQLException exception) {
        throw new IllegalStateException("Unable to update site profile", exception);
    }
}

@Override
public void upsertInventoryItem(int siteId, int merchandiseId, int stockQuantity) {
    try (PreparedStatement ps = getConnection().prepareStatement(UPSERT_INVENTORY_SQL)) {
        ps.setInt(1, siteId);
        ps.setInt(2, merchandiseId);
        ps.setInt(3, stockQuantity);
        ps.executeUpdate();
    } catch (SQLException exception) {
        throw new IllegalStateException("Unable to update site inventory", exception);
    }
}

@Override
public void removeInventoryItem(int siteId, int merchandiseId) {
    try (PreparedStatement ps = getConnection().prepareStatement(REMOVE_INVENTORY_SQL)) {
        ps.setInt(1, siteId);
        ps.setInt(2, merchandiseId);
        ps.executeUpdate();
    } catch (SQLException exception) {
        throw new IllegalStateException("Unable to remove site inventory item", exception);
    }
}
```

If `JdbcSiteRepository` does not already have a nullable integer helper, add:

```java
private void setNullableInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
    if (value == null) {
        ps.setNull(index, Types.INTEGER);
    } else {
        ps.setInt(index, value);
    }
}
```

- [ ] **Step 6: Run focused model tests**

Run:

```powershell
.\mvnw.cmd -q -Dtest=OverseasSiteApplicationServiceTest test
```

Expected after Task 2: profile and inventory tests pass through direct service construction in the test. Do not wire the service into `SiteModule` until Task 3 adds the required Site order port.

---

## Task 3: Site Order Query, Detail, And Confirmation Model Slice

**Files:**
- Create: `src/main/java/org/itss/prj_itss/model/order/application/port/SiteOrderRepository.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/self/SiteOrderRow.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/self/SiteOrderItemRow.java`
- Create: `src/main/java/org/itss/prj_itss/model/site/application/self/SiteWorkspaceSnapshot.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/application/self/OverseasSiteApplicationService.java`
- Modify: `src/main/java/org/itss/prj_itss/model/order/infrastructure/persistence/JdbcOrderRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/SiteModule.java`
- Test: `src/test/java/org/itss/prj_itss/model/site/application/self/OverseasSiteApplicationServiceTest.java`

- [ ] **Step 1: Add failing order tests**

Add:

```java
@Test
void loadIncludesOnlyOrdersForTheSite() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
    FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
        new Order(10, 1, 5, LocalDateTime.now(), "pending"),
        new Order(11, 1, 9, LocalDateTime.now(), "pending")
    );
    OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

    SiteWorkspaceSnapshot snapshot = service.load(5);

    assertTrue(snapshot.available());
    assertEquals(1, snapshot.orders().size());
    assertEquals(10, snapshot.orders().get(0).orderId());
}

@Test
void confirmSupplyRejectsOrderFromDifferentSite() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
    FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
        new Order(10, 1, 9, LocalDateTime.now(), "pending")
    );
    OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

    SiteWorkspaceResult result = service.confirmSupply(5, 10);

    assertFalse(result.success());
    assertEquals("Đơn hàng không thuộc site này.", result.message());
    assertEquals(0, orderRepository.updatedOrderId);
}

@Test
void loadOrderItemsReturnsItemsOnlyAfterSiteOwnershipCheck() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
    FakeCatalogRepository catalogRepository = new FakeCatalogRepository();
    catalogRepository.merchandise.put(7, new Merchandise(7, "M-01", "Tea", "box"));
    FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
        new Order(10, 1, 5, LocalDateTime.now(), "pending")
    );
    orderRepository.items.put(10, List.of(new OrderMerchandise(10, 7, BigDecimal.valueOf(12), "Tau")));
    OverseasSiteApplicationService service = newService(siteRepository, catalogRepository, orderRepository);

    List<SiteOrderItemRow> rows = service.loadOrderItems(5, 10);

    assertEquals(1, rows.size());
    assertEquals("M-01", rows.get(0).merchandiseCode());
    assertEquals("12", rows.get(0).quantity());
}

@Test
void loadOrderItemsRejectsOrderFromDifferentSite() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
    FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
        new Order(10, 1, 9, LocalDateTime.now(), "pending")
    );
    OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

    List<SiteOrderItemRow> rows = service.loadOrderItems(5, 10);

    assertTrue(rows.isEmpty());
}

@Test
void confirmSupplyRejectsNonPendingOrder() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
    FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
        new Order(10, 1, 5, LocalDateTime.now(), "shipping")
    );
    OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

    SiteWorkspaceResult result = service.confirmSupply(5, 10);

    assertFalse(result.success());
    assertEquals("Chỉ có thể xác nhận đơn hàng đang chờ xác nhận.", result.message());
}

@Test
void confirmSupplyMovesPendingOrderToShipping() {
    FakeSiteRepository siteRepository = new FakeSiteRepository();
    siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
    FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
        new Order(10, 1, 5, LocalDateTime.now(), "pending")
    );
    OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

    SiteWorkspaceResult result = service.confirmSupply(5, 10);

    assertTrue(result.success());
    assertEquals(10, orderRepository.updatedOrderId);
    assertEquals("shipping", orderRepository.orders.get(10).getStatus());
}
```

- [ ] **Step 2: Run the tests and verify they fail**

Run:

```powershell
.\mvnw.cmd -q -Dtest=OverseasSiteApplicationServiceTest test
```

Expected: FAIL because the Site order port and snapshot are missing.

- [ ] **Step 3: Add Site order records**

Create:

```java
package org.itss.prj_itss.model.site.application.self;

public record SiteOrderRow(
    int orderId,
    int requestId,
    String orderCode,
    String requestCode,
    String createdAt,
    String status,
    String statusText,
    boolean confirmable
) {}
```

```java
package org.itss.prj_itss.model.site.application.self;

public record SiteOrderItemRow(
    int merchandiseId,
    String merchandiseCode,
    String merchandiseName,
    String unit,
    String quantity,
    String deliveryMethod
) {}
```

```java
package org.itss.prj_itss.model.site.application.self;

import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.site.domain.Site;

import java.util.List;

public record SiteWorkspaceSnapshot(
    boolean available,
    String message,
    Site site,
    List<Merchandise> merchandiseOptions,
    List<SiteInventoryRow> inventoryRows,
    List<SiteOrderRow> orders
) {
    public SiteWorkspaceSnapshot {
        merchandiseOptions = merchandiseOptions == null ? List.of() : List.copyOf(merchandiseOptions);
        inventoryRows = inventoryRows == null ? List.of() : List.copyOf(inventoryRows);
        orders = orders == null ? List.of() : List.copyOf(orders);
    }

    public static SiteWorkspaceSnapshot unavailable(String message) {
        return new SiteWorkspaceSnapshot(false, message, null, List.of(), List.of(), List.of());
    }
}
```

- [ ] **Step 4: Add `SiteOrderRepository`**

```java
package org.itss.prj_itss.model.order.application.port;

import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;

import java.util.List;

public interface SiteOrderRepository {
    List<Order> findBySiteId(int siteId);
    Order findByIdForSite(int orderId, int siteId);
    List<OrderMerchandise> findItemsByOrderId(int orderId);
    boolean updateStatusForSite(int orderId, int siteId, String newStatus);
}
```

- [ ] **Step 5: Complete `OverseasSiteApplicationService`**

Add dependency:

```java
private final SiteOrderRepository siteOrderRepository;
```

Complete constructor:

```java
public OverseasSiteApplicationService(
    SiteUseCase siteUseCase,
    CatalogUseCase catalogUseCase,
    SiteProfileCommandPort profileCommandPort,
    SiteInventoryCommandPort inventoryCommandPort,
    SiteOrderRepository siteOrderRepository
) {
    this.siteUseCase = Objects.requireNonNull(siteUseCase, "siteUseCase");
    this.catalogUseCase = Objects.requireNonNull(catalogUseCase, "catalogUseCase");
    this.profileCommandPort = Objects.requireNonNull(profileCommandPort, "profileCommandPort");
    this.inventoryCommandPort = Objects.requireNonNull(inventoryCommandPort, "inventoryCommandPort");
    this.siteOrderRepository = Objects.requireNonNull(siteOrderRepository, "siteOrderRepository");
}
```

Add load:

```java
public SiteWorkspaceSnapshot load(int siteId) {
    Site site = siteUseCase.findById(siteId);
    if (site == null) {
        return SiteWorkspaceSnapshot.unavailable("Site không tồn tại.");
    }

    Map<Integer, Integer> inventory = siteUseCase.getInventoryBySiteId(siteId);
    List<Merchandise> merchandise = catalogUseCase.findAll();
    List<SiteInventoryRow> inventoryRows = merchandise.stream()
        .filter(item -> inventory.containsKey(item.getId()))
        .map(item -> new SiteInventoryRow(
            item.getId(),
            item.getCode(),
            item.getName(),
            item.getUnit(),
            inventory.getOrDefault(item.getId(), 0)
        ))
        .toList();

    List<SiteOrderRow> orderRows = siteOrderRepository.findBySiteId(siteId).stream()
        .map(this::toOrderRow)
        .toList();

    return new SiteWorkspaceSnapshot(true, "", site, merchandise, inventoryRows, orderRows);
}
```

Add confirm:

```java
public SiteWorkspaceResult confirmSupply(int siteId, int orderId) {
    Site site = siteUseCase.findById(siteId);
    if (site == null) {
        return SiteWorkspaceResult.failure("Site không tồn tại.");
    }

    Order order = siteOrderRepository.findByIdForSite(orderId, siteId);
    if (order == null) {
        return SiteWorkspaceResult.failure("Đơn hàng không thuộc site này.");
    }

    String statusKey = OrderingFormatters.normalizeStatusKey(order.getStatus());
    if (!OrderingFormatters.STATUS_PENDING.equals(statusKey)) {
        return SiteWorkspaceResult.failure("Chỉ có thể xác nhận đơn hàng đang chờ xác nhận.");
    }

    boolean updated = siteOrderRepository.updateStatusForSite(orderId, siteId, OrderingFormatters.STATUS_SHIPPING);
    if (!updated) {
        return SiteWorkspaceResult.failure("Không thể cập nhật trạng thái đơn hàng.");
    }

    return SiteWorkspaceResult.success("Đã xác nhận cung ứng đơn hàng.");
}
```

Add item-detail loading:

```java
public List<SiteOrderItemRow> loadOrderItems(int siteId, int orderId) {
    Order order = siteOrderRepository.findByIdForSite(orderId, siteId);
    if (order == null) {
        return List.of();
    }
    return siteOrderRepository.findItemsByOrderId(orderId).stream()
        .map(this::toOrderItemRow)
        .toList();
}
```

Add row mapper:

```java
private SiteOrderRow toOrderRow(Order order) {
    String statusKey = OrderingFormatters.normalizeStatusKey(order.getStatus());
    return new SiteOrderRow(
        order.getId(),
        order.getRequestId(),
        OrderingFormatters.formatOrderCode(order.getId()),
        OrderingFormatters.formatRequestCode(order.getRequestId()),
        OrderingFormatters.formatDateOrEmpty(order.getCreatedAt()),
        order.getStatus(),
        OrderingFormatters.orderStatusText(order.getStatus()),
        OrderingFormatters.STATUS_PENDING.equals(statusKey)
    );
}

private SiteOrderItemRow toOrderItemRow(OrderMerchandise item) {
    Merchandise merchandise = catalogUseCase.findById(item.getMerchandiseId());
    return new SiteOrderItemRow(
        item.getMerchandiseId(),
        merchandise == null ? "N/A" : merchandise.getCode(),
        merchandise == null ? "N/A" : merchandise.getName(),
        merchandise == null ? "N/A" : merchandise.getUnit(),
        OrderingFormatters.formatQuantity(item.getQuantity()),
        OrderingFormatters.deliveryMethodText(item.getDeliveryMethod())
    );
}
```

- [ ] **Step 6: Implement `SiteOrderRepository` in `JdbcOrderRepository`**

Add SQL:

```java
private static final String FIND_BY_SITE_ID_SQL = """
    SELECT id, request_id, site_id, created_at, status
    FROM "order"
    WHERE site_id = ?
    ORDER BY id DESC
    """;

private static final String FIND_BY_ID_FOR_SITE_SQL = """
    SELECT id, request_id, site_id, created_at, status
    FROM "order"
    WHERE id = ? AND site_id = ?
    """;

private static final String UPDATE_STATUS_FOR_SITE_SQL = """
    UPDATE "order"
    SET status = ?
    WHERE id = ? AND site_id = ?
    """;
```

Add methods:

```java
@Override
public List<Order> findBySiteId(int siteId) {
    List<Order> list = new ArrayList<>();
    try (PreparedStatement ps = getConnection().prepareStatement(FIND_BY_SITE_ID_SQL)) {
        ps.setInt(1, siteId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapOrder(rs));
            }
        }
    } catch (SQLException exception) {
        System.err.println("SiteOrderRepository.findBySiteId: " + exception.getMessage());
    }
    return list;
}

@Override
public Order findByIdForSite(int orderId, int siteId) {
    try (PreparedStatement ps = getConnection().prepareStatement(FIND_BY_ID_FOR_SITE_SQL)) {
        ps.setInt(1, orderId);
        ps.setInt(2, siteId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapOrder(rs);
            }
        }
    } catch (SQLException exception) {
        System.err.println("SiteOrderRepository.findByIdForSite: " + exception.getMessage());
    }
    return null;
}

@Override
public boolean updateStatusForSite(int orderId, int siteId, String newStatus) {
    try (PreparedStatement ps = getConnection().prepareStatement(UPDATE_STATUS_FOR_SITE_SQL)) {
        ps.setString(1, newStatus);
        ps.setInt(2, orderId);
        ps.setInt(3, siteId);
        return ps.executeUpdate() > 0;
    } catch (SQLException exception) {
        System.err.println("SiteOrderRepository.updateStatusForSite: " + exception.getMessage());
    }
    return false;
}
```

- [ ] **Step 7: Rewire `SiteModule` with the order port**

Update `SiteModule` constructor to accept `SiteOrderRepository`:

```java
public SiteModule(
    ConnectionProvider connectionProvider,
    TransactionRunner transactionRunner,
    CatalogModule catalogModule,
    SiteAccountProvisioningPort siteAccountProvisioningPort,
    SiteOrderRepository siteOrderRepository
)
```

Initialize:

```java
this.overseasSiteApplicationService = new OverseasSiteApplicationService(
    siteUseCase,
    catalogModule.catalogUseCase(),
    siteRepository,
    siteRepository,
    siteOrderRepository
);
```

Update `MvcContext` construction order if needed: `OrderModule` currently needs `SiteModule`, so direct injection from `OrderModule` into `SiteModule` creates a cycle. Prefer the smaller change:

1. Keep `SiteModule` constructor unchanged.
2. Add `initializeSiteOrderRepository(SiteOrderRepository siteOrderRepository)` to `SiteModule`.
3. In `MvcContext`, construct `SiteModule`, then `OrderModule`, then call `siteModule.initializeSiteOrderRepository(orderModule.siteOrderRepository())`.

Use this exact guard in `SiteModule`:

```java
public void initializeSiteOrderRepository(SiteOrderRepository siteOrderRepository) {
    this.overseasSiteApplicationService = new OverseasSiteApplicationService(
        siteUseCase,
        catalogModule.catalogUseCase(),
        siteRepository,
        siteRepository,
        siteOrderRepository
    );
}
```

This requires storing `CatalogModule catalogModule` in `SiteModule` as a field. If a nullable service is uncomfortable, make `overseasSiteApplicationService()` throw `IllegalStateException("Site order repository has not been initialized")` until initialization is called.

- [ ] **Step 8: Expose the Site order port from `OrderModule`**

Add:

```java
public SiteOrderRepository siteOrderRepository() {
    return (SiteOrderRepository) orderRepository;
}
```

This is acceptable because `JdbcOrderRepository` implements both `OrderRepository` and `SiteOrderRepository`. The service still depends on the narrow interface.

- [ ] **Step 9: Run focused model tests**

Run:

```powershell
.\mvnw.cmd -q -Dtest=OverseasSiteApplicationServiceTest test
```

Expected: PASS.

---

## Task 4: Site Controller Module

**Files:**
- Create: `src/main/java/org/itss/prj_itss/controller/site/SiteWorkspaceController.java`
- Create: `src/main/java/org/itss/prj_itss/controller/site/SiteControllerModule.java`

- [ ] **Step 1: Add `SiteWorkspaceController`**

```java
package org.itss.prj_itss.controller.site;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.RoleType;
import org.itss.prj_itss.model.site.application.self.OverseasSiteApplicationService;
import org.itss.prj_itss.model.site.application.self.SiteInventoryDraft;
import org.itss.prj_itss.model.site.application.self.SiteOrderItemRow;
import org.itss.prj_itss.model.site.application.self.SiteProfileDraft;
import org.itss.prj_itss.model.site.application.self.SiteWorkspaceResult;
import org.itss.prj_itss.model.site.application.self.SiteWorkspaceSnapshot;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class SiteWorkspaceController {

    private final OverseasSiteApplicationService service;
    private final Supplier<AuthenticatedUser> authenticatedUserSupplier;

    public SiteWorkspaceController(
        OverseasSiteApplicationService service,
        Supplier<AuthenticatedUser> authenticatedUserSupplier
    ) {
        this.service = Objects.requireNonNull(service, "service");
        this.authenticatedUserSupplier = Objects.requireNonNull(authenticatedUserSupplier, "authenticatedUserSupplier");
    }

    public SiteWorkspaceSnapshot load() {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceSnapshot.unavailable("Tài khoản Site chưa được liên kết với site.");
        }
        return service.load(siteId);
    }

    public SiteWorkspaceResult updateProfile(SiteProfileDraft draft) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceResult.failure("Tài khoản Site chưa được liên kết với site.");
        }
        return service.updateProfile(siteId, draft);
    }

    public SiteWorkspaceResult updateInventoryItem(SiteInventoryDraft draft) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceResult.failure("Tài khoản Site chưa được liên kết với site.");
        }
        return service.updateInventoryItem(siteId, draft);
    }

    public SiteWorkspaceResult removeInventoryItem(int merchandiseId) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceResult.failure("Tài khoản Site chưa được liên kết với site.");
        }
        return service.removeInventoryItem(siteId, merchandiseId);
    }

    public SiteWorkspaceResult confirmSupply(int orderId) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return SiteWorkspaceResult.failure("Tài khoản Site chưa được liên kết với site.");
        }
        return service.confirmSupply(siteId, orderId);
    }

    public List<SiteOrderItemRow> loadOrderItems(int orderId) {
        Integer siteId = currentSiteId();
        if (siteId == null) {
            return List.of();
        }
        return service.loadOrderItems(siteId, orderId);
    }

    private Integer currentSiteId() {
        AuthenticatedUser user = authenticatedUserSupplier.get();
        if (user == null || !RoleType.from(user).isSiteRole()) {
            return null;
        }
        return user.account().getSiteId();
    }
}
```

- [ ] **Step 2: Add `SiteControllerModule`**

```java
package org.itss.prj_itss.controller.site;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.site.SiteModule;

import java.util.function.Supplier;

public final class SiteControllerModule {

    private final SiteWorkspaceController siteWorkspaceController;

    public SiteControllerModule(SiteModule siteModule, Supplier<AuthenticatedUser> authenticatedUserSupplier) {
        this.siteWorkspaceController = new SiteWorkspaceController(
            siteModule.overseasSiteApplicationService(),
            authenticatedUserSupplier
        );
    }

    public SiteWorkspaceController siteWorkspaceController() {
        return siteWorkspaceController;
    }
}
```

- [ ] **Step 3: Compile the controller module**

Run:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: PASS. `MvcContext` route wiring is intentionally delayed until Task 5 because the view/FXML package does not exist yet.

---

## Task 5: Site Workspace JavaFX View

**Files:**
- Create: `src/main/java/org/itss/prj_itss/view/site/workspace/SiteWorkspaceView.java`
- Create: `src/main/java/org/itss/prj_itss/view/site/workspace/site-workspace-view.fxml`
- Modify: `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`
- Modify: `src/main/java/module-info.java`
- Modify: `src/main/java/org/itss/prj_itss/view/auth/RoleWorkspaceContentFactory.java`

- [ ] **Step 1: Create FXML shell**

Use a single route with tabs:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.ComboBox?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.Tab?>
<?import javafx.scene.control.TabPane?>
<?import javafx.scene.control.TableColumn?>
<?import javafx.scene.control.TableView?>
<?import javafx.scene.control.TextArea?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Region?>
<?import javafx.scene.layout.VBox?>

<BorderPane xmlns="http://javafx.com/javafx/17"
            xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="org.itss.prj_itss.view.site.workspace.SiteWorkspaceView"
            styleClass="content-area">
    <top>
        <VBox spacing="4" styleClass="page-header">
            <children>
                <Label text="Không gian Site" styleClass="page-title"/>
                <Label fx:id="siteSubtitleLabel" styleClass="page-description"/>
            </children>
        </VBox>
    </top>
    <center>
        <TabPane fx:id="workspaceTabs" tabClosingPolicy="UNAVAILABLE">
            <tabs>
                <Tab text="Hồ sơ">
                    <VBox spacing="14" styleClass="page-content">
                        <children>
                            <Label fx:id="profileMessageLabel" styleClass="page-description"/>
                            <TextField fx:id="siteCodeField" editable="false"/>
                            <TextField fx:id="siteNameField"/>
                            <TextArea fx:id="descriptionArea" prefRowCount="4"/>
                            <HBox spacing="12">
                                <children>
                                    <TextField fx:id="shipDaysField" promptText="Số ngày vận chuyển biển"/>
                                    <TextField fx:id="airDaysField" promptText="Số ngày vận chuyển hàng không"/>
                                </children>
                            </HBox>
                            <Button fx:id="saveProfileButton" text="Lưu hồ sơ" styleClass="btn-primary"/>
                        </children>
                    </VBox>
                </Tab>
                <Tab text="Mặt hàng & tồn kho">
                    <VBox spacing="14" styleClass="page-content">
                        <children>
                            <HBox spacing="12">
                                <children>
                                    <ComboBox fx:id="merchandiseComboBox" prefWidth="280"/>
                                    <TextField fx:id="stockQuantityField" promptText="Tồn kho" prefWidth="120"/>
                                    <Button fx:id="saveInventoryButton" text="Cập nhật" styleClass="btn-primary"/>
                                    <Button fx:id="removeInventoryButton" text="Bỏ mặt hàng"/>
                                </children>
                            </HBox>
                            <TableView fx:id="inventoryTable">
                                <columns>
                                    <TableColumn fx:id="inventoryCodeColumn" text="Mã hàng" prefWidth="120"/>
                                    <TableColumn fx:id="inventoryNameColumn" text="Tên mặt hàng" prefWidth="220"/>
                                    <TableColumn fx:id="inventoryUnitColumn" text="Đơn vị" prefWidth="100"/>
                                    <TableColumn fx:id="inventoryStockColumn" text="Tồn kho" prefWidth="100"/>
                                </columns>
                            </TableView>
                        </children>
                    </VBox>
                </Tab>
                <Tab text="Đơn hàng">
                    <VBox spacing="14" styleClass="page-content">
                        <children>
                            <TableView fx:id="orderTable">
                                <columns>
                                    <TableColumn fx:id="orderCodeColumn" text="Mã đơn" prefWidth="120"/>
                                    <TableColumn fx:id="requestCodeColumn" text="Yêu cầu gốc" prefWidth="120"/>
                                    <TableColumn fx:id="createdAtColumn" text="Ngày tạo" prefWidth="120"/>
                                    <TableColumn fx:id="orderStatusColumn" text="Trạng thái" prefWidth="160"/>
                                    <TableColumn fx:id="orderActionColumn" text="Thao tác" prefWidth="180"/>
                                </columns>
                            </TableView>
                            <VBox fx:id="orderDetailBox" spacing="10" styleClass="card"/>
                            <TableView fx:id="orderItemTable">
                                <columns>
                                    <TableColumn fx:id="orderItemCodeColumn" text="Mã hàng" prefWidth="120"/>
                                    <TableColumn fx:id="orderItemNameColumn" text="Tên mặt hàng" prefWidth="220"/>
                                    <TableColumn fx:id="orderItemQuantityColumn" text="Số lượng" prefWidth="100"/>
                                    <TableColumn fx:id="orderItemUnitColumn" text="Đơn vị" prefWidth="100"/>
                                    <TableColumn fx:id="orderItemDeliveryColumn" text="Vận chuyển" prefWidth="140"/>
                                </columns>
                            </TableView>
                        </children>
                    </VBox>
                </Tab>
            </tabs>
        </TabPane>
    </center>
</BorderPane>
```

- [ ] **Step 2: Create `SiteWorkspaceView` fields and initialization**

Required fields:

```java
private final ObservableList<SiteInventoryRow> inventoryRows = FXCollections.observableArrayList();
private final ObservableList<SiteOrderRow> orderRows = FXCollections.observableArrayList();
private final ObservableList<SiteOrderItemRow> orderItemRows = FXCollections.observableArrayList();
private final ObservableList<Merchandise> merchandiseOptions = FXCollections.observableArrayList();

private Navigator navigator;
private SiteWorkspaceController controller;
private SiteWorkspaceSnapshot currentSnapshot;
```

In `initialize()`:

```java
TableViewSupport.useConstrainedResize(inventoryTable);
TableViewSupport.bindStringColumn(inventoryCodeColumn, SiteInventoryRow::merchandiseCode);
TableViewSupport.bindStringColumn(inventoryNameColumn, SiteInventoryRow::merchandiseName);
TableViewSupport.bindStringColumn(inventoryUnitColumn, SiteInventoryRow::unit);
TableViewSupport.bindStringColumn(inventoryStockColumn, row -> String.valueOf(row.stockQuantity()));
inventoryTable.setItems(inventoryRows);

TableViewSupport.useConstrainedResize(orderTable);
TableViewSupport.bindStringColumn(orderCodeColumn, SiteOrderRow::orderCode);
TableViewSupport.bindStringColumn(requestCodeColumn, SiteOrderRow::requestCode);
TableViewSupport.bindStringColumn(createdAtColumn, SiteOrderRow::createdAt);
TableViewSupport.bindStringColumn(orderStatusColumn, SiteOrderRow::statusText);
TableViewSupport.bindRowColumn(orderActionColumn);
orderTable.setItems(orderRows);

TableViewSupport.useConstrainedResize(orderItemTable);
TableViewSupport.bindStringColumn(orderItemCodeColumn, SiteOrderItemRow::merchandiseCode);
TableViewSupport.bindStringColumn(orderItemNameColumn, SiteOrderItemRow::merchandiseName);
TableViewSupport.bindStringColumn(orderItemQuantityColumn, SiteOrderItemRow::quantity);
TableViewSupport.bindStringColumn(orderItemUnitColumn, SiteOrderItemRow::unit);
TableViewSupport.bindStringColumn(orderItemDeliveryColumn, SiteOrderItemRow::deliveryMethod);
orderItemTable.setItems(orderItemRows);

saveProfileButton.setOnAction(event -> saveProfile());
saveInventoryButton.setOnAction(event -> saveInventory());
removeInventoryButton.setOnAction(event -> removeInventory());
inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, row) -> populateInventorySelection(row));
orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, row) -> showOrderDetail(row));
```

- [ ] **Step 3: Add `init` and reload behavior**

```java
public void init(Navigator navigator, SiteWorkspaceController controller) {
    this.navigator = navigator;
    this.controller = controller;
    reload();
}

@Override
public void onViewShown() {
    reload();
}

private void reload() {
    if (controller == null) {
        return;
    }
    SiteWorkspaceSnapshot snapshot = controller.load();
    currentSnapshot = snapshot;
    if (!snapshot.available()) {
        setWorkspaceDisabled(snapshot.message());
        return;
    }
    setWorkspaceEnabled(snapshot);
}
```

Disable state:

```java
private void setWorkspaceDisabled(String message) {
    workspaceTabs.setDisable(true);
    siteSubtitleLabel.setText(message);
    profileMessageLabel.setText(message);
    inventoryRows.clear();
    orderRows.clear();
    orderItemRows.clear();
    merchandiseOptions.clear();
}
```

- [ ] **Step 4: Populate snapshot data**

```java
private void setWorkspaceEnabled(SiteWorkspaceSnapshot snapshot) {
    workspaceTabs.setDisable(false);
    Site site = snapshot.site();
    siteSubtitleLabel.setText(site.getSiteCode() + " - " + site.getName());
    siteCodeField.setText(site.getSiteCode());
    siteNameField.setText(site.getName());
    descriptionArea.setText(site.getDescription() == null ? "" : site.getDescription());
    shipDaysField.setText(site.getShipDeliveryDays() == null ? "" : String.valueOf(site.getShipDeliveryDays()));
    airDaysField.setText(site.getAirDeliveryDays() == null ? "" : String.valueOf(site.getAirDeliveryDays()));
    merchandiseOptions.setAll(snapshot.merchandiseOptions());
    merchandiseComboBox.setItems(merchandiseOptions);
    merchandiseComboBox.setConverter(new StringConverter<>() {
        @Override
        public String toString(Merchandise merchandise) {
            return merchandise == null ? "" : merchandise.getCode() + " - " + merchandise.getName();
        }

        @Override
        public Merchandise fromString(String value) {
            return null;
        }
    });
    inventoryRows.setAll(snapshot.inventoryRows());
    orderRows.setAll(snapshot.orders());
}
```

- [ ] **Step 5: Implement profile save**

```java
private void saveProfile() {
    SiteWorkspaceResult result = controller.updateProfile(new SiteProfileDraft(
        siteNameField.getText(),
        descriptionArea.getText(),
        parseOptionalInt(shipDaysField.getText()),
        parseOptionalInt(airDaysField.getText())
    ));
    showResult(result);
    if (result.success()) {
        reload();
    }
}
```

Helper:

```java
private Integer parseOptionalInt(String value) {
    if (value == null || value.trim().isEmpty()) {
        return null;
    }
    try {
        return Integer.parseInt(value.trim());
    } catch (NumberFormatException exception) {
        return -1;
    }
}
```

Returning `-1` intentionally lets the model validation return the existing invalid-day message.

- [ ] **Step 6: Implement inventory actions**

```java
private void saveInventory() {
    Merchandise selected = merchandiseComboBox.getValue();
    if (selected == null) {
        showWarning("Vui lòng chọn mặt hàng.");
        return;
    }
    Integer stock = parseRequiredInt(stockQuantityField.getText());
    if (stock == null) {
        showWarning("Tồn kho phải là số nguyên không âm.");
        return;
    }
    SiteWorkspaceResult result = controller.updateInventoryItem(new SiteInventoryDraft(selected.getId(), stock));
    showResult(result);
    if (result.success()) {
        reload();
    }
}

private void removeInventory() {
    SiteInventoryRow selected = inventoryTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
        showWarning("Vui lòng chọn mặt hàng cần bỏ.");
        return;
    }
    SiteWorkspaceResult result = controller.removeInventoryItem(selected.merchandiseId());
    showResult(result);
    if (result.success()) {
        reload();
    }
}

private Integer parseRequiredInt(String value) {
    if (value == null || value.trim().isEmpty()) {
        return null;
    }
    try {
        return Integer.parseInt(value.trim());
    } catch (NumberFormatException exception) {
        return null;
    }
}
```

- [ ] **Step 7: Implement order actions**

In `orderActionColumn` cell factory, create:

- `Chi tiết` button: calls `showOrderDetail(row)`.
- `Xác nhận` button: enabled only when `row.confirmable()` is true, calls `confirmSupply(row.orderId())`.

Confirmation method:

```java
private void confirmSupply(int orderId) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Xác nhận cung ứng");
    confirm.setHeaderText("Xác nhận cung ứng đơn hàng này?");
    confirm.setContentText("Sau khi xác nhận, trạng thái đơn hàng sẽ chuyển sang Đang giao.");
    confirm.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            SiteWorkspaceResult result = controller.confirmSupply(orderId);
            showResult(result);
            if (result.success()) {
                reload();
            }
        }
    });
}
```

For detail display, show order metadata in `orderDetailBox` and load item rows through the controller:

```java
private void showOrderDetail(SiteOrderRow row) {
    orderDetailBox.getChildren().clear();
    if (row == null) {
        orderDetailBox.getChildren().add(new Label("Chọn một đơn hàng để xem chi tiết."));
        orderItemRows.clear();
        return;
    }
    orderDetailBox.getChildren().addAll(
        new Label("Mã đơn: " + row.orderCode()),
        new Label("Yêu cầu gốc: " + row.requestCode()),
        new Label("Trạng thái: " + row.statusText())
    );
    orderItemRows.setAll(controller.loadOrderItems(row.orderId()));
}
```

- [ ] **Step 8: Register the route in `MvcContext`**

Add imports for the new controller and view:

```java
import org.itss.prj_itss.controller.site.SiteControllerModule;
import org.itss.prj_itss.view.site.workspace.SiteWorkspaceView;
```

Avoid name collision with existing `controller.ordering.site.SiteControllerModule` by renaming the existing field to `orderingSiteControllers` if needed:

```java
private final org.itss.prj_itss.controller.ordering.site.SiteControllerModule orderingSiteControllers =
    new org.itss.prj_itss.controller.ordering.site.SiteControllerModule(siteModule);

private final SiteControllerModule siteWorkspaceControllers =
    new SiteControllerModule(siteModule, this::currentAuthenticatedUser);
```

Add route:

```java
RouteRegistry.fxml(
    "site-workspace",
    "/org/itss/prj_itss/view/site/workspace/site-workspace-view.fxml",
    (viewId, viewInstance, navigator) ->
        ((SiteWorkspaceView) viewInstance).init(
            navigator,
            siteWorkspaceControllers.siteWorkspaceController()
        )
),
```

- [ ] **Step 9: Open the new view package**

Add to `module-info.java`:

```java
opens org.itss.prj_itss.view.site.workspace to javafx.fxml;
```

- [ ] **Step 10: Update Site role workspace copy**

In `RoleWorkspaceContentFactory`, change the Site content from "module chưa triển khai" to "đã triển khai" and point the text to the new workspace. Do not change access policy there; access remains in `RoleAccessPolicy`.

- [ ] **Step 11: Run compile**

Run:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: PASS.

---

## Task 6: Final Verification

**Files:**
- Verify all touched files.

- [ ] **Step 1: Run model and access tests**

Run:

```powershell
.\mvnw.cmd -q -Dtest=OverseasSiteApplicationServiceTest,RoleAccessPolicyTest test
```

Expected: PASS.

- [ ] **Step 2: Run MVC architecture guardrail**

Run:

```powershell
.\mvnw.cmd -q -Dtest=MvcDependencyTest test
```

Expected: PASS.

- [ ] **Step 3: Compile the app**

Run:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: PASS.

- [ ] **Step 4: Run full tests**

Run:

```powershell
.\mvnw.cmd -q test
```

Expected: PASS. If Maven clean/file-lock issues appear under `target`, treat them separately from source correctness and rerun plain `test` without `clean`.

- [ ] **Step 5: Manual JavaFX smoke**

Run only when GUI smoke is needed:

```powershell
.\mvnw.cmd javafx:run
```

Smoke checklist:

- Login as Ordering.
- Confirm `Quản lý Site` still works for Site records and Site-account creation.
- Create or pick a Site account linked to a Site.
- Login as that Site account.
- Confirm default screen is `Không gian Site`.
- Confirm Ordering/Admin/Sales/Warehouse nav groups are not visible.
- Update profile fields and reload.
- Add/update a merchandise stock row.
- Remove a merchandise stock row.
- Confirm only orders for that Site appear.
- Confirm a `pending` order changes to `shipping` after Site accepts supply.
- Confirm a non-pending order does not show an enabled accept action.

---

## Design Review Notes

- Keep the names separate: `SiteManagementView` is an Ordering screen; `SiteWorkspaceView` is the Site actor screen.
- Do not add Site self-service methods into `SiteManagementApplicationService`. That service already handles Ordering-owned Site management and Site-account provisioning.
- Do not let `SiteWorkspaceView` import `JdbcSiteRepository`, `JdbcOrderRepository`, `ConnectionProvider`, or `TransactionManager`.
- Do not let `SiteWorkspaceController` import JavaFX.
- Do not let `OverseasSiteApplicationService` depend on view/controller packages.
- Prefer narrow ports (`SiteProfileCommandPort`, `SiteInventoryCommandPort`, `SiteOrderRepository`) even if the same JDBC adapter implements them.
- Keep Site code read-only for Site users. Ordering owns initial Site creation and code management.
- Keep `pending -> shipping` as the only Site confirmation transition for this release.
- If Site rejection becomes required later, add a separate `rejectSupply` model method and decide whether it maps to `cancelled` or a distinct rejected status before touching UI.
