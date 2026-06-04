package org.itss.prj_itss.model.order.application.port;

import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;

import java.util.List;

public interface OrderRepository {
    List<Order> findAll();
    List<Order> findByStatus(String status);
    Order findById(int id);
    List<OrderMerchandise> findItemsByOrderId(int orderId);
    int create(Order order);
    boolean addItem(OrderMerchandise item);
    boolean updateStatus(int orderId, String newStatus);
    java.time.LocalDate findDesiredDeliveryDate(int orderId, int merchandiseId);
    default java.util.Map<Integer, Integer> countItemsGroupedByOrderId() {
        return java.util.Map.of();
    }
}
