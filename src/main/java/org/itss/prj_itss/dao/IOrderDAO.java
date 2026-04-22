package org.itss.prj_itss.dao;

import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;

import java.util.List;

public interface IOrderDAO {
    List<Order> findAll();
    Order findById(int id);
    List<OrderMerchandise> findItemsByOrderId(int orderId);
    int create(Order order);
    boolean addItem(OrderMerchandise item);
    boolean updateStatus(int orderId, String newStatus);
}
