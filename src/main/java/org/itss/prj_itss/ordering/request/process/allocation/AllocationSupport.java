package org.itss.prj_itss.ordering.request.process.allocation;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AllocationSupport {

    private AllocationSupport() {
    }

    public static int getAllocated(Map<Integer, Map<Integer, Allocation>> allocations, int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Map.of())
            .values()
            .stream()
            .mapToInt(Allocation::getQuantity)
            .sum();
    }

    public static void pruneExcludedAllocations(
        Map<Integer, Map<Integer, Allocation>> allocations,
        Set<Integer> excludedSiteIds
    ) {
        for (Map<Integer, Allocation> itemAllocations : allocations.values()) {
            itemAllocations.keySet().removeIf(excludedSiteIds::contains);
        }
    }

    public static Map<Integer, List<Allocation>> groupAllocationsBySite(
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

    public static SiteStockOption findSiteInfo(List<SiteStockOption> allSites, int siteId) {
        for (SiteStockOption site : allSites) {
            if (site.id == siteId) {
                return site;
            }
        }
        return null;
    }

    public static ItemRequirement findItem(List<ItemRequirement> items, int merchandiseId) {
        for (ItemRequirement item : items) {
            if (item.merchandiseId == merchandiseId) {
                return item;
            }
        }
        return null;
    }

    public static int getDeliveryDays(SiteStockOption site, String transport) {
        return isAirTransport(transport) ? site.airDays : site.shipDays;
    }

    public static String pickDefaultTransport(SiteStockOption site, int deadlineDays) {
        if (site.shipDays <= deadlineDays && site.shipDays < 999) {
            return AllocationTransport.SHIP;
        }
        if (site.airDays <= deadlineDays && site.airDays < 999) {
            return AllocationTransport.AIR;
        }
        if (site.shipDays < 999) {
            return AllocationTransport.SHIP;
        }
        if (site.airDays < 999) {
            return AllocationTransport.AIR;
        }
        return AllocationTransport.SHIP;
    }

    public static String normalizeTransport(String rawTransport, SiteStockOption site, int deadlineDays) {
        if (rawTransport == null || rawTransport.isBlank()) {
            return pickDefaultTransport(site, deadlineDays);
        }

        String normalized = rawTransport.trim().toLowerCase();
        if (normalized.contains("air") || normalized.contains("máy") || normalized.contains("may") || normalized.contains("hang khong") || normalized.contains("hàng không")) {
            return AllocationTransport.AIR;
        }
        if (normalized.contains("ship") || normalized.contains("tàu") || normalized.contains("tau") || normalized.contains("duong bien") || normalized.contains("đường biển")) {
            return AllocationTransport.SHIP;
        }

        return pickDefaultTransport(site, deadlineDays);
    }

    public static boolean isAirTransport(String transport) {
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

    public static String toDisplayDeliveryMethod(String transport) {
        return isAirTransport(transport) ? "Hàng không" : "Đường biển";
    }

    public static String transportLabel(String transport) {
        return toDisplayDeliveryMethod(transport);
    }
}
