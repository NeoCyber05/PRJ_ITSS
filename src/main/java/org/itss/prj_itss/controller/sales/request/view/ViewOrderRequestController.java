package org.itss.prj_itss.controller.sales.request.view;

import org.itss.prj_itss.model.request.application.sales.view.RequestReadOnlyView;
import org.itss.prj_itss.model.request.application.sales.RequestSalesApplicationService;

public final class ViewOrderRequestController {

    private final RequestSalesApplicationService salesService;

    public ViewOrderRequestController(RequestSalesApplicationService salesService) {
        this.salesService = salesService;
    }

    public RequestReadOnlyView loadRequest(int requestId) {
        return salesService.findReadOnlyView(requestId);
    }
}
