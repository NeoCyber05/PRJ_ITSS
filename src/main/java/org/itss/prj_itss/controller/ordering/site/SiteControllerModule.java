package org.itss.prj_itss.controller.ordering.site;

import org.itss.prj_itss.model.site.SiteModule;

public final class SiteControllerModule {

    private final SiteManagementController siteManagementController;

    public SiteControllerModule(SiteModule siteModule) {
        this.siteManagementController = new SiteManagementController(siteModule.siteManagementApplicationService());
    }

    public SiteManagementController siteManagementController() {
        return siteManagementController;
    }
}
