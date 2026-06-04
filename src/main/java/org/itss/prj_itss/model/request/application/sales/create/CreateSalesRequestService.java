package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandPort;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.domain.request.Request;

import java.util.List;

public class CreateSalesRequestService implements CreateSalesRequestUseCase {

    private final SalesRequestCommandPort commandPort;

    public CreateSalesRequestService(SalesRequestCommandPort commandPort) {
        this.commandPort = commandPort;
    }

    @Override
    public int createRequest(List<SalesRequestItemSubmission> items, String note) throws Exception {
        Request request = new Request(note);
        for (SalesRequestItemSubmission item : items) {
            request.addItem(item.merchandiseId(), item.quantityOrdered(), item.desiredDeliveryDate());
        }
        return commandPort.createRequest(request);
    }
}
