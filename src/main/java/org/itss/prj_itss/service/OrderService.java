package org.itss.prj_itss.service;

import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.repository.OrderRepository;

import java.util.List;

public final class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
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
