package org.itss.prj_itss.auth.role;

import org.itss.prj_itss.auth.AuthenticatedUser;

public final class RoleAccessPolicy {

    private RoleAccessPolicy() {
    }

    public static boolean canAccess(AuthenticatedUser user, String viewId) {
        return canAccess(RoleType.from(user), viewId);
    }

    public static boolean canAccess(RoleType roleType, String viewId) {
        String normalizedViewId = normalizeViewId(viewId);
        if (roleType.isOrderingRole()) {
            return switch (normalizedViewId) {
                case "home", "site-management", "received-requests", "orders",
                    "request-processing", "order-detail",
                    "sales-request-create", "sales-request-update",
                    "ordering-order-handle-cancellation" -> true;
                default -> false;
            };
        }
        if (roleType.isWarehouseRole()) {
            return switch (normalizedViewId) {
                case "warehouse-order-confirm-arrival" -> true;
                default -> false;
            };
        }
        return "role-workspace".equals(normalizedViewId);
    }

    public static String defaultViewId(AuthenticatedUser user) {
        return defaultViewId(RoleType.from(user));
    }

    public static String defaultViewId(RoleType roleType) {
        if (roleType.isOrderingRole()) {
            return "home";
        }
        if (roleType.isWarehouseRole()) {
            return "warehouse-order-confirm-arrival";
        }
        return "role-workspace";
    }

    private static String normalizeViewId(String viewId) {
        if (viewId == null || viewId.isBlank()) {
            return "";
        }
        if (viewId.startsWith("order-detail:")) {
            return "order-detail";
        }
        if (viewId.startsWith("request-processing:")) {
            return "request-processing";
        }
        return viewId;
    }
}
