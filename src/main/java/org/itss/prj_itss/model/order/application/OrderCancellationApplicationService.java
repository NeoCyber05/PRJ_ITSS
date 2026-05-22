package org.itss.prj_itss.model.order.application;

import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.common.application.OrderingFormatters;
import org.itss.prj_itss.model.order.application.OrderUseCase;

import java.util.Objects;

public final class OrderCancellationApplicationService {

    public static final String CANCELLED_STATUS = OrderingFormatters.STATUS_CANCELLED;

    private final OrderUseCase orderService;

    public OrderCancellationApplicationService(OrderUseCase orderService) {
        this.orderService = Objects.requireNonNull(orderService, "orderService");
    }

    public CancellationResult cancel(int orderId) {
        if (orderId <= 0) {
            return CancellationResult.invalid(orderId, "Invalid order id");
        }

        Order order = orderService.findById(orderId);
        if (order == null) {
            return CancellationResult.notFound(orderId);
        }

        String statusKey = OrderingFormatters.normalizeStatusKey(order.getStatus());
        if (OrderingFormatters.STATUS_CANCELLED.equals(statusKey)) {
            return CancellationResult.alreadyCancelled(order);
        }
        if (!OrderingFormatters.STATUS_PENDING.equals(statusKey)) {
            return CancellationResult.rejected(order, "Only pending orders can be cancelled");
        }

        boolean updated = orderService.updateStatus(orderId, CANCELLED_STATUS);
        if (!updated) {
            return CancellationResult.failed(order, "Unable to update order status");
        }

        order.setStatus(CANCELLED_STATUS);
        return CancellationResult.cancelled(order);
    }

    public boolean canCancel(int orderId) {
        return isCancellable(orderService.findById(orderId));
    }

    public static boolean isCancellable(Order order) {
        return order != null
            && OrderingFormatters.STATUS_PENDING.equals(OrderingFormatters.normalizeStatusKey(order.getStatus()));
    }

    public record CancellationResult(
        int orderId,
        Order order,
        boolean success,
        boolean statusChanged,
        String status,
        String message
    ) {
        private static CancellationResult invalid(int orderId, String message) {
            return new CancellationResult(orderId, null, false, false, null, message);
        }

        private static CancellationResult notFound(int orderId) {
            return new CancellationResult(orderId, null, false, false, null, "Order not found");
        }

        private static CancellationResult rejected(Order order, String message) {
            return new CancellationResult(order.getId(), order, false, false, order.getStatus(), message);
        }

        private static CancellationResult failed(Order order, String message) {
            return new CancellationResult(order.getId(), order, false, false, order.getStatus(), message);
        }

        private static CancellationResult alreadyCancelled(Order order) {
            return new CancellationResult(order.getId(), order, true, false, order.getStatus(), "Order already cancelled");
        }

        private static CancellationResult cancelled(Order order) {
            return new CancellationResult(order.getId(), order, true, true, CANCELLED_STATUS, "Order cancelled");
        }
    }
}
