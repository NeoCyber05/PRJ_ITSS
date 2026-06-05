package org.itss.prj_itss.model.request.domain.processing.allocation.validator;

import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Bộ kiểm thử DefaultAllocationValidator")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
final class DefaultAllocationValidatorTest {

    private final DefaultAllocationValidator validator = new DefaultAllocationValidator();

    @RegisterExtension
    private static final ConsoleResultPrinter RESULTS = new ConsoleResultPrinter("DefaultAllocationValidatorTest");

    @Test
    @Order(1)
    @DisplayName("TC_01: danh sách mặt hàng rỗng hợp lệ")
    void validateSubmission_shouldReturnNull_whenItemsAreEmpty() {
        String result = validator.validateSubmission(
            List.of(),
            List.of(),
            Map.of(),
            Map.of()
        );

        assertNull(result, "Yêu cầu không có mặt hàng thì không có phân bổ sai để từ chối");
    }

    @Test
    @Order(2)
    @DisplayName("TC_02: thiếu số lượng bị từ chối")
    void validateSubmission_shouldReturnMissingQuantityMessage_whenAllocationBelowRequirement() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 9, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);

        String result = validator.validateSubmission(
            List.of(item),
            List.of(),
            allocations,
            Map.of()
        );

        assertEquals(
            "Chưa đủ số lượng hàng cần",
            result,
            "Số lượng phân bổ thấp hơn yêu cầu phải bị từ chối"
        );
    }

    @Test
    @Order(3)
    @DisplayName("TC_03: thừa số lượng bị từ chối")
    void validateSubmission_shouldReturnExcessQuantityMessage_whenAllocationAboveRequirement() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 11, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);

        String result = validator.validateSubmission(
            List.of(item),
            List.of(),
            allocations,
            Map.of()
        );

        assertEquals(
            "Số lượng phân bổ vượt yêu cầu",
            result,
            "Số lượng phân bổ cao hơn yêu cầu phải bị từ chối"
        );
    }

    @Test
    @Order(4)
    @DisplayName("TC_06: ngày nhận tương lai chấp nhận giao kịp hạn")
    void validateSubmission_shouldReturnNull_whenFutureDesiredDateCanBeMet() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.AIR.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 3, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(1, LocalDate.now().plusDays(5))
        );

        assertNull(result, "Giao bằng đường hàng không trong 3 ngày phải đáp ứng ngày nhận mong muốn sau 5 ngày");
    }

    @Test
    @Order(5)
    @DisplayName("TC_07: site không tồn tại bị từ chối")
    void validateSubmission_shouldReturnDeliveryMessage_whenSiteCannotBeFound() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(999, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(1, LocalDate.now().plusDays(7))
        );

        assertEquals(
            "Không đáp ứng ngày nhận mong muốn",
            result,
            "Phân bổ tham chiếu tới site không tồn tại phải bị từ chối"
        );
    }

    @Test
    @Order(6)
    @DisplayName("TC_08: phương thức vận chuyển không hỗ trợ bị từ chối")
    void validateSubmission_shouldReturnDeliveryMessage_whenTransportIsUnsupported() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", null, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(1, LocalDate.now().plusDays(7))
        );

        assertEquals(
            "Không đáp ứng ngày nhận mong muốn",
            result,
            "Site không có tuyến đường biển (shipDays == null) phải bị từ chối"
        );
    }

    @Test
    @Order(7)
    @DisplayName("TC_09: giao trễ hạn bị từ chối")
    void validateSubmission_shouldReturnDeliveryMessage_whenDeliveryExceedsDesiredDeadline() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(1, LocalDate.now().plusDays(3))
        );

        assertEquals(
            "Không đáp ứng ngày nhận mong muốn",
            result,
            "Giao bằng đường biển trong 5 ngày không được đáp ứng ngày nhận mong muốn sau 3 ngày"
        );
    }

    @Test
    @Order(8)
    @DisplayName("TC_10: yêu cầu số lượng 0 chấp nhận phân bổ rỗng")
    void validateSubmission_shouldReturnNull_whenZeroRequirementHasNoAllocations() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 0);

        String result = validator.validateSubmission(
            List.of(item),
            List.of(),
            Map.of(),
            Map.of()
        );

        assertNull(result, "Mặt hàng yêu cầu số lượng 0 có thể không cần phân bổ");
    }

    private static Map<Integer, Map<Integer, Allocation>> allocationsFor(
        int merchandiseId,
        Allocation... allocations
    ) {
        Map<Integer, Allocation> siteAllocations = new LinkedHashMap<>();
        for (Allocation allocation : allocations) {
            siteAllocations.put(allocation.siteId, allocation);
        }

        Map<Integer, Map<Integer, Allocation>> result = new LinkedHashMap<>();
        result.put(merchandiseId, siteAllocations);
        return result;
    }

    private static final class ConsoleResultPrinter implements TestWatcher, AfterAllCallback {
        private final String testClassName;
        private int passed;
        private int failures;
        private int errors;
        private int skipped;

        private ConsoleResultPrinter(String testClassName) {
            this.testClassName = testClassName;
        }

        @Override
        public void testSuccessful(ExtensionContext context) {
            passed++;
            System.out.println("[ĐẠT] " + context.getDisplayName());
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            if (cause instanceof AssertionError) {
                failures++;
                System.out.println("[KHÔNG ĐẠT] " + context.getDisplayName() + " -> " + cause.getMessage());
                return;
            }
            errors++;
            System.out.println("[LỖI] " + context.getDisplayName() + " -> " + cause.getMessage());
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            skipped++;
            System.out.println("[BỎ QUA] " + context.getDisplayName() + " -> " + cause.getMessage());
        }

        @Override
        public void testDisabled(ExtensionContext context, Optional<String> reason) {
            skipped++;
            String suffix = reason.map(value -> " -> " + value).orElse("");
            System.out.println("[BỎ QUA] " + context.getDisplayName() + suffix);
        }

        @Override
        public void afterAll(ExtensionContext context) {
            int testsRun = passed + failures + errors + skipped;
            System.out.println();
            System.out.println("Bộ test: " + context.getDisplayName() + " (" + testClassName + ")");
            System.out.println(
                "Số test chạy: " + testsRun
                    + ", Thất bại: " + failures
                    + ", Lỗi: " + errors
                    + ", Bỏ qua: " + skipped
            );
            System.out.println(failures == 0 && errors == 0 ? "Kết quả: ĐẠT" : "Kết quả: KHÔNG ĐẠT");
        }
    }
}
