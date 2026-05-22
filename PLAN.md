# Refactor PRJ_ITSS Sang MVC Thuần Top-Level

## Summary
- Mục tiêu là đổi toàn repo sang MVC thuần: `FXML` + JavaFX controller là **View**, plain Java `Controller` điều phối input/flow, `Model` chứa toàn bộ nghiệp vụ, use case, entity, repository contract và JDBC adapter.
- Giữ nguyên hành vi nghiệp vụ trong `BussinessLogic.md`: sales tạo/sửa/xem request, ordering xử lý phân bổ/order/site, warehouse xác nhận hàng đến, auth/role navigation.
- Đây là refactor lớn một lần, nên cần đổi đồng bộ package, import, FXML `fx:controller`, `module-info.java`, tests kiến trúc và navigation wiring.

## Folder Architecture
```text
src/main/java/org/itss/prj_itss/
  App.java
  bootstrap/
    AppFactory.java
    ControllerRegistry.java
    ModelContext.java

  view/
    shared/
      ViewLifecycle.java
      ViewLoader.java
      StatusBadgeFactory.java
      TableViewSupport.java
    layout/
      MainLayoutView.java
    auth/
      LoginView.java
      RoleWorkspaceView.java
    home/
      HomeView.java
    ordering/
      site/SiteManagementView.java
      order/OrderManagementView.java
      order/OrderDetailView.java
      order/OrderCancellationView.java
      request/ReceivedRequestsView.java
      request/RequestDetailPopupView.java
      request/process/...View.java
    sales/request/
      SalesRequestListView.java
      CreateOrderRequestView.java
      UpdateOrderRequestView.java
      ViewOrderRequestView.java
    warehouse/
      ConfirmOrderArrivalView.java

  controller/
    navigation/
      Navigator.java
      MainNavigationController.java
    auth/
    home/
    ordering/site/
    ordering/order/
    ordering/request/
    sales/request/
    warehouse/

  model/
    shared/
      OrderingFormatters.java
      transaction/
      database/
    auth/
      entity/
      usecase/
      repository/
      persistence/
    catalog/
    dashboard/
    order/
    request/
      entity/
      usecase/
      allocation/
      command/
      query/
      repository/
      persistence/
    site/
    warehouse/
```

`src/main/resources/org/itss/prj_itss/view/...` sẽ mirror cây `view/...`; toàn bộ FXML `fx:controller` phải trỏ tới các lớp `*View`.

## Key Changes
- Đổi tên/tách vai trò:
  - Các JavaFX classes hiện là `*Controller` sẽ thành `*View` nếu có `@FXML`, `FXMLLoader`, `TableView`, `Alert`, `Stage`, CSS, cell factory, hoặc render UI.
  - Các plain controllers mới trong `controller/...` không import JavaFX; chúng nhận command/input từ View, gọi Model, trả về immutable state/result.
  - `ApplicationContext` hiện tại được thay bằng `bootstrap.ModelContext` cho wiring model và `ControllerRegistry` cho wiring controller.
- Dependency rule bắt buộc:
  - `view -> controller`, `view -> view.shared`, JavaFX only; không gọi repository/JDBC/use case trực tiếp.
  - `controller -> model`; không import JavaFX, không biết FXML, không gọi JDBC adapter trực tiếp.
  - `model` không import `view`, `controller`, `javafx`; `model.*.persistence` là nơi duy nhất dùng JDBC/SQL.
  - `App`/`bootstrap` là composition root duy nhất được phép biết cả View, Controller, Model.
- Public interfaces/types:
  - `controller.navigation.Navigator` thay `layout.INavigator`.
  - `view.shared.ViewLifecycle` thay `layout.IViewController`; View nhận controller cụ thể hoặc `ControllerRegistry`, không nhận ModelContext trực tiếp.
  - Controller methods chuẩn hóa theo dạng `loadState()`, `handle(command)`, `select(id)`, `confirm(...)`, trả về `ScreenState`, `ActionResult`, hoặc `ValidationResult`.
  - Repository contracts nằm trong `model.<feature>.repository`; JDBC implementations nằm trong `model.<feature>.persistence`.
- Ưu tiên refactor fat controllers trước trong cùng đợt:
  - `UpdateOrderRequestController` tách thành `UpdateOrderRequestView` + `sales.request.UpdateOrderRequestController` + form state/command trong model/controller.
  - `ConfirmOrderArrivalController` tách tương tự để đưa load/validate/confirm ra khỏi JavaFX View.
  - Chuẩn hóa request-processing hiện đã gần MVC: giữ mẫu `RequestProcessingLayoutView` + `RequestProcessingController`, nhưng di chuyển package sang top-level MVC mới.

## Test Plan
- Baseline hiện tại:
  - `.\mvnw.cmd -q -DskipTests compile` đang pass.
  - `.\mvnw.cmd -q test` hiện fail ở JUnit discovery do `NoClassDefFoundError: RequestProcessingException`; cần sửa hoặc ghi nhận riêng trước khi đánh giá refactor.
- Cập nhật `CleanArchitectureDependencyTest` thành `MvcDependencyTest`:
  - `model` không import `controller`, `view`, `javafx`.
  - `controller` không import `javafx`, `view`, `persistence`, `java.sql`.
  - `view` không import `model.*.persistence` hoặc repository adapter.
  - Mọi FXML `fx:controller` đều nằm dưới `org.itss.prj_itss.view`.
  - Không còn `ApplicationContext.getInstance()` ngoài `bootstrap`.
- Chạy validation sau refactor:
  - `.\mvnw.cmd -q -DskipTests compile`
  - `.\mvnw.cmd -q test`
  - Smoke thủ công bằng `.\mvnw.cmd clean javafx:run`: login, role navigation, sales request create/update/view, ordering received request/process/allocation/confirm, site management, order list/detail/cancel, warehouse confirm arrival.

## Assumptions
- Chọn đúng theo phản hồi của bạn: MVC thuần top-level, refactor toàn repo một lần.
- Không đổi schema/database, không đổi nghiệp vụ trong `BussinessLogic.md`, không thêm framework DI mới.
- Các package cũ `auth`, `request`, `order`, `site`, `warehouse`, `common`, `layout`, `home`, `db` sẽ được di chuyển/đổi import đồng bộ, không để song song lâu dài.
