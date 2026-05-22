package org.itss.prj_itss.model.order.application;

import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.domain.Site;

import java.util.List;

public record OrderDetailViewModel(
    Order order,
    Site site,
    List<OrderMerchandise> items
) {
}
