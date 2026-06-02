package org.itss.prj_itss.model.request.domain.processing.suggestion.algo;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.allocation.policy.AllocationObjective;
import org.itss.prj_itss.model.request.domain.processing.suggestion.SiteSelectionScope;
import org.itss.prj_itss.model.request.domain.processing.suggestion.SuggestedPlan;

import java.util.List;
import java.util.Set;

public final class AllSuggest {

    private final AllocationSuggestEngine engine;

    public AllSuggest(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> selectedSiteIds,
        int deadlineDays,
        AllocationObjective objective
    ) {
        SiteSelectionScope scope = new SiteSelectionScope(allSites, excludedSiteIds, selectedSiteIds);
        this.engine = new AllocationSuggestEngine(items, allSites, scope, objective, deadlineDays);
    }

    public List<SuggestedPlan> buildSuggestedPlans(int limit, int maxItemVariants) {
        return engine.suggestMany(limit, maxItemVariants);
    }
}
