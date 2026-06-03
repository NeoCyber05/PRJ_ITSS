package org.itss.prj_itss.model.warehouse.application;

public record IncomingOrderRow(
    int orderId,
    int requestId,
    int siteId,
    String orderCode,
    String requestCode,
    String siteCode,
    String siteName,
    String createdAt,
    String status,
    String statusText,
    int itemCount
) {
}
