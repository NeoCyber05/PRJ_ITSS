package org.itss.prj_itss.controller.ordering.request.process.site;

import org.itss.prj_itss.controller.ordering.request.process.state.ProcessingSiteState;
import org.itss.prj_itss.controller.ordering.request.process.state.SiteFilterState;

import java.util.List;
import java.util.Set;

public final class SiteFilterController {

    private final SiteFilterState model = new SiteFilterState();
    private String keyword = "";

    public void init(List<ProcessingSiteState> allSites) {
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

    public void selectSite(ProcessingSiteState site) {
        model.select(site);
        refreshVisibleSites();
    }

    public void deselectSite(ProcessingSiteState site) {
        model.deselect(site);
        refreshVisibleSites();
    }

    public void excludeSite(ProcessingSiteState site) {
        model.exclude(site);
        refreshVisibleSites();
    }

    public void removeSelected(int siteId) {
        model.removeSelected(siteId);
        refreshVisibleSites();
    }

    public void removeExcluded(int siteId) {
        model.removeExcluded(siteId);
        refreshVisibleSites();
    }

    public List<ProcessingSiteState> allSites() {
        return model.allSites();
    }

    public List<ProcessingSiteState> visibleSites() {
        return model.visibleSites();
    }

    public List<ProcessingSiteState> selectedSites() {
        return model.selectedSites();
    }

    public List<ProcessingSiteState> excludedSites() {
        return model.excludedSites();
    }

    public Set<Integer> selectedSiteIds() {
        return model.selectedSiteIds();
    }

    public Set<Integer> excludedSiteIds() {
        return model.excludedSiteIds();
    }

    public boolean isSelected(ProcessingSiteState site) {
        return model.isSelected(site);
    }

    public String keyword() {
        return keyword;
    }

    private void refreshVisibleSites() {
        model.refreshVisibleSites(keyword);
    }
}
