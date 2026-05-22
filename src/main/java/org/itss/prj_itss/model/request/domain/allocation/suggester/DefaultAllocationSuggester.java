package org.itss.prj_itss.model.request.domain.allocation.suggester;

import org.itss.prj_itss.model.request.domain.allocation.algo.AllSuggestAlgo;
import org.itss.prj_itss.model.request.domain.allocation.algo.OptimalSuggestAlgo;
import org.itss.prj_itss.model.request.domain.allocation.model.AllocationDraft;
import org.itss.prj_itss.model.request.domain.allocation.policy.AllocationPolicy;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.suggestion.SuggestedPlan;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DefaultAllocationSuggester implements AllocationSuggester {
    private final AllocationPolicy allocationPolicy;

    public DefaultAllocationSuggester() {
        this(new AllocationPolicy());
    }

    public DefaultAllocationSuggester(AllocationPolicy allocationPolicy) {
        this.allocationPolicy = Objects.requireNonNull(allocationPolicy, "allocationPolicy");
    }

    @Override
    public Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        int deadlineDays
    ) {
        OptimalSuggestAlgo optimalAlgo = new OptimalSuggestAlgo(
            allSites,
            excludedSiteIds,
            deadlineDays,
            allocationPolicy
        );
        Map<Integer, Map<Integer, AllocationDraft>> draftsByItem = new LinkedHashMap<>();

        for (ItemRequirement item : items) {
            int remaining = item.required;
            Map<Integer, AllocationDraft> draftsBySite = new LinkedHashMap<>();

            for (SiteStockOption site : optimalAlgo.buildCandidateSites(item)) {
                if (remaining <= 0) {
                    break;
                }

                String transport = optimalAlgo.pickSuggestedTransport(site);
                if (transport == null) {
                    continue;
                }

                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                int quantity = Math.min(remaining, stock);
                if (quantity <= 0) {
                    continue;
                }

                draftsBySite.put(site.id, new AllocationDraft(site.id, item.merchandiseId, quantity, transport));
                remaining -= quantity;
            }

            draftsByItem.put(item.merchandiseId, draftsBySite);
        }

        return draftsByItem;
    }

    @Override
    public List<SuggestedPlan> buildSuggestedPlans(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        int deadlineDays,
        int limit,
        int maxItemVariants
    ) {
        return new AllSuggestAlgo(
            items,
            allSites,
            excludedSiteIds,
            prioritySiteIds,
            deadlineDays,
            allocationPolicy
        ).buildSuggestedPlans(limit, maxItemVariants);
    }
}
