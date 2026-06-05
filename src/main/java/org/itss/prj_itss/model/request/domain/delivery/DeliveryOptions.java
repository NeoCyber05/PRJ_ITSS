package org.itss.prj_itss.model.request.domain.delivery;

import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

public final class DeliveryOptions {

    private DeliveryOptions() {
    }

    public static Integer deliveryDays(SiteStockOption site, DeliveryMethod method) {
        return method == DeliveryMethod.AIR ? site.airDays : site.shipDays;
    }

    public static DeliveryMethod pickDefault(SiteStockOption site, int deadlineDays) {
        if (site.shipDays != null && site.shipDays <= deadlineDays) {
            return DeliveryMethod.SHIP;
        }
        if (site.airDays != null && site.airDays <= deadlineDays) {
            return DeliveryMethod.AIR;
        }
        if (site.shipDays != null) {
            return DeliveryMethod.SHIP;
        }
        if (site.airDays != null) {
            return DeliveryMethod.AIR;
        }
        return DeliveryMethod.SHIP;
    }

    public static DeliveryMethod resolve(SiteStockOption site, String rawTransport, int deadlineDays) {
        DeliveryMethod parsed = DeliveryMethod.fromRaw(rawTransport);
        return parsed != null ? parsed : pickDefault(site, deadlineDays);
    }

    public static String resolveStorageValue(SiteStockOption site, String rawTransport, int deadlineDays) {
        return resolve(site, rawTransport, deadlineDays).storageValue();
    }
}

