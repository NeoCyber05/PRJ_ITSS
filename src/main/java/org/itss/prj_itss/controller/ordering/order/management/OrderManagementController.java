package org.itss.prj_itss.controller.ordering.order.management;

import org.itss.prj_itss.model.order.application.management.OrderManagementApplicationService;
import org.itss.prj_itss.model.order.application.management.OrderRow;

import java.util.List;

public final class OrderManagementController {

    private final OrderManagementApplicationService orderManagementService;

    public OrderManagementController(OrderManagementApplicationService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    public List<OrderRow> findRows() {
        return orderManagementService.loadRows();
    }
}
