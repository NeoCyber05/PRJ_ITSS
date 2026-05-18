package org.itss.prj_itss.request.domain.allocation.suggester;

import org.itss.prj_itss.request.domain.allocation.model.Allocation;
import org.itss.prj_itss.request.domain.allocation.model.AllocationDraft;
import org.itss.prj_itss.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.request.domain.suggestion.SuggestedPlan;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AllocationSuggester {
    Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        int deadlineDays
    );

    List<SuggestedPlan> buildSuggestedPlans(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        int deadlineDays,
        int limit,
        int maxItemVariants
    );
}
