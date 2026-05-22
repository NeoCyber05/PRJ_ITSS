# Review PLAN.md — Refactor PRJ_ITSS Sang MVC Thuần (v2)

> Review dựa trên 3 skills: **MVC Pattern**, **SOLID Principles**, **Java Coding Standards**

---

## I. Tổng Quan Đánh Giá

| Tiêu chí | Điểm | Nhận xét |
|----------|-------|----------|
| Tuân thủ MVC pattern | ⭐⭐⭐⭐ | Tốt — phân tách rõ 3 layer, dependency rule chặt chẽ |
| SOLID principles | ⭐⭐⭐ | Khá — SRP & DIP tốt, nhưng thiếu ISP và OCP ở một số chỗ |
| Java coding standards | ⭐⭐⭐ | Khá — cần bổ sung convention chi tiết hơn |
| Tính khả thi | ⭐⭐⭐⭐ | Refactor 1 lần lớn là hợp lý cho project size này |
| Test plan | ⭐⭐⭐ | Cần bổ sung — hiện chỉ ~8 test files |

**Kết luận**: Plan tổng thể **tốt và đúng hướng**. Có một số điểm cần cải thiện trước khi thực thi.

---

## II. Phân Tích Theo MVC Skill

### ✅ Điểm Plan làm đúng

**1. Phân tách vai trò rõ ràng**

Plan xác định chính xác:
- `*View.java` (FXML controller) = **View** — chỉ chứa presentation logic, `@FXML` bindings, render UI
- Plain `Controller` trong `controller/` = **Controller** — điều phối, không import JavaFX
- `model/` = **Model** — entity, usecase, repository contract, persistence

Đây đúng chuẩn MVC theo skill: *"Models should be completely independent of UI concerns"*, *"Controllers coordinate but don't implement domain logic"*.

**2. Naming convention `*View.java` cho FXML controllers — ĐÚNG**

Đặt tên `*View.java` cho các class gắn với FXML là hợp lý trong kiến trúc này vì:
- FXML file + `*View.java` cùng thuộc tầng **View** trong MVC
- `fx:controller` trỏ tới `*View` class — nhất quán về mặt ngữ nghĩa: class này **là** View
- Phân biệt rõ ràng với plain `Controller` trong `controller/` package — tránh nhầm lẫn 2 loại "controller"

**3. Dependency rule bắt buộc**

```
view → controller, view.shared   (JavaFX only)
controller → model               (no JavaFX, no JDBC)
model → nothing above            (no view, no controller, no javafx)
```

Đây chính xác là **Step 5: Maintain Separation of Concerns** trong MVC skill. Plan cũng liệt kê các "red flags" tương tự skill: View không gọi repository/JDBC trực tiếp, Controller không import JavaFX.

**4. Xử lý Fat Controllers**

Plan nhắm đúng 2 "worst offenders":
- `UpdateOrderRequestController` (tách View + Controller + form state)
- `ConfirmOrderArrivalController` (673 dòng → tách load/validate/confirm ra khỏi JavaFX)

Theo MVC skill: *"Fat Controllers containing business logic that belongs in Models — Fix: Extract to Model methods."*

### ⚠️ Điểm cần cải thiện

**5. Thiếu data flow diagram**

> [!IMPORTANT]
> MVC skill nhấn mạnh **Step 4: Establish Data Flow** — "Wire together the MVC components with clear data flow: User → Controller → Model → Controller → View."

Plan mô tả dependency rules nhưng **không mô tả cụ thể data flow**. Ví dụ: khi user click "Create Order Request", dòng chảy dữ liệu là gì?

**Đề xuất**: Thêm 1-2 sequence diagram cho flows chính (ví dụ: tạo request, xử lý allocation).

---

## III. Về Việc Bỏ `bootstrap/` Folder — Phân Tích

Plan đề xuất folder `bootstrap/` làm **composition root**:

```
bootstrap/
    AppFactory.java          # Khởi tạo app, wire mọi thứ
    ControllerRegistry.java  # Quản lý/tra cứu controllers
    ModelContext.java         # Wire model dependencies (thay ApplicationContext)
```

### Có thể bỏ `bootstrap/` không?

**Trả lời: CÓ THỂ, nhưng cần thay thế — không thể xóa hoàn toàn.**

Vai trò của `bootstrap/` là **composition root** — nơi duy nhất được phép biết cả View, Controller, và Model để wire chúng lại. Đây là yêu cầu bắt buộc khi không dùng DI framework (Spring, Guice, etc.).

#### Phương án 1: Gộp vào `App.java` (nếu bỏ `bootstrap/`)

Nếu bỏ folder `bootstrap/`, toàn bộ logic wiring phải nằm trong `App.java`:

```java
public class App extends Application {
    @Override
    public void start(Stage stage) {
        // Wire models
        var accountRepo = new JdbcAccountRepository(connection);
        var authService = new AuthenticationService(accountRepo);

        // Wire controllers
        var loginController = new LoginController(authService);

        // Wire views
        var loginView = ViewLoader.load("login", loginController);
        // ...
    }
}
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Đơn giản, ít files hơn | `App.java` sẽ phình to (hiện `ApplicationContext` đã 175 dòng) |
| Dễ hiểu cho project nhỏ | Khó maintain khi thêm features |
| Không cần thêm abstraction | Vi phạm SRP — App vừa là entry point vừa là DI container |

#### Phương án 2: Giữ composition root nhưng đổi tên (đề xuất)

Nếu thấy tên `bootstrap/` không phù hợp, có thể đổi thành:

| Tên | Ý nghĩa |
|-----|---------|
| `config/` | Cấu hình & wiring — phổ biến nhất |
| `wiring/` | Rõ ràng: nơi "nối dây" dependencies |
| `infrastructure/` | Theo DDD convention |

#### Phương án 3: Tách nhỏ hơn — phân tán trách nhiệm

Thay vì 1 folder tập trung, mỗi feature tự wire:

```
auth/AuthModule.java         # wire auth dependencies
order/OrderModule.java       # wire order dependencies
request/RequestModule.java   # wire request dependencies
App.java                     # chỉ gọi các Module.init()
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Mỗi feature tự quản lý DI | Thêm 1 lớp abstraction |
| Dễ thêm/xóa feature | Cross-cutting concerns khó hơn |
| `App.java` gọn | Cần convention rõ ràng |

### 🏆 Khuyến nghị

> [!TIP]
> **Giữ nguyên concept composition root** nhưng tùy bạn chọn tên và cách tổ chức:
> - Nếu project < 20 screens: **Phương án 1** (gộp vào `App.java`) là đủ
> - Nếu project 20-50 screens: **Phương án 2** (giữ folder, đổi tên nếu muốn) hợp lý nhất — đây chính là cách Plan hiện tại đang làm
> - Nếu project > 50 screens: **Phương án 3** (modular) mới cần thiết

---

## IV. Phân Tích Theo SOLID Skill

### ✅ Single Responsibility Principle (SRP)

Plan tách đúng trách nhiệm:
- View: chỉ render UI
- Controller: chỉ điều phối
- Model: chỉ business logic + data

**Tốt**: Plan nhắm giải quyết các God Objects hiện tại (`ApplicationContext` 175 dòng, `ConfirmOrderArrivalController` 673 dòng, `OrderDetailPanel` 548 dòng).

### ✅ Dependency Inversion Principle (DIP)

- Repository contracts ở `model.<feature>.repository`
- JDBC implementations ở `model.<feature>.persistence`
- Controller depends on model abstractions, not concretions

Đúng chuẩn: *"Source code dependencies point toward high-level policies"*.

### ⚠️ Interface Segregation Principle (ISP)

> [!WARNING]
> Plan đề xuất `ViewLifecycle` thay `IViewController` và `Navigator` thay `INavigator` — nhưng **không mô tả** các interface này sẽ có bao nhiêu methods.

Hiện tại `IViewController` có:
```java
void init(INavigator navigator, ApplicationContext context);
default void onViewShown(String viewId) {}
```

**Vấn đề**: `init()` nhận cả `ApplicationContext` — vi phạm ISP vì View không cần toàn bộ context. Plan đã nói View sẽ nhận "controller cụ thể hoặc `ControllerRegistry`" — **đúng hướng nhưng cần cụ thể hóa hơn**.

**Đề xuất**: Định nghĩa rõ `ViewLifecycle` interface:
```java
public interface ViewLifecycle {
    void onViewShown();   // không nhận tham số — DI qua constructor
}
```

### ⚠️ Open/Closed Principle (OCP)

Plan chưa đề cập cách thêm feature mới mà không sửa code hiện có. Ví dụ:
- Thêm 1 role mới → phải sửa `ControllerRegistry`? `Navigator`?
- Thêm 1 screen mới → flow nào?

**Đề xuất**: Mô tả convention cho việc thêm feature mới (checklist: tạo View, tạo Controller, register vào Registry, thêm navigation route).

### ⚠️ Primitive Obsession (từ Code Smells)

SOLID skill nhấn mạnh: *"ALWAYS wrap primitives in domain objects"*. Hiện tại codebase dùng `String viewId` cho navigation — nên cân nhắc dùng enum hoặc sealed class:

```java
public enum ViewId {
    LOGIN, HOME, SALES_REQUEST_LIST, ORDER_MANAGEMENT, ...
}
```

---

## V. Phân Tích Theo Java Coding Standards Skill

### ✅ Project Structure

Project dùng JavaFX (không phải Spring/Quarkus) → áp dụng shared conventions only. Layout `view/controller/model` phù hợp cho JavaFX desktop app.

### ⚠️ Immutability

Java coding standards: *"Favor records and final fields"*. Plan đề cập Controller methods trả về `ScreenState`, `ActionResult`, `ValidationResult` — **tốt**, nhưng cần xác nhận:

- Các types này phải là `record` (immutable)
- Không dùng mutable DTO truyền giữa Controller ↔ View

**Đề xuất** thêm vào Plan:
```java
// Controller trả về immutable state
public record ScreenState<T>(T data, List<String> errors, boolean loading) {}
public record ActionResult(boolean success, String message) {}
```

### ⚠️ Exception Handling

Plan **không đề cập** chiến lược exception handling. Hiện tại có `RequestProcessingException` đã gây fail test.

**Đề xuất**: Thêm section về domain exceptions:
```java
model/shared/exception/
    DomainException.java              // base
    EntityNotFoundException.java
    ValidationException.java
    RequestProcessingException.java
```

---

## VI. Các Vấn Đề Khác Phát Hiện Từ Codebase

### 1. Duplicated Status Mapping — Vi phạm DRY

Ít nhất 4 controllers có `renderStatusVietnamese()` / `statusText()` / `toRequestStatusText()` khác nhau. Plan đề cập `OrderingFormatters` ở `model/shared/` — **tốt, nhưng cần nhấn mạnh**: tất cả status mapping phải consolidate về đây.

### 2. Empty Legacy Packages

5+ packages rỗng (`auth/login/`, `auth/role/`, `warehouse/order/confirm_arrival/`, ...). Plan nên ghi rõ: **xóa toàn bộ empty packages** trong refactor.

### 3. Parallel Architecture Remnants

`order/business/model/port/service/` tồn tại song song với `order/domain/application/` — dấu hiệu refactoring cũ chưa hoàn thành. Plan cần xác nhận: **xóa `business/` package** hay merge?

### 4. `db/` Package

`DatabaseConnection` + `WarehouseDatabaseConnection` nằm riêng ở `db/` thay vì trong `common/config/`. Plan nên consolidate vào `model/shared/database/`.

### 5. Test Coverage Quá Thấp

Chỉ ~8 test files cho toàn bộ codebase. Theo SOLID skill: *"Runs all the tests — Must work correctly"* là ưu tiên #1.

> [!CAUTION]
> Plan đề cập `.\mvnw.cmd -q test` đang **fail** do `NoClassDefFoundError: RequestProcessingException`. Cần sửa test hiện tại trước khi refactor, nếu không sẽ không có baseline để verify.

---

## VII. Tóm Tắt Đề Xuất Thay Đổi Cho PLAN.md

| # | Đề xuất | Mức độ |
|---|---------|--------|
| 1 | Thêm data flow diagram cho 1-2 flows chính | 🟡 Nên có |
| 2 | Định nghĩa cụ thể `ViewLifecycle`, `ScreenState`, `ActionResult` interfaces/records | 🟡 Nên có |
| 3 | Thêm section exception handling strategy | 🟡 Nên có |
| 4 | Dùng `enum ViewId` thay `String viewId` | 🟢 Nice-to-have |
| 5 | Xác nhận xử lý `order/business/` và `db/` packages | 🔴 Quan trọng |
| 6 | Fix test baseline trước khi refactor | 🔴 Quan trọng |
| 7 | Xóa empty legacy packages | ✅ Nên làm |
| 8 | Consolidate duplicated status mapping vào `OrderingFormatters` | ✅ Nên làm |
| 9 | Quyết định giữ/bỏ/đổi tên `bootstrap/` folder (xem phần III) | 🟡 Tùy chọn |
