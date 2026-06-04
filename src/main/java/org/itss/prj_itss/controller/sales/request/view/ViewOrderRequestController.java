package org.itss.prj_itss.controller.sales.request.view;

import org.itss.prj_itss.model.request.application.sales.view.RequestReadOnlyView;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryService;

public final class ViewOrderRequestController {

    private final SalesRequestQueryService queryService;

    public ViewOrderRequestController(SalesRequestQueryService queryService) {
        this.queryService = queryService;
    }

    public RequestReadOnlyView loadRequest(int requestId) {
        return queryService.findReadOnlyView(requestId);
    }
}
