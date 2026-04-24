package org.itss.prj_itss.service;

import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AllocationPlanningService {

    public static final String TRANSPORT_SHIP = "ship";
    public static final String TRANSPORT_AIR = "air";

    public String pickSuggestedTransport(SiteStockOption site, int deadlineDays) {
        if (site.shipDays <= deadlineDays && site.shipDays < 999) {
            return TRANSPORT_SHIP;
        }
        if (site.airDays < 999) {
            return TRANSPORT_AIR;
        }
        return null;
    }

    public Map<Integer, Map<Integer, Allocation>> buildOptimalAllocation(
        List<ItemRequirement> items,
        List<SiteStockOption> sites,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        int deadlineDays
    ) {
        Map<Integer, Map<Integer, Allocation>> result = new LinkedHashMap<>();
        for (ItemRequirement item : items) {
            int remaining = item.required;
            Map<Integer, Allocation> itemAllocations = new LinkedHashMap<>();

            for (SiteStockOption site : sortedCandidateSites(item, sites, excludedSiteIds, prioritySiteIds, deadlineDays)) {
                if (remaining <= 0) {
                    break;
                }

                String transport = pickSuggestedTransport(site, deadlineDays);
                if (transport == null) {
                    continue;
                }

                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                int quantity = Math.min(remaining, stock);
                if (quantity > 0) {
                    itemAllocations.put(site.id, new Allocation(site.id, item.merchandiseId, quantity, transport));
                    remaining -= quantity;
                }
            }

            result.put(item.merchandiseId, itemAllocations);
        }
        return result;
    }

    private List<SiteStockOption> sortedCandidateSites(
        ItemRequirement item,
        List<SiteStockOption> sites,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        int deadlineDays
    ) {
        return sites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
            .filter(site -> pickSuggestedTransport(site, deadlineDays) != null)
            .sorted(Comparator
                .comparing((SiteStockOption site) -> !prioritySiteIds.contains(site.id))
                .thenComparingInt(site -> bestDeliveryDays(site, deadlineDays))
                .thenComparing(Comparator.comparingInt(
                    (SiteStockOption site) -> site.stock.getOrDefault(item.merchandiseId, 0)
                ).reversed())
                .thenComparing(site -> site.name == null ? "" : site.name))
            .toList();
    }

    private int bestDeliveryDays(SiteStockOption site, int deadlineDays) {
        String transport = pickSuggestedTransport(site, deadlineDays);
        if (TRANSPORT_AIR.equals(transport)) {
            return site.airDays;
        }
        return site.shipDays;
    }
}
