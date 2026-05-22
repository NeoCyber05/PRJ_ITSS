package org.itss.prj_itss.controller.ordering.site;

import org.itss.prj_itss.model.site.application.SiteManagementApplicationService;
import org.itss.prj_itss.model.site.application.SiteRow;
import org.itss.prj_itss.model.site.domain.Site;

import java.util.List;

public final class SiteManagementController {

    private final SiteManagementApplicationService siteManagementApplicationService;

    public SiteManagementController(SiteManagementApplicationService siteManagementApplicationService) {
        this.siteManagementApplicationService = siteManagementApplicationService;
    }

    public List<Site> getSites() {
        return siteManagementApplicationService.findSites();
    }

    public SiteRow toRow(Site site) {
        return siteManagementApplicationService.toRow(site);
    }

    public int countMerchandise() {
        return siteManagementApplicationService.countMerchandise();
    }
}
