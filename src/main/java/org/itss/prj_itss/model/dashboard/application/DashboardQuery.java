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
        java.util.List<org.itss.prj_itss.model.request.domain.request.Request> requests = requestService.findAll();
        java.util.Set<Integer> requestIds = requests.stream()
            .map(org.itss.prj_itss.model.request.domain.request.Request::getId)
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        java.util.Map<Integer, java.time.LocalDate> earliestDeliveries =
            requestService.findEarliestDeliveryDatesByRequestIds(requestIds);
        java.util.Map<Integer, Integer> itemCounts =
            requestService.countItemTypesByRequestIds(requestIds);

        java.util.List<DashboardRequestInfo> requestInfos = requests.stream()
            .map(req -> new DashboardRequestInfo(
                req,
                earliestDeliveries.get(req.getId()),
                itemCounts.getOrDefault(req.getId(), 0)
            ))
            .toList();

        return new DashboardData(
            requestInfos,
            orderRepository.findAll(),
            siteService.countAll()
        );
    }
}
