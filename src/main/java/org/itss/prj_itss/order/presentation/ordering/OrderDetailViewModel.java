package org.itss.prj_itss.order.presentation.ordering;

import org.itss.prj_itss.catalog.domain.Merchandise;
import org.itss.prj_itss.order.domain.Order;
import org.itss.prj_itss.order.domain.OrderMerchandise;
import org.itss.prj_itss.site.domain.Site;
import org.itss.prj_itss.common.application.OrderingFormatters;

import java.util.List;

public record OrderDetailViewModel(
    Order order,
    Site site,
    int requestedOrderId,
    int orderId,
    int requestId,
    int siteId,
    String orderCode,
    String requestCode,
    String siteCode,
    String siteName,
    String createdAt,
    String createdAtMultiline,
    String status,
    String statusKey,
    String statusText,
    boolean found,
    boolean cancellable,
    List<ItemLine> items
) {

    public OrderDetailViewModel {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static OrderDetailViewModel notFound(int requestedOrderId) {
        return new OrderDetailViewModel(
            null,
            null,
            requestedOrderId,
            requestedOrderId,
            0,
            0,
            OrderingFormatters.formatOrderCode(requestedOrderId),
            "N/A",
            "N/A",
            "N/A",
            "N/A",
            "N/A",
            "N/A",
            OrderingFormatters.STATUS_OTHER,
            "N/A",
            false,
            false,
            List.of()
        );
    }

    public int totalItems() {
        return items.size();
    }

    public String totalItemsText() {
        return totalItems() + " m\u1eb7t h\u00e0ng";
    }

    public record ItemLine(
        OrderMerchandise source,
        Merchandise merchandise,
        int index,
        int merchandiseId,
        String merchandiseCode,
        String merchandiseName,
        String quantity,
        String unit,
        String deliveryMethod,
        String deliveryMethodText
    ) {
    }
}
