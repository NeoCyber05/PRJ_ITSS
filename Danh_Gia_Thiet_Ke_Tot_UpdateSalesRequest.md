# Đánh giá thiết kế tốt cho chức năng Update Sales Request

## 1. Bối cảnh cải thiện thiết kế

Chức năng được đánh giá là `sales/request/update`, dùng để mở form chỉnh sửa yêu cầu đặt hàng, cho phép người dùng cập nhật danh sách mặt hàng, số lượng, ngày nhận mong muốn và lưu thay đổi.

Sau khi được học lại các nguyên lý thiết kế như MVC, SOLID, High Cohesion, Low Coupling và Clean Architecture, em đã điều chỉnh thiết kế theo hướng tách trách nhiệm rõ hơn:

- View chỉ phụ trách hiển thị giao diện và phát sinh sự kiện người dùng.
- Controller chỉ điều phối luồng giữa View và Session.
- Session giữ trạng thái tạm thời của một phiên chỉnh sửa.
- Use Case xử lý nghiệp vụ cấp ứng dụng.
- Validator, State, Mapper và SelectionPolicy xử lý logic nghiệp vụ lõi.
- Gateway đóng vai trò cổng giao tiếp với service bên ngoài.

Thiết kế hiện tại không phải Clean Architecture thuần túy, mà là MVC ở tầng trình bày kết hợp với Application/Domain style ở tầng xử lý nghiệp vụ. Cách tổ chức này phù hợp với phạm vi bài tập lớn vì vừa đủ rõ ràng để trình bày, vừa không làm hệ thống trở nên quá phức tạp.

## 2. Low Coupling - Độ phụ thuộc thấp

Thiết kế đã giảm phụ thuộc trực tiếp giữa các tầng bằng cách dùng các hợp đồng trung gian.

Ví dụ:

- `SalesRequestEditView` không gọi trực tiếp logic nghiệp vụ, mà phát sự kiện qua `SalesRequestEditActionHandler`.
- `SalesRequestEditController` không phụ thuộc trực tiếp vào class View cụ thể, mà làm việc qua `SalesRequestEditViewPort`.
- `SalesRequestEditUseCase` không gọi trực tiếp `SalesRequestQueryService` hoặc `SalesRequestCommandService`, mà phụ thuộc vào interface `SalesRequestEditGateway`.

Ý nghĩa thiết kế:

- View có thể thay đổi layout, FXML hoặc component UI mà không làm thay đổi logic update.
- Use Case có thể được kiểm thử bằng gateway giả mà không cần mở giao diện hoặc kết nối database thật.
- Các tầng giao tiếp chủ yếu qua DTO/record như `SalesRequestEditDialogInput`, `SalesRequestEditViewState`, `SalesRequestEditDraft`, `SalesRequestEditData`.

Đây là điểm cải thiện quan trọng so với thiết kế ban đầu, vì nếu View giữ trực tiếp Controller hoặc Controller tự xử lý quá nhiều nghiệp vụ, việc sửa giao diện sẽ dễ kéo theo sửa logic.

## 3. High Cohesion - Độ gắn kết cao

Các class chính trong module đều có mục đích tương đối rõ:

| Class | Trách nhiệm chính | Mức gắn kết |
| --- | --- | --- |
| `SalesRequestEditView` | Hiển thị form update và render dữ liệu | Cao |
| `SalesRequestEditController` | Nhận action từ View và điều phối sang Session | Cao |
| `SalesRequestEditSession` | Quản lý trạng thái của một phiên chỉnh sửa | Cao |
| `SalesRequestEditUseCase` | Điều phối nghiệp vụ load, validate, submit update | Cao |
| `SalesRequestEditState` | Quản lý danh sách item nháp đang chỉnh sửa | Cao |
| `SalesRequestEditValidator` | Kiểm tra dữ liệu hợp lệ trước khi lưu | Cao |
| `SalesRequestEditSelectionPolicy` | Xử lý quy tắc chọn mặt hàng không trùng | Cao |
| `SalesRequestEditMapper` | Chuyển đổi dữ liệu giữa form, state và input submit | Cao |

Ý nghĩa thiết kế:

- Mỗi class có một lý do thay đổi tương đối riêng biệt.
- Khi đổi quy tắc validate, chủ yếu sửa `SalesRequestEditValidator`.
- Khi đổi cách map dữ liệu từ form cũ sang draft, chủ yếu sửa `SalesRequestEditMapper`.
- Khi đổi cách render bảng, chủ yếu sửa View/Table component, không ảnh hưởng Use Case.

Điểm này thể hiện nguyên lý SRP: mỗi thành phần tập trung vào một vai trò cụ thể thay vì gom tất cả vào một class lớn.

## 4. Dễ mở rộng

Thiết kế hỗ trợ mở rộng bằng cách tách các điểm dễ thay đổi ra thành class riêng.

Ví dụ:

- Quy tắc chọn mặt hàng không trùng nằm trong `SalesRequestEditSelectionPolicy`.
- Quy tắc kiểm tra dữ liệu nằm trong `SalesRequestEditValidator`.
- Luồng lưu dữ liệu đi qua `SalesRequestEditGateway`.

Nếu sau này yêu cầu thay đổi, ví dụ:

- Cho phép trùng mặt hàng trong một số trường hợp đặc biệt.
- Thêm validate ngày nhận theo chính sách mới.
- Thay service lưu dữ liệu bằng service khác.

thì hệ thống có điểm mở rộng rõ ràng, không cần sửa lan sang View hoặc Controller.

Ý nghĩa đem lại:

- Giảm rủi ro sửa một nghiệp vụ nhưng làm hỏng giao diện.
- Dễ trình bày trước hội đồng vì nhìn biểu đồ có thể thấy rule nghiệp vụ được đặt trong Domain/Application, không đặt trong UI.

## 5. Dễ bảo trì và dễ thay đổi

Thiết kế đã tách luồng update thành các bước rõ ràng:

1. `SalesRequestEditDialog` mở form.
2. `SalesRequestEditController` khởi động luồng.
3. `SalesRequestEditSession` load dữ liệu và giữ trạng thái.
4. `SalesRequestEditUseCase` gọi gateway, mapper, validator.
5. `SalesRequestEditView` nhận `SalesRequestEditViewState` để render.

Cách chia này giúp bảo trì tốt hơn vì mỗi thay đổi có phạm vi rõ:

- Thay đổi giao diện: sửa View.
- Thay đổi xử lý action: sửa Controller/Session.
- Thay đổi dữ liệu nháp: sửa State/Draft.
- Thay đổi luật hợp lệ: sửa Validator/SelectionPolicy.
- Thay đổi cách gọi service: sửa Gateway adapter.

Ý nghĩa đem lại:

- Tránh hiện tượng Shotgun Surgery, tức là sửa một yêu cầu nhỏ nhưng phải sửa nhiều file không liên quan.
- Khi đọc code, lập trình viên mới có thể lần theo luồng từ View -> Controller -> Session -> UseCase -> Gateway.

## 6. Dễ kiểm thử

Các logic quan trọng được đưa ra khỏi JavaFX nên có thể kiểm thử độc lập.

Các class dễ viết unit test:

- `SalesRequestEditValidator`: kiểm thử các trường hợp item rỗng, thiếu mặt hàng, số lượng không hợp lệ, ngày nhận không hợp lệ.
- `SalesRequestEditSelectionPolicy`: kiểm thử danh sách mặt hàng khả dụng và phát hiện trùng mặt hàng.
- `SalesRequestEditState`: kiểm thử thêm dòng, đổi mặt hàng, đổi số lượng, đổi ngày nhận.
- `SalesRequestEditUseCase`: có thể dùng mock/stub `SalesRequestEditGateway` để kiểm thử load/update mà không cần database thật.

Ý nghĩa đem lại:

- Logic nghiệp vụ không bị phụ thuộc vào JavaFX `TableView`, `Button`, `Label`.
- Có thể kiểm thử nhanh ở terminal bằng JUnit.
- Khi lỗi xảy ra, dễ xác định lỗi nằm ở validate, state, mapper hay gateway.

Đây là bằng chứng cho DIP: Use Case phụ thuộc vào `SalesRequestEditGateway` thay vì phụ thuộc trực tiếp vào service cụ thể.

## 7. Dễ hiểu và tính biểu cảm cao

Tên class trong module phản ánh rõ vai trò:

- `SalesRequestEditController`: controller cho màn hình edit.
- `SalesRequestEditSession`: phiên chỉnh sửa.
- `SalesRequestEditUseCase`: ca sử dụng update.
- `SalesRequestEditValidator`: kiểm tra hợp lệ.
- `SalesRequestEditSelectionPolicy`: chính sách chọn mặt hàng.
- `ValidatedSalesRequestEditDraft`: draft đã được xác thực trước khi submit.

Ý nghĩa thiết kế:

- Người đọc nhìn tên class có thể đoán được chức năng.
- Biểu đồ phụ thuộc package có thể trình bày theo 4 cụm lớn: View, Controller, Session/Application, Domain/Infrastructure.
- Hạn chế phải đọc sâu từng method mới hiểu hệ thống.

Đây là tiêu chí quan trọng khi bảo vệ bài tập lớn, vì thời gian trình bày ngắn nên thiết kế phải có khả năng “tự giải thích” qua tên package và tên class.

## 8. Tính tái sử dụng

Một số thành phần có khả năng tái sử dụng hoặc dùng lại trong kiểm thử:

- `SalesRequestEditValidator` có thể dùng lại trong cả validate realtime trên form và validate trước khi submit.
- `SalesRequestEditSelectionPolicy` có thể dùng cho cả UI lọc combobox và nghiệp vụ phòng vệ không cho trùng mặt hàng.
- `SalesRequestEditMapper` gom logic chuyển đổi dữ liệu, tránh lặp mapping ở nhiều nơi.
- `SalesRequestEditGateway` giúp Use Case không phụ thuộc service cụ thể.

Ý nghĩa đem lại:

- Quy tắc nghiệp vụ có một nguồn định nghĩa chính, tránh mỗi nơi viết một kiểu.
- Khi cần mở rộng hoặc test, có thể dùng lại các class lõi mà không kéo theo toàn bộ UI.

## 9. Information Hiding và Encapsulation

Thiết kế che giấu trạng thái nội bộ tương đối tốt:

- `SalesRequestEditState` giữ danh sách item nháp bên trong và chỉ cho thay đổi qua các method như `addBlankItem`, `changeMerchandise`, `changeQuantity`, `changeDesiredDate`.
- `snapshot()` tạo ra `SalesRequestEditDraft` để các tầng khác đọc trạng thái hiện tại thay vì thao tác trực tiếp vào list nội bộ.
- `SalesRequestEditViewState` copy dữ liệu đầu vào bằng `List.copyOf` và `Collectors.toUnmodifiableMap`, hạn chế việc bên ngoài mutate dữ liệu render.
- `ValidatedSalesRequestEditDraft` chỉ được tạo sau khi validator xác nhận hợp lệ.

Ý nghĩa đem lại:

- Tránh việc View hoặc Controller tự ý sửa sâu vào cấu trúc item.
- Giảm lỗi do dữ liệu nháp bị thay đổi ngoài ý muốn.
- Làm rõ hợp đồng: dữ liệu trước khi submit phải đi qua validate.

## 10. Protected Variations - Bảo vệ trước sự thay đổi

Các điểm dễ thay đổi trong hệ thống đã được bao quanh bằng interface hoặc class chính sách:

| Điểm dễ thay đổi | Cách bảo vệ |
| --- | --- |
| Cách gọi service load/update | `SalesRequestEditGateway` |
| Quy tắc chọn mặt hàng | `SalesRequestEditSelectionPolicy` |
| Quy tắc validate | `SalesRequestEditValidator` |
| Dữ liệu render lên UI | `SalesRequestEditViewState` |
| Cách View phát action | `SalesRequestEditActionHandler` |
| Cách Controller render View | `SalesRequestEditViewPort` |

Ý nghĩa đem lại:

- Nếu service ngoài thay đổi, Use Case ít bị ảnh hưởng.
- Nếu UI thay đổi, Domain/Application không cần biết.
- Nếu rule nghiệp vụ thay đổi, không cần sửa FXML hoặc JavaFX component.

Đây là cách áp dụng tư duy GRASP Protected Variations: phát hiện điểm biến động và đặt biên giới thiết kế quanh điểm đó.

## 11. Phân tích cách chỉnh sửa và ý nghĩa đem lại

Sau khi học về SOLID và các tiêu chí thiết kế tốt, em đã nhìn lại module update và nhận ra nếu để toàn bộ logic trong một View hoặc một Controller lớn thì sẽ có các vấn đề:

- View vừa hiển thị vừa xử lý nghiệp vụ, gây khó test.
- Controller dễ bị “fat controller”, vừa giữ state vừa validate vừa gọi service.
- Logic chọn mặt hàng và validate có thể bị lặp ở nhiều nơi.
- Khi sửa giao diện hoặc sửa rule nghiệp vụ dễ gây conflict với phần create/list/view của thành viên khác.

Vì vậy, hướng chỉnh sửa là tách module theo trách nhiệm:

### 11.1. Tách View khỏi xử lý nghiệp vụ

View chỉ còn nhiệm vụ render `SalesRequestEditViewState` và chuyển action người dùng sang `SalesRequestEditActionHandler`.

Ý nghĩa:

- View trở thành lớp trình bày, không chứa nghiệp vụ update.
- UI có thể thay đổi mà không làm thay đổi Use Case.

### 11.2. Làm mỏng Controller

Controller nhận action từ View, gọi Session, sau đó render lại View. Controller không tự validate chi tiết và không tự gọi database.

Ý nghĩa:

- Controller dễ đọc.
- Luồng xử lý rõ: action -> session -> state/usecase -> render.

### 11.3. Tách Session để quản lý phiên chỉnh sửa

`SalesRequestEditSession` giữ dữ liệu tạm thời của lần mở form hiện tại, gồm state, listener và danh sách mặt hàng khả dụng.

Ý nghĩa:

- Mỗi lần mở form có một phiên xử lý rõ ràng.
- Tránh trộn trạng thái giữa các lần mở form.
- Dễ mô tả bằng sequence diagram.

### 11.4. Tách Use Case khỏi Gateway

`SalesRequestEditUseCase` chỉ phụ thuộc vào `SalesRequestEditGateway`, còn `SalesRequestEditServiceGateway` là adapter gọi service thật.

Ý nghĩa:

- Tuân thủ DIP.
- Dễ viết mock gateway để test Use Case.
- Hạn chế phụ thuộc trực tiếp vào hạ tầng.

### 11.5. Tách Validator và SelectionPolicy

Luật hợp lệ của dữ liệu đặt trong `SalesRequestEditValidator`. Luật không chọn trùng mặt hàng đặt trong `SalesRequestEditSelectionPolicy`.

Ý nghĩa:

- Tránh lặp logic validate ở UI.
- Quy tắc nghiệp vụ được gom về một nơi.
- Dễ mở rộng khi có rule mới.

### 11.6. Dùng Draft và ValidatedDraft

`SalesRequestEditDraft` biểu diễn dữ liệu đang chỉnh sửa. `ValidatedSalesRequestEditDraft` biểu diễn dữ liệu đã qua kiểm tra và đủ điều kiện submit.

Ý nghĩa:

- Làm rõ trạng thái dữ liệu.
- Tránh submit dữ liệu chưa validate.
- Giảm phụ thuộc ngầm kiểu “phải gọi validate trước rồi mới map”.

## 12. Kết luận

Module `sales/request/update` hiện đạt được nhiều tiêu chí của một thiết kế tốt:

- Có phân tầng rõ theo MVC kết hợp Application/Domain style.
- Có Low Coupling nhờ interface và DTO trung gian.
- Có High Cohesion vì các class chính tập trung vào một vai trò.
- Dễ kiểm thử vì nghiệp vụ lõi không phụ thuộc JavaFX.
- Dễ bảo trì vì thay đổi được khoanh vùng.
- Dễ hiểu vì tên class thể hiện trực tiếp trách nhiệm.
- Có bảo vệ trước thay đổi thông qua Gateway, Validator, SelectionPolicy và ViewPort.

Điểm cần giải thích khi trình bày là: trong thiết kế này, tầng `Model` không chỉ là entity dữ liệu như MVC cơ bản, mà bao gồm cả Application/Domain/Infrastructure phục vụ cho nghiệp vụ update. Đây là cách kết hợp MVC với tư duy Clean Architecture ở mức vừa đủ cho bài tập lớn.

