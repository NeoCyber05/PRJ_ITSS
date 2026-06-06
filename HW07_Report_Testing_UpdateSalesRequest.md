# HW07 - Báo cáo kiểm thử chức năng Update Sales Request

## Phạm vi kiểm thử

Module phụ trách: `sales/request/update`.

Đối tượng kiểm thử chính:

- Unit Test: `SalesRequestEditValidator`.
- Use Case Test: luồng tổng thể `Update Sales Request`.

Tài liệu này chỉ tập trung vào phần update của Sales Request, không mở rộng sang module create, list, ordering hay repository bên ngoài.

---

## PHẦN 1: KIỂM THỬ ĐƠN VỊ (UNIT TESTING)

### 1.1. Mô tả module

Class được kiểm thử: `org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidator`.

Phương thức chính:

- `validate(SalesRequestEditDraft draft, LocalDate today)`
- `validateForSubmission(SalesRequestEditDraft draft, LocalDate today)`

Input:

- `SalesRequestEditDraft draft`: dữ liệu nháp của form update.
- `LocalDate today`: ngày hiện tại dùng để kiểm tra ngày nhận hàng.

Mỗi dòng hàng trong draft là `SalesRequestEditItemDraft`, gồm:

- `lineId`: mã dòng trên form.
- `merchandise`: mặt hàng được chọn.
- `quantity`: số lượng yêu cầu.
- `desiredDate`: ngày mong muốn nhận hàng.

Output:

- Với `validate(...)`: trả về `SalesRequestEditValidationResult`.
- Với `validateForSubmission(...)`: trả về `ValidatedSalesRequestEditDraft` nếu hợp lệ, hoặc ném `SalesRequestEditValidationException` nếu không hợp lệ.

Quy tắc nghiệp vụ:

- Danh sách item không được rỗng.
- Mỗi dòng phải chọn mặt hàng.
- Không được chọn trùng mặt hàng giữa các dòng.
- Số lượng không được `null` và phải lớn hơn `0`.
- Ngày nhận không được `null` và không được trước ngày hiện tại.

Ghi chú quan trọng: code hiện tại không có giới hạn trên cho `quantity`, nên tài liệu không thiết kế test case cho biên trên như `1000` hoặc `1001`.

### 1.2. Kiểm thử hộp đen - Bảng quyết định

Ở kiểm thử hộp đen, `SalesRequestEditValidator` được xem như một hộp kín. Test case được thiết kế dựa trên đặc tả nghiệp vụ của chức năng cập nhật Sales Request, không xét tới cách mã nguồn hiện thực bên trong.

Dữ liệu mẫu dùng trong test:

- `today = 2026-05-25`
- `M1 = MerchandiseOption(10, "MH-001", "Item 1", "box")`

#### 1.2.1. Điều kiện và hành động

| Ký hiệu | Điều kiện |
|---|---|
| C1 | Danh sách item rỗng: `items=[]` |
| C2 | Chưa chọn mặt hàng: `merchandise == null` |
| C3 | Số lượng bị bỏ trống: `quantity == null` |
| C4 | Số lượng nhỏ hơn hoặc bằng 0: `quantity <= 0` |
| C5 | Ngày nhận bị bỏ trống: `desiredDate == null` |
| C6 | Ngày nhận trước ngày hiện tại: `desiredDate < today` |
| C7 | Draft hợp lệ khi submit |
| C8 | Draft không hợp lệ khi submit |

| Ký hiệu | Hành động |
|---|---|
| A1 | Trả về violation field `items` |
| A2 | Trả về violation field `merchandise` do chưa chọn mặt hàng |
| A3 | Trả về violation field `quantity` |
| A4 | Trả về violation field `desiredDate` |
| A5 | Không có violation, `validForm() = true` |
| A6 | Trả về `ValidatedSalesRequestEditDraft` |
| A7 | Ném `SalesRequestEditValidationException` |

Quan hệ phụ thuộc giữa các điều kiện:

- C1 có mức ưu tiên cao nhất. Nếu `items=[]`, validator trả về lỗi `items` và không cần xét các điều kiện trên từng dòng hàng.
- Điều kiện trùng mặt hàng không được đưa vào bảng quyết định hộp đen vì FE đã lọc combobox, người dùng chỉ chọn được các mặt hàng chưa xuất hiện ở dòng khác.
- C3 và C4 nằm trong cùng nhóm kiểm tra `quantity`; C4 chỉ có ý nghĩa khi `quantity != null`.
- C5 và C6 nằm trong cùng nhóm kiểm tra `desiredDate`; C6 chỉ có ý nghĩa khi `desiredDate != null`.
- C7 và C8 là hai trường hợp của `validateForSubmission(...)`: hợp lệ thì trả về validated draft, không hợp lệ thì ném exception.
- Trùng mặt hàng vẫn được kiểm thử ở phần hộp trắng C1 vì trong validator có nhánh phòng vệ `selectionPolicy.isDuplicateSelection(...)`.

#### 1.2.2. Bảng quyết định

Bảng quyết định dưới đây chỉ xét các input có thể phát sinh từ luồng người dùng trên FE. Trường hợp trùng mặt hàng không được đưa vào bảng hộp đen vì combobox đã lọc, người dùng không thể chọn lại mặt hàng đã xuất hiện ở dòng khác.

| Điều kiện / Hành động | DT-01 | DT-02 | DT-03 | DT-04 | DT-05 | DT-06 | DT-07 | DT-08 | DT-09 |
|---|---|---|---|---|---|---|---|---|---|
| C1: `items=[]` | Có | Không | Không | Không | Không | Không | Không | Không | Không |
| C2: `merchandise == null` | - | Có | Không | Không | Không | Không | Không | Không | Có |
| C3: `quantity == null` | - | Không | Có | Không | Không | Không | Không | Không | Không |
| C4: `quantity <= 0` | - | Không | - | Có | Không | Không | Không | Không | Có |
| C5: `desiredDate == null` | - | Không | Không | Không | Có | Không | Không | Không | Không |
| C6: `desiredDate < today` | - | Không | Không | Không | - | Có | Không | Không | Có |
| C7: submit draft hợp lệ | - | - | - | - | - | - | - | Có | Không |
| C8: submit draft không hợp lệ | - | - | - | - | - | - | - | Không | Có |
| A1: violation `items` | X |  |  |  |  |  |  |  |  |
| A2: violation `merchandise` |  | X |  |  |  |  |  |  | X |
| A3: violation `quantity` |  |  | X | X |  |  |  |  | X |
| A4: violation `desiredDate` |  |  |  |  | X | X |  |  | X |
| A5: `validForm() = true` |  |  |  |  |  |  | X |  |  |
| A6: trả về validated draft |  |  |  |  |  |  |  | X |  |
| A7: ném validation exception |  |  |  |  |  |  |  |  | X |

#### 1.2.3. Danh sách test case hộp đen

| Mã TC | Quy tắc | Tên test JUnit | Dữ liệu kiểm thử | Kết quả mong đợi |
|---|---|---|---|---|
| DT-01 | C1 | `validate_shouldReject_whenItemsEmpty` | `items=[]` | Violation `items` |
| DT-02 | C2 | `validate_shouldReject_whenMerchandiseMissing` | `merchandise=null` | Violation `merchandise` |
| DT-03 | C3 | `validate_shouldReject_whenQuantityNull` | `quantity=null` | Violation `quantity` |
| DT-04 | C4 | `validate_shouldReject_whenQuantityNonPositive` | `quantity=0` | Violation `quantity` |
| DT-05 | C5 | `validate_shouldReject_whenDesiredDateNull` | `desiredDate=null` | Violation `desiredDate` |
| DT-06 | C6 | `validate_shouldReject_whenDesiredDateBeforeToday` | `desiredDate=2026-05-24` | Violation `desiredDate` |
| DT-07 | Valid form | `validate_shouldHaveNoViolations_whenDraftIsValid` | 1 item: `M1`, `quantity=1`, `desiredDate=2026-05-25` | Không có violation |
| DT-08 | C7 | `validateForSubmission_shouldReturnValidatedDraft_whenDraftValid` | Draft hợp lệ | Trả về `ValidatedSalesRequestEditDraft` |
| DT-09 | C8 | `validateForSubmission_shouldThrow_whenDraftInvalid` | Draft không hợp lệ | Ném `SalesRequestEditValidationException` |

### 1.3. Kỹ thuật kiểm thử hộp trắng (White-box - C1 Branch Coverage)

Kiểm thử hộp trắng C1 yêu cầu mọi nhánh điều kiện True/False phải được đi qua ít nhất một lần.

Các nhánh điều kiện trong `SalesRequestEditValidator`:

| Mã nhánh | Điều kiện | Nhánh cần bao phủ |
|---|---|---|
| B1 | `draft.items().isEmpty()` | True và False |
| B2 | `merchandise == null` | True và False |
| B3 | `selectionPolicy.isDuplicateSelection(...)` | True và False |
| B4a | `quantity == null` | True và False |
| B4b | `quantity.compareTo(BigDecimal.ZERO) <= 0` | True và False khi `quantity != null` |
| B5a | `desiredDate == null` | True và False |
| B5b | `desiredDate.isBefore(today)` | True và False khi `desiredDate != null` |
| B6 | `!validationResult.validForm()` trong `validateForSubmission(...)` | True và False |

Ghi chú: B4 và B5 dùng toán tử `||`, nên có short-circuit. Vì vậy cần test riêng trường hợp `null` và trường hợp non-null nhưng sai giá trị.

#### Bảng test case hộp trắng

| Test Case ID | Nhánh đi qua (Execution Path) | Dữ liệu đầu vào (Input) | Kết quả mong đợi |
|---|---|---|---|
| WB-01 | B1=True | `items=[]` | Trả về violation field `items`, return sớm |
| WB-02 | B1=False, B2=False, B3=False, B4a=False, B4b=False, B5a=False, B5b=False, B6=False | 1 item hợp lệ: `M1`, `quantity=1`, `desiredDate=today`; gọi thêm `validateForSubmission(...)` | `validate(...)` hợp lệ; `validateForSubmission(...)` trả về `ValidatedSalesRequestEditDraft` |
| WB-03 | B1=False, B2=True, B4a=True, B5a=True, B6=True | 1 item: `merchandise=null`, `quantity=null`, `desiredDate=null`; gọi thêm `validateForSubmission(...)` | Có violation `merchandise`, `quantity`, `desiredDate`; submission ném exception |
| WB-04 | B1=False, B2=False, B3=True, B4a=False, B4b=True, B5a=False, B5b=True | 2 item cùng `M1`; dòng thứ hai có `quantity=0`, `desiredDate=today.minusDays(1)` | Có violation trùng merchandise, quantity không hợp lệ, desiredDate không hợp lệ |

Với 4 test case trên, các nhánh True/False của các điều kiện chính đều được đi qua. Do đó tập test case này đạt mục tiêu C1 Branch Coverage cho logic validator hiện tại.

---

## PHẦN 2: CÀI ĐẶT KIỂM THỬ TỰ ĐỘNG (AUTOMATED TESTING)

### 2.1. Tên đầy đủ của các class JUnit Test

Để thể hiện rõ hai phương pháp kiểm thử, phần automated test được tách thành 2 class riêng:

1. Black-box testing:

`org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidatorBlackBoxTest`

Vị trí file:

`src/test/java/org/itss/prj_itss/model/request/application/sales/update/SalesRequestEditValidatorBlackBoxTest.java`

2. White-box C1 branch coverage:

`org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidatorWhiteBoxC1Test`

Vị trí file:

`src/test/java/org/itss/prj_itss/model/request/application/sales/update/SalesRequestEditValidatorWhiteBoxC1Test.java`

### 2.2. JUnit 5 Skeleton Code

Mã nguồn test đã được cài đặt trực tiếp trong project theo 2 file trên. Cấu trúc triển khai như sau:

```java
@DisplayName("HW07 - SalesRequestEditValidator Black-box Tests")
class SalesRequestEditValidatorBlackBoxTest {
    // DT-01: valid draft
    // DT-02: empty item list
    // DT-03: missing merchandise
    // DT-04: non-positive quantity
    // DT-05: null desiredDate
    // DT-06: desiredDate before today
    // DT-07: valid draft
    // DT-08: valid validateForSubmission
    // DT-09: invalid validateForSubmission throws exception
}

@DisplayName("HW07 - SalesRequestEditValidator White-box C1 Tests")
class SalesRequestEditValidatorWhiteBoxC1Test {
    // WB-01: cover B1 true, empty items
    // WB-02: cover valid false branches and valid submission
    // WB-03: cover null merchandise, null quantity, null desiredDate, invalid submission
    // WB-04: cover duplicate merchandise, zero quantity, past desiredDate
}
```

Việc tách thành 2 class giúp phần nộp bài rõ ràng hơn:

- `SalesRequestEditValidatorBlackBoxTest` chứng minh thiết kế test hộp đen bằng bảng quyết định.
- `SalesRequestEditValidatorWhiteBoxC1Test` chứng minh thiết kế test dựa trên cấu trúc mã nguồn và mục tiêu bao phủ nhánh C1.

### 2.3. Lệnh chạy test và log kết quả

Chạy kiểm thử hộp đen:

```bash
./mvnw -Dtest=SalesRequestEditValidatorBlackBoxTest -Dsurefire.useFile=false test
```

Kết quả mong đợi:

- In ra từng test case `DT-01` đến `DT-09`.
- Mỗi test case có `Rule`, `Input`, `Expected`, `Actual`, `Result`.
- Kết quả tổng kết: `Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`.
- Maven kết thúc bằng `BUILD SUCCESS`.

Chạy kiểm thử hộp trắng C1:

```bash
./mvnw -Dtest=SalesRequestEditValidatorWhiteBoxC1Test -Dsurefire.useFile=false test
```

Kết quả mong đợi:

- In ra từng test case `WB-01` đến `WB-04`.
- Mỗi test case có `Execution Path`, `Input`, `Expected`, `Actual`, `Result`.
- Kết quả tổng kết: `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`.
- Maven kết thúc bằng `BUILD SUCCESS`.

Chạy cả hai nhóm test:

```bash
./mvnw -Dtest=SalesRequestEditValidatorBlackBoxTest,SalesRequestEditValidatorWhiteBoxC1Test -Dsurefire.useFile=false test
```

Kết quả mong đợi:

- In ra toàn bộ `9` test case hộp đen và `4` test case hộp trắng.
- Kết quả tổng kết: `Tests run: 13, Failures: 0, Errors: 0, Skipped: 0`.
- Maven kết thúc bằng `BUILD SUCCESS`.

---

## PHẦN 3: KIỂM THỬ USE CASE (USE CASE TESTING)

### 3.1. Phân tích kịch bản

Use case tổng thể: người dùng Sales mở form Update Sales Request, chỉnh sửa dữ liệu mặt hàng, sau đó lưu hoặc hủy thao tác.

Các kịch bản chính:

- Happy Path: mở form thành công, dữ liệu hợp lệ, lưu thành công.
- Alternate Path: mở form thành công nhưng nhập dữ liệu không hợp lệ, hệ thống hiển thị lỗi validate và không gửi update.
- Cancel Path: người dùng mở form nhưng hủy thao tác, hệ thống đóng form và không lưu thay đổi.
- Exception Path: request không tồn tại hoặc không tải được dữ liệu, hệ thống báo lỗi và đóng form.

### 3.2. Bảng test case use case

| Test Case ID | Tên kịch bản (Scenario) | Điều kiện tiên quyết (Preconditions) | Các bước thực hiện (Test Steps) | Kết quả mong đợi (Expected Results) | Kết quả thực tế (Actual Results) |
|---|---|---|---|---|---|
| UC-01 | Happy Path - Lưu update thành công | Sales request tồn tại; form có ít nhất 1 item; danh sách merchandise được tải thành công | 1. Mở màn hình Sales Request. 2. Chọn một request cần update. 3. Mở form update. 4. Chọn merchandise hợp lệ. 5. Nhập `quantity > 0`. 6. Chọn `desiredDate >= today`. 7. Bấm lưu/cập nhật. | Form validate thành công; hệ thống gọi luồng update; hiển thị thông báo cập nhật thành công; form đóng; listener nhận sự kiện saved. | PASS |
| UC-02 | Alternate Path - Dữ liệu nhập không hợp lệ | Sales request tồn tại; form update mở thành công | 1. Mở form update. 2. Xóa lựa chọn merchandise hoặc chọn trùng merchandise. 3. Nhập `quantity = 0`. 4. Chọn `desiredDate < today`. 5. Bấm lưu/cập nhật. | Hệ thống không submit update; hiển thị lỗi tại các field `merchandise`, `quantity`, `desiredDate`; form vẫn mở để người dùng sửa. | PASS |
| UC-03 | Cancel Path - Hủy thao tác update | Sales request tồn tại; form update mở thành công | 1. Mở form update. 2. Thay đổi một số dữ liệu trên form. 3. Bấm hủy/đóng form. | Hệ thống gọi luồng cancel; không submit dữ liệu; form đóng; listener nhận sự kiện cancel nếu có state hợp lệ. | PASS |
| UC-04 | Exception Path - Không tìm thấy request cần update | Request ID không tồn tại hoặc tầng load trả về dữ liệu rỗng | 1. Thử mở form update với request ID không tồn tại. | Hệ thống hiển thị thông báo không tìm thấy yêu cầu cần cập nhật; form đóng; không cho thao tác lưu. | PASS |
| UC-05 | Exception Path - Lỗi khi submit update | Sales request tồn tại; dữ liệu nhập hợp lệ; gateway/service phát sinh lỗi khi update | 1. Mở form update. 2. Nhập dữ liệu hợp lệ. 3. Bấm lưu/cập nhật. 4. Giả lập lỗi service khi submit. | Hệ thống bắt lỗi nghiệp vụ ở controller/session; hiển thị thông báo lỗi; form không đóng thành công. | PASS |

### 3.3. Kết luận

Bộ test ở mức unit giúp kiểm tra chính xác các quy tắc validate của `SalesRequestEditValidator`.

Bộ test use case giúp kiểm tra luồng nghiệp vụ ở mức người dùng: mở form, sửa dữ liệu, validate, lưu, hủy và xử lý lỗi. Hai mức kiểm thử này bổ sung cho nhau: unit test đảm bảo logic nhỏ đúng, use case test đảm bảo chức năng update hoạt động đúng theo kịch bản sử dụng thực tế.
