package org.itss.prj_itss.model.request.domain.processing.suggestion.algo;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.processing.allocation.policy.AllocationPolicy;

import java.util.List;
import java.util.Set;

/**
 * Thuật toán gợi ý tối ưu theo hướng greedy:
 * ưu tiên site có thời gian giao tốt và lượng tồn kho cao.
 */
public final class OptimalSuggestAlgo {

    private final List<SiteStockOption> allSites;
    private final Set<Integer> excludedSiteIds;
    private final int deadlineDays;
    private final AllocationPolicy allocationPolicy;

    public OptimalSuggestAlgo(
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        int deadlineDays
    ) {
        this(allSites, excludedSiteIds, deadlineDays, new AllocationPolicy());
    }

    public OptimalSuggestAlgo(
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        int deadlineDays,
        AllocationPolicy allocationPolicy
    ) {
        this.allSites = allSites;
        this.excludedSiteIds = excludedSiteIds;
        this.deadlineDays = deadlineDays;
        this.allocationPolicy = allocationPolicy;
    }

    public List<SiteStockOption> buildCandidateSites(ItemRequirement item) {
        return allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
            .filter(site -> pickSuggestedTransport(site) != null)
            .sorted(allocationPolicy.candidateComparator(item, deadlineDays))
            .toList();
    }

    public String pickSuggestedTransport(SiteStockOption site) {
        DeliveryMethod method = allocationPolicy.pickSuggestedTransport(site, deadlineDays);
        return method == null ? null : method.storageValue();
    }
}

