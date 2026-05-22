package org.itss.prj_itss.controller.ordering.request;

import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.request.application.sales.AllocatedOrderRow;
import org.itss.prj_itss.model.request.application.sales.RequestDetailApplicationService;
import org.itss.prj_itss.model.request.application.sales.RequestDetailViewModel;

public final class RequestDetailPopupController {

    private final RequestDetailApplicationService detailService;
    private final OrderCancellationApplicationService orderCancellationService;

    public RequestDetailPopupController(
            RequestDetailApplicationService detailService,
            OrderCancellationApplicationService orderCancellationService) {
        this.detailService = detailService;
        this.orderCancellationService = orderCancellationService;
    }

    public RequestDetailViewModel load(String requestCode) {
        return detailService.load(requestCode);
    }

    public OrderCancellationApplicationService.CancellationResult cancel(int orderId) {
        return orderCancellationService.cancel(orderId);
    }

    public AllocatedOrderRow findOrderRow(int orderId) {
        return detailService.findOrderRow(orderId);
    }
}
