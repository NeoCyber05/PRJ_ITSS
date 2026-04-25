package org.itss.prj_itss.warehouse.order.confirm_arrival;

import org.itss.prj_itss.model.OrderStatus;
import org.itss.prj_itss.service.OrderService;

/**
 * Service skeleton cho UC "Xác nhận đơn hàng giao tới".
 */
public final class ConfirmOrderArrivalService {

    private final OrderService orderService;

    public ConfirmOrderArrivalService(OrderService orderService) {
        this.orderService = orderService;
    }

    public boolean confirmDelivered(int orderId) {
        return orderService.updateStatus(orderId, OrderStatus.DELIVERED.displayValue());
    }
}
