package org.itss.prj_itss.model.order.application.port;

import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;

import java.util.List;

public interface SiteOrderRepository {
    List<Order> findBySiteId(int siteId);
    Order findByIdForSite(int orderId, int siteId);
    List<OrderMerchandise> findItemsByOrderId(int orderId);
    boolean updateStatusForSite(int orderId, int siteId, String newStatus);
}
