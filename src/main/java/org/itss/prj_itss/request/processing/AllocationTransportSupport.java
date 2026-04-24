package org.itss.prj_itss.request.processing;

import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.service.AllocationPlanningService;

final class AllocationTransportSupport {

    private AllocationTransportSupport() {
    }

    static int getDeliveryDays(SiteStockOption site, String transport) {
        return AllocationPlanningService.TRANSPORT_AIR.equals(transport) ? site.airDays : site.shipDays;
    }

    static String pickDefaultTransport(SiteStockOption site, int deadlineDays) {
        if (site.shipDays <= deadlineDays && site.shipDays < 999) {
            return AllocationPlanningService.TRANSPORT_SHIP;
        }
        if (site.airDays < 999) {
            return AllocationPlanningService.TRANSPORT_AIR;
        }
        return AllocationPlanningService.TRANSPORT_SHIP;
    }

    static String normalizeTransport(String rawTransport, SiteStockOption site, int deadlineDays) {
        if (rawTransport == null || rawTransport.isBlank()) {
            return pickDefaultTransport(site, deadlineDays);
        }

        String normalized = rawTransport.trim().toLowerCase();
        if (normalized.contains("air") || normalized.contains("máy") || normalized.contains("hang khong") || normalized.contains("hàng không")) {
            return AllocationPlanningService.TRANSPORT_AIR;
        }
        if (normalized.contains("ship") || normalized.contains("tàu") || normalized.contains("duong bien") || normalized.contains("đường biển")) {
            return AllocationPlanningService.TRANSPORT_SHIP;
        }

        return pickDefaultTransport(site, deadlineDays);
    }

    static String transportLabel(String transport) {
        return AllocationPlanningService.TRANSPORT_AIR.equals(transport) ? "Hàng không" : "Đường biển";
    }
}
