package org.itss.prj_itss.request.application.sales;

public record AllocatedOrderRow(
    int orderId,
    String orderCode,
    String siteName,
    String deliveryMethod,
    String createdAt,
    String status,
    String statusText,
    boolean cancellable
) {
}
