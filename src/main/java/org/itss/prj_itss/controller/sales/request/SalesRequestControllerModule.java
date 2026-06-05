package org.itss.prj_itss.controller.sales.request;

import org.itss.prj_itss.model.request.RequestModule;
import org.itss.prj_itss.controller.sales.request.list.SalesRequestListController;
import org.itss.prj_itss.controller.sales.request.create.SalesRequestCreationController;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditController;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditServiceGateway;
import org.itss.prj_itss.controller.sales.request.view.ViewOrderRequestController;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditMapper;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditUseCase;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidator;

public final class SalesRequestControllerModule {

    private final SalesRequestListController salesRequestListController;
    private final SalesRequestCreationController salesRequestCreationController;
    private final SalesRequestEditController salesRequestEditController;
    private final ViewOrderRequestController viewOrderRequestController;

    public SalesRequestControllerModule(RequestModule requestModule) {
        this.salesRequestListController =
            new SalesRequestListController(requestModule.receivedRequestsApplicationService());
        this.salesRequestCreationController =
            new SalesRequestCreationController(
                requestModule.salesRequestQueryService(),
                requestModule.createSalesRequestUseCase()
            );
        this.salesRequestEditController =
            new SalesRequestEditController(
                new SalesRequestEditUseCase(
                    new SalesRequestEditServiceGateway(
                        requestModule.salesRequestQueryService(),
                        requestModule.salesRequestCommandService()
                    ),
                    new SalesRequestEditMapper(),
                    new SalesRequestEditValidator()
                )
            );
        this.viewOrderRequestController =
            new ViewOrderRequestController(requestModule.salesRequestQueryService());
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
