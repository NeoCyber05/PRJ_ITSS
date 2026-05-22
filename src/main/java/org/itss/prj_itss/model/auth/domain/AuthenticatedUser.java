package org.itss.prj_itss.model.auth.domain;

import java.util.Locale;
import java.util.Objects;

public record AuthenticatedUser(Account account, Role role) {

    public AuthenticatedUser {
        Objects.requireNonNull(account, "account must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }

    public String displayName() {
        if (account.getFullName() != null && !account.getFullName().isBlank()) {
            return account.getFullName().trim();
        }
        return account.getUsername();
    }

    public String username() {
        return account.getUsername();
    }

    public String roleName() {
        return role.getName();
    }

    public String initials() {
        String displayName = displayName() == null ? "" : displayName().trim();
        if (displayName.isBlank()) {
            return "NA";
        }

        String[] parts = displayName.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.ROOT);
        }

        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase(Locale.ROOT);
    }
}
