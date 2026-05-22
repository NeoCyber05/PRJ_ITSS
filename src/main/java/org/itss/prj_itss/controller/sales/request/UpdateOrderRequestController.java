package org.itss.prj_itss.controller.sales.request;

import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.model.request.application.sales.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.RequestItemInput;
import org.itss.prj_itss.model.request.application.sales.RequestSalesApplicationService;

import java.util.List;

public final class UpdateOrderRequestController {

    private final RequestSalesApplicationService salesService;

    public UpdateOrderRequestController(RequestSalesApplicationService salesService) {
        this.salesService = salesService;
    }

    public RequestFormView loadRequest(int requestId) {
        return salesService.findFormView(requestId);
    }

    public List<MerchandiseOption> getMerchandiseOptions() {
        return salesService.findMerchandiseOptions();
    }

    public ActionResult updateRequest(int requestId, List<RequestItemInput> items) {
        try {
            salesService.updateRequest(requestId, items, null);
            return new ActionResult(true, "Cập nhật yêu cầu đặt hàng thành công");
        } catch (Exception e) {
            return new ActionResult(false, e.getMessage());
        }
    }
}
