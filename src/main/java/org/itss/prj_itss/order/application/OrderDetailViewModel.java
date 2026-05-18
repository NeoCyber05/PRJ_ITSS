package org.itss.prj_itss.order.application;

import org.itss.prj_itss.order.domain.Order;
import org.itss.prj_itss.order.domain.OrderMerchandise;
import org.itss.prj_itss.site.domain.Site;

import java.util.List;

public record OrderDetailViewModel(
    Order order,
    Site site,
    List<OrderMerchandise> items
) {
}
