package org.itss.prj_itss.controller.sales.request;

import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.model.request.application.listing.RequestRow;

import java.util.List;

public final class SalesRequestListController {

    private final ReceivedRequestsApplicationService receivedRequestsService;

    public SalesRequestListController(ReceivedRequestsApplicationService receivedRequestsService) {
        this.receivedRequestsService = receivedRequestsService;
    }

    public List<RequestRow> getRequests() {
        return receivedRequestsService.findRows();
    }

    public boolean deleteRequest(int requestId) {
        return receivedRequestsService.deleteRequest(requestId);
    }
}
