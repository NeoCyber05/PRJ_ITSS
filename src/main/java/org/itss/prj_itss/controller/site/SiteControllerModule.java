package org.itss.prj_itss.controller.site;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.site.SiteModule;

import java.util.function.Supplier;

public final class SiteControllerModule {

    private final SiteWorkspaceController siteWorkspaceController;

    public SiteControllerModule(SiteModule siteModule, Supplier<AuthenticatedUser> authenticatedUserSupplier) {
        this.siteWorkspaceController = new SiteWorkspaceController(
            siteModule.overseasSiteApplicationService(),
            authenticatedUserSupplier
        );
    }

    public SiteWorkspaceController siteWorkspaceController() {
        return siteWorkspaceController;
    }
}
