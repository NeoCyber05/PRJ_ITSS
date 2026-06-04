package org.itss.prj_itss.model.dashboard;

import org.itss.prj_itss.model.dashboard.application.DashboardQuery;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.request.RequestModule;
import org.itss.prj_itss.model.site.SiteModule;

public final class DashboardModule {

    private final DashboardQuery dashboardQuery;

    public DashboardModule(RequestModule requestModule, OrderModule orderModule, SiteModule siteModule) {
        this.dashboardQuery = new DashboardQuery(
            requestModule.dashboardRequestPort(),
            orderModule.orderUseCase(),
            siteModule.siteUseCase()
        );
    }

    public DashboardQuery dashboardQuery() {
        return dashboardQuery;
    }
}
