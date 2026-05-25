package org.itss.prj_itss.controller.ordering.request;

import org.itss.prj_itss.controller.ordering.request.process.RequestProcessingController;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.request.RequestModule;

public final class RequestControllerModule {

    private final ReceivedRequestsController receivedRequestsController;
    private final RequestDetailPopupController requestDetailPopupController;
    private final RequestProcessingController requestProcessingController;

    public RequestControllerModule(RequestModule requestModule, OrderModule orderModule) {
        this.receivedRequestsController =
            new ReceivedRequestsController(requestModule.receivedRequestsApplicationService());
        this.requestDetailPopupController = new RequestDetailPopupController(
            requestModule.requestDetailApplicationService(),
            orderModule.orderCancellationApplicationService()
        );
        this.requestProcessingController =
            new RequestProcessingController(requestModule.requestProcessingUseCase());
    }

    public ReceivedRequestsController receivedRequestsController() {
        return receivedRequestsController;
    }

    public RequestDetailPopupController requestDetailPopupController() {
        return requestDetailPopupController;
    }

    public RequestProcessingController requestProcessingController() {
        return requestProcessingController;
    }
}
