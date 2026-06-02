package org.itss.prj_itss.model.request.domain.processing.suggestion;

import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

import java.util.List;
import java.util.Set;

public final class SiteSelectionScope {
    private final List<SiteStockOption> allSites;
    private final Set<Integer> excludedSiteIds;
    private final Set<Integer> selectedSiteIds;

    public SiteSelectionScope(
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> selectedSiteIds
    ) {
        this.allSites = allSites;
        this.excludedSiteIds = excludedSiteIds;
        this.selectedSiteIds = selectedSiteIds;
    }

    public List<SiteStockOption> candidateSites() {
        if (selectedSiteIds.isEmpty()) {
            return allSites.stream()
                .filter(site -> !excludedSiteIds.contains(site.id))
                .toList();
        }
        return allSites.stream()
            .filter(site -> selectedSiteIds.contains(site.id))
            .filter(site -> !excludedSiteIds.contains(site.id))
            .toList();
    }
}
