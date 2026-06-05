package org.itss.prj_itss.model.request.domain.processing.allocation.policy;

import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.suggestion.ItemVariant;
import org.itss.prj_itss.model.request.domain.processing.suggestion.SuggestedPlan;

import java.util.Comparator;

public final class FastDeliveryObjective implements AllocationObjective {

    @Override
    public String pickTransport(SiteStockOption site, int deadlineDays) {
        if (site.shipDays != null && site.shipDays <= deadlineDays) {
            return DeliveryMethod.SHIP.storageValue();
        }
        if (site.airDays != null && site.airDays <= deadlineDays) {
            return DeliveryMethod.AIR.storageValue();
        }
        return null;
    }

    @Override
    public Comparator<SiteStockOption> siteComparator(ItemRequirement item, int deadlineDays) {
        return Comparator
            .comparingInt((SiteStockOption site) -> transportRank(site, deadlineDays))
            .thenComparing(
                Comparator.comparingInt((SiteStockOption site) -> site.stock.getOrDefault(item.merchandiseId, 0))
                    .reversed()
            )
            .thenComparingInt(site -> site.id);
    }

    @Override
    public Comparator<ItemVariant> itemVariantComparator() {
        return Comparator
            .comparingInt(ItemVariant::siteCount)
            .thenComparingInt(ItemVariant::totalDeliveryDays)
            .thenComparing(ItemVariant::signature);
    }

    @Override
    public Comparator<SuggestedPlan> planComparator() {
        return Comparator
            .comparingInt(SuggestedPlan::siteCount)
            .thenComparingInt(SuggestedPlan::totalDeliveryDays)
            .thenComparingInt(SuggestedPlan::totalLineCount)
            .thenComparing(SuggestedPlan::signature);
    }

    private int transportRank(SiteStockOption site, int deadlineDays) {
        String transport = pickTransport(site, deadlineDays);
        if (DeliveryMethod.SHIP.storageValue().equals(transport)) {
            return 0;
        }
        if (DeliveryMethod.AIR.storageValue().equals(transport)) {
            return 1;
        }
        return 2;
    }
}
