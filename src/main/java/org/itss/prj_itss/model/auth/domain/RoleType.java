package org.itss.prj_itss.model.auth.domain;

import java.text.Normalizer;
import java.util.Locale;

public enum RoleType {
    ADMIN(1),
    SALES(2),
    ORDERING(3),
    SITE(4),
    WAREHOUSE(5),
    UNKNOWN(-1);

    private final int id;

    RoleType(int id) {
        this.id = id;
    }

    public static RoleType from(AuthenticatedUser user) {
        int roleId = user.account().getRoleId();
        for (RoleType roleType : values()) {
            if (roleType.id == roleId) {
                return roleType;
            }
        }

        String normalizedRoleName = normalizeText(user.roleName());
        if (normalizedRoleName.contains("quan tri")) {
            return ADMIN;
        }
        if (normalizedRoleName.contains("ban hang")) {
            return SALES;
        }
        if (normalizedRoleName.contains("dat hang")) {
            return ORDERING;
        }
        if ("site".equals(normalizedRoleName)) {
            return SITE;
        }
        if (normalizedRoleName.contains("quan ly kho")) {
            return WAREHOUSE;
        }
        return UNKNOWN;
    }

    public boolean isOrderingRole() {
        return this == ORDERING;
    }

    public boolean isSalesRole() {
        return this == SALES;
    }

    public boolean isWarehouseRole() {
        return this == WAREHOUSE;
    }

    public int id() {
        return id;
    }

    public boolean isAdminRole() {
        return this == ADMIN;
    }

    public boolean isSiteRole() {
        return this == SITE;
    }

    public boolean isInternalUserRole() {
        return this == ADMIN || this == SALES || this == ORDERING || this == WAREHOUSE;
    }

    public static RoleType fromRoleId(int roleId) {
        for (RoleType roleType : values()) {
            if (roleType.id == roleId) return roleType;
        }
        return UNKNOWN;
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }
}
