package org.itss.prj_itss.model.request.domain.processing.allocation.policy;

import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FastDeliveryObjectiveTest {
    private final FastDeliveryObjective objective = new FastDeliveryObjective();

    @Test
    void prefersShipBeforeAirWhenBothCanMeetDeadline() {
        SiteStockOption shipSite = site(1, 5, 999, 4);
        SiteStockOption airSite = site(2, 999, 1, 50);
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 3);

        List<SiteStockOption> sorted = List.of(airSite, shipSite).stream()
            .sorted(objective.siteComparator(item, 7))
            .toList();

        assertEquals(1, sorted.get(0).id);
        assertEquals(DeliveryMethod.SHIP.storageValue(), objective.pickTransport(shipSite, 7));
    }

    @Test
    void prefersHigherStockWithinSameTransportPriority() {
        SiteStockOption lowStock = site(1, 5, 999, 4);
        SiteStockOption highStock = site(2, 6, 999, 20);
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 3);

        List<SiteStockOption> sorted = List.of(lowStock, highStock).stream()
            .sorted(objective.siteComparator(item, 7))
            .toList();

        assertEquals(2, sorted.get(0).id);
    }

    private SiteStockOption site(int id, int shipDays, int airDays, int stock) {
        return new SiteStockOption(
            id,
            "S" + id,
            "Site " + id,
            "",
            shipDays,
            airDays,
            Map.of(10, stock)
        );
    }
}
