package org.itss.prj_itss.controller.ordering.order;

import org.itss.prj_itss.model.order.application.OrderManagementApplicationService;
import org.itss.prj_itss.model.order.application.OrderRow;

import java.util.List;

public final class OrderManagementController {

    private final OrderManagementApplicationService orderManagementService;

    public OrderManagementController(OrderManagementApplicationService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    public List<OrderRow> findRows() {
        return orderManagementService.findRows();
    }
}
