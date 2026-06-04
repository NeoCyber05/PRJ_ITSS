package org.itss.prj_itss.model.order.application;

import java.util.List;

/**
 * A pure presentation model for the order detail screen.
 * Contains only pre-formatted strings so the View layer
 * never imports domain classes.
 */
public record OrderDetailViewModel(
    int orderId,
    String orderCode,
    String status,
    String createdAt,
    String siteCode,
    String siteName,
    int itemCount,
    boolean cancellable,
    List<OrderItemRow> items
) {

    public record OrderItemRow(
        String merchandiseCode,
        String merchandiseName,
        String quantity,
        String unit,
        String deliveryMethod
    ) {
    }
}
