package org.itss.prj_itss.model.request.domain.processing.suggestion;

import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationDraft;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AllocationSuggester {
    Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> selectedSiteIds,
        int deadlineDays
    );

    List<SuggestedPlan> buildSuggestedPlans(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> selectedSiteIds,
        int deadlineDays,
        int limit,
        int maxItemVariants
    );
}
