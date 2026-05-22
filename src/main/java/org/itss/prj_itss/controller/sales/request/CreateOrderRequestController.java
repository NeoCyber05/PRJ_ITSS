package org.itss.prj_itss.controller.sales.request;

import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.model.request.application.sales.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.RequestItemInput;
import org.itss.prj_itss.model.request.application.sales.RequestSalesApplicationService;

import java.util.List;

public final class CreateOrderRequestController {

    private final RequestSalesApplicationService salesService;

    public CreateOrderRequestController(RequestSalesApplicationService salesService) {
        this.salesService = salesService;
    }

    public MerchandiseOption getMerchandiseOptionByCode(String code) {
        return salesService.findMerchandiseOptionByCode(code);
    }

    public ActionResult createRequest(List<RequestItemInput> items) {
        try {
            salesService.createRequest(items, "");
            return new ActionResult(true, "Yêu cầu nhập hàng đã được gửi thành công.");
        } catch (Exception e) {
            return new ActionResult(false, e.getMessage());
        }
    }
}
