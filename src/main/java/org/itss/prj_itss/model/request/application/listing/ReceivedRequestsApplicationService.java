package org.itss.prj_itss.model.request.application.listing;

import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsPort;

import java.time.LocalDate;
import java.util.List;

public final class ReceivedRequestsApplicationService {

    private final ReceivedRequestsPort requestService;

    public ReceivedRequestsApplicationService(ReceivedRequestsPort requestService) {
        this.requestService = requestService;
    }

    public List<RequestRow> findRows() {
        return requestService.findAll().stream()
            .map(request -> {
                LocalDate earliestDelivery = requestService.getEarliestDeliveryDate(request.getId());
                return new RequestRow(
                    request.getId(),
                    OrderingFormatters.formatRequestCode(request.getId()),
                    OrderingFormatters.formatDateOrEmpty(request.getCreatedAt()),
                    OrderingFormatters.formatItemTypes(requestService.countItemTypes(request.getId())),
                    OrderingFormatters.formatDate(earliestDelivery),
                    request.getStatus() == null ? "N/A" : request.getStatusKey()
                );
            })
            .toList();
    }

    public boolean deleteRequest(int requestId) {
        return requestService.deleteById(requestId);
    }
}
