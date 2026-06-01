package org.itss.prj_itss.model.request.domain.processing.allocation.validator;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class DefaultAllocationValidatorTest {

    private final DefaultAllocationValidator validator = new DefaultAllocationValidator();

    // Helper method to create allocations map easily
    private Map<Integer, Map<Integer, Allocation>> buildAllocations(int merchandiseId, Allocation... allocations) {
        Map<Integer, Map<Integer, Allocation>> result = new LinkedHashMap<>();
        Map<Integer, Allocation> siteAllocations = new LinkedHashMap<>();
        for (Allocation allocation : allocations) {
            siteAllocations.put(allocation.siteId, allocation);
        }
        result.put(merchandiseId, siteAllocations);
        return result;
    }

    /**
     * TC_01: testEmptyItems
     * Nhánh rẽ phủ (C1): Vòng lặp items rỗng (1a)
     * Kết quả mong đợi: Trả về null
     */
    @Test
    void testEmptyItems() {
        String result = validator.validateSubmission(
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            7
        );
        assertNull(result);
    }

    /**
     * TC_02: testMissingQuantity
     * Nhánh rẽ phủ (C1): Lượng phân bổ nhỏ hơn yêu cầu (1b, 2a - TRUE)
     * Kết quả mong đợi: Trả về "Chua du so luong hang can"
     */
    @Test
    void testMissingQuantity() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        // Phân bổ 9 sản phẩm, thiếu so với yêu cầu là 10
        Allocation allocation = new Allocation(101, 1, 9, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = buildAllocations(1, allocation);

        String result = validator.validateSubmission(
            List.of(item),
            List.of(),
            allocations,
            Map.of(),
            7
        );
        assertEquals("Chua du so luong hang can", result);
    }

    /**
     * TC_03: testExcessQuantity
     * Nhánh rẽ phủ (C1): Lượng phân bổ lớn hơn yêu cầu (1b, 2b - FALSE, 3a - TRUE)
     * Kết quả mong đợi: Trả về "So luong phan bo vuot yeu cau"
     */
    @Test
    void testExcessQuantity() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        // Phân bổ 11 sản phẩm, thừa so với yêu cầu là 10
        Allocation allocation = new Allocation(101, 1, 11, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = buildAllocations(1, allocation);

        String result = validator.validateSubmission(
            List.of(item),
            List.of(),
            allocations,
            Map.of(),
            7
        );
        assertEquals("So luong phan bo vuot yeu cau", result);
    }

    /**
     * TC_04: testNoDesiredDateDeliverySuccess
     * Nhánh rẽ phủ (C1): desiredDeliveryDates không có thông tin (desiredDate == null) (4a - TRUE), giao kịp hạn mặc định (7b - FALSE)
     * Kết quả mong đợi: Trả về null
     */
    @Test
    void testNoDesiredDateDeliverySuccess() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        // Phân bổ 10 sản phẩm (đúng yêu cầu)
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = buildAllocations(1, allocation);

        // Site 101 hỗ trợ shipDays = 5 (hợp lệ cho deadline 7 ngày)
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(), // desiredDeliveryDates trống -> desiredDate == null
            7 // deadlineDays = 7
        );
        assertNull(result);
    }

    /**
     * TC_05: testDesiredDateInPast
     * Nhánh rẽ phủ (C1): desiredDate ở quá khứ (4b -> 4b.1 - TRUE), itemDeadlineDays = 1, giao hàng kịp hạn (7b - FALSE)
     * Kết quả mong đợi: Trả về null
     */
    @Test
    void testDesiredDateInPast() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.AIR.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = buildAllocations(1, allocation);

        // Site 101 có airDays = 1 (kịp hạn 1 ngày)
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 1, Map.of(1, 10));

        // desiredDate là hôm qua -> itemDeadlineDays sẽ được tính bằng Math.max(1, số_ngày_âm) = 1
        Map<Integer, LocalDate> desiredDates = new HashMap<>();
        desiredDates.put(1, LocalDate.now().minusDays(1));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            desiredDates,
            7
        );
        assertNull(result);
    }

    /**
     * TC_06: testDesiredDateInFutureSuccess
     * Nhánh rẽ phủ (C1): desiredDate ở tương lai (4b -> 4b.2 - FALSE), itemDeadlineDays = 5, giao hàng kịp hạn (7b - FALSE)
     * Kết quả mong đợi: Trả về null
     */
    @Test
    void testDesiredDateInFutureSuccess() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.AIR.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = buildAllocations(1, allocation);

        // Site 101 có airDays = 3 (kịp hạn 5 ngày)
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 3, Map.of(1, 10));

        // desiredDate là 5 ngày sau -> itemDeadlineDays = 5
        Map<Integer, LocalDate> desiredDates = new HashMap<>();
        desiredDates.put(1, LocalDate.now().plusDays(5));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            desiredDates,
            7
        );
        assertNull(result);
    }

    /**
     * TC_07: testSiteNotFound
     * Nhánh rẽ phủ (C1): allocation chỉ định siteId không tồn tại trong allSites (6a - TRUE)
     * Kết quả mong đợi: Trả về "Khong dap ung ngay nhan mong muon"
     */
    @Test
    void testSiteNotFound() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(999, 1, 10, DeliveryMethod.SHIP.storageValue()); // siteId 999
        Map<Integer, Map<Integer, Allocation>> allocations = buildAllocations(1, allocation);

        // allSites chỉ chứa siteId 101
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(),
            7
        );
        assertEquals("Khong dap ung ngay nhan mong muon", result);
    }

    /**
     * TC_08: testDeliveryUnsupportedMethod
     * Nhánh rẽ phủ (C1): deliveryDays >= 999 do site không hỗ trợ giao bằng đường biển (7a - TRUE)
     * Kết quả mong đợi: Trả về "Khong dap ung ngay nhan mong muon"
     */
    @Test
    void testDeliveryUnsupportedMethod() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = buildAllocations(1, allocation);

        // Site 101 có shipDays = 999 (không hỗ trợ giao đường biển)
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 999, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(),
            7
        );
        assertEquals("Khong dap ung ngay nhan mong muon", result);
    }

    /**
     * TC_09: testDeliveryLateThanDeadline
     * Nhánh rẽ phủ (C1): deliveryDays > itemDeadlineDays do trễ hạn giao hàng (7a - TRUE)
     * Kết quả mong đợi: Trả về "Khong dap ung ngay nhan mong muon"
     */
    @Test
    void testDeliveryLateThanDeadline() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = buildAllocations(1, allocation);

        // Site 101 giao mất 5 ngày, trong khi mong muốn là 3 ngày sau
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 2, Map.of(1, 10));

        Map<Integer, LocalDate> desiredDates = new HashMap<>();
        desiredDates.put(1, LocalDate.now().plusDays(3)); // itemDeadlineDays = 3

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            desiredDates,
            7
        );
        assertEquals("Khong dap ung ngay nhan mong muon", result);
    }

    /**
     * TC_10: testNoAllocationsForRequirement
     * Nhánh rẽ phủ (C1): required = 0, allocations trống (vòng lặp allocations không chạy - 5a)
     * Kết quả mong đợi: Trả về null
     */
    @Test
    void testNoAllocationsForRequirement() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 0);
        // Không có phân bổ cho item 1
        Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();

        String result = validator.validateSubmission(
            List.of(item),
            List.of(),
            allocations,
            Map.of(),
            7
        );
        assertNull(result);
    }
}
