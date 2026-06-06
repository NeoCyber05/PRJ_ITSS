package org.itss.prj_itss.controller.ordering.request;

import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.request.RequestModule;
import org.itss.prj_itss.controller.ordering.request.process.RequestProcessingLayoutController;
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;

import java.util.function.Supplier;

public final class RequestControllerModule {

    private final RequestModule requestModule;
    private final ReceivedRequestsController receivedRequestsController;
    private final RequestDetailPopupController requestDetailPopupController;
    private final Supplier<LockOwner> lockOwnerSupplier;

    public RequestControllerModule(
            RequestModule requestModule,
            OrderModule orderModule,
            Supplier<LockOwner> lockOwnerSupplier
    ) {
        this.requestModule = requestModule;
        this.receivedRequestsController =
            new ReceivedRequestsController(
                requestModule.receivedRequestsApplicationService(),
                requestModule.requestLockUseCase()
            );
        this.requestDetailPopupController = new RequestDetailPopupController(
            requestModule.receivedRequestDetailApplicationService(),
            orderModule.orderCancellationApplicationService()
        );
        this.lockOwnerSupplier = lockOwnerSupplier;
    }

    public ReceivedRequestsController receivedRequestsController() {
        return receivedRequestsController;
    }

    public RequestDetailPopupController requestDetailPopupController() {
        return requestDetailPopupController;
    }

    public RequestProcessingLayoutController requestProcessingLayoutController() {
        return new RequestProcessingLayoutController(
            requestModule.requestProcessingUseCase(),
            requestModule.requestLockUseCase(),
            lockOwnerSupplier
        );
    }
}
