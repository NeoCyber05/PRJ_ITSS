package org.itss.prj_itss.service;

import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.repository.IOrderRepository;

import java.util.List;

public final class OrderService {

    private final IOrderRepository orderRepository;

    public OrderService(IOrderRepository orderRepository) {
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
