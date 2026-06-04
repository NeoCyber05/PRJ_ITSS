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

- Đây là bước kiểm tra phân bổ cuối cùng trong luồng `UC_RQ_PROCESS` trước khi người dùng xác nhận tạo đơn hàng, có vai trò nghiệp vụ rõ ràng và ảnh hưởng trực tiếp đến tính đúng đắn của yêu cầu.
- Đây là logic domain thuần Java, không phụ thuộc JavaFX hay database.
- Phương thức có đủ các nhánh nghiệp vụ để áp dụng kiểm thử hộp đen bằng bảng quyết định, và đủ cấu trúc điều khiển để áp dụng kiểm thử hộp trắng bằng đồ thị luồng điều khiển.

Hai kỹ thuật kiểm thử được áp dụng **độc lập**:

1. **Hộp đen — Bảng quyết định (Decision Table):** thiết kế từ đặc tả nghiệp vụ, không nhìn vào code.
2. **Hộp trắng — Luồng điều khiển (Control Flow):** thiết kế từ đồ thị luồng điều khiển (CFG), xác định đường đi độc lập theo phương pháp đường cơ sở của McCabe.

Sau khi thiết kế xong cả hai, tài liệu so sánh hai bộ test case.

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
    Map<Integer, LocalDate> desiredDeliveryDates
)
```

Các đầu vào chính:

| Tham số | Ý nghĩa |
|---|---|
| `items` | Danh sách mặt hàng cần xử lý, mỗi item có mã hàng và số lượng yêu cầu |
| `allSites` | Danh sách site có thể cung cấp hàng, kèm thời gian vận chuyển |
| `allocations` | Dữ liệu phân bổ số lượng hàng cho từng site |
| `desiredDeliveryDates` | Ngày nhận mong muốn theo từng mặt hàng, luôn non-null và không ở quá khứ |

Kết quả trả về:

| Trường hợp | Kết quả |
|---|---|
| Phân bổ hợp lệ | `null` |
| Thiếu số lượng | `"Chưa đủ số lượng hàng cần"` |
| Thừa số lượng | `"Số lượng phân bổ vượt yêu cầu"` |
| Site hoặc thời gian giao hàng không đáp ứng | `"Không đáp ứng ngày nhận mong muốn"` |

## 3. Kiểm thử hộp đen — Bảng quyết định (Decision Table)

Ở phần hộp đen, tôi xét phương thức qua đặc tả nghiệp vụ mà không nhìn vào cấu trúc code bên trong. Kỹ thuật được dùng là bảng quyết định: liệt kê các điều kiện nghiệp vụ, xác định tất cả tổ hợp điều kiện có ý nghĩa, và thiết kế một test case cho mỗi quy tắc.

### 3.1. Xác định điều kiện và hành động

**Điều kiện:**

| Ký hiệu | Điều kiện |
|---|---|
| C1 | Tổng phân bổ < số lượng yêu cầu (`allocated < required`) |
| C2 | Tổng phân bổ > số lượng yêu cầu (`allocated > required`) |
| C3 | Site không tồn tại trong `allSites` (`site == null`) |
| C4 | Phương thức vận chuyển không hỗ trợ (`deliveryDays >= 999`) |
| C5 | Giao hàng trễ hơn ngày mong muốn (`deliveryDays > itemDeadlineDays`) |

Quan hệ phụ thuộc giữa các điều kiện: C1 và C2 loại trừ nhau — một số không thể vừa nhỏ hơn vừa lớn hơn một số khác. C3, C4, C5 chỉ được kiểm tra khi C1 = Không và C2 = Không, vì phương thức trả về sớm nếu C1 hoặc C2 đúng. C4 và C5 được kiểm tra trong cùng điều kiện `||` — khi C4 đúng, C5 không ảnh hưởng đến kết quả.

**Hành động:**

| Ký hiệu | Hành động |
|---|---|
| A1 | Trả về `null` |
| A2 | Trả về `"Chưa đủ số lượng hàng cần"` |
| A3 | Trả về `"Số lượng phân bổ vượt yêu cầu"` |
| A4 | Trả về `"Không đáp ứng ngày nhận mong muốn"` |

### 3.2. Bảng quyết định

| | **R1** | **R2** | **R3** | **R4** | **R5** | **R6** |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| C1: `allocated < required` | Có | Không | Không | Không | Không | Không |
| C2: `allocated > required` | — | Có | Không | Không | Không | Không |
| C3: `site == null` | — | — | Có | Không | Không | Không |
| C4: `deliveryDays >= 999` | — | — | — | Có | Không | Không |
| C5: `deliveryDays > deadline` | — | — | — | — | Có | Không |
| **A1: `null`** | | | | | | **X** |
| **A2: "Chưa đủ..."** | **X** | | | | | |
| **A3: "Thừa..."** | | **X** | | | | |
| **A4: "Không đáp ứng..."** | | | **X** | **X** | **X** | |

Ký hiệu `—` có nghĩa điều kiện không ảnh hưởng đến kết quả vì phương thức đã trả về trước khi kiểm tra điều kiện đó.

Bảng quyết định cho **6 quy tắc → 6 test case hộp đen**.

*Lưu ý:* Trường hợp `items` rỗng không được biểu diễn trong bảng vì khi đó các vòng lặp không chạy và không có điều kiện nào trong C1–C5 được kiểm tra. Đây là trường hợp cấu trúc, không phải quy tắc nghiệp vụ — kiểm thử hộp trắng sẽ phát hiện trường hợp này.

### 3.3. Danh sách test case hộp đen

| Mã TC | Quy tắc | Tên test JUnit | Dữ liệu kiểm thử | Kết quả mong đợi |
|---|:---:|---|---|---|
| DT_01 | R1 | `validateSubmission_shouldReturnMissingQuantityMessage_whenAllocationBelowRequirement` | `required=10`, `allocated=9` | `"Chưa đủ số lượng hàng cần"` |
| DT_02 | R2 | `validateSubmission_shouldReturnExcessQuantityMessage_whenAllocationAboveRequirement` | `required=10`, `allocated=11` | `"Số lượng phân bổ vượt yêu cầu"` |
| DT_03 | R3 | `validateSubmission_shouldReturnDeliveryMessage_whenSiteCannotBeFound` | `siteId=999`, `allSites` chỉ có `id=101` | `"Không đáp ứng ngày nhận mong muốn"` |
| DT_04 | R4 | `validateSubmission_shouldReturnDeliveryMessage_whenTransportIsUnsupported` | `deliveryDays=999` | `"Không đáp ứng ngày nhận mong muốn"` |
| DT_05 | R5 | `validateSubmission_shouldReturnDeliveryMessage_whenDeliveryExceedsDesiredDeadline` | `shipDays=5`, deadline `3` ngày | `"Không đáp ứng ngày nhận mong muốn"` |
| DT_06 | R6 | `validateSubmission_shouldReturnNull_whenFutureDesiredDateCanBeMet` | `airDays=3`, deadline `5` ngày | `null` |

## 4. Kiểm thử hộp trắng — Luồng điều khiển (Control Flow)

Ở phần hộp trắng, tôi đọc mã nguồn `validateSubmission(...)` và xây dựng đồ thị luồng điều khiển (CFG). Từ CFG, tôi tính độ phức tạp vòng theo phương pháp McCabe để xác định số đường đi độc lập cần kiểm thử, sau đó thiết kế một test case cho mỗi đường đi. Bộ test case ở mục này được thiết kế **độc lập** với mục 3.

### 4.1. Đồ thị luồng điều khiển (CFG)

Phương thức có cấu trúc: hai vòng lặp `for` tuần tự duyệt `items`, vòng thứ hai lồng thêm một vòng `for` duyệt `itemAllocations`, và bốn cấu trúc `if` với điều kiện đơn hoặc phức hợp.

**Bảng nút CFG:**

| Nút | Loại | Mô tả (dòng trong source) |
|---|---|---|
| N1 | Xử lý | `plan = AllocationPlan.using(allocations)` (dòng 42) |
| N2 | Quyết định | Điều kiện vòng lặp 1: `items` còn phần tử? (dòng 43) |
| N3 | Xử lý | `allocated = plan.allocatedQuantity(item.merchandiseId)` (dòng 44) |
| N4 | Quyết định | `allocated < item.required` (dòng 45) |
| N5 | Kết thúc | `RETURN "Chưa đủ số lượng hàng cần"` (dòng 46) |
| N6 | Quyết định | `allocated > item.required` (dòng 48) |
| N7 | Kết thúc | `RETURN "Số lượng phân bổ vượt yêu cầu"` (dòng 49) |
| N8 | Quyết định | Điều kiện vòng lặp 2: `items` còn phần tử? (dòng 53) |
| N9 | Xử lý | Tính `desiredDate`, `itemDeadlineDays`, lấy `itemAllocations` (dòng 54–57) |
| N10 | Quyết định | Điều kiện vòng lặp trong: `itemAllocations` còn phần tử? (dòng 58) |
| N11 | Xử lý | `site = allSites.stream().filter(...).findFirst().orElse(null)` (dòng 59–62) |
| N12 | Quyết định | `site == null` (dòng 63) |
| N13 | Kết thúc | `RETURN "Không đáp ứng ngày nhận mong muốn"` (dòng 64) |
| N14 | Xử lý | `deliveryDays = DeliveryOptions.deliveryDays(...)` (dòng 67–70) |
| N15 | Quyết định | `deliveryDays >= 999 \|\| deliveryDays > itemDeadlineDays` (dòng 71) |
| N16 | Kết thúc | `RETURN "Không đáp ứng ngày nhận mong muốn"` (dòng 72) |
| N17 | Kết thúc | `RETURN null` (dòng 77) |

**Các cạnh (luồng điều khiển):**

```
N1  → N2
N2  → N3   (Có: vào thân vòng lặp 1)
N2  → N8   (Không: thoát vòng lặp 1)
N3  → N4
N4  → N5   (Có: allocated < required)
N4  → N6   (Không)
N6  → N7   (Có: allocated > required)
N6  → N2   (Không: lặp tiếp vòng 1)
N8  → N9   (Có: vào thân vòng lặp 2)
N8  → N17  (Không: thoát vòng lặp 2)
N9  → N10
N10 → N11  (Có: vào thân vòng lặp trong)
N10 → N8   (Không: thoát vòng lặp trong, lặp tiếp vòng 2)
N11 → N12
N12 → N13  (Có: site == null)
N12 → N14  (Không)
N14 → N15
N15 → N16  (Có: điều kiện giao hàng vi phạm)
N15 → N10  (Không: lặp tiếp vòng trong)
```

### 4.2. Độ phức tạp vòng và số đường đi độc lập

Các nút quyết định trong CFG: N2, N4, N6, N8, N10, N12, N15 — tổng cộng **7 nút quyết định**.

Độ phức tạp vòng (McCabe):

```
V(G) = số nút quyết định + 1 = 7 + 1 = 8
```

Phương thức cần **8 đường đi độc lập** để đảm bảo độ phủ đường cơ sở.

### 4.3. Các đường đi độc lập

| Mã đường | Chuỗi nút | Điều kiện kích hoạt |
|---|---|---|
| P1 | N1→N2(Không)→N8(Không)→N17 | `items` rỗng — cả hai vòng lặp không chạy |
| P2 | N1→N2(Có)→N3→N4(Có)→N5 | `allocated < required` |
| P3 | N1→N2(Có)→N3→N4(Không)→N6(Có)→N7 | `allocated > required` |
| P4 | N1→N2(Có)→N3→N4(Không)→N6(Không)→N2(Không)→N8(Có)→N9→N10(Không)→N8(Không)→N17 | Số lượng khớp; `itemAllocations` rỗng — vòng lặp trong không chạy |
| P5 | ...→N9→N10(Có)→N11→N12(Có)→N13 | `site == null` |
| P6 | ...→N11→N12(Không)→N14→N15(Có, `deliveryDays >= 999`)→N16 | Vận chuyển không hỗ trợ |
| P7 | ...→N11→N12(Không)→N14→N15(Có, `deliveryDays > deadline`)→N16 | `deliveryDays < 999` nhưng giao trễ hạn |
| P8 | ...→N11→N12(Không)→N14→N15(Không)→N10(Không)→N8(Không)→N17 | Tất cả điều kiện thỏa mãn |

P6 và P7 đều kích hoạt nhánh đúng của N15, nhưng qua hai vế khác nhau của điều kiện `||`, nên cần hai test case riêng.

### 4.4. Danh sách test case hộp trắng

| Mã TC | Đường đi | Tên test JUnit | Dữ liệu kiểm thử | Kết quả mong đợi |
|---|:---:|---|---|---|
| CF_01 | P1 | `validateSubmission_shouldReturnNull_whenItemsAreEmpty` | `items = []` | `null` |
| CF_02 | P2 | `validateSubmission_shouldReturnMissingQuantityMessage_whenAllocationBelowRequirement` | `required=10`, `allocated=9` | `"Chưa đủ số lượng hàng cần"` |
| CF_03 | P3 | `validateSubmission_shouldReturnExcessQuantityMessage_whenAllocationAboveRequirement` | `required=10`, `allocated=11` | `"Số lượng phân bổ vượt yêu cầu"` |
| CF_04 | P4 | `validateSubmission_shouldReturnNull_whenZeroRequirementHasNoAllocations` | `required=0`, `allocations = {}` | `null` |
| CF_05 | P5 | `validateSubmission_shouldReturnDeliveryMessage_whenSiteCannotBeFound` | `siteId=999`, `allSites` chỉ có `id=101` | `"Không đáp ứng ngày nhận mong muốn"` |
| CF_06 | P6 | `validateSubmission_shouldReturnDeliveryMessage_whenTransportIsUnsupported` | `deliveryDays=999` | `"Không đáp ứng ngày nhận mong muốn"` |
| CF_07 | P7 | `validateSubmission_shouldReturnDeliveryMessage_whenDeliveryExceedsDesiredDeadline` | `shipDays=5`, deadline `3` ngày | `"Không đáp ứng ngày nhận mong muốn"` |
| CF_08 | P8 | `validateSubmission_shouldReturnNull_whenFutureDesiredDateCanBeMet` | `airDays=3`, deadline `5` ngày | `null` |

## 5. So sánh hai phương pháp

### 5.1. Bảng đối chiếu test case

| Mã DT | Mã CF tương ứng | Tên JUnit method | Kịch bản |
|---|---|---|---|
| DT_01 | CF_02 | `..._whenAllocationBelowRequirement` | `allocated < required` |
| DT_02 | CF_03 | `..._whenAllocationAboveRequirement` | `allocated > required` |
| DT_03 | CF_05 | `..._whenSiteCannotBeFound` | `site == null` |
| DT_04 | CF_06 | `..._whenTransportIsUnsupported` | `deliveryDays >= 999` |
| DT_05 | CF_07 | `..._whenDeliveryExceedsDesiredDeadline` | `deliveryDays > deadline` |
| DT_06 | CF_08 | `..._whenFutureDesiredDateCanBeMet` | Happy path |
| — | CF_01 | `..._whenItemsAreEmpty` | `items` rỗng |
| — | CF_04 | `..._whenZeroRequirementHasNoAllocations` | Inner loop không chạy |

Sáu test case của hộp đen đều có tương đương trong hộp trắng. Hai test case CF_01 và CF_04 chỉ xuất hiện trong hộp trắng.

### 5.2. So sánh theo tiêu chí

| Tiêu chí | Hộp đen — Decision Table | Hộp trắng — Control Flow |
|---|---|---|
| **Xuất phát từ** | Đặc tả nghiệp vụ | Cấu trúc code (CFG) |
| **Số test case** | 6 | 8 |
| **Độ phủ cam kết** | Mọi tổ hợp điều kiện nghiệp vụ có ý nghĩa | Mọi đường đi độc lập (V(G) = 8) |
| **Phát hiện trường hợp vòng lặp rỗng** | Không — bảng quyết định không mô hình hóa vòng lặp | Có — P1 (`items` rỗng) và P4 (inner loop rỗng) là đường đi riêng |
| **Phụ thuộc vào code** | Không | Có — cần đọc source để vẽ CFG |
| **Tính trực quan với nghiệp vụ** | Cao — quy tắc R1–R6 đối chiếu trực tiếp với đặc tả | Thấp hơn — đường đi P1–P8 bám cấu trúc code |
| **Phát hiện lỗi thiếu điều kiện trong đặc tả** | Có — tổ hợp điều kiện bị bỏ sót sẽ thiếu quy tắc trong bảng | Không trực tiếp |
| **Đảm bảo code được thực thi** | Một phần | Có — mỗi đường đi độc lập được kích hoạt ít nhất một lần |

### 5.3. Nhận xét

Sáu test case của hộp đen phủ đủ sáu quy tắc nghiệp vụ trong bảng quyết định, nhưng để lại hai đường đi cấu trúc không được kiểm tra: vòng lặp không chạy khi `items` rỗng (P1) và vòng lặp trong không chạy khi không có allocation (P4). Hai trường hợp này không gây lỗi nghiệp vụ nếu code đúng, nhưng không được xác nhận bằng test.

Tám test case của hộp trắng phủ toàn bộ CFG, bao gồm cả hai trường hợp cấu trúc trên. Tuy nhiên, hộp trắng không tự động bảo đảm mọi tổ hợp điều kiện nghiệp vụ đã được xem xét — tính đầy đủ đó phụ thuộc vào cách tester chọn dữ liệu cho mỗi đường đi.

Dùng kết hợp cả hai: bảng quyết định xác nhận tính đúng đắn nghiệp vụ, control flow xác nhận tính đúng đắn cấu trúc code. Tổng số JUnit method duy nhất khi hợp nhất hai bộ là **8** — bộ test case hộp trắng là tập cha.

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
- Dùng `@DisplayName` để hiển thị mã test case và mục đích kiểm thử.
- Đặt tên method theo mẫu `methodName_shouldExpectedBehavior_whenScenario`.
- Dùng Arrange - Act - Assert.
- Dùng `assertNull(...)` cho trường hợp hợp lệ.
- Dùng `assertEquals(...)` cho trường hợp trả về thông báo lỗi.
- Thêm message cho assertion để khi test fail có thể hiểu lỗi nhanh hơn.
- Mỗi test case tự chuẩn bị dữ liệu riêng, không phụ thuộc database hoặc JavaFX.

Mapping giữa mã test case và JUnit method (8 method, là union của hai bộ):

| JUnit method | Mã DT | Mã CF |
|---|:---:|:---:|
| `validateSubmission_shouldReturnNull_whenItemsAreEmpty` | — | CF_01 |
| `validateSubmission_shouldReturnMissingQuantityMessage_whenAllocationBelowRequirement` | DT_01 | CF_02 |
| `validateSubmission_shouldReturnExcessQuantityMessage_whenAllocationAboveRequirement` | DT_02 | CF_03 |
| `validateSubmission_shouldReturnNull_whenZeroRequirementHasNoAllocations` | — | CF_04 |
| `validateSubmission_shouldReturnDeliveryMessage_whenSiteCannotBeFound` | DT_03 | CF_05 |
| `validateSubmission_shouldReturnDeliveryMessage_whenTransportIsUnsupported` | DT_04 | CF_06 |
| `validateSubmission_shouldReturnDeliveryMessage_whenDeliveryExceedsDesiredDeadline` | DT_05 | CF_07 |
| `validateSubmission_shouldReturnNull_whenFutureDesiredDateCanBeMet` | DT_06 | CF_08 |

Không cần thêm dependency mới.

## 7. Kết quả thực thi kiểm thử

Kết quả tổng quan:

```
[ĐẠT] CF_01 / danh sách mặt hàng rỗng hợp lệ
[ĐẠT] DT_01 / CF_02 / thiếu số lượng bị từ chối
[ĐẠT] DT_02 / CF_03 / thừa số lượng bị từ chối
[ĐẠT] CF_04 / yêu cầu số lượng 0 chấp nhận phân bổ rỗng
[ĐẠT] DT_03 / CF_05 / site không tồn tại bị từ chối
[ĐẠT] DT_04 / CF_06 / phương thức vận chuyển không hỗ trợ bị từ chối
[ĐẠT] DT_05 / CF_07 / giao trễ hạn bị từ chối
[ĐẠT] DT_06 / CF_08 / ngày nhận tương lai chấp nhận giao kịp hạn

Bộ test: Bộ kiểm thử DefaultAllocationValidator (DefaultAllocationValidatorTest)
Số test chạy: 8, Thất bại: 0, Lỗi: 0, Bỏ qua: 0
Kết quả: ĐẠT
```

Bảng kết quả chi tiết:

| Mã DT | Mã CF | Tên test JUnit | Kết quả thực tế | Ghi chú |
|---|:---:|---|---|---|
| — | CF_01 | `validateSubmission_shouldReturnNull_whenItemsAreEmpty` | **ĐẠT** | Trả về `null` như mong đợi |
| DT_01 | CF_02 | `validateSubmission_shouldReturnMissingQuantityMessage_whenAllocationBelowRequirement` | **ĐẠT** | Trả về `"Chưa đủ số lượng hàng cần"` như mong đợi |
| DT_02 | CF_03 | `validateSubmission_shouldReturnExcessQuantityMessage_whenAllocationAboveRequirement` | **ĐẠT** | Trả về `"Số lượng phân bổ vượt yêu cầu"` như mong đợi |
| — | CF_04 | `validateSubmission_shouldReturnNull_whenZeroRequirementHasNoAllocations` | **ĐẠT** | Trả về `null` như mong đợi |
| DT_03 | CF_05 | `validateSubmission_shouldReturnDeliveryMessage_whenSiteCannotBeFound` | **ĐẠT** | Trả về `"Không đáp ứng ngày nhận mong muốn"` như mong đợi |
| DT_04 | CF_06 | `validateSubmission_shouldReturnDeliveryMessage_whenTransportIsUnsupported` | **ĐẠT** | Trả về `"Không đáp ứng ngày nhận mong muốn"` như mong đợi |
| DT_05 | CF_07 | `validateSubmission_shouldReturnDeliveryMessage_whenDeliveryExceedsDesiredDeadline` | **ĐẠT** | Trả về `"Không đáp ứng ngày nhận mong muốn"` như mong đợi |
| DT_06 | CF_08 | `validateSubmission_shouldReturnNull_whenFutureDesiredDateCanBeMet` | **ĐẠT** | Trả về `null` như mong đợi |

Tất cả 8 test case đều chạy thành công. Bộ test phủ đầy đủ 6 quy tắc trong bảng quyết định (hộp đen) và 8 đường đi độc lập trong CFG (hộp trắng).
