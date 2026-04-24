package org.itss.prj_itss.service;

import org.itss.prj_itss.dto.DashboardData;

public final class DashboardService {

    private final RequestService requestService;
    private final OrderService orderService;
    private final SiteService siteService;

    public DashboardService(RequestService requestService, OrderService orderService, SiteService siteService) {
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
