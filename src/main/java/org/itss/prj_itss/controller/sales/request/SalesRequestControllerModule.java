package org.itss.prj_itss.controller.sales.request;

import org.itss.prj_itss.model.request.RequestModule;
import org.itss.prj_itss.controller.sales.request.list.SalesRequestListController;
import org.itss.prj_itss.controller.sales.request.create.SalesRequestCreationController;
import org.itss.prj_itss.controller.sales.request.update.DefaultSalesRequestEditPresenter;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditController;
import org.itss.prj_itss.controller.sales.request.view.ViewOrderRequestController;

public final class SalesRequestControllerModule {

    private final SalesRequestListController salesRequestListController;
    private final SalesRequestCreationController salesRequestCreationController;
    private final SalesRequestEditController salesRequestEditController;
    private final ViewOrderRequestController viewOrderRequestController;

    public SalesRequestControllerModule(RequestModule requestModule) {
        this.salesRequestListController =
            new SalesRequestListController(requestModule.receivedRequestsApplicationService());
        this.salesRequestCreationController =
            new SalesRequestCreationController(requestModule.salesRequestCreationApplicationService());
        this.salesRequestEditController =
            new SalesRequestEditController(
                requestModule.salesRequestEditUseCase(),
                new DefaultSalesRequestEditPresenter(requestModule.requestDisplayFormatter())
            );
        this.viewOrderRequestController =
            new ViewOrderRequestController(requestModule.requestSalesApplicationService());
    }

    public SalesRequestListController salesRequestListController() {
        return salesRequestListController;
    }

    public SalesRequestCreationController salesRequestCreationController() {
        return salesRequestCreationController;
    }

    public SalesRequestEditController salesRequestEditController() {
        return salesRequestEditController;
    }

    public ViewOrderRequestController viewOrderRequestController() {
        return viewOrderRequestController;
    }
}
