package org.itss.prj_itss.ordering.request.process.allocation.algo;

import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.model.DeliveryMethod;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Thuáº­t toÃ¡n gá»£i Ã½ tá»‘i Æ°u theo hÆ°á»›ng greedy:
 * Æ°u tiÃªn site cÃ³ thá»i gian giao tá»‘t vÃ  lÆ°á»£ng tá»“n kho cao.
 */
public final class OptimalSuggestAlgo {

    private final List<SiteStockOption> allSites;
    private final Set<Integer> excludedSiteIds;
    private final int deadlineDays;

    public OptimalSuggestAlgo(
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        int deadlineDays
    ) {
        this.allSites = allSites;
        this.excludedSiteIds = excludedSiteIds;
        this.deadlineDays = deadlineDays;
    }

    public List<SiteStockOption> buildCandidateSites(ItemRequirement item) {
        return allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
            .filter(site -> pickSuggestedTransport(site) != null)
            .sorted(buildSiteComparator(item))
            .toList();
    }

    public String pickSuggestedTransport(SiteStockOption site) {
        if (site.shipDays <= deadlineDays && site.shipDays < 999) {
            return DeliveryMethod.SHIP.storageValue();
        }
        if (site.airDays <= deadlineDays && site.airDays < 999) {
            return DeliveryMethod.AIR.storageValue();
        }
        return null;
    }

    private int bestFeasibleDeliveryDays(SiteStockOption site) {
        int best = 999;
        if (site.shipDays <= deadlineDays && site.shipDays < best) {
            best = site.shipDays;
        }
        if (site.airDays <= deadlineDays && site.airDays < best) {
            best = site.airDays;
        }
        return best;
    }

    private Comparator<SiteStockOption> buildSiteComparator(ItemRequirement item) {
        return Comparator
            .comparingInt(this::bestFeasibleDeliveryDays)
            .thenComparing(
                Comparator.comparingInt((SiteStockOption site) -> site.stock.getOrDefault(item.merchandiseId, 0))
                    .reversed()
            )
            .thenComparingInt(site -> site.id);
    }
}
