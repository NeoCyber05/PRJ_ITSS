package org.itss.prj_itss.model.request.application.listing;

import org.itss.prj_itss.model.request.application.RequestManagementUseCase;
import org.itss.prj_itss.model.request.application.port.RequestDisplayFormatter;

import java.time.LocalDate;
import java.util.List;

public final class ReceivedRequestsApplicationService {

    private final RequestManagementUseCase requestService;
    private final RequestDisplayFormatter formatter;

    public ReceivedRequestsApplicationService(
            RequestManagementUseCase requestService,
            RequestDisplayFormatter formatter
    ) {
        this.requestService = requestService;
        this.formatter = formatter;
    }

    public List<RequestRow> findRows() {
        return requestService.findAll().stream()
            .map(request -> {
                LocalDate earliestDelivery = requestService.getEarliestDeliveryDate(request.getId());
                String status = request.getStatus() == null ? "N/A" : request.getStatus();
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
        return requestService.deleteRequest(requestId);
    }
}
