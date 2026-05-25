package org.itss.prj_itss.model.request.application.listing;

import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;

import java.util.Locale;

public record RequestRow(
    int requestId,
    String requestCode,
    String createdAt,
    String itemCount,
    String deadline,
    String status,
    String statusKey,
    String statusText
) {

    public RequestRow(
        int requestId,
        String requestCode,
        String createdAt,
        String itemCount,
        String deadline,
        String status
    ) {
        this(
            requestId,
            requestCode,
            createdAt,
            itemCount,
            deadline,
            status,
            OrderingFormatters.normalizeStatusKey(status),
            OrderingFormatters.requestStatusText(status)
        );
    }

    public boolean processable() {
        return OrderingFormatters.STATUS_PENDING.equals(statusKey);
    }

    public boolean matchesKeyword(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank()
            || contains(requestCode, normalized)
            || contains(createdAt, normalized)
            || contains(deadline, normalized);
    }

    public boolean matchesStatusKey(String selectedStatusKey) {
        return OrderingFormatters.statusMatches(status, selectedStatusKey);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
