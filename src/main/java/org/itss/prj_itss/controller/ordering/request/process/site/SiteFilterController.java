package org.itss.prj_itss.controller.ordering.request.process.site;

import org.itss.prj_itss.view.ordering.request.process.state.ProcessingSiteView;
import org.itss.prj_itss.view.ordering.request.process.state.SiteFilterModel;

import java.util.List;
import java.util.Set;

public final class SiteFilterController {

    private final SiteFilterModel model = new SiteFilterModel();
    private String keyword = "";

    public void init(List<ProcessingSiteView> allSites) {
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

    public void selectSite(ProcessingSiteView site) {
        model.select(site);
        refreshVisibleSites();
    }

    public void deselectSite(ProcessingSiteView site) {
        model.deselect(site);
        refreshVisibleSites();
    }

    public void excludeSite(ProcessingSiteView site) {
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

    public List<ProcessingSiteView> allSites() {
        return model.allSites();
    }

    public List<ProcessingSiteView> visibleSites() {
        return model.visibleSites();
    }

    public List<ProcessingSiteView> selectedSites() {
        return model.selectedSites();
    }

    public List<ProcessingSiteView> excludedSites() {
        return model.excludedSites();
    }

    public Set<Integer> selectedSiteIds() {
        return model.selectedSiteIds();
    }

    public Set<Integer> excludedSiteIds() {
        return model.excludedSiteIds();
    }

    public boolean isSelected(ProcessingSiteView site) {
        return model.isSelected(site);
    }

    public String keyword() {
        return keyword;
    }

    private void refreshVisibleSites() {
        model.refreshVisibleSites(keyword);
    }
}
