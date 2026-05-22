package org.itss.prj_itss.dashboard.application;

import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.request.application.RequestManagementUseCase;
import org.itss.prj_itss.model.site.application.SiteUseCase;

public final class DashboardQuery {

    private final RequestManagementUseCase requestService;
    private final OrderUseCase orderService;
    private final SiteUseCase siteService;

    public DashboardQuery(RequestManagementUseCase requestService, OrderUseCase orderService, SiteUseCase siteService) {
        this.requestService = requestService;
        this.orderService = orderService;
        this.siteService = siteService;
    }

    public DashboardData loadDashboardData() {
        return new DashboardData(
            requestService.findAll(),
            orderService.findAll(),
            siteService.countAll()
        );
    }
}
