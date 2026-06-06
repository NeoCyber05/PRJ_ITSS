# Kịch Bản Bảo Vệ Ngắn: Update Sales Request

## 1. Chiến Lược Trình Bày

Đây là bài tập lớn, thời gian bảo vệ ngắn. Không nên trình bày kiểu đi sâu từng method vì người nghe không có đủ thời gian đọc code. Cách tốt hơn là dùng **biểu đồ phụ thuộc package/class** để chứng minh tư duy thiết kế ở mức tổng thể.

Thông điệp chính cần truyền đạt:

> Chức năng Update Sales Request được thiết kế theo hướng MVC ở tầng giao diện và Clean Architecture style ở tầng nghiệp vụ. Mục tiêu là tách UI, trạng thái chỉnh sửa, logic nghiệp vụ và truy cập service thành các khối độc lập, giảm phụ thuộc và dễ bảo trì khi làm nhóm.

Nói ngắn gọn:

- View chỉ lo giao diện và phát event.
- Controller chỉ điều phối.
- Session giữ trạng thái chỉnh sửa tạm thời.
- UseCase xử lý luồng nghiệp vụ.
- Gateway tách nghiệp vụ khỏi service/database.
- Validator/Policy gom rule nghiệp vụ vào một chỗ.

---

## 2. Kịch Bản Nói 5 Phút

### Phút 1: Vấn Đề Thiết Kế Cần Giải Quyết

"Chức năng update request không đơn giản là bấm sửa rồi ghi database ngay. Khi người dùng mở form update, họ có thể thay đổi nhiều dòng hàng, đổi số lượng, đổi ngày nhận, thêm hoặc xóa dòng, sau đó mới bấm cập nhật hoặc hủy. Vì vậy em cần một thiết kế tách rõ **trạng thái đang chỉnh sửa tạm thời** với dữ liệu thật trong hệ thống."

"Nếu để View hoặc Controller giữ toàn bộ logic, code sẽ rất dễ phình to, khó test và dễ conflict khi làm nhóm. Do đó em chia module update thành các lớp có trách nhiệm rõ."

### Phút 2: Nhìn Tổng Quan Từ Biểu Đồ Phụ Thuộc

"Nhìn vào biểu đồ dependency, có thể thấy em chia thành 4 cụm chính."

| Cụm | Vai trò | Class tiêu biểu |
|---|---|---|
| View Layer | Hiển thị form và nhận tương tác người dùng | `SalesRequestEditView`, `SalesRequestEditTableComponent`, `SalesRequestEditHeaderPanel` |
| Controller Layer | Điều phối event từ View xuống model/application | `SalesRequestEditController`, `SalesRequestEditViewState` |
| Session | Giữ trạng thái tạm thời của form update | `SalesRequestEditSession`, `SalesRequestEditState` |
| Application Layer | Xử lý nghiệp vụ update | `SalesRequestEditUseCase`, `SalesRequestEditValidator`, `SalesRequestEditSelectionPolicy`, `SalesRequestEditGateway` |

"Điểm quan trọng nhất là dependency đi theo hướng có kiểm soát: View không gọi thẳng UseCase, UseCase không biết View, và UseCase không phụ thuộc trực tiếp vào service cụ thể."

### Phút 3: Điểm Tốt Lớn Số 1 - Tách View Và Controller Bằng Interface

"Ở bản thiết kế này, View không import trực tiếp `SalesRequestEditController`. Thay vào đó View chỉ biết interface `SalesRequestEditActionHandler`."

Ý nghĩa thiết kế:

- Tránh dependency vòng giữa View và Controller.
- View chỉ phát event như `saveRequested`, `quantityChanged`, `merchandiseChanged`.
- Controller là lớp implement các event đó.
- Đây là cách áp dụng DIP ở tầng Presentation.

Câu nói chốt:

> Em vẫn giữ tinh thần MVC: View nhận tương tác, Controller xử lý. Nhưng em không để View phụ thuộc concrete Controller để tránh cyclic dependency ở mức package.

### Phút 4: Điểm Tốt Lớn Số 2 - Có Session Riêng Cho Edit State

"Em tách `SalesRequestEditSession` và `SalesRequestEditState` vì form update là một phiên chỉnh sửa tạm thời."

Điểm tốt nhìn từ thiết kế:

- Người dùng sửa nhiều dòng nhưng chưa ghi database ngay.
- Nếu bấm cancel, state tạm thời bị bỏ, dữ liệu thật không bị ảnh hưởng.
- Mọi thay đổi như đổi merchandise, quantity, desired date đều đi qua `SalesRequestEditState`.
- View không tự mutate dữ liệu nghiệp vụ.

Câu nói chốt:

> Session giúp em cô lập trạng thái nháp của form update. Đây là điểm quan trọng để tránh việc UI thao tác trực tiếp vào dữ liệu thật.

### Phút 5: Điểm Tốt Lớn Số 3 - UseCase Và Gateway Tách Nghiệp Vụ Khỏi Service

"Ở tầng Application, `SalesRequestEditUseCase` không gọi trực tiếp `SalesRequestQueryService` hay `SalesRequestCommandService`. Nó chỉ phụ thuộc vào interface `SalesRequestEditGateway`."

Ý nghĩa thiết kế:

- UseCase độc lập với cách lấy/ghi dữ liệu.
- Nếu sau này thay service, repository hoặc database, UseCase không cần đổi nhiều.
- Có thể unit test UseCase bằng fake gateway.
- Đây là Dependency Inversion trong Clean Architecture.

Câu nói chốt:

> Service/database là chi tiết hạ tầng. UseCase chỉ biết port, nên logic update không bị khóa chặt vào implementation hiện tại.

---

## 3. Các Điểm Tốt Nên Nhấn Mạnh Khi Nhìn Biểu Đồ

### 3.1. Không Có View Gọi Thẳng Xuống Application

Trên biểu đồ, luồng đúng là:

```text
View -> ActionHandler -> Controller -> Session -> UseCase -> Gateway -> Service
```

Điều này chứng minh:

- Không có layer skipping nghiêm trọng.
- View không gọi thẳng service.
- View không tự validate business rule.
- Controller không tự ghi database.

Đây là điểm hội đồng nhìn biểu đồ có thể hiểu ngay.

### 3.2. Controller Mỏng

`SalesRequestEditController` không ôm logic nghiệp vụ. Nó chỉ:

- Nhận event từ View.
- Gọi Session tương ứng.
- Render lại `SalesRequestEditViewState`.
- Xử lý kết quả save success/invalid/error.

Điểm tốt:

- Controller không phải God class.
- Logic nghiệp vụ không bị lẫn với UI flow.
- Dễ đọc khi nhìn vào sơ đồ vì Controller chỉ là cầu nối.

### 3.3. View Được Phân Rã Thành Component

Thay vì một View lớn xử lý tất cả, View được tách thành:

- `SalesRequestEditHeaderPanel`: hiển thị thông tin header.
- `SalesRequestEditSearchFilterBar`: xử lý ô tìm kiếm.
- `SalesRequestEditTableComponent`: quản lý bảng item.
- `ValidationMessageDispatcher`: hiển thị lỗi validate.

Điểm tốt:

- Tăng cohesion vì mỗi component lo một phần UI.
- Giảm conflict khi làm nhóm.
- Dễ bảo trì hơn một file View khổng lồ.

Điểm cần nói vừa phải:

> Em không tách quá nhỏ mọi thứ. Em chỉ tách những phần thay đổi độc lập rõ ràng như header, search, table và validation.

### 3.4. Business Rule Có Nơi Chứa Rõ Ràng

Các rule nghiệp vụ không nằm trong View:

- `SalesRequestEditValidator`: kiểm tra form hợp lệ.
- `SalesRequestEditSelectionPolicy`: rule không chọn trùng merchandise.
- `ValidatedSalesRequestEditDraft`: đại diện cho draft đã validate.

Điểm tốt:

- Rule không bị duplicate ở nhiều chỗ.
- Khi đổi rule, sửa ở application/model thay vì sửa UI.
- Dễ giải thích SOLID, đặc biệt là SRP và OCP.

### 3.5. Có Hợp Đồng Dữ Liệu Rõ Ràng

Các record/DTO quan trọng:

- `SalesRequestEditDialogInput`: input mở form.
- `SalesRequestEditViewState`: dữ liệu để View render.
- `SalesRequestEditDraft`: snapshot trạng thái nháp.
- `ValidatedSalesRequestEditDraft`: dữ liệu đã qua validate.
- `SalesRequestEditData`: dữ liệu load ban đầu.

Điểm tốt:

- Giao tiếp giữa các tầng rõ ràng.
- Giảm phụ thuộc vào mutable object.
- Dễ đọc trên sơ đồ vì dữ liệu đi qua các tầng bằng object cụ thể.

---

## 4. Cách Nói Về SOLID Trong 1 Phút

Không cần nói đủ 5 nguyên lý quá sâu. Nên chọn 3 nguyên lý dễ chứng minh nhất từ biểu đồ.

### SRP

"Mỗi lớp có một trách nhiệm chính. View render UI, Controller điều phối, Session giữ state, UseCase xử lý nghiệp vụ, Validator validate, Gateway kết nối service."

Ví dụ nói nhanh:

> `SalesRequestEditValidator` chỉ validate. `SalesRequestEditState` chỉ quản lý draft state. `SalesRequestEditServiceGateway` chỉ adapter sang service.

### DIP

"UseCase không phụ thuộc service cụ thể mà phụ thuộc interface `SalesRequestEditGateway`. View cũng không phụ thuộc concrete Controller mà phụ thuộc `SalesRequestEditActionHandler`."

Ví dụ nói nhanh:

> Đây là hai điểm áp dụng Dependency Inversion rõ nhất trong thiết kế.

### OCP

"Rule chọn merchandise được tách vào `SalesRequestEditSelectionPolicy`. Khi thay đổi chính sách chọn hàng, em không cần sửa TableView hay ComboBox UI."

Ví dụ nói nhanh:

> UI chỉ nhận danh sách options hợp lệ để hiển thị, còn rule tạo ra options hợp lệ nằm ở policy.

---

## 5. Câu Trả Lời Nếu Bị Hỏi Nhanh

### Hỏi: "Tại sao phải có Session?"

Trả lời:

"Vì update form là một phiên chỉnh sửa tạm thời. Người dùng có thể sửa nhiều dòng rồi mới bấm save hoặc cancel. Session giúp giữ draft state riêng, không ghi trực tiếp vào dữ liệu thật khi người dùng mới đang nhập."

### Hỏi: "Tại sao View không gọi thẳng Controller?"

Trả lời:

"Nếu View import concrete Controller và Controller cũng import View, package sẽ bị phụ thuộc vòng. Em dùng `SalesRequestEditActionHandler` để View chỉ phát event, còn Controller implement event đó. Như vậy vẫn đúng MVC nhưng giảm coupling."

### Hỏi: "Tại sao phải có UseCase và Gateway?"

Trả lời:

"UseCase chứa luồng nghiệp vụ update. Gateway là port để UseCase không phụ thuộc trực tiếp vào service/database. Cách này giúp thay đổi hạ tầng không ảnh hưởng logic nghiệp vụ."

### Hỏi: "Tại sao cần `ValidatedSalesRequestEditDraft`?"

Trả lời:

"Nó là hợp đồng dữ liệu đã validate. Mapper submit chỉ nhận draft đã validate, nên tránh lỗi gọi nhầm mapper với dữ liệu chưa hợp lệ. Đây là cách dùng type để bảo vệ invariant thay vì dựa vào if-else rải rác."

### Hỏi: "View có logic không?"

Trả lời:

"View có presentation logic như render table, filter hiển thị, style lỗi. Nhưng business rule như validate quantity, date, duplicate merchandise không nằm trong View mà nằm ở Validator và SelectionPolicy."

---

## 6. Điểm Cần Thừa Nhận Nếu Bị Bắt Bẻ

Không nên cố nói thiết kế hoàn hảo tuyệt đối. Nên chủ động thừa nhận giới hạn nhỏ để câu trả lời đáng tin hơn.

### 6.1. `SalesRequestEditTableComponent` Còn Khá Lớn

Cách trả lời:

"Đúng là TableComponent còn nhiều chi tiết JavaFX như cell factory, pagination và validation style. Nhưng tất cả đều xoay quanh một boundary là bảng item update. Nếu mở rộng tiếp, em sẽ tách thêm cell factory hoặc pagination renderer."

### 6.2. Gateway Có Bắt `Exception` Ở Biên Service

Cách trả lời:

"Ở tầng domain thì bắt Exception chung là không tốt. Nhưng đoạn này nằm ở adapter hạ tầng, mục đích là chuẩn hóa lỗi từ service cũ thành `SalesRequestEditGatewayException`. Nếu cải tiến tiếp, em sẽ thay bằng exception cụ thể hơn từ service."

### 6.3. Session Nên Reset Sau Save/Cancel

Cách trả lời:

"Đúng, để lifecycle sạch hơn thì sau khi save hoặc cancel nên reset listener/state trong Session. Hiện tại chưa gây lỗi nghiệp vụ, nhưng đây là cải tiến tốt để tránh giữ tham chiếu lâu hơn cần thiết."

---

## 7. Slide/Talk Track Gợi Ý

### Slide 1: Problem

Nội dung nói:

"Update request cần giữ trạng thái nháp trước khi submit, nên em không để UI ghi trực tiếp xuống service."

### Slide 2: Architecture Diagram

Nội dung nói:

"Nhìn vào dependency diagram, luồng chính là View -> Controller -> Session -> UseCase -> Gateway -> Service. Đây là hướng phụ thuộc có kiểm soát."

### Slide 3: Design Point 1 - View/Controller Decoupling

Nội dung nói:

"View phát event qua `SalesRequestEditActionHandler`, không phụ thuộc concrete Controller. Điểm này giúp tránh cyclic dependency."

### Slide 4: Design Point 2 - Edit Session State

Nội dung nói:

"Session giữ draft state của form update. Người dùng sửa nhiều dòng nhưng chưa ghi database cho tới khi bấm save."

### Slide 5: Design Point 3 - UseCase/Gateway Boundary

Nội dung nói:

"UseCase phụ thuộc vào Gateway interface. Adapter cụ thể gọi service hiện có. Đây là phần Clean Architecture style."

### Slide 6: Validation and Business Rules

Nội dung nói:

"Rule validate nằm ở `SalesRequestEditValidator`, rule chọn merchandise nằm ở `SalesRequestEditSelectionPolicy`, dữ liệu đã validate được biểu diễn bằng `ValidatedSalesRequestEditDraft`."

### Slide 7: Conclusion

Nội dung nói:

"Thiết kế này giúp giảm coupling, tăng cohesion, tránh View quá lớn, và tách logic nghiệp vụ khỏi UI."

---

## 8. Bản Chốt 30 Giây

"Tóm lại, điểm chính trong thiết kế của em là tách rõ trách nhiệm. View chỉ render và phát event. Controller điều phối. Session giữ trạng thái nháp của form update. UseCase xử lý nghiệp vụ và chỉ phụ thuộc Gateway interface. Validator và SelectionPolicy giữ business rule. Nhờ vậy, khi nhìn vào biểu đồ package có thể thấy dependency đi theo một chiều rõ ràng, không có View gọi thẳng service, không có Controller ghi database trực tiếp, và các phần thay đổi độc lập được cô lập tốt hơn."

