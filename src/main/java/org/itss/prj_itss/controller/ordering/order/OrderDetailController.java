package org.itss.prj_itss.controller.ordering.order;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.domain.Site;

import java.util.List;

public final class OrderDetailController {

    private final OrderUseCase orderService;
    private final OrderCancellationApplicationService orderCancellationApplicationService;
    private final SiteUseCase siteService;
    private final MerchandiseUseCase merchandiseService;

    public OrderDetailController(
            OrderUseCase orderService,
            OrderCancellationApplicationService orderCancellationApplicationService,
            SiteUseCase siteService,
            MerchandiseUseCase merchandiseService) {
        this.orderService = orderService;
        this.orderCancellationApplicationService = orderCancellationApplicationService;
        this.siteService = siteService;
        this.merchandiseService = merchandiseService;
    }

    public Order findById(int id) {
        return orderService.findById(id);
    }

    public OrderCancellationApplicationService.CancellationResult cancel(int orderId) {
        return orderCancellationApplicationService.cancel(orderId);
    }

    public Site findSiteById(int id) {
        return siteService.findById(id);
    }

    public List<OrderMerchandise> findItemsByOrderId(int orderId) {
        return orderService.findItemsByOrderId(orderId);
    }

    public Merchandise findMerchandiseById(int id) {
        return merchandiseService.findById(id);
    }
}
