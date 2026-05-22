package org.itss.prj_itss.controller.ordering.order;

import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;

public final class OrderCancellationController {

    private final OrderCancellationApplicationService orderCancellationService;

    public OrderCancellationController(OrderCancellationApplicationService orderCancellationService) {
        this.orderCancellationService = orderCancellationService;
    }

    public OrderCancellationApplicationService.CancellationResult cancel(int orderId) {
        return orderCancellationService.cancel(orderId);
    }
}
