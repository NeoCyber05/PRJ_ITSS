package org.itss.prj_itss.controller.ordering.order.detail;

import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.model.order.application.cancellation.OrderCancellationApplicationService;
import org.itss.prj_itss.model.order.application.detail.OrderDetailApplicationService;
import org.itss.prj_itss.model.order.application.detail.OrderDetailViewModel;

public final class OrderDetailController {

    private final OrderDetailApplicationService detailService;
    private final OrderCancellationApplicationService cancellationService;

    public OrderDetailController(
            OrderDetailApplicationService detailService,
            OrderCancellationApplicationService cancellationService) {
        this.detailService = detailService;
        this.cancellationService = cancellationService;
    }

    public OrderDetailViewModel loadDetail(int orderId) {
        return detailService.load(orderId);
    }

    public ActionResult cancel(int orderId) {
        OrderCancellationApplicationService.CancellationResult result = cancellationService.cancel(orderId);
        return new ActionResult(result.success(), result.message());
    }
}
