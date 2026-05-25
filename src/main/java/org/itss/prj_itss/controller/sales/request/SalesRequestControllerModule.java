package org.itss.prj_itss.controller.sales.request;

import org.itss.prj_itss.model.request.RequestModule;
import org.itss.prj_itss.controller.sales.request.list.SalesRequestListController;
import org.itss.prj_itss.controller.sales.request.create.CreateOrderRequestController;
import org.itss.prj_itss.controller.sales.request.update.UpdateOrderRequestController;
import org.itss.prj_itss.controller.sales.request.view.ViewOrderRequestController;

public final class SalesRequestControllerModule {

    private final SalesRequestListController salesRequestListController;
    private final CreateOrderRequestController createOrderRequestController;
    private final UpdateOrderRequestController updateOrderRequestController;
    private final ViewOrderRequestController viewOrderRequestController;

    public SalesRequestControllerModule(RequestModule requestModule) {
        this.salesRequestListController =
            new SalesRequestListController(requestModule.receivedRequestsApplicationService());
        this.createOrderRequestController =
            new CreateOrderRequestController(requestModule.requestSalesApplicationService());
        this.updateOrderRequestController =
            new UpdateOrderRequestController(requestModule.requestSalesApplicationService());
        this.viewOrderRequestController =
            new ViewOrderRequestController(requestModule.requestSalesApplicationService());
    }

    public SalesRequestListController salesRequestListController() {
        return salesRequestListController;
    }

    public CreateOrderRequestController createOrderRequestController() {
        return createOrderRequestController;
    }

    public UpdateOrderRequestController updateOrderRequestController() {
        return updateOrderRequestController;
    }

    public ViewOrderRequestController viewOrderRequestController() {
        return viewOrderRequestController;
    }
}
