package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.util.List;

public final class SalesRequestCommandService {

    private final SalesRequestCommandPort commandPort;

    public SalesRequestCommandService(SalesRequestCommandPort commandPort) {
        this.commandPort = commandPort;
    }

    public int createRequest(List<SalesRequestItemSubmission> items, String note) throws Exception {
        Request request = new Request(note);
        for (SalesRequestItemSubmission item : items) {
            request.addItem(item.merchandiseId(), item.quantityOrdered(), item.desiredDeliveryDate());
        }
        return commandPort.createRequest(request);
    }

    public void updateRequest(int requestId, List<SalesRequestItemSubmission> items, String note) throws Exception {
        List<RequestMerchandise> domainItems = items.stream()
            .map(i -> new RequestMerchandise(requestId, i.merchandiseId(), i.quantityOrdered(), i.desiredDeliveryDate()))
            .toList();
        commandPort.updateRequestItems(requestId, domainItems, note);
    }

    public boolean deleteRequest(int requestId) {
        return commandPort.deleteById(requestId);
    }
}
