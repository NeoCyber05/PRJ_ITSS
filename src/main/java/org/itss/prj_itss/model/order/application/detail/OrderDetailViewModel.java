package org.itss.prj_itss.model.order.application.detail;

import java.util.List;

/**
 * A pure presentation model for the order detail screen.
 * Contains only business information so the View layer
 * can format it as needed.
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
            String deliveryMethod,
            String desiredDateText,
            Integer dayDelta,
            boolean deliveryAvailable
    ) {
    }
}
