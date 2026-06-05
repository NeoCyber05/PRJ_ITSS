package org.itss.prj_itss.model.order.application.cancellation;

import org.itss.prj_itss.model.order.application.cancellation.OrderCancellationApplicationService;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderCancellationApplicationServiceTest {

    @Test
    void cancelsPendingOrder() {
        FakeOrderRepository repository = new FakeOrderRepository(orderWithStatus(10, "pending"));
        OrderCancellationApplicationService applicationService = new OrderCancellationApplicationService(repository);

        OrderCancellationApplicationService.CancellationResult result = applicationService.cancel(10);

        assertTrue(result.success());
        assertTrue(result.statusChanged());
        assertEquals("cancelled", result.status());
        assertEquals("cancelled", repository.findById(10).getStatus());
        assertEquals(10, repository.updatedOrderId);
    }

    @Test
    void rejectsNonPendingOrderWithoutUpdatingStatus() {
        FakeOrderRepository repository = new FakeOrderRepository(orderWithStatus(11, "shipping"));
        OrderCancellationApplicationService applicationService = new OrderCancellationApplicationService(repository);

        OrderCancellationApplicationService.CancellationResult result = applicationService.cancel(11);

        assertFalse(result.success());
        assertFalse(result.statusChanged());
        assertEquals("shipping", repository.findById(11).getStatus());
        assertEquals(0, repository.updatedOrderId);
    }

    @Test
    void returnsNotFoundWhenOrderDoesNotExist() {
        FakeOrderRepository repository = new FakeOrderRepository();
        OrderCancellationApplicationService applicationService = new OrderCancellationApplicationService(repository);

        OrderCancellationApplicationService.CancellationResult result = applicationService.cancel(99);

        assertFalse(result.success());
        assertFalse(result.statusChanged());
        assertNull(result.order());
        assertEquals(99, result.orderId());
    }

    @Test
    void alreadyCancelledOrderIsIdempotent() {
        FakeOrderRepository repository = new FakeOrderRepository(orderWithStatus(12, "cancelled"));
        OrderCancellationApplicationService applicationService = new OrderCancellationApplicationService(repository);

        OrderCancellationApplicationService.CancellationResult result = applicationService.cancel(12);

        assertTrue(result.success());
        assertFalse(result.statusChanged());
        assertEquals("cancelled", result.status());
        assertEquals(0, repository.updatedOrderId);
    }

    private static Order orderWithStatus(int id, String status) {
        return new Order(id, 1, 2, LocalDateTime.now(), status);
    }

    private static final class FakeOrderRepository implements OrderRepository {
        private final Map<Integer, Order> orders = new LinkedHashMap<>();
        private int updatedOrderId;

        private FakeOrderRepository(Order... orders) {
            for (Order order : orders) {
                this.orders.put(order.getId(), order);
            }
        }

        @Override
        public List<Order> findAll() {
            return List.copyOf(orders.values());
        }

        @Override
        public List<Order> findByStatus(String status) {
            return orders.values().stream()
                    .filter(order -> status.equals(order.getStatus()))
                    .toList();
        }

        @Override
        public Order findById(int id) {
            return orders.get(id);
        }

        @Override
        public List<OrderMerchandise> findItemsByOrderId(int orderId) {
            return List.of();
        }

        @Override
        public int create(Order order) {
            orders.put(order.getId(), order);
            return order.getId();
        }

        @Override
        public boolean addItem(OrderMerchandise item) {
            return true;
        }

        @Override
        public boolean updateStatus(int orderId, String newStatus) {
            Order order = orders.get(orderId);
            if (order == null) {
                return false;
            }
            updatedOrderId = orderId;
            order.setStatus(newStatus);
            return true;
        }

        @Override
        public java.time.LocalDate findDesiredDeliveryDate(int orderId, int merchandiseId) {
            return null;
        }
    }
}
