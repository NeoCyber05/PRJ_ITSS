package org.itss.prj_itss.model.request.domain.processing.suggestion.algo;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationDraft;
import org.itss.prj_itss.model.request.domain.processing.allocation.policy.AllocationObjective;
import org.itss.prj_itss.model.request.domain.processing.suggestion.SiteSelectionScope;
import org.itss.prj_itss.model.request.domain.processing.suggestion.SuggestedPlan;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OptimalSuggest {

    private final List<SiteStockOption> allSites;
    private final Set<Integer> excludedSiteIds;
    private final Set<Integer> selectedSiteIds;
    private final int deadlineDays;
    private final AllocationObjective objective;

    public OptimalSuggest(
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> selectedSiteIds,
        int deadlineDays,
        AllocationObjective objective
    ) {
        this.allSites = allSites;
        this.excludedSiteIds = excludedSiteIds;
        this.selectedSiteIds = selectedSiteIds;
        this.deadlineDays = deadlineDays;
        this.objective = objective;
    }

    public Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(List<ItemRequirement> items) {
        SiteSelectionScope scope = new SiteSelectionScope(allSites, excludedSiteIds, selectedSiteIds);
        AllocationSuggestEngine engine = new AllocationSuggestEngine(items, allSites, scope, objective, deadlineDays);
        List<SuggestedPlan> plans = engine.suggestMany(1, 12);
        if (plans.isEmpty()) {
            return Map.of();
        }
        return plans.get(0).allocationsByItem();
    }
}
