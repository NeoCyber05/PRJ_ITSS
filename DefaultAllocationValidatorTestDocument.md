# TÀI LIỆU THIẾT KẾ TEST CASE CHO DEFAULTALLOCATIONVALIDATOR

Tài liệu này trình bày chi tiết việc áp dụng kỹ thuật kiểm thử hộp đen và kiểm thử hộp trắng (độ đo quyết định C1) để thiết kế các ca kiểm thử (test cases) cho phương thức `validateSubmission` thuộc lớp `org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidator`.

Tên đầy đủ (full name) của lớp kiểm thử tự động:
**`org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidatorTest`**

---

## 1. Mô tả lớp và phương thức kiểm thử

### Lớp kiểm thử: `DefaultAllocationValidator`
Lớp này hiện thực hóa interface `AllocationValidator`, chịu trách nhiệm kiểm tra tính hợp lệ của việc phân bổ hàng hóa cho các yêu cầu đặt hàng.

### Phương thức kiểm thử: `validateSubmission`
Phương thức này thực hiện xác thực toàn bộ việc phân bổ hàng hóa trước khi gửi đi. Phương thức trả về:
- `null`: Nếu việc phân bổ hoàn toàn hợp lệ.
- Một thông điệp lỗi (`String`): Nếu phát hiện bất kỳ vi phạm nào.

#### Chữ ký phương thức:
```java
public String validateSubmission(
    List<ItemRequirement> items,
    List<SiteStockOption> allSites,
    Map<Integer, Map<Integer, Allocation>> allocations,
    Map<Integer, LocalDate> desiredDeliveryDates,
    int deadlineDays
)
```

#### Mã nguồn chi tiết của phương thức:
```java
public String validateSubmission(
    List<ItemRequirement> items,
    List<SiteStockOption> allSites,
    Map<Integer, Map<Integer, Allocation>> allocations,
    Map<Integer, LocalDate> desiredDeliveryDates,
    int deadlineDays
) {
    AllocationPlan plan = AllocationPlan.using(allocations);
    for (ItemRequirement item : items) {
        int allocated = plan.allocatedQuantity(item.merchandiseId);
        if (allocated < item.required) {
            return "Chua du so luong hang can"; // Nhánh [1]
        }
        if (allocated > item.required) {
            return "So luong phan bo vuot yeu cau"; // Nhánh [2]
        }
    }

    for (ItemRequirement item : items) {
        LocalDate desiredDate = desiredDeliveryDates.get(item.merchandiseId);
        int itemDeadlineDays = desiredDate == null
            ? deadlineDays // Nhánh [3]
            : Math.max(1, (int) ChronoUnit.DAYS.between(LocalDate.now(), desiredDate)); // Nhánh [4]

        Map<Integer, Allocation> itemAllocations = allocations.getOrDefault(item.merchandiseId, Map.of());
        for (Allocation allocation : itemAllocations.values()) {
            SiteStockOption site = allSites.stream()
                .filter(candidate -> candidate.id == allocation.siteId)
                .findFirst()
                .orElse(null);
            if (site == null) {
                return "Khong dap ung ngay nhan mong muon"; // Nhánh [5]
            }

            int deliveryDays = DeliveryOptions.deliveryDays(
                site,
                DeliveryOptions.resolve(site, allocation.transport, itemDeadlineDays)
            );
            if (deliveryDays >= 999 || deliveryDays > itemDeadlineDays) {
                return "Khong dap ung ngay nhan mong muon"; // Nhánh [6]
            }
        }
    }

    return null; // Nhánh [7]
}
```

---

## 2. Phân tích Kỹ thuật Kiểm thử Hộp đen (Black-box Testing)

Áp dụng kỹ thuật **Phân vùng tương đương (Equivalence Partitioning)** và **Phân tích giá trị biên (Boundary Value Analysis)** cho các tham số đầu vào.

### A. Xác thực số lượng phân bổ (Quantity Validation)
Ta xét tương quan giữa tổng số lượng được phân bổ (`allocated`) và số lượng yêu cầu (`required`):
- **Phân vùng hợp lệ (Valid Partition):**
  - $P_{Q1}$: `allocated` == `required` cho tất cả các mặt hàng.
- **Phân vùng không hợp lệ (Invalid Partitions):**
  - $P_{Q2}$ (Thiếu số lượng): Có ít nhất một mặt hàng có `allocated` < `required`.
    - *Giá trị biên tiêu biểu*: `allocated = required - 1`.
  - $P_{Q3}$ (Thừa số lượng): Có ít nhất một mặt hàng có `allocated` > `required`.
    - *Giá trị biên tiêu biểu*: `allocated = required + 1`.

### B. Xác thực thời gian giao hàng (Delivery Date and Method Validation)
Ta xét tương quan giữa ngày giao hàng thực tế của site (`deliveryDays`) và thời hạn nhận hàng mong muốn (`itemDeadlineDays`):
- **Thời hạn nhận hàng (`itemDeadlineDays`):**
  - Được xác định bởi `deadlineDays` (nếu ngày mong muốn là `null`).
  - Được tính toán từ `desiredDate` (nếu ngày mong muốn khác `null`).
- **Phân vùng hợp lệ (Valid Partition):**
  - $P_{D1}$ (Giao kịp hạn): `deliveryDays` $\le$ `itemDeadlineDays` và `deliveryDays` < 999.
    - *Giá trị biên tiêu biểu*: `deliveryDays = itemDeadlineDays` (kịp khít khao), `deliveryDays = itemDeadlineDays - 1` (kịp sớm).
- **Phân vùng không hợp lệ (Invalid Partitions):**
  - $P_{D2}$ (Giao trễ hạn): `deliveryDays` > `itemDeadlineDays`.
    - *Giá trị biên tiêu biểu*: `deliveryDays = itemDeadlineDays + 1`.
  - $P_{D3}$ (Không hỗ trợ/Lỗi): `deliveryDays >= 999` (Kho không hỗ trợ phương thức vận chuyển được chọn).
  - $P_{D4}$ (Không tìm thấy Kho): Phân bổ hàng tại một kho (`siteId`) không tồn tại trong hệ thống.

---

## 3. Phân tích Kỹ thuật Kiểm thử Hộp trắng (White-box Testing - Độ đo C1)

Độ đo quyết định (C1 - Branch/Decision Coverage) yêu cầu bộ kiểm thử phải đi qua tất cả các nhánh rẽ trong luồng điều khiển của chương trình.

### Các điểm quyết định và các nhánh rẽ cần phủ:

1. **Điểm quyết định 1 (Vòng lặp số lượng `items`):**
   - **Nhánh 1a:** Danh sách `items` trống (vòng lặp không thực hiện).
   - **Nhánh 1b:** Danh sách `items` không trống (vòng lặp chạy).

2. **Điểm quyết định 2 (`if (allocated < item.required)`):**
   - **Nhánh 2a (TRUE):** Lượng phân bổ nhỏ hơn yêu cầu $\rightarrow$ trả về `"Chua du so luong hang can"`.
   - **Nhánh 2b (FALSE):** Lượng phân bổ lớn hơn hoặc bằng yêu cầu.

3. **Điểm quyết định 3 (`if (allocated > item.required)`):**
   - **Nhánh 3a (TRUE):** Lượng phân bổ lớn hơn yêu cầu $\rightarrow$ trả về `"So luong phan bo vuot yeu cau"`.
   - **Nhánh 3b (FALSE):** Lượng phân bổ đúng bằng yêu cầu.

4. **Điểm quyết định 4 (`desiredDate == null`):**
   - **Nhánh 4a (TRUE):** `desiredDate` là `null` $\rightarrow$ `itemDeadlineDays = deadlineDays`.
   - **Nhánh 4b (FALSE):** `desiredDate` khác `null` $\rightarrow$ tính `itemDeadlineDays` theo khoảng cách ngày từ hôm nay.
     - *Nhánh con 4b.1:* Khoảng cách ngày $\le$ 0 $\rightarrow$ `itemDeadlineDays` lấy giá trị `1` (do hàm `Math.max(1, ...)`).
     - *Nhánh con 4b.2:* Khoảng cách ngày > 0 $\rightarrow$ `itemDeadlineDays` lấy giá trị khoảng cách thực tế.

5. **Điểm quyết định 5 (Vòng lặp duyệt phân bổ `itemAllocations`):**
   - **Nhánh 5a:** Không có phân bổ nào cho item (vòng lặp không chạy).
   - **Nhánh 5b:** Có phân bổ (vòng lặp chạy).

6. **Điểm quyết định 6 (`if (site == null)`):**
   - **Nhánh 6a (TRUE):** Site không tồn tại $\rightarrow$ trả về `"Khong dap ung ngay nhan mong muon"`.
   - **Nhánh 6b (FALSE):** Site tồn tại trong hệ thống.

7. **Điểm quyết định 7 (`if (deliveryDays >= 999 || deliveryDays > itemDeadlineDays)`):**
   - **Nhánh 7a (TRUE):** Trễ hạn hoặc kho không hỗ trợ phương thức vận chuyển $\rightarrow$ trả về `"Khong dap ung ngay nhan mong muon"`.
   - **Nhánh 7b (FALSE):** Đáp ứng thời gian giao hàng hợp lệ.

---

## 4. Danh sách các Test Case đã thiết kế

Dưới đây là bảng tổng hợp các test case được thiết kế bằng cách kết hợp cả hai phương pháp để đạt độ phủ C1 tối đa:

| Mã Test Case | Tên Test Case | Kỹ thuật áp dụng | Mô tả & Dữ liệu kiểm thử | Nhánh rẽ phủ (C1) | Kết quả mong đợi |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_01** | `testEmptyItems` | Hộp trắng | `items` là một danh sách rỗng (`List.of()`). | 1a | `null` |
| **TC_02** | `testMissingQuantity` | Hộp đen + Hộp trắng | Có 1 item yêu cầu 10 sản phẩm, nhưng tổng phân bổ thực tế chỉ có 9 sản phẩm (biên dưới). | 1b, 2a (TRUE) | `"Chua du so luong hang can"` |
| **TC_03** | `testExcessQuantity` | Hộp đen + Hộp trắng | Có 1 item yêu cầu 10 sản phẩm, nhưng tổng phân bổ thực tế là 11 sản phẩm (biên trên). | 1b, 2b (FALSE), 3a (TRUE) | `"So luong phan bo vuot yeu cau"` |
| **TC_04** | `testNoDesiredDateDeliverySuccess` | Hộp trắng + Hộp đen | `desiredDate` là `null`, `deadlineDays` = 7. Site có thời gian vận chuyển bằng đường biển là 5 ngày, phân bổ 5 sản phẩm qua đường biển (hợp lệ). | 1b, 2b (FALSE), 3b (FALSE), 4a (TRUE), 5b, 6b, 7b | `null` |
| **TC_05** | `testDesiredDateInPast` | Hộp trắng | `desiredDate` ở quá khứ (ví dụ: hôm qua). Khoảng cách $\le$ 0 ngày. `itemDeadlineDays` sẽ tự động chuyển thành 1. Phân bổ sử dụng phương thức vận chuyển có thời gian giao hàng là 1 ngày (hợp lệ). | 1b, 2b (FALSE), 3b (FALSE), 4b (FALSE) $\rightarrow$ 4b.1, 5b, 6b, 7b | `null` |
| **TC_06** | `testDesiredDateInFutureSuccess` | Hộp đen | `desiredDate` trong tương lai (sau 5 ngày). `itemDeadlineDays` = 5. Site giao hàng trong 3 ngày qua đường hàng không (hợp lệ). | 1b, 2b (FALSE), 3b (FALSE), 4b (FALSE) $\rightarrow$ 4b.2, 5b, 6b, 7b | `null` |
| **TC_07** | `testSiteNotFound` | Hộp đen + Hộp trắng | Allocation chỉ định `siteId` = 999 không tồn tại trong danh sách `allSites`. | 1b, 2b (FALSE), 3b (FALSE), 4a, 5b, 6a (TRUE) | `"Khong dap ung ngay nhan mong muon"` |
| **TC_08** | `testDeliveryUnsupportedMethod` | Hộp đen + Hộp trắng | Site có `shipDays` = 999 (không hỗ trợ giao bằng đường biển). Allocation yêu cầu giao bằng `"ship"`. | 1b, 2b (FALSE), 3b (FALSE), 4a, 5b, 6b, 7a (TRUE - `deliveryDays >= 999`) | `"Khong dap ung ngay nhan mong muon"` |
| **TC_09** | `testDeliveryLateThanDeadline` | Hộp đen + Hộp trắng | `desiredDate` sau 3 ngày (`itemDeadlineDays` = 3). Site giao hàng mất 5 ngày (`shipDays` = 5). Giao hàng bị trễ hạn. | 1b, 2b (FALSE), 3b (FALSE), 4b, 5b, 6b, 7a (TRUE - `deliveryDays > itemDeadlineDays`) | `"Khong dap ung ngay nhan mong muon"` |
| **TC_10** | `testNoAllocationsForRequirement` | Hộp trắng | `items` chứa 1 sản phẩm yêu cầu số lượng là 0. Danh sách phân bổ trống (`allocations` trống). | 1b, 2b (FALSE), 3b (FALSE), 4a, 5a | `null` |

---

## 5. Kết luận
Bằng việc kết hợp kỹ thuật phân vùng tương đương, phân tích giá trị biên của hộp đen cùng với việc dò các nhánh rẽ trong luồng điều khiển của hộp trắng, bộ test cases gồm **10 trường hợp** trên đảm bảo độ phủ nhánh (C1) đạt **100%** và kiểm thử đầy đủ các điều kiện biên nghiệp vụ của phương thức `validateSubmission`.
