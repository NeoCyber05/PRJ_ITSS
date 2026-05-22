package org.itss.prj_itss.model.request.application.sales;

import java.util.List;

public record RequestDetailViewModel(
    int requestId,
    String requestCode,
    String createdAt,
    String status,
    String statusText,
    String note,
    String earliestDeadline,
    List<RequestDetailItemRow> requestItems,
    List<AllocatedOrderRow> allocatedOrders
) {
    public RequestDetailViewModel {
        requestItems = requestItems == null ? List.of() : List.copyOf(requestItems);
        allocatedOrders = allocatedOrders == null ? List.of() : List.copyOf(allocatedOrders);
    }
}
