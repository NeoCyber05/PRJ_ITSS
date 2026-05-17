package org.itss.prj_itss.request.presentation.ordering.process.site;

import org.itss.prj_itss.request.business.model.SiteStockOption;

import java.util.List;
import java.util.Set;

public final class SiteFilterController {

    private final SiteFilterModel model = new SiteFilterModel();
    private String keyword = "";

    public void init(List<SiteStockOption> allSites) {
        model.setSites(allSites);
        refreshVisibleSites();
    }

    public void search(String keyword) {
        this.keyword = keyword == null ? "" : keyword;
        refreshVisibleSites();
    }

    public void clearAllFilters() {
        model.clearFilters();
        keyword = "";
        refreshVisibleSites();
    }

    public void prioritizeSite(SiteStockOption site) {
        model.prioritize(site);
        refreshVisibleSites();
    }

    public void unprioritizeSite(SiteStockOption site) {
        model.unprioritize(site);
        refreshVisibleSites();
    }

    public void excludeSite(SiteStockOption site) {
        model.exclude(site);
        refreshVisibleSites();
    }

    public void removePriority(int siteId) {
        model.removePriority(siteId);
        refreshVisibleSites();
    }

    public void removeExcluded(int siteId) {
        model.removeExcluded(siteId);
        refreshVisibleSites();
    }

    public List<SiteStockOption> allSites() {
        return model.allSites();
    }

    public List<SiteStockOption> visibleSites() {
        return model.visibleSites();
    }

    public List<SiteStockOption> prioritySites() {
        return model.prioritySites();
    }

    public List<SiteStockOption> excludedSites() {
        return model.excludedSites();
    }

    public Set<Integer> prioritySiteIds() {
        return model.prioritySiteIds();
    }

    public Set<Integer> excludedSiteIds() {
        return model.excludedSiteIds();
    }

    public boolean isPriority(SiteStockOption site) {
        return model.isPriority(site);
    }

    public String keyword() {
        return keyword;
    }

    private void refreshVisibleSites() {
        model.refreshVisibleSites(keyword);
    }
}

