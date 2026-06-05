package org.itss.prj_itss.model.dashboard.application;

import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.dashboard.application.port.DashboardRequestPort;
import org.itss.prj_itss.model.site.application.SiteUseCase;

public final class DashboardQuery {

    private final DashboardRequestPort requestService;
    private final OrderRepository orderRepository;
    private final SiteUseCase siteService;

    public DashboardQuery(DashboardRequestPort requestService, OrderRepository orderRepository, SiteUseCase siteService) {
        this.requestService = requestService;
        this.orderRepository = orderRepository;
        this.siteService = siteService;
    }

    public DashboardData loadDashboardData() {
        java.util.List<DashboardRequestInfo> requestInfos = requestService.findAll().stream()
            .map(req -> new DashboardRequestInfo(
                req,
                requestService.getEarliestDeliveryDate(req.getId()),
                requestService.countItemTypes(req.getId())
            ))
            .toList();

        return new DashboardData(
            requestInfos,
            orderRepository.findAll(),
            siteService.countAll()
        );
    }
}
