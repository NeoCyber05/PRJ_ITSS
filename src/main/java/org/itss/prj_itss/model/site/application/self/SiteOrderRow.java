package org.itss.prj_itss.model.site.application.self;

public record SiteOrderRow(
    int orderId,
    int requestId,
    String orderCode,
    String requestCode,
    String createdAt,
    String status,
    String statusText,
    boolean confirmable
) {}
