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
        java.util.List<org.itss.prj_itss.model.request.domain.request.Request> requests = requestService.findAll();
        java.util.Set<Integer> requestIds = requests.stream()
            .map(org.itss.prj_itss.model.request.domain.request.Request::getId)
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        java.util.Map<Integer, Integer> itemCounts = requestService.countItemTypesByRequestIds(requestIds);
        java.util.Map<Integer, LocalDate> earliestDeliveries =
            requestService.findEarliestDeliveryDatesByRequestIds(requestIds);

        return requests.stream()
            .map(request -> new RequestRow(
                request.getId(),
                OrderingFormatters.formatRequestCode(request.getId()),
                OrderingFormatters.formatDateOrEmpty(request.getCreatedAt()),
                OrderingFormatters.formatItemTypes(itemCounts.getOrDefault(request.getId(), 0)),
                OrderingFormatters.formatDate(earliestDeliveries.get(request.getId())),
                request.getStatus() == null ? "N/A" : request.getStatusKey()
            ))
            .toList();
    }

    public boolean deleteRequest(int requestId) {
        return requestService.deleteById(requestId);
    }
}
