package org.itss.prj_itss.model.order.application;

import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.site.application.SiteUseCase;

public final class OrderDetailApplicationService {

    private final OrderUseCase orderService;
    private final SiteUseCase siteService;

    public OrderDetailApplicationService(OrderUseCase orderService, SiteUseCase siteService) {
        this.orderService = orderService;
        this.siteService = siteService;
    }

    public OrderDetailViewModel load(int orderId) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            return new OrderDetailViewModel(null, null, java.util.List.of());
        }
        return new OrderDetailViewModel(
            order,
            siteService.findById(order.getSiteId()),
            orderService.findItemsByOrderId(orderId)
        );
    }
}
