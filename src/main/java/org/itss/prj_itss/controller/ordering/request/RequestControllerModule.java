package org.itss.prj_itss.controller.ordering.request;

import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.request.RequestModule;
import org.itss.prj_itss.controller.ordering.request.process.RequestProcessingLayoutController;

public final class RequestControllerModule {

    private final RequestModule requestModule;
    private final ReceivedRequestsController receivedRequestsController;
    private final RequestDetailPopupController requestDetailPopupController;

    public RequestControllerModule(RequestModule requestModule, OrderModule orderModule) {
        this.requestModule = requestModule;
        this.receivedRequestsController =
            new ReceivedRequestsController(requestModule.receivedRequestsApplicationService());
        this.requestDetailPopupController = new RequestDetailPopupController(
            requestModule.receivedRequestDetailApplicationService(),
            orderModule.orderCancellationApplicationService()
        );
    }

    public ReceivedRequestsController receivedRequestsController() {
        return receivedRequestsController;
    }

    public RequestDetailPopupController requestDetailPopupController() {
        return requestDetailPopupController;
    }

    public RequestProcessingLayoutController requestProcessingLayoutController() {
        return new RequestProcessingLayoutController(requestModule.requestProcessingUseCase());
    }
}
