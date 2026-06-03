package org.itss.prj_itss.controller.ordering.request;

import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.request.application.international.detail.AllocatedOrderRow;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailApplicationService;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailViewModel;

public final class RequestDetailPopupController {

    private final ReceivedRequestDetailApplicationService detailService;
    private final OrderCancellationApplicationService orderCancellationService;

    public RequestDetailPopupController(
            ReceivedRequestDetailApplicationService detailService,
            OrderCancellationApplicationService orderCancellationService) {
        this.detailService = detailService;
        this.orderCancellationService = orderCancellationService;
    }

    public ReceivedRequestDetailViewModel load(String requestCode) {
        return detailService.load(requestCode);
    }

    public OrderCancellationApplicationService.CancellationResult cancel(int orderId) {
        return orderCancellationService.cancel(orderId);
    }

    public AllocatedOrderRow findOrderRow(int orderId) {
        return detailService.findOrderRow(orderId);
    }
}
