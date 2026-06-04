package org.itss.prj_itss.model.request.domain.processing.suggestion;

import org.itss.prj_itss.model.request.domain.processing.suggestion.algo.AllSuggest;
import org.itss.prj_itss.model.request.domain.processing.suggestion.algo.OptimalSuggest;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationDraft;
import org.itss.prj_itss.model.request.domain.processing.allocation.policy.AllocationObjective;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DefaultAllocationSuggester implements AllocationSuggester {
    private final AllocationObjective objective;

    public DefaultAllocationSuggester(AllocationObjective objective) {
        this.objective = Objects.requireNonNull(objective, "objective");
    }

    @Override
    public Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> selectedSiteIds,
        int deadlineDays
    ) {
        return new OptimalSuggest(
            allSites,
            excludedSiteIds,
            selectedSiteIds,
            deadlineDays,
            objective
        ).buildOptimalDrafts(items);
    }

    @Override
    public List<SuggestedPlan> buildSuggestedPlans(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> selectedSiteIds,
        int deadlineDays,
        int limit,
        int maxItemVariants
    ) {
        return new AllSuggest(
            items,
            allSites,
            excludedSiteIds,
            selectedSiteIds,
            deadlineDays,
            objective
        ).buildSuggestedPlans(limit, maxItemVariants);
    }
}
