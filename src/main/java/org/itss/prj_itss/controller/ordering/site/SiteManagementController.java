package org.itss.prj_itss.controller.ordering.site;

import org.itss.prj_itss.model.site.application.SiteAccountDraft;
import org.itss.prj_itss.model.site.application.SiteDraft;
import org.itss.prj_itss.model.site.application.SiteManagementApplicationService;
import org.itss.prj_itss.model.site.application.SiteManagementResult;
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

    public SiteManagementApplicationService.Snapshot load() {
        return siteManagementApplicationService.load();
    }

    public List<SiteRow> filterRows(List<SiteRow> rows, String keyword) {
        return siteManagementApplicationService.filterRows(rows, keyword);
    }

    public SiteManagementResult createSiteWithAccount(SiteDraft siteDraft, SiteAccountDraft accountDraft) {
        return siteManagementApplicationService.createSiteWithAccount(siteDraft, accountDraft);
    }

    public SiteManagementResult createSite(SiteDraft draft) {
        return siteManagementApplicationService.createSite(draft);
    }

    public SiteManagementResult updateSite(int siteId, SiteDraft draft) {
        return siteManagementApplicationService.updateSite(siteId, draft);
    }

    public SiteManagementResult provisionSiteAccount(int siteId, SiteAccountDraft draft) {
        return siteManagementApplicationService.provisionSiteAccount(siteId, draft);
    }
}
