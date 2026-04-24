package org.itss.prj_itss.request.processing;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class RequestProcessingAllocationSupport {

    private RequestProcessingAllocationSupport() {
    }

    static int getAllocated(Map<Integer, Map<Integer, Allocation>> allocations, int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Map.of())
            .values()
            .stream()
            .mapToInt(Allocation::getQuantity)
            .sum();
    }

    static void pruneExcludedAllocations(
        Map<Integer, Map<Integer, Allocation>> allocations,
        Set<Integer> excludedSiteIds
    ) {
        for (Map<Integer, Allocation> itemAllocations : allocations.values()) {
            itemAllocations.keySet().removeIf(excludedSiteIds::contains);
        }
    }

    static Map<Integer, List<Allocation>> groupAllocationsBySite(
        Map<Integer, Map<Integer, Allocation>> allocations
    ) {
        Map<Integer, List<Allocation>> groupedAllocations = new LinkedHashMap<>();
        for (Map<Integer, Allocation> itemAllocations : allocations.values()) {
            for (Allocation allocation : itemAllocations.values()) {
                if (allocation.getQuantity() <= 0) {
                    continue;
                }
                groupedAllocations
                    .computeIfAbsent(allocation.siteId, key -> new ArrayList<>())
                    .add(allocation);
            }
        }
        return groupedAllocations;
    }

    static SiteStockOption findSiteInfo(List<SiteStockOption> allSites, int siteId) {
        for (SiteStockOption site : allSites) {
            if (site.id == siteId) {
                return site;
            }
        }
        return null;
    }

    static ItemRequirement findItem(List<ItemRequirement> items, int merchandiseId) {
        for (ItemRequirement item : items) {
            if (item.merchandiseId == merchandiseId) {
                return item;
            }
        }
        return null;
    }

    static int getDeliveryDays(SiteStockOption site, String transport) {
        return isAirTransport(transport) ? site.airDays : site.shipDays;
    }

    static boolean isAirTransport(String transport) {
        if (transport == null) {
            return false;
        }
        String normalized = transport.trim().toLowerCase();
        return normalized.contains("air")
            || normalized.contains("hàng không")
            || normalized.contains("hang khong")
            || normalized.contains("máy")
            || normalized.contains("may");
    }

    static String toDisplayDeliveryMethod(String transport) {
        return isAirTransport(transport) ? "Hàng không" : "Đường biển";
    }
}
