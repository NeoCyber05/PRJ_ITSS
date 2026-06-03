# Thiết kế kiểm thử `DefaultAllocationValidator`

## 1. Tóm tắt

Module được chọn nằm trong luồng xử lý yêu cầu đặt hàng:

```text
org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidator
```

Tài liệu này tập trung kiểm thử phương thức:

```java
validateSubmission(...)
```

Lý do chọn phương thức này:

- Phương thức xuất hiện trong `Sequence_Processing.md`, ở bước `Session -> UC: validateSubmission(...)`.
- Đây là logic domain thuần Java, không phụ thuộc JavaFX hay database.
- Phương thức có đủ các nhánh nghiệp vụ để áp dụng lần lượt kiểm thử hộp đen và kiểm thử hộp trắng C1.

Class kiểm thử tự động đầy đủ:

```text
org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidatorTest
```

## 2. Mô tả lớp và phương thức cần kiểm thử

`DefaultAllocationValidator` có nhiệm vụ kiểm tra việc phân bổ hàng trước khi người dùng xác nhận tạo đơn hàng. Trong luồng `UC_RQ_PROCESS`, lớp này được dùng khi hệ thống cần biết dữ liệu phân bổ hiện tại đã đủ điều kiện gửi đi hay chưa.

Phương thức được kiểm thử:

```java
public String validateSubmission(
    List<ItemRequirement> items,
    List<SiteStockOption> allSites,
    Map<Integer, Map<Integer, Allocation>> allocations,
    Map<Integer, LocalDate> desiredDeliveryDates,
    int deadlineDays
)
```

Các đầu vào chính:

| Tham số | Ý nghĩa |
|---|---|
| `items` | Danh sách mặt hàng cần xử lý, mỗi item có mã hàng và số lượng yêu cầu |
| `allSites` | Danh sách site có thể cung cấp hàng, kèm thời gian vận chuyển |
| `allocations` | Dữ liệu phân bổ số lượng hàng cho từng site |
| `desiredDeliveryDates` | Ngày nhận mong muốn theo từng mặt hàng |
| `deadlineDays` | Deadline mặc định khi item không có ngày nhận mong muốn riêng |

Kết quả trả về:

| Trường hợp | Kết quả |
|---|---|
| Phân bổ hợp lệ | `null` |
| Thiếu số lượng | `"Chua du so luong hang can"` |
| Thừa số lượng | `"So luong phan bo vuot yeu cau"` |
| Site hoặc thời gian giao hàng không đáp ứng | `"Khong dap ung ngay nhan mong muon"` |

## 3. Thiết kế test case bằng kỹ thuật hộp đen

Ở phần hộp đen, tôi xét phương thức theo đầu vào và đầu ra mong đợi, chưa đi vào chi tiết từng nhánh trong code. Hai kỹ thuật được dùng là phân vùng tương đương và giá trị biên.

### 3.1. Phân vùng theo số lượng phân bổ

Điều kiện nghiệp vụ là tổng số lượng phân bổ phải đúng bằng số lượng yêu cầu.

| Phân vùng | Điều kiện | Hợp lệ | Test case đại diện |
|---|---|---:|---|
| Q1 | `allocated == required` | Có | Phân bổ đúng 10/10 |
| Q2 | `allocated < required` | Không | Phân bổ 9/10 |
| Q3 | `allocated > required` | Không | Phân bổ 11/10 |

Giá trị 9 và 11 được chọn vì sát với biên của yêu cầu 10. Nếu 10 là hợp lệ, thì 9 là thiếu ngay dưới biên và 11 là thừa ngay trên biên.

### 3.2. Phân vùng theo ngày nhận mong muốn

Ngày nhận mong muốn ảnh hưởng trực tiếp đến deadline của item.

| Phân vùng | Điều kiện | Ý nghĩa | Test case đại diện |
|---|---|---|---|
| D1 | Không có `desiredDate` | Dùng `deadlineDays` mặc định | `deadlineDays = 7` |
| D2 | `desiredDate` ở quá khứ | Deadline bị chặn về tối thiểu 1 ngày | `LocalDate.now().minusDays(1)` |
| D3 | `desiredDate` trong tương lai | Deadline tính theo số ngày còn lại | `LocalDate.now().plusDays(5)` |

### 3.3. Phân vùng theo site

Mỗi allocation chỉ hợp lệ nếu `siteId` tồn tại trong danh sách `allSites`.

| Phân vùng | Điều kiện | Hợp lệ | Test case đại diện |
|---|---|---:|---|
| S1 | `siteId` tồn tại | Có | Allocation dùng site 101, `allSites` có site 101 |
| S2 | `siteId` không tồn tại | Không | Allocation dùng site 999, `allSites` chỉ có site 101 |

### 3.4. Phân vùng theo phương thức vận chuyển

Sau khi tìm được site, hệ thống kiểm tra số ngày giao hàng.

| Phân vùng | Điều kiện | Hợp lệ | Test case đại diện |
|---|---|---:|---|
| T1 | `deliveryDays <= itemDeadlineDays` và `< 999` | Có | Ship 5 ngày, deadline 7 ngày |
| T2 | `deliveryDays > itemDeadlineDays` | Không | Ship 5 ngày, deadline 3 ngày |
| T3 | `deliveryDays >= 999` | Không | Ship 999 ngày, xem như không hỗ trợ |

Từ các phân vùng trên, các test case hộp đen chính gồm: phân bổ thiếu, phân bổ thừa, phân bổ đúng, site không tồn tại, giao kịp hạn, giao trễ hạn và phương thức vận chuyển không hỗ trợ.

## 4. Thiết kế test case bằng kỹ thuật hộp trắng C1

Sau khi có các nhóm test từ hộp đen, tôi xét tiếp cấu trúc điều khiển bên trong `validateSubmission(...)`. Mục tiêu của độ đo C1 là mỗi nhánh quyết định chính phải được chạy ít nhất một lần.

Các nhánh cần phủ:

| Mã nhánh | Điều kiện trong code | Nhánh cần kiểm tra |
|---|---|---|
| C1_01 | Vòng lặp duyệt `items` lần đầu | `items` rỗng và `items` không rỗng |
| C1_02 | `allocated < item.required` | Đúng và sai |
| C1_03 | `allocated > item.required` | Đúng và sai |
| C1_04 | Vòng lặp duyệt `items` lần thứ hai | Có chạy và không chạy |
| C1_05 | `desiredDate == null` | Đúng và sai |
| C1_06 | `Math.max(1, daysBetween)` | Ngày quá khứ và ngày tương lai |
| C1_07 | Vòng lặp duyệt `itemAllocations.values()` | Có allocation và không có allocation |
| C1_08 | `site == null` | Đúng và sai |
| C1_09 | `deliveryDays >= 999 || deliveryDays > itemDeadlineDays` | Đúng và sai |

Một test case có thể phủ nhiều nhánh cùng lúc. Ví dụ, trường hợp phân bổ đúng và giao kịp hạn sẽ phủ nhánh sai của điều kiện thiếu/thừa số lượng, nhánh tìm thấy site và nhánh giao hàng hợp lệ.

## 5. Danh sách test case

| Mã TC | Tên test JUnit | Kỹ thuật áp dụng | Dữ liệu kiểm thử | Kết quả mong đợi | Nhánh C1 chính |
|---|---|---|---|---|---|
| TC_01 | `testEmptyItems` | Hộp trắng | `items` rỗng | `null` | C1_01 không chạy, C1_04 không chạy |
| TC_02 | `testMissingQuantity` | Hộp đen + hộp trắng | Yêu cầu 10, phân bổ 9 | `"Chua du so luong hang can"` | C1_02 đúng |
| TC_03 | `testExcessQuantity` | Hộp đen + hộp trắng | Yêu cầu 10, phân bổ 11 | `"So luong phan bo vuot yeu cau"` | C1_02 sai, C1_03 đúng |
| TC_04 | `testNoDesiredDateDeliverySuccess` | Hộp đen + hộp trắng | Phân bổ đúng, không có `desiredDate`, `deadlineDays = 7`, `shipDays = 5` | `null` | C1_05 đúng, C1_09 sai |
| TC_05 | `testDesiredDateInPast` | Hộp trắng | `desiredDate` quá khứ, deadline bị chặn về 1, `airDays = 1` | `null` | C1_05 sai, C1_06 nhánh quá khứ |
| TC_06 | `testDesiredDateInFutureSuccess` | Hộp đen + hộp trắng | `desiredDate` tương lai 5 ngày, `airDays = 3` | `null` | C1_06 nhánh tương lai |
| TC_07 | `testSiteNotFound` | Hộp đen + hộp trắng | Allocation dùng `siteId` không tồn tại | `"Khong dap ung ngay nhan mong muon"` | C1_08 đúng |
| TC_08 | `testDeliveryUnsupportedMethod` | Hộp đen + hộp trắng | Phương thức vận chuyển không hỗ trợ, `shipDays = 999` | `"Khong dap ung ngay nhan mong muon"` | C1_09 đúng do `deliveryDays >= 999` |
| TC_09 | `testDeliveryLateThanDeadline` | Hộp đen + hộp trắng | `shipDays = 5`, deadline item là 3 ngày | `"Khong dap ung ngay nhan mong muon"` | C1_09 đúng do `deliveryDays > itemDeadlineDays` |
| TC_10 | `testNoAllocationsForRequirement` | Hộp trắng | Item yêu cầu 0, không có allocation | `null` | C1_07 không chạy |

## 6. Cài đặt kiểm thử tự động bằng JUnit

File test:

```text
src/test/java/org/itss/prj_itss/model/request/domain/processing/allocation/validator/DefaultAllocationValidatorTest.java
```

Full name của class kiểm thử:

```text
org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidatorTest
```

Cách cài đặt:

- Dùng JUnit 5.
- Dùng `@Test` cho từng test case.
- Dùng Arrange - Act - Assert.
- Dùng `assertNull(...)` cho trường hợp hợp lệ.
- Dùng `assertEquals(...)` cho trường hợp trả về thông báo lỗi.
- Mỗi test case tự chuẩn bị dữ liệu riêng, không phụ thuộc database hoặc JavaFX.

Không cần thêm dependency mới vì các test case hiện tại không cần parameterized test.

## 7. Kết quả thực thi kiểm thử

Thời gian chạy: 2026-06-03

Lệnh sử dụng:

```powershell
.\mvnw.cmd -q -Dtest=DefaultAllocationValidatorTest test
```

Kết quả tổng quan:

```
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Bảng kết quả chi tiết từng test case:

| Mã TC | Tên test JUnit | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| TC_01 | `testEmptyItems` | **PASS** | Trả về `null` như mong đợi |
| TC_02 | `testMissingQuantity` | **PASS** | Trả về `"Chua du so luong hang can"` như mong đợi |
| TC_03 | `testExcessQuantity` | **PASS** | Trả về `"So luong phan bo vuot yeu cau"` như mong đợi |
| TC_04 | `testNoDesiredDateDeliverySuccess` | **PASS** | Trả về `null` như mong đợi |
| TC_05 | `testDesiredDateInPast` | **PASS** | Trả về `null` như mong đợi |
| TC_06 | `testDesiredDateInFutureSuccess` | **PASS** | Trả về `null` như mong đợi |
| TC_07 | `testSiteNotFound` | **PASS** | Trả về `"Khong dap ung ngay nhan mong muon"` như mong đợi |
| TC_08 | `testDeliveryUnsupportedMethod` | **PASS** | Trả về `"Khong dap ung ngay nhan mong muon"` như mong đợi |
| TC_09 | `testDeliveryLateThanDeadline` | **PASS** | Trả về `"Khong dap ung ngay nhan mong muon"` như mong đợi |
| TC_10 | `testNoAllocationsForRequirement` | **PASS** | Trả về `null` như mong đợi |

Tất cả 10 test case đều chạy thành công, không có failure hay error. Độ đo C1 được phủ đầy đủ qua các nhánh đã thiết kế.

## 8. Kiểm chứng

Chạy targeted test:

```powershell
.\mvnw.cmd -q -Dtest=DefaultAllocationValidatorTest test
```

Nếu cần kiểm tra toàn bộ project trước khi nộp:

```powershell
.\mvnw.cmd -q test
```

Sau khi lưu tài liệu, cần mở lại file để kiểm tra tiếng Việt có dấu vẫn hiển thị đúng theo UTF-8.

## 9. Ghi chú

- Giữ module đã chọn là `DefaultAllocationValidator`, không tạo thêm module kiểm thử khác song song.
- Không sửa public API hoặc production code.
- Không đụng tới các thay đổi không liên quan trong worktree.
- Tài liệu này là phần báo cáo mô tả phương pháp thiết kế test case và class JUnit tương ứng.
