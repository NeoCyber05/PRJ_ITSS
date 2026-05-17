package org.itss.prj_itss.request.business.model;

public final class DeliveryOptions {

    private DeliveryOptions() {
    }

    public static int deliveryDays(SiteStockOption site, DeliveryMethod method) {
        return method == DeliveryMethod.AIR ? site.airDays : site.shipDays;
    }

    public static DeliveryMethod pickDefault(SiteStockOption site, int deadlineDays) {
        if (site.shipDays <= deadlineDays && site.shipDays < 999) {
            return DeliveryMethod.SHIP;
        }
        if (site.airDays <= deadlineDays && site.airDays < 999) {
            return DeliveryMethod.AIR;
        }
        if (site.shipDays < 999) {
            return DeliveryMethod.SHIP;
        }
        if (site.airDays < 999) {
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

