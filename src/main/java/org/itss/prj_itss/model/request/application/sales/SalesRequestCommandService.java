package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;

import java.util.List;

public final class SalesRequestCommandService {

    private final SalesRequestCommandPort commandPort;

    public SalesRequestCommandService(SalesRequestCommandPort commandPort) {
        this.commandPort = commandPort;
    }

    public int createRequest(List<SalesRequestItemSubmission> items, String note) throws Exception {
        List<RequestMerchandise> domainItems = items.stream()
            .map(i -> new RequestMerchandise(0, i.merchandiseId(), i.quantityOrdered(), i.desiredDeliveryDate()))
            .toList();
        return commandPort.createRequest(domainItems, note);
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
