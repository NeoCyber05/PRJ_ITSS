package org.itss.prj_itss.controller.ordering.order;

import org.itss.prj_itss.model.merchandise.MerchandiseModule;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.site.SiteModule;
import org.itss.prj_itss.controller.ordering.order.management.OrderManagementController;
import org.itss.prj_itss.controller.ordering.order.detail.OrderDetailController;
import org.itss.prj_itss.controller.ordering.order.cancellation.OrderCancellationProcessingController;
import org.itss.prj_itss.controller.ordering.order.cancellation.state.CancelledOrderProcessingSession;

public final class OrderControllerModule {

    private final OrderModule orderModule;
    private final OrderManagementController orderManagementController;
    private final OrderDetailController orderDetailController;

    public OrderControllerModule(OrderModule orderModule, SiteModule siteModule, MerchandiseModule merchandiseModule) {
        this.orderModule = orderModule;
        this.orderManagementController = new OrderManagementController(orderModule.orderManagementApplicationService());
        this.orderDetailController = new OrderDetailController(
            orderModule.orderDetailApplicationService(),
            orderModule.orderCancellationApplicationService()
        );
    }

    public OrderManagementController orderManagementController() {
        return orderManagementController;
    }

    public OrderDetailController orderDetailController() {
        return orderDetailController;
    }

    public OrderCancellationProcessingController newCancellationProcessingController() {
        CancelledOrderProcessingSession session = new CancelledOrderProcessingSession(
            orderModule.cancelledOrderProcessingUseCase()
        );
        return new OrderCancellationProcessingController(session);
    }
}
