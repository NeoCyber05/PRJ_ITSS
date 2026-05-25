package org.itss.prj_itss.controller.ordering.request.process.site;

import org.itss.prj_itss.model.request.application.processing.ProcessingSiteView;
import org.itss.prj_itss.model.request.application.processing.SiteFilterModel;

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

    public void prioritizeSite(ProcessingSiteView site) {
        model.prioritize(site);
        refreshVisibleSites();
    }

    public void unprioritizeSite(ProcessingSiteView site) {
        model.unprioritize(site);
        refreshVisibleSites();
    }

    public void excludeSite(ProcessingSiteView site) {
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

    public List<ProcessingSiteView> allSites() {
        return model.allSites();
    }

    public List<ProcessingSiteView> visibleSites() {
        return model.visibleSites();
    }

    public List<ProcessingSiteView> prioritySites() {
        return model.prioritySites();
    }

    public List<ProcessingSiteView> excludedSites() {
        return model.excludedSites();
    }

    public Set<Integer> prioritySiteIds() {
        return model.prioritySiteIds();
    }

    public Set<Integer> excludedSiteIds() {
        return model.excludedSiteIds();
    }

    public boolean isPriority(ProcessingSiteView site) {
        return model.isPriority(site);
    }

    public String keyword() {
        return keyword;
    }

    private void refreshVisibleSites() {
        model.refreshVisibleSites(keyword);
    }
}
