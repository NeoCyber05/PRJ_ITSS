package org.itss.prj_itss.controller.ordering.order;

import org.itss.prj_itss.model.merchandise.MerchandiseModule;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.site.SiteModule;

public final class OrderControllerModule {

    private final OrderManagementController orderManagementController;
    private final OrderDetailController orderDetailController;
    private final OrderCancellationController orderCancellationController;

    public OrderControllerModule(OrderModule orderModule, SiteModule siteModule, MerchandiseModule merchandiseModule) {
        this.orderManagementController = new OrderManagementController(orderModule.orderManagementApplicationService());
        this.orderDetailController = new OrderDetailController(
            orderModule.orderUseCase(),
            orderModule.orderCancellationApplicationService(),
            siteModule.siteUseCase(),
            merchandiseModule.merchandiseUseCase()
        );
        this.orderCancellationController =
            new OrderCancellationController(orderModule.orderCancellationApplicationService());
    }

    public OrderManagementController orderManagementController() {
        return orderManagementController;
    }

    public OrderDetailController orderDetailController() {
        return orderDetailController;
    }

    public OrderCancellationController orderCancellationController() {
        return orderCancellationController;
    }
}
