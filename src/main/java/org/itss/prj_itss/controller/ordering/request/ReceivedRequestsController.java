package org.itss.prj_itss.controller.ordering.request;

import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.model.request.application.listing.RequestRow;

import java.util.List;

public final class ReceivedRequestsController {

    private final ReceivedRequestsApplicationService receivedRequestsService;

    public ReceivedRequestsController(ReceivedRequestsApplicationService receivedRequestsService) {
        this.receivedRequestsService = receivedRequestsService;
    }

    public List<RequestRow> findRows() {
        return receivedRequestsService.findRows();
    }
}
