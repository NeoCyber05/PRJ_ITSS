package org.itss.prj_itss.request.business.policy;

import org.itss.prj_itss.request.business.model.DeliveryMethod;
import org.itss.prj_itss.request.business.model.ItemRequirement;
import org.itss.prj_itss.request.business.model.SiteStockOption;

import java.util.Comparator;

public final class AllocationPolicy {
    private static final int UNAVAILABLE_DAYS = 999;

    public DeliveryMethod pickSuggestedTransport(SiteStockOption site, int deadlineDays) {
        if (site.shipDays <= deadlineDays && site.shipDays < UNAVAILABLE_DAYS) {
            return DeliveryMethod.SHIP;
        }
        if (site.airDays <= deadlineDays && site.airDays < UNAVAILABLE_DAYS) {
            return DeliveryMethod.AIR;
        }
        return null;
    }

    public Comparator<SiteStockOption> candidateComparator(ItemRequirement item, int deadlineDays) {
        return Comparator
            .comparingInt((SiteStockOption site) -> transportRank(site, deadlineDays))
            .thenComparing(
                Comparator.comparingInt((SiteStockOption site) -> site.stock.getOrDefault(item.merchandiseId, 0))
                    .reversed()
            )
            .thenComparingInt(site -> site.id);
    }

    private int transportRank(SiteStockOption site, int deadlineDays) {
        DeliveryMethod method = pickSuggestedTransport(site, deadlineDays);
        if (method == DeliveryMethod.SHIP) {
            return 0;
        }
        if (method == DeliveryMethod.AIR) {
            return 1;
        }
        return 2;
    }
}
