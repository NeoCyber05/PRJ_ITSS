# PLAN: Implement kiểm thử `DefaultAllocationValidator`

## Mục tiêu

Triển khai và chuẩn hóa phần kiểm thử cho module xử lý yêu cầu đặt hàng, tập trung vào:

```text
org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidator
```

Phương thức cần kiểm thử:

```java
validateSubmission(...)
```

Lý do chọn:

- Phương thức nằm trong luồng `UC_RQ_PROCESS`.
- Có trong `Sequence_Processing.md` ở bước `Session -> UC: validateSubmission(...)`.
- Là logic domain thuần Java, không phụ thuộc JavaFX hoặc database.
- Có đủ nhánh để thiết kế test case bằng hộp đen và hộp trắng C1.

## File cần làm

### 1. Tài liệu báo cáo

File:

```text
testing_UC_RQ_PROCESS.md
```

Nội dung cần có:

- Mô tả lớp/phương thức cần kiểm thử.
- Nêu rõ input/output và vai trò trong sơ đồ lớp, sequence.
- Phân tích hộp đen trước:
  - phân bổ thiếu, đủ, thừa số lượng;
  - giao đúng hạn, trễ hạn;
  - site tồn tại hoặc không tồn tại;
  - phương thức vận chuyển được hỗ trợ hoặc không hỗ trợ.
- Phân tích hộp trắng sau bằng C1:
  - `items` rỗng/không rỗng;
  - `allocated < required`;
  - `allocated > required`;
  - `desiredDate == null`;
  - vòng lặp allocation rỗng/không rỗng;
  - `site == null`;
  - điều kiện giao hàng không đáp ứng.
- Chia test case thành hai lượt riêng:
  - lượt 1: test case sinh bằng hộp đen, dựa trên phân vùng tương đương và giá trị biên;
  - lượt 2: test case bổ sung bằng hộp trắng C1, chỉ dùng sau khi đã rà soát nhánh còn thiếu.
- Không ghi một test case là "hộp đen + hộp trắng". Nếu test hộp đen đi qua một nhánh C1, chỉ ghi nhận ở phần rà soát độ phủ, không coi là sinh đồng thời bởi hai kỹ thuật.
- Ghi rõ full name của class test:

```text
org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidatorTest
```

### 2. Test JUnit

File:

```text
src/test/java/org/itss/prj_itss/model/request/domain/processing/allocation/validator/DefaultAllocationValidatorTest.java
```

Yêu cầu:

- Dùng JUnit 5.
- Dùng `@Test`.
- Dùng `@DisplayName` để hiển thị mã test case và mục đích kiểm thử rõ hơn.
- Đặt tên method theo mẫu `methodName_shouldExpectedBehavior_whenScenario`.
- Dùng Arrange - Act - Assert.
- Dùng `assertNull(...)` cho trường hợp hợp lệ.
- Dùng `assertEquals(...)` cho trường hợp có thông báo lỗi.
- Thêm message cho assertion để lỗi test dễ đọc hơn.
- Mỗi test độc lập.
- Không phụ thuộc DB/JavaFX.
- Không thêm dependency mới vì không cần parameterized test.

## Test case cần có

| Mã TC | Mô tả | Kết quả mong đợi |
|---|---|---|
| TC_01 | `items` rỗng | `null` |
| TC_02 | Phân bổ thiếu 9/10 | `"Chưa đủ số lượng hàng cần"` |
| TC_03 | Phân bổ thừa 11/10 | `"Số lượng phân bổ vượt yêu cầu"` |
| TC_04 | Phân bổ đúng, không có `desiredDate`, `deadlineDays = 7`, `shipDays = 5` | `null` |
| TC_05 | `desiredDate` quá khứ, deadline bị chặn về 1, `airDays = 1` | `null` |
| TC_06 | `desiredDate` tương lai 5 ngày, `airDays = 3` | `null` |
| TC_07 | Allocation dùng `siteId` không tồn tại | `"Không đáp ứng ngày nhận mong muốn"` |
| TC_08 | Phương thức vận chuyển không hỗ trợ, `shipDays = 999` | `"Không đáp ứng ngày nhận mong muốn"` |
| TC_09 | Giao trễ, `shipDays = 5`, deadline item là 3 ngày | `"Không đáp ứng ngày nhận mong muốn"` |
| TC_10 | Item yêu cầu 0, không có allocation | `null` |

## Ràng buộc

- Không sửa public API; nếu cần chỉ chuẩn hóa thông báo trả về sang tiếng Việt có dấu.
- Không tạo module kiểm thử khác song song.
- Không đụng tới các thay đổi không liên quan trong worktree.
- Tài liệu phải viết bằng tiếng Việt UTF-8.
- Văn phong tài liệu nên tự nhiên, giống báo cáo kỹ thuật, tránh câu quá chung chung.

## Kiểm chứng

Chạy targeted test:

```powershell
.\mvnw.cmd -q -Dtest=DefaultAllocationValidatorTest test
```

Nếu cần kiểm tra toàn bộ project:

```powershell
.\mvnw.cmd -q test
```

Sau khi ghi tài liệu, mở lại file để kiểm tra tiếng Việt có dấu hiển thị đúng.
