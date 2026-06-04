package org.itss.prj_itss.model.request.application.listing;

import java.util.Locale;

public record RequestRow(
    int requestId,
    String requestCode,
    String createdAt,
    String itemCount,
    String deadline,
    String status,
    String statusKey,
    String statusText,
    boolean processable
) {

    public boolean matchesKeyword(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank()
            || contains(requestCode, normalized)
            || contains(createdAt, normalized)
            || contains(deadline, normalized);
    }

    public boolean matchesStatusKey(String selectedStatusKey) {
        return selectedStatusKey == null
            || "all".equalsIgnoreCase(selectedStatusKey)
            || selectedStatusKey.equalsIgnoreCase(statusKey);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
