package org.itss.prj_itss.model.auth.application.management;

import java.util.Locale;

public record AccountRow(int accountId, String username, String fullName, String roleName, String status) {

    public boolean matchesKeyword(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank()
            || contains(username, normalized)
            || contains(fullName, normalized)
            || contains(roleName, normalized)
            || contains(status, normalized);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
