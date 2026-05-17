package org.itss.prj_itss.request.business.port;

import org.itss.prj_itss.request.business.allocation.algo.AllSuggestAlgo.AllocationDraft;
import org.itss.prj_itss.request.business.allocation.algo.AllSuggestAlgo.SuggestedPlan;
import org.itss.prj_itss.request.business.model.ItemRequirement;
import org.itss.prj_itss.request.business.model.SiteStockOption;

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
