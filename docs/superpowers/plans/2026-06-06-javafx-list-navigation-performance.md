# JavaFX List Navigation Performance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reduce visible lag when users click JavaFX navigation buttons for list screens by removing duplicate reloads and replacing N+1 list queries with aggregate queries.

**Architecture:** Keep the current JavaFX MVC structure. Views remain responsible for binding UI controls and triggering lifecycle refreshes, controllers stay thin, application services assemble view rows, and repositories own SQL. Do not add, delete, or rename implementation files; all code changes must be made inside existing files.

**Tech Stack:** Java 21, JavaFX/FXML, Maven, JDBC/PostgreSQL, JUnit 5.

---

## Scope And Constraints

- Do not add implementation files.
- Do not delete files.
- Do not rename files.
- Do not move classes/packages.
- Do not change route ids, FXML names, or resource paths.
- The plan file itself is the only new file created for planning.
- Keep strict MVC: View triggers reload/display only; Controller delegates; Model/Application/Repository owns data assembly and SQL.
- Do not introduce background loading until duplicate reloads and N+1 query sources are fixed first.

## Root Cause Summary

The current navigation flow is synchronous:

1. `MainLayoutView.registerNavButton(...)` installs `button.setOnAction(event -> showView(viewId))`.
2. `MainLayoutView.showView(...)` resolves/sets the content node and calls `ViewLifecycle.onViewShown()`.
3. Several list views call `reload()` in both `init(...)` and `onViewShown()`.
4. Each `reload()` calls controller/application/repository methods synchronously on the JavaFX Application Thread.
5. Some application services issue N+1 queries while mapping list rows.

This means the UI freezes while JDBC and row mapping finish. Route-level FXML caching in `RouteRegistry` does not prevent data reloads, because `onViewShown()` still executes whenever the cached view is shown.

## File Responsibility Map

### Navigation And Lifecycle

- Modify: `src/main/java/org/itss/prj_itss/view/sales/request/list/SalesRequestListView.java`
  - Stop initial duplicate reload; load on `onViewShown()` and after successful create/edit/delete.
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/ReceivedRequestsView.java`
  - Stop initial duplicate reload; load on `onViewShown()`.
- Modify: `src/main/java/org/itss/prj_itss/view/warehouse/WarehouseIncomingOrdersView.java`
  - Stop initial duplicate reload; load on `onViewShown()`.
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/order/management/OrderManagementView.java`
  - Stop initial duplicate reload; load on `onViewShown()`.
- Modify: `src/main/java/org/itss/prj_itss/view/sales/merchandise/SalesMerchandiseManagementView.java`
  - Stop initial duplicate reload; load on `onViewShown()` and after successful mutations.
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/site/SiteManagementView.java`
  - Stop initial duplicate reload; load on `onViewShown()` and after successful create.
- Modify: `src/main/java/org/itss/prj_itss/view/admin/account/AccountManagementView.java`
  - Stop initial duplicate reload; load on `onViewShown()` and after successful mutations.

### Request List Aggregation

- Modify: `src/main/java/org/itss/prj_itss/model/request/application/listing/ReceivedRequestsPort.java`
  - Add aggregate methods for item counts and earliest delivery dates by request id.
- Modify: `src/main/java/org/itss/prj_itss/model/dashboard/application/port/DashboardRequestPort.java`
  - Add the same aggregate methods because dashboard has the same N+1 pattern.
- Modify: `src/main/java/org/itss/prj_itss/model/request/infrastructure/persistence/JdbcReceivedRequestsRepository.java`
  - Implement aggregate SQL with `GROUP BY request_id`.
- Modify: `src/main/java/org/itss/prj_itss/model/request/infrastructure/persistence/JdbcDashboardRequestRepository.java`
  - Implement aggregate SQL with `GROUP BY request_id`.
- Modify: `src/main/java/org/itss/prj_itss/model/request/application/listing/ReceivedRequestsApplicationService.java`
  - Use aggregate maps once per list reload instead of querying per row.
- Modify: `src/main/java/org/itss/prj_itss/model/dashboard/application/DashboardQuery.java`
  - Use aggregate maps once per dashboard reload instead of querying per request.

### Warehouse Incoming Orders Aggregation

- Modify: `src/main/java/org/itss/prj_itss/model/warehouse/application/WarehouseIncomingOrderQuery.java`
  - Use one site map and one item-count map for all incoming order rows.
- Modify: `src/main/java/org/itss/prj_itss/model/order/application/port/OrderRepository.java`
  - Keep existing `countItemsGroupedByOrderId()` default method; no signature change required.
- Modify: `src/main/java/org/itss/prj_itss/model/order/infrastructure/persistence/JdbcOrderRepository.java`
  - Keep existing grouped count implementation; no new query needed unless tests expose a bug.
- Test: `src/test/java/org/itss/prj_itss/model/warehouse/application/WarehouseIncomingOrderQueryTest.java`
  - Add/adjust assertions proving list rows do not call per-order item lookup.

### Site Management Aggregation

- Modify: `src/main/java/org/itss/prj_itss/model/site/application/port/InventoryRepository.java`
  - Add grouped merchandise-count method for all sites.
- Modify: `src/main/java/org/itss/prj_itss/model/site/infrastructure/persistence/JdbcSiteRepository.java`
  - Implement grouped count SQL.
- Modify: `src/main/java/org/itss/prj_itss/model/site/application/SiteUseCase.java`
  - Expose grouped count through the model use case.
- Modify: `src/main/java/org/itss/prj_itss/model/site/application/SiteManagementApplicationService.java`
  - Use the grouped count map while creating `SiteRow`.
- Test: `src/test/java/org/itss/prj_itss/model/site/application/SiteManagementApplicationServiceTest.java`
  - Update fake inventory repository and add/adjust test coverage for grouped count usage.

## Task 1: Remove Duplicate Initial Reloads

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/view/sales/request/list/SalesRequestListView.java:121-138`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/request/ReceivedRequestsView.java:172-184`
- Modify: `src/main/java/org/itss/prj_itss/view/warehouse/WarehouseIncomingOrdersView.java:90-98`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/order/management/OrderManagementView.java:138-146`
- Modify: `src/main/java/org/itss/prj_itss/view/sales/merchandise/SalesMerchandiseManagementView.java:93-101`
- Modify: `src/main/java/org/itss/prj_itss/view/ordering/site/SiteManagementView.java:60-68`
- Modify: `src/main/java/org/itss/prj_itss/view/admin/account/AccountManagementView.java:60-68`

- [ ] **Step 1: Confirm current duplicate reload pattern**

Run:

```powershell
rg -n "public void init\(|reload\(\);|public void onViewShown\(" src\main\java\org\itss\prj_itss\view\sales\request\list\SalesRequestListView.java src\main\java\org\itss\prj_itss\view\ordering\request\ReceivedRequestsView.java src\main\java\org\itss\prj_itss\view\warehouse\WarehouseIncomingOrdersView.java src\main\java\org\itss\prj_itss\view\ordering\order\management\OrderManagementView.java src\main\java\org\itss\prj_itss\view\sales\merchandise\SalesMerchandiseManagementView.java src\main\java\org\itss\prj_itss\view\ordering\site\SiteManagementView.java src\main\java\org\itss\prj_itss\view\admin\account\AccountManagementView.java
```

Expected before change: each listed view has `reload();` inside `init(...)` and `reload();` inside `onViewShown()`.

- [ ] **Step 2: Remove reload from each `init(...)` method**

For each listed view, keep dependency assignment and remove only the initial `reload();`.

Example target shape:

```java
public void init(Navigator navigator, SomeController controller) {
    this.navigator = navigator;
    this.controller = controller;
}

@Override
public void onViewShown() {
    reload();
}
```

For `SalesRequestListView`, preserve all five assigned collaborators:

```java
public void init(
        Navigator navigator,
        SalesRequestListController controller,
        SalesRequestCreationController createController,
        SalesRequestEditDialogLauncher editDialogLauncher,
        ViewOrderRequestController viewController
) {
    this.navigator = navigator;
    this.controller = controller;
    this.createController = createController;
    this.editDialogLauncher = editDialogLauncher;
    this.viewController = viewController;
}
```

- [ ] **Step 3: Keep mutation-triggered reloads**

Do not remove reloads after successful create/edit/delete/disable/deactivate/restore. Those reloads are data invalidation points and should stay.

Keep examples like:

```java
if (result.success()) reload();
```

and callbacks like:

```java
public void onSalesRequestSaved(SalesRequestSavedEvent event) {
    reload();
}
```

- [ ] **Step 4: Verify duplicate reload removal by search**

Run:

```powershell
rg -n "public void init\(|reload\(\);|public void onViewShown\(" src\main\java\org\itss\prj_itss\view\sales\request\list\SalesRequestListView.java src\main\java\org\itss\prj_itss\view\ordering\request\ReceivedRequestsView.java src\main\java\org\itss\prj_itss\view\warehouse\WarehouseIncomingOrdersView.java src\main\java\org\itss\prj_itss\view\ordering\order\management\OrderManagementView.java src\main\java\org\itss\prj_itss\view\sales\merchandise\SalesMerchandiseManagementView.java src\main\java\org\itss\prj_itss\view\ordering\site\SiteManagementView.java src\main\java\org\itss\prj_itss\view\admin\account\AccountManagementView.java
```

Expected after change: `init(...)` methods no longer contain immediate `reload();`; `onViewShown()` methods still call `reload();`.

## Task 2: Replace Request List N+1 Queries With Aggregate Maps

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/model/request/application/listing/ReceivedRequestsPort.java`
- Modify: `src/main/java/org/itss/prj_itss/model/dashboard/application/port/DashboardRequestPort.java`
- Modify: `src/main/java/org/itss/prj_itss/model/request/infrastructure/persistence/JdbcReceivedRequestsRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/request/infrastructure/persistence/JdbcDashboardRequestRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/request/application/listing/ReceivedRequestsApplicationService.java`
- Modify: `src/main/java/org/itss/prj_itss/model/dashboard/application/DashboardQuery.java`

- [ ] **Step 1: Extend request query ports**

In `ReceivedRequestsPort.java`, add imports and methods:

```java
import java.util.Map;
import java.util.Set;
```

```java
Map<Integer, Integer> countItemTypesByRequestIds(Set<Integer> requestIds);
Map<Integer, LocalDate> findEarliestDeliveryDatesByRequestIds(Set<Integer> requestIds);
```

In `DashboardRequestPort.java`, add the same imports and methods:

```java
import java.util.Map;
import java.util.Set;
```

```java
Map<Integer, Integer> countItemTypesByRequestIds(Set<Integer> requestIds);
Map<Integer, LocalDate> findEarliestDeliveryDatesByRequestIds(Set<Integer> requestIds);
```

- [ ] **Step 2: Implement aggregate methods in `JdbcReceivedRequestsRepository`**

Add imports:

```java
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
```

Add helper:

```java
private String placeholders(int count) {
    return String.join(", ", Collections.nCopies(count, "?"));
}
```

Add implementation:

```java
@Override
public Map<Integer, Integer> countItemTypesByRequestIds(Set<Integer> requestIds) {
    Map<Integer, Integer> counts = new HashMap<>();
    if (requestIds == null || requestIds.isEmpty()) {
        return counts;
    }

    String sql = "SELECT request_id, COUNT(*) AS item_count " +
                 "FROM request_merchandise " +
                 "WHERE request_id IN (" + placeholders(requestIds.size()) + ") " +
                 "GROUP BY request_id";
    try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
        int index = 1;
        for (Integer requestId : requestIds) {
            ps.setInt(index++, requestId);
        }
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counts.put(rs.getInt("request_id"), rs.getInt("item_count"));
            }
        }
    } catch (SQLException e) {
        System.err.println("JdbcReceivedRequestsRepository.countItemTypesByRequestIds: " + e.getMessage());
    }
    return counts;
}

@Override
public Map<Integer, LocalDate> findEarliestDeliveryDatesByRequestIds(Set<Integer> requestIds) {
    Map<Integer, LocalDate> dates = new HashMap<>();
    if (requestIds == null || requestIds.isEmpty()) {
        return dates;
    }

    String sql = "SELECT request_id, MIN(desired_delivery_date) AS earliest_delivery_date " +
                 "FROM request_merchandise " +
                 "WHERE request_id IN (" + placeholders(requestIds.size()) + ") " +
                 "GROUP BY request_id";
    try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
        int index = 1;
        for (Integer requestId : requestIds) {
            ps.setInt(index++, requestId);
        }
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date date = rs.getDate("earliest_delivery_date");
                if (date != null) {
                    dates.put(rs.getInt("request_id"), date.toLocalDate());
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("JdbcReceivedRequestsRepository.findEarliestDeliveryDatesByRequestIds: " + e.getMessage());
    }
    return dates;
}
```

- [ ] **Step 3: Implement the same aggregate methods in `JdbcDashboardRequestRepository`**

Use the same imports, `placeholders(int count)` helper, and method bodies as `JdbcReceivedRequestsRepository`. Keep the error prefix as `JdbcDashboardRequestRepository...`.

- [ ] **Step 4: Update `ReceivedRequestsApplicationService.findRows()`**

Replace per-row calls with one collection of ids and two maps:

```java
public List<RequestRow> findRows() {
    List<Request> requests = requestService.findAll();
    java.util.Set<Integer> requestIds = requests.stream()
        .map(Request::getId)
        .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    java.util.Map<Integer, Integer> itemCounts = requestService.countItemTypesByRequestIds(requestIds);
    java.util.Map<Integer, LocalDate> earliestDeliveries =
        requestService.findEarliestDeliveryDatesByRequestIds(requestIds);

    return requests.stream()
        .map(request -> new RequestRow(
            request.getId(),
            OrderingFormatters.formatRequestCode(request.getId()),
            OrderingFormatters.formatDateOrEmpty(request.getCreatedAt()),
            OrderingFormatters.formatItemTypes(itemCounts.getOrDefault(request.getId(), 0)),
            OrderingFormatters.formatDate(earliestDeliveries.get(request.getId())),
            request.getStatus() == null ? "N/A" : request.getStatusKey()
        ))
        .toList();
}
```

Add fully qualified names as shown, or add imports for `LinkedHashSet`, `Map`, and `Collectors`.

- [ ] **Step 5: Update `DashboardQuery.loadDashboardData()`**

Replace per-request calls with maps:

```java
public DashboardData loadDashboardData() {
    java.util.List<org.itss.prj_itss.model.request.domain.request.Request> requests = requestService.findAll();
    java.util.Set<Integer> requestIds = requests.stream()
        .map(org.itss.prj_itss.model.request.domain.request.Request::getId)
        .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    java.util.Map<Integer, java.time.LocalDate> earliestDeliveries =
        requestService.findEarliestDeliveryDatesByRequestIds(requestIds);
    java.util.Map<Integer, Integer> itemCounts =
        requestService.countItemTypesByRequestIds(requestIds);

    java.util.List<DashboardRequestInfo> requestInfos = requests.stream()
        .map(req -> new DashboardRequestInfo(
            req,
            earliestDeliveries.get(req.getId()),
            itemCounts.getOrDefault(req.getId(), 0)
        ))
        .toList();

    return new DashboardData(
        requestInfos,
        orderRepository.findAll(),
        siteService.countAll()
    );
}
```

- [ ] **Step 6: Compile-check request aggregation**

Run:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: compile passes. If it fails, fix missing imports or classes that implement the edited interfaces.

## Task 3: Use Existing Order Aggregate Count In Warehouse Incoming Orders

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/model/warehouse/application/WarehouseIncomingOrderQuery.java`
- Modify if needed by tests: `src/test/java/org/itss/prj_itss/model/warehouse/application/WarehouseIncomingOrderQueryTest.java`

- [ ] **Step 1: Add a failing behavior assertion in existing warehouse test**

In `WarehouseIncomingOrderQueryTest.java`, update the fake `OrderRepository` so it tracks calls to `findItemsByOrderId(int orderId)` during `findIncomingRows()`.

Add a fake field:

```java
int findItemsByOrderIdCalls;
```

In fake `findItemsByOrderId(...)`:

```java
findItemsByOrderIdCalls++;
return itemsByOrderId.getOrDefault(orderId, List.of());
```

In the test that calls `findIncomingRows()`, assert:

```java
assertEquals(0, orderRepository.findItemsByOrderIdCalls);
```

Expected before implementation: this fails because `toRow(order)` currently calls `findItemsByOrderId(order.getId())` for every row.

- [ ] **Step 2: Update `WarehouseIncomingOrderQuery.findIncomingRows()`**

Change row building to prefetch sites and item counts:

```java
public List<IncomingOrderRow> findIncomingRows() {
    List<Order> orders = orderRepository.findByStatus(OrderStatus.SHIPPING.displayValue());
    java.util.Map<Integer, Site> siteMap = siteUseCase.findAll().stream()
        .collect(java.util.stream.Collectors.toMap(Site::getId, java.util.function.Function.identity(), (a, b) -> a));
    java.util.Map<Integer, Integer> itemCounts = orderRepository.countItemsGroupedByOrderId();

    return orders.stream()
        .sorted(Comparator
            .comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Comparator.comparingInt(Order::getId).reversed()))
        .map(order -> toRow(order, siteMap, itemCounts))
        .toList();
}
```

Add overloaded `toRow(...)`:

```java
private IncomingOrderRow toRow(Order order, java.util.Map<Integer, Site> siteMap, java.util.Map<Integer, Integer> itemCounts) {
    Site site = siteMap.get(order.getSiteId());
    int itemCount = itemCounts.getOrDefault(order.getId(), 0);
    return new IncomingOrderRow(
        order.getId(),
        order.getRequestId(),
        order.getSiteId(),
        OrderingFormatters.formatOrderCode(order.getId()),
        OrderingFormatters.formatRequestCode(order.getRequestId()),
        site == null ? "N/A" : safeText(site.getSiteCode()),
        site == null ? "Site #" + order.getSiteId() : safeText(site.getName()),
        OrderingFormatters.formatDateOrEmpty(order.getCreatedAt()),
        order.getStatus(),
        OrderingFormatters.orderStatusText(order.getStatus()),
        itemCount
    );
}
```

Keep the existing `toRow(Order order)` for detail dialogs and make it delegate safely:

```java
private IncomingOrderRow toRow(Order order) {
    Site site = siteUseCase.findById(order.getSiteId());
    int itemCount = orderRepository.findItemsByOrderId(order.getId()).size();
    return new IncomingOrderRow(
        order.getId(),
        order.getRequestId(),
        order.getSiteId(),
        OrderingFormatters.formatOrderCode(order.getId()),
        OrderingFormatters.formatRequestCode(order.getRequestId()),
        site == null ? "N/A" : safeText(site.getSiteCode()),
        site == null ? "Site #" + order.getSiteId() : safeText(site.getName()),
        OrderingFormatters.formatDateOrEmpty(order.getCreatedAt()),
        order.getStatus(),
        OrderingFormatters.orderStatusText(order.getStatus()),
        itemCount
    );
}
```

- [ ] **Step 3: Run warehouse query test**

Run:

```powershell
.\mvnw.cmd -q -Dtest=WarehouseIncomingOrderQueryTest test
```

Expected: test passes, including the assertion that `findIncomingRows()` does not call per-order `findItemsByOrderId(...)`.

## Task 4: Replace Site Management N+1 Count With Grouped Count

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/model/site/application/port/InventoryRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/infrastructure/persistence/JdbcSiteRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/application/SiteUseCase.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/application/SiteManagementApplicationService.java`
- Modify: `src/test/java/org/itss/prj_itss/model/site/application/SiteManagementApplicationServiceTest.java`

- [ ] **Step 1: Extend `InventoryRepository`**

Add method:

```java
Map<Integer, Integer> countMerchandiseGroupedBySiteId();
```

- [ ] **Step 2: Implement grouped count in `JdbcSiteRepository`**

Add method:

```java
@Override
public Map<Integer, Integer> countMerchandiseGroupedBySiteId() {
    Map<Integer, Integer> counts = new HashMap<>();
    String sql = "SELECT site_id, COUNT(*) AS item_count " +
                 "FROM site_inventory " +
                 "WHERE stock_quantity > 0 " +
                 "GROUP BY site_id";
    try (PreparedStatement ps = getConnection().prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            counts.put(rs.getInt("site_id"), rs.getInt("item_count"));
        }
    } catch (SQLException e) {
        System.err.println("SiteRepository.countMerchandiseGroupedBySiteId: " + e.getMessage());
    }
    return counts;
}
```

- [ ] **Step 3: Expose grouped count from `SiteUseCase`**

Add method:

```java
public Map<Integer, Integer> countMerchandiseGroupedBySiteId() {
    return inventoryRepository.countMerchandiseGroupedBySiteId();
}
```

- [ ] **Step 4: Update `SiteManagementApplicationService.load()`**

Replace `sites.stream().map(this::toRow).toList()` with grouped count usage:

```java
public Snapshot load() {
    List<Site> sites = siteService.findAll();
    java.util.Map<Integer, Integer> merchandiseCounts = siteService.countMerchandiseGroupedBySiteId();
    List<SiteRow> rows = sites.stream()
        .map(site -> toRow(site, merchandiseCounts))
        .toList();
    return new Snapshot(rows, sites.size(), sites.size(), merchandiseService.countAll());
}
```

Add overloaded row mapper:

```java
private SiteRow toRow(Site site, java.util.Map<Integer, Integer> merchandiseCounts) {
    int itemCount = merchandiseCounts.getOrDefault(site.getId(), 0);
    return new SiteRow(
        site,
        site.getId(),
        site.getSiteCode(),
        site.getName(),
        OrderingFormatters.blankToFallback(site.getDescription(), "-"),
        OrderingFormatters.formatDays(site.getShipDeliveryDays()),
        OrderingFormatters.formatDays(site.getAirDeliveryDays()),
        String.valueOf(itemCount)
    );
}
```

Keep public `toRow(Site site)` for existing callers:

```java
public SiteRow toRow(Site site) {
    int itemCount = siteService.countMerchandiseAtSite(site.getId());
    return new SiteRow(
        site,
        site.getId(),
        site.getSiteCode(),
        site.getName(),
        OrderingFormatters.blankToFallback(site.getDescription(), "-"),
        OrderingFormatters.formatDays(site.getShipDeliveryDays()),
        OrderingFormatters.formatDays(site.getAirDeliveryDays()),
        String.valueOf(itemCount)
    );
}
```

- [ ] **Step 5: Update existing site management test fakes**

In `SiteManagementApplicationServiceTest.java`, update the fake inventory repository used by `SiteUseCase`:

```java
Map<Integer, Integer> merchandiseCountsBySiteId = new HashMap<>();
int countMerchandiseAtSiteCalls;
int groupedCountCalls;
```

Implement:

```java
@Override
public int countMerchandiseAtSite(int siteId) {
    countMerchandiseAtSiteCalls++;
    return merchandiseCountsBySiteId.getOrDefault(siteId, 0);
}

@Override
public Map<Integer, Integer> countMerchandiseGroupedBySiteId() {
    groupedCountCalls++;
    return new HashMap<>(merchandiseCountsBySiteId);
}
```

Add a test against `load()`:

```java
@Test
@DisplayName("load uses grouped merchandise counts instead of per-site count queries")
void load_shouldUseGroupedMerchandiseCounts_whenBuildingRows() {
    FakeSiteCommandRepository siteRepository = new FakeSiteCommandRepository();
    siteRepository.sites.add(site(1, "TOKYO", "Tokyo"));
    siteRepository.sites.add(site(2, "OSAKA", "Osaka"));
    siteRepository.merchandiseCountsBySiteId.put(1, 3);
    siteRepository.merchandiseCountsBySiteId.put(2, 1);
    SiteManagementApplicationService service = newService(siteRepository);

    SiteManagementApplicationService.Snapshot snapshot = service.load();

    assertEquals(1, siteRepository.groupedCountCalls);
    assertEquals(0, siteRepository.countMerchandiseAtSiteCalls);
    assertEquals("3", snapshot.rows().get(0).itemCount());
    assertEquals("1", snapshot.rows().get(1).itemCount());
}
```

Use the existing helper method names in the test file. If the helper is named differently, adapt only names, not behavior.

- [ ] **Step 6: Run site management tests**

Run:

```powershell
.\mvnw.cmd -q -Dtest=SiteManagementApplicationServiceTest test
```

Expected: test passes and proves grouped count is used by `load()`.

## Task 5: Defer Background Loading Unless Synchronous Work Still Freezes UI

**Files:**
- No file changes in this task unless manual smoke still shows visible freezes after Tasks 1-4.

- [ ] **Step 1: Manual smoke before adding Task-based loading**

Run the app and test list navigation:

1. Login as an Ordering user.
2. Click `received-requests`.
3. Click `orders`.
4. Click `site-management`.
5. Login as Sales.
6. Click `sales-requests`.
7. Click `merchandise-management`.
8. Login as Warehouse.
9. Click `warehouse-inbound-orders`.

Expected: navigation feels responsive after duplicate reloads and aggregate queries.

- [ ] **Step 2: If lag remains, inspect DB connection strategy before adding background tasks**

Current DB classes keep singleton `Connection` instances:

- `src/main/java/org/itss/prj_itss/model/shared/database/DatabaseConnection.java`
- `src/main/java/org/itss/prj_itss/model/shared/database/WarehouseDatabaseConnection.java`

Do not blindly run several list loads concurrently on the same singleton JDBC connection. If background loading becomes necessary, either serialize list-load tasks or first update connection management in existing connection-provider files. That is a separate architectural step and should be reviewed before implementation.

## Verification

- [ ] **Compile**

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: build compiles.

- [ ] **Targeted tests**

```powershell
.\mvnw.cmd -q -Dtest=WarehouseIncomingOrderQueryTest test
.\mvnw.cmd -q -Dtest=SiteManagementApplicationServiceTest test
.\mvnw.cmd -q test -Dtest=MvcDependencyTest
```

Expected: all targeted tests pass.

- [ ] **Architecture grep**

```powershell
rg -n "reload\(\);" src\main\java\org\itss\prj_itss\view\sales\request\list\SalesRequestListView.java src\main\java\org\itss\prj_itss\view\ordering\request\ReceivedRequestsView.java src\main\java\org\itss\prj_itss\view\warehouse\WarehouseIncomingOrdersView.java src\main\java\org\itss\prj_itss\view\ordering\order\management\OrderManagementView.java src\main\java\org\itss\prj_itss\view\sales\merchandise\SalesMerchandiseManagementView.java src\main\java\org\itss\prj_itss\view\ordering\site\SiteManagementView.java src\main\java\org\itss\prj_itss\view\admin\account\AccountManagementView.java
```

Expected: `reload()` remains in `onViewShown()` and mutation success callbacks, but not in list-screen `init(...)` methods.

- [ ] **Manual smoke**

Run the app and navigate through the list screens. Expected user-visible outcome: clicking list buttons no longer feels like the whole app freezes during normal list navigation.

## Rollback Notes

If a regression appears:

1. Revert Task 2 aggregate methods first if compile breaks because an interface implementation was missed.
2. Revert Task 1 only if a screen stops loading data on first navigation.
3. Keep grouped SQL changes if tests pass; they reduce query count without changing UI behavior.

