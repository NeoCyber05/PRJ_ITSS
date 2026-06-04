package org.itss.prj_itss.model.request.application.listing;

import org.itss.prj_itss.model.request.application.port.RequestDisplayFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class ReceivedRequestsApplicationService {

    private final ReceivedRequestsPort requestService;
    private final RequestDisplayFormatter formatter;

    public ReceivedRequestsApplicationService(
            ReceivedRequestsPort requestService,
            RequestDisplayFormatter formatter
    ) {
        this.requestService = Objects.requireNonNull(requestService, "requestService");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    public List<RequestRow> findRows() {
        return requestService.findAll().stream()
            .map(request -> {
                LocalDate earliestDelivery = requestService.getEarliestDeliveryDate(request.getId());
                String status = request.getStatusKey() == null ? "N/A" : request.getStatusKey();
                String statusKey = formatter.normalizeStatusKey(status);
                return new RequestRow(
                    request.getId(),
                    formatter.formatRequestCode(request.getId()),
                    formatter.formatDateOrEmpty(request.getCreatedAt()),
                    requestService.countItemTypes(request.getId()) + " loại",
                    formatter.formatDate(earliestDelivery),
                    status,
                    statusKey,
                    formatter.requestStatusText(status),
                    formatter.pendingStatusKey().equals(statusKey)
                );
            })
            .toList();
    }

    public boolean deleteRequest(int requestId) {
        return requestService.deleteById(requestId);
    }
}
