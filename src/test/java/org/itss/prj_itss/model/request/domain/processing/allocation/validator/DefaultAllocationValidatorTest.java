package org.itss.prj_itss.model.request.domain.processing.allocation.validator;

import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("DefaultAllocationValidator")
final class DefaultAllocationValidatorTest {

    private final DefaultAllocationValidator validator = new DefaultAllocationValidator();

    @Test
    @DisplayName("TC_01: empty items are valid")
    void validateSubmission_shouldReturnNull_whenItemsAreEmpty() {
        String result = validator.validateSubmission(
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            7
        );

        assertNull(result, "A request with no items has no invalid allocation to reject");
    }

    @Test
    @DisplayName("TC_02: missing quantity is rejected")
    void validateSubmission_shouldReturnMissingQuantityMessage_whenAllocationBelowRequirement() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 9, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);

        String result = validator.validateSubmission(
            List.of(item),
            List.of(),
            allocations,
            Map.of(),
            7
        );

        assertEquals(
            "Chua du so luong hang can",
            result,
            "Allocated quantity below the requirement must be rejected"
        );
    }

    @Test
    @DisplayName("TC_03: excess quantity is rejected")
    void validateSubmission_shouldReturnExcessQuantityMessage_whenAllocationAboveRequirement() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 11, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);

        String result = validator.validateSubmission(
            List.of(item),
            List.of(),
            allocations,
            Map.of(),
            7
        );

        assertEquals(
            "So luong phan bo vuot yeu cau",
            result,
            "Allocated quantity above the requirement must be rejected"
        );
    }

    @Test
    @DisplayName("TC_04: default deadline accepts on-time delivery")
    void validateSubmission_shouldReturnNull_whenNoDesiredDateAndDeliveryMeetsDefaultDeadline() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(),
            7
        );

        assertNull(result, "Ship delivery in 5 days should satisfy the default 7-day deadline");
    }

    @Test
    @DisplayName("TC_05: past desired date falls back to one-day deadline")
    void validateSubmission_shouldReturnNull_whenPastDesiredDateFallsBackToOneDayDeadline() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.AIR.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 1, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(1, LocalDate.now().minusDays(1)),
            7
        );

        assertNull(result, "Air delivery in 1 day should satisfy the minimum one-day deadline");
    }

    @Test
    @DisplayName("TC_06: future desired date accepts on-time delivery")
    void validateSubmission_shouldReturnNull_whenFutureDesiredDateCanBeMet() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.AIR.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 3, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(1, LocalDate.now().plusDays(5)),
            7
        );

        assertNull(result, "Air delivery in 3 days should satisfy a desired date 5 days ahead");
    }

    @Test
    @DisplayName("TC_07: unknown site is rejected")
    void validateSubmission_shouldReturnDeliveryMessage_whenSiteCannotBeFound() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(999, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(),
            7
        );

        assertEquals(
            "Khong dap ung ngay nhan mong muon",
            result,
            "An allocation referencing a missing site must be rejected"
        );
    }

    @Test
    @DisplayName("TC_08: unsupported transport is rejected")
    void validateSubmission_shouldReturnDeliveryMessage_whenTransportIsUnsupported() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 999, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(),
            7
        );

        assertEquals(
            "Khong dap ung ngay nhan mong muon",
            result,
            "A transport option with deliveryDays >= 999 is treated as unsupported"
        );
    }

    @Test
    @DisplayName("TC_09: late delivery is rejected")
    void validateSubmission_shouldReturnDeliveryMessage_whenDeliveryExceedsDesiredDeadline() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 10);
        Allocation allocation = new Allocation(101, 1, 10, DeliveryMethod.SHIP.storageValue());
        Map<Integer, Map<Integer, Allocation>> allocations = allocationsFor(1, allocation);
        SiteStockOption site = new SiteStockOption(101, "S101", "Site 101", "", 5, 2, Map.of(1, 10));

        String result = validator.validateSubmission(
            List.of(item),
            List.of(site),
            allocations,
            Map.of(1, LocalDate.now().plusDays(3)),
            7
        );

        assertEquals(
            "Khong dap ung ngay nhan mong muon",
            result,
            "Ship delivery in 5 days should not satisfy a desired date 3 days ahead"
        );
    }

    @Test
    @DisplayName("TC_10: zero requirement accepts empty allocations")
    void validateSubmission_shouldReturnNull_whenZeroRequirementHasNoAllocations() {
        ItemRequirement item = new ItemRequirement(1, "M01", "Item 1", 0);

        String result = validator.validateSubmission(
            List.of(item),
            List.of(),
            Map.of(),
            Map.of(),
            7
        );

        assertNull(result, "An item requiring zero quantity can have no allocation");
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
}
