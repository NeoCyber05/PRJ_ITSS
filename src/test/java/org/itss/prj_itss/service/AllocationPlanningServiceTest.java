package org.itss.prj_itss.service;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class AllocationPlanningServiceTest {

    private final AllocationPlanningService service = new AllocationPlanningService();

    @Test
    void picksShipWhenShipMeetsDeadline() {
        SiteStockOption site = site(1, 3, 1, 5);

        assertEquals(AllocationPlanningService.TRANSPORT_SHIP, service.pickSuggestedTransport(site, 5));
    }

    @Test
    void picksAirWhenShipMissesDeadline() {
        SiteStockOption site = site(1, 10, 2, 5);

        assertEquals(AllocationPlanningService.TRANSPORT_AIR, service.pickSuggestedTransport(site, 5));
    }

    @Test
    void returnsNullWhenNoTransportIsAvailable() {
        SiteStockOption site = site(1, 999, 999, 5);

        assertNull(service.pickSuggestedTransport(site, 5));
    }

    @Test
    void allocatesEnoughQuantityAcrossPrioritizedSites() {
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 8);
        SiteStockOption normal = site(1, 3, 1, 10, 5);
        SiteStockOption priority = site(2, 4, 2, 10, 6);

        Map<Integer, Map<Integer, Allocation>> result = service.buildOptimalAllocation(
                List.of(item),
                List.of(normal, priority),
                Set.of(),
                Set.of(priority.id),
                10
        );

        assertEquals(6, result.get(item.merchandiseId).get(priority.id).getQuantity());
        assertEquals(2, result.get(item.merchandiseId).get(normal.id).getQuantity());
    }

    @Test
    void ignoresExcludedSitesAndLeavesShortageUnfilled() {
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 8);
        SiteStockOption excluded = site(1, 3, 1, 10, 8);
        SiteStockOption available = site(2, 4, 2, 10, 3);

        Map<Integer, Map<Integer, Allocation>> result = service.buildOptimalAllocation(
                List.of(item),
                List.of(excluded, available),
                Set.of(excluded.id),
                Set.of(),
                10
        );

        Map<Integer, Allocation> allocations = result.get(item.merchandiseId);
        assertFalse(allocations.containsKey(excluded.id));
        assertEquals(3, allocations.get(available.id).getQuantity());
    }

    private SiteStockOption site(int id, int shipDays, int airDays, int merchandiseId) {
        return site(id, shipDays, airDays, merchandiseId, 10);
    }

    private SiteStockOption site(int id, int shipDays, int airDays, int merchandiseId, int stock) {
        return new SiteStockOption(
                id,
                "S" + id,
                "Site " + id,
                "",
                shipDays,
                airDays,
                Map.of(merchandiseId, stock)
        );
    }
}