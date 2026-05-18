package org.itss.prj_itss.order.application;

import org.itss.prj_itss.order.domain.Order;
import org.itss.prj_itss.order.domain.OrderMerchandise;
import org.itss.prj_itss.order.application.port.OrderRepository;

import java.util.List;

public final class OrderUseCase {

    private final OrderRepository orderRepository;

    public OrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public Order findById(int id) {
        return orderRepository.findById(id);
    }

    public List<OrderMerchandise> findItemsByOrderId(int orderId) {
        return orderRepository.findItemsByOrderId(orderId);
    }

    public boolean updateStatus(int orderId, String newStatus) {
        return orderRepository.updateStatus(orderId, newStatus);
    }
}
