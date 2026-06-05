package org.itss.prj_itss.model.order.application.management;

import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;

import java.util.Locale;

public record OrderRow(
    Order order,
    int orderId,
    int requestId,
    int siteId,
    String orderCode,
    String requestCode,
    String siteName,
    String itemsSummary,
    String createdAt,
    String status,
    String statusKey,
    String statusText
) {

    public boolean cancellable() {
        return OrderingFormatters.STATUS_PENDING.equals(statusKey);
    }

    public boolean matchesKeyword(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank()
            || contains(orderCode, normalized)
            || contains(requestCode, normalized)
            || contains(siteName, normalized)
            || contains(itemsSummary, normalized);
    }

    public boolean matchesStatusKey(String selectedStatusKey) {
        return OrderingFormatters.statusMatches(status, selectedStatusKey);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
