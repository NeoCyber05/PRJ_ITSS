package org.itss.prj_itss.model.request.application.international.detail;

import java.util.List;

public record ReceivedRequestDetailViewModel(
    int requestId,
    String requestCode,
    String createdAt,
    String status,
    String statusText,
    String note,
    String earliestDeadline,
    List<ReceivedRequestDetailItemRow> requestItems,
    List<AllocatedOrderRow> allocatedOrders
) {
    public ReceivedRequestDetailViewModel {
        requestItems = requestItems == null ? List.of() : List.copyOf(requestItems);
        allocatedOrders = allocatedOrders == null ? List.of() : List.copyOf(allocatedOrders);
    }
}
