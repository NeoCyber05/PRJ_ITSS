# Warehouse Incoming Orders + Sales Merchandise CRUD Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Warehouse incoming-order list/detail behavior and Sales merchandise CRUD while preserving the current JavaFX MVC architecture.

**Architecture:** Keep the current Model-Controller-View split: model owns business rules and persistence ports, controllers stay JavaFX-free, and views/FXML own JavaFX rendering only. Rename the existing catalog naming to Merchandise so code language matches the `merchandise` table and business vocabulary.

**Tech Stack:** Java 17, JavaFX/FXML, Maven, JDBC repositories, Supabase SQL migrations, JUnit architecture/service tests.

---

## Summary

Implement two actor capabilities from `BussinessLogic.md`:

- Warehouse Management Department can view incoming delivery orders and open order details for physical receiving comparison.
- Sales Department can CRUD the merchandise master data stored in the `merchandise` table.

Warehouse confirmation already exists as an action in the incoming order list. Keep that interaction model: each incoming-order row has `Chi tiết` and `Xác nhận`; both actions open popup/dialog content instead of routing to a separate full page.

Sales merchandise delete must be soft delete. Do not hard-delete `merchandise` rows because historical request/order/site-inventory rows reference merchandise data.

## Structure Sau Implement

```text
src/main/java/org/itss/prj_itss/
├─ bootstrap/
│  └─ MvcContext.java
├─ model/
│  ├─ merchandise/
│  │  ├─ MerchandiseModule.java
│  │  ├─ application/
│  │  │  ├─ MerchandiseUseCase.java
│  │  │  ├─ MerchandiseManagementService.java
│  │  │  ├─ MerchandiseDraft.java
│  │  │  ├─ MerchandiseManagementResult.java
│  │  │  └─ port/
│  │  │     └─ MerchandiseRepository.java
│  │  ├─ domain/
│  │  │  └─ Merchandise.java
│  │  └─ infrastructure/
│  │     └─ persistence/
│  │        └─ JdbcMerchandiseRepository.java
│  └─ warehouse/
│     ├─ WarehouseModule.java
│     └─ application/
│        ├─ WarehouseReceivingUseCase.java
│        ├─ WarehouseIncomingOrderQuery.java
│        ├─ IncomingOrderRow.java
│        └─ IncomingOrderDetail.java
├─ controller/
│  ├─ sales/
│  │  └─ merchandise/
│  │     ├─ SalesMerchandiseController.java
│  │     └─ SalesMerchandiseControllerModule.java
│  └─ warehouse/
│     ├─ WarehouseControllerModule.java
│     ├─ WarehouseIncomingOrderController.java
│     └─ ConfirmOrderArrivalController.java
└─ view/
   ├─ layout/
   │  ├─ MainLayoutView.java
   │  └─ main-layout.fxml
   ├─ sales/
   │  └─ merchandise/
   │     ├─ SalesMerchandiseManagementView.java
   │     └─ sales-merchandise-management-view.fxml
   └─ warehouse/
      ├─ WarehouseIncomingOrdersView.java
      ├─ warehouse-incoming-orders-view.fxml
      ├─ IncomingOrderDetailDialog.java
      └─ ConfirmOrderArrivalView.java
```

## Implementation Tasks

### Task 1: Merchandise Schema + Naming Alignment

**Files:**
- Create: `supabase/migrations/20260603093000_add_merchandise_active_status.sql`
- Move/rename package: `src/main/java/org/itss/prj_itss/model/catalog` to `src/main/java/org/itss/prj_itss/model/merchandise`
- Modify imports across model/controller/view/tests from `model.catalog` to `model.merchandise`
- Modify: `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`
- Modify: `src/main/java/module-info.java`

- [ ] Add SQL migration:

```sql
ALTER TABLE public.merchandise
ADD COLUMN IF NOT EXISTS is_active boolean NOT NULL DEFAULT true;
```

- [ ] Rename existing catalog classes:
  - `CatalogModule` -> `MerchandiseModule`
  - `CatalogUseCase` -> `MerchandiseUseCase`
  - package `org.itss.prj_itss.model.catalog` -> `org.itss.prj_itss.model.merchandise`

- [ ] Keep these names because they already match the DB:
  - `Merchandise`
  - `MerchandiseRepository`
  - `JdbcMerchandiseRepository`

- [ ] Update every dependency injection point to use `MerchandiseModule` / `MerchandiseUseCase`, especially:
  - `MvcContext`
  - `RequestModule`
  - `OrderModule`
  - `SiteModule`
  - `WarehouseModule`
  - Sales request controllers
  - Warehouse controllers

- [ ] Run compile after mechanical rename:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Expected: compile passes before adding CRUD behavior.

### Task 2: Merchandise Model CRUD API

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/model/merchandise/domain/Merchandise.java`
- Modify: `src/main/java/org/itss/prj_itss/model/merchandise/application/port/MerchandiseRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/merchandise/infrastructure/persistence/JdbcMerchandiseRepository.java`
- Modify: `src/main/java/org/itss/prj_itss/model/merchandise/application/MerchandiseUseCase.java`
- Create: `src/main/java/org/itss/prj_itss/model/merchandise/application/MerchandiseDraft.java`
- Create: `src/main/java/org/itss/prj_itss/model/merchandise/application/MerchandiseManagementResult.java`
- Create: `src/main/java/org/itss/prj_itss/model/merchandise/application/MerchandiseManagementService.java`
- Test: `src/test/java/org/itss/prj_itss/model/merchandise/application/MerchandiseManagementServiceTest.java`

- [ ] Add `active` to `Merchandise` with getter/setter and constructor support.

- [ ] Extend `MerchandiseRepository`:

```java
List<Merchandise> findAll();
List<Merchandise> findActive();
Merchandise findById(int id);
Merchandise findByCode(String code);
int countAll();
int create(Merchandise merchandise);
boolean update(Merchandise merchandise);
boolean setActive(int merchandiseId, boolean active);
```

- [ ] Update `JdbcMerchandiseRepository` queries:
  - Select `id, code, name, unit, is_active`.
  - `findActive()` filters `WHERE is_active = true ORDER BY code`.
  - `create()` inserts `code, name, unit, is_active`.
  - `update()` updates `code, name, unit`.
  - `setActive()` updates only `is_active`.

- [ ] Add `MerchandiseManagementService` rules:
  - Code, name, unit are required after trim.
  - Code is normalized with `trim().toUpperCase(Locale.ROOT)`.
  - Create fails when another merchandise already has the same code.
  - Update fails when the ID does not exist.
  - Update fails when the new code belongs to another ID.
  - Deactivate/restore call `setActive(id, false/true)`.

- [ ] Keep historical lookups alive:
  - `findById()` returns active and inactive rows.
  - `findByCode()` returns active and inactive rows.
  - New item-pickers must use `findActive()`.

### Task 3: Sales Merchandise Management UI

**Files:**
- Create: `src/main/java/org/itss/prj_itss/controller/sales/merchandise/SalesMerchandiseController.java`
- Create: `src/main/java/org/itss/prj_itss/controller/sales/merchandise/SalesMerchandiseControllerModule.java`
- Create: `src/main/java/org/itss/prj_itss/view/sales/merchandise/SalesMerchandiseManagementView.java`
- Create: `src/main/java/org/itss/prj_itss/view/sales/merchandise/sales-merchandise-management-view.fxml`
- Modify: `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`
- Modify: `src/main/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicy.java`
- Modify: `src/main/java/org/itss/prj_itss/view/layout/MainLayoutView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/layout/main-layout.fxml`
- Modify: `src/main/java/module-info.java`
- Test: `src/test/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicyTest.java`

- [ ] Add route `merchandise-management` in `MvcContext`.

- [ ] Add Sales role access:
  - `RoleAccessPolicy.canAccess(RoleType.SALES, "merchandise-management") == true`
  - Sales default remains `sales-requests`.

- [ ] Add Sales sidebar button:
  - Button label: `Mặt hàng`
  - `MainLayoutView.registerNavButton("merchandise-management", merchandiseManagementButton)`
  - Keep the existing Sales request button.

- [ ] Build table columns:
  - Mã hàng
  - Tên mặt hàng
  - Đơn vị
  - Trạng thái
  - Hành động

- [ ] Build controls:
  - Search field filters by code/name/unit.
  - `Thêm mặt hàng` opens create dialog.
  - `Sửa` opens edit dialog for selected row.
  - `Vô hiệu hóa` soft-deletes active merchandise.
  - `Khôi phục` restores inactive merchandise.

- [ ] Controller returns `ActionResult` or `MerchandiseManagementResult` messages to the view. Do not import JavaFX in controller.

### Task 4: Active Merchandise Selection in Existing Flows

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/model/request/application/sales/SalesRequestQueryService.java`
- Modify: `src/main/java/org/itss/prj_itss/model/site/application/self/OverseasSiteApplicationService.java`
- Update tests that fake `MerchandiseRepository`

- [ ] Update Sales request creation/update option loading to use `MerchandiseUseCase.findActive()`.

- [ ] Update Site workspace "add merchandise" options to use active merchandise only.

- [ ] Keep read-only historical detail screens using `findById()` so inactive merchandise still renders in existing request/order/site records.

- [ ] Update fake repositories in tests to implement new repository methods.

### Task 5: Warehouse Incoming Order Read Model

**Files:**
- Create: `src/main/java/org/itss/prj_itss/model/warehouse/application/WarehouseIncomingOrderQuery.java`
- Create: `src/main/java/org/itss/prj_itss/model/warehouse/application/IncomingOrderRow.java`
- Create: `src/main/java/org/itss/prj_itss/model/warehouse/application/IncomingOrderDetail.java`
- Modify: `src/main/java/org/itss/prj_itss/model/warehouse/WarehouseModule.java`
- Test: `src/test/java/org/itss/prj_itss/model/warehouse/application/WarehouseIncomingOrderQueryTest.java`

- [ ] `WarehouseIncomingOrderQuery.findIncomingRows()` reads orders with status `OrderStatus.SHIPPING.displayValue()`.

- [ ] Sort incoming orders by `createdAt` descending, then `id` descending, matching the current Warehouse receiving behavior.

- [ ] Row fields:

```java
int orderId;
int requestId;
int siteId;
String orderCode;
String requestCode;
String siteCode;
String siteName;
String createdAt;
String status;
String statusText;
int itemCount;
```

- [ ] Detail fields:

```java
IncomingOrderRow summary;
List<IncomingOrderItemRow> items;
```

- [ ] Item fields:

```java
int merchandiseId;
String merchandiseCode;
String merchandiseName;
String unit;
String orderedQuantity;
String deliveryMethod;
```

- [ ] Use `MerchandiseUseCase.findById()` for item names so inactive merchandise still displays correctly.

### Task 6: Warehouse List, Detail Popup, and Confirm Popup

**Files:**
- Create: `src/main/java/org/itss/prj_itss/controller/warehouse/WarehouseIncomingOrderController.java`
- Modify: `src/main/java/org/itss/prj_itss/controller/warehouse/WarehouseControllerModule.java`
- Create: `src/main/java/org/itss/prj_itss/view/warehouse/WarehouseIncomingOrdersView.java`
- Create: `src/main/java/org/itss/prj_itss/view/warehouse/warehouse-incoming-orders-view.fxml`
- Create: `src/main/java/org/itss/prj_itss/view/warehouse/IncomingOrderDetailDialog.java`
- Keep/adjust: `src/main/java/org/itss/prj_itss/view/warehouse/ConfirmOrderArrivalView.java`
- Modify: `src/main/java/org/itss/prj_itss/bootstrap/MvcContext.java`
- Modify: `src/main/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicy.java`
- Modify: `src/main/java/org/itss/prj_itss/view/layout/MainLayoutView.java`
- Modify: `src/main/java/org/itss/prj_itss/view/layout/main-layout.fxml`

- [ ] Add route `warehouse-inbound-orders`.

- [ ] Keep `warehouse-order-confirm-arrival` as a compatibility alias if existing code/tests still reference it. Both routes should load the incoming-orders list.

- [ ] Warehouse default route becomes `warehouse-inbound-orders`.

- [ ] Add Warehouse sidebar container/button:
  - Button label: `Đơn giao tới`
  - Visible only for `RoleType.WAREHOUSE`

- [ ] Warehouse list table columns:
  - Mã đơn hàng
  - Mã yêu cầu
  - Mã site
  - Tên site
  - Ngày tạo
  - Trạng thái
  - Hành động

- [ ] Row actions:
  - `Chi tiết`: open read-only `IncomingOrderDetailDialog`.
  - `Xác nhận`: open existing receiving/inspection dialog behavior.

- [ ] After successful confirmation:
  - Update order status to `completed`.
  - Refresh incoming list.
  - Show success message in the list view.

### Task 7: Role Workspace Copy + Verification

**Files:**
- Modify: `src/main/java/org/itss/prj_itss/view/auth/RoleWorkspaceContentFactory.java`
- Test: `src/test/java/org/itss/prj_itss/model/auth/application/RoleAccessPolicyTest.java`
- Test: `src/test/java/org/itss/prj_itss/architecture/MvcDependencyTest.java`

- [ ] Update Sales role copy so `Quản lý danh mục mặt hàng` is no longer described as unimplemented.

- [ ] Update Warehouse role copy so incoming-order lookup/detail and confirmation are described as implemented.

- [ ] Add/adjust role policy tests:

```java
assertTrue(RoleAccessPolicy.canAccess(RoleType.SALES, "merchandise-management"));
assertEquals("sales-requests", RoleAccessPolicy.defaultViewId(RoleType.SALES));
assertTrue(RoleAccessPolicy.canAccess(RoleType.WAREHOUSE, "warehouse-inbound-orders"));
assertTrue(RoleAccessPolicy.canAccess(RoleType.WAREHOUSE, "warehouse-order-confirm-arrival"));
assertEquals("warehouse-inbound-orders", RoleAccessPolicy.defaultViewId(RoleType.WAREHOUSE));
```

- [ ] Run full verification:

```powershell
.\mvnw.cmd -q test
```

Expected: tests pass, including `MvcDependencyTest`.

## Architecture Guardrails

- Model packages must not import `view`, `controller`, or `javafx`.
- Controller packages must not import `javafx`.
- View packages must not import JDBC or persistence classes.
- FXML files must stay under `src/main/java/org/itss/prj_itss/view/...`.
- Any new FXML controller package must match its FXML file path.
- New UI routes must be registered in `MvcContext` and guarded in `RoleAccessPolicy`.

## Acceptance Criteria

- Sales can open `Mặt hàng`, search merchandise, create, edit, deactivate, and restore rows.
- Inactive merchandise does not appear when creating/updating new Sales requests or adding Site merchandise.
- Existing details for old requests/orders still render inactive merchandise by ID.
- Warehouse can see all `shipping` orders in one list.
- Warehouse can open a read-only detail popup with item names, quantities, units, and delivery method.
- Warehouse can confirm arrival from the same list; after success the order is no longer in the incoming list.
- `.\mvnw.cmd -q test` passes.

## Assumptions

- `BussinessLogic.md` is the business source of truth.
- The code should not keep parallel `Catalog` and `Merchandise` naming. Rename toward `Merchandise`.
- Incoming Warehouse order means order status `shipping`.
- Confirmed arrival means order status `completed`.
- Soft delete is the required delete behavior for merchandise.
