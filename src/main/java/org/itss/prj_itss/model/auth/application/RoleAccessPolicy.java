package org.itss.prj_itss.model.auth.application;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.RoleType;

public final class RoleAccessPolicy {

    private RoleAccessPolicy() {
    }

    public static boolean canAccess(AuthenticatedUser user, String viewId) {
        return canAccess(RoleType.from(user), viewId);
    }

    public static boolean canAccess(RoleType roleType, String viewId) {
        String normalizedViewId = normalizeViewId(viewId);
        if (roleType.isAdminRole()) {
            return switch (normalizedViewId) {
                case "account-management" -> true;
                default -> false;
            };
        }
        if (roleType.isOrderingRole()) {
            return switch (normalizedViewId) {
                case "home", "site-management", "received-requests", "orders",
                    "request-processing", "order-detail",
                    "ordering-order-handle-cancellation" -> true;
                default -> false;
            };
        }
        if (roleType.isSalesRole()) {
            return switch (normalizedViewId) {
                case "sales-requests", "sales-request-create",
                    "sales-request-update", "sales-request-detail",
                    "merchandise-management" -> true;
                default -> false;
            };
        }
        if (roleType.isWarehouseRole()) {
            return switch (normalizedViewId) {
                case "warehouse-inbound-orders", "warehouse-order-confirm-arrival" -> true;
                default -> false;
            };
        }
        if (roleType.isSiteRole()) {
            return switch (normalizedViewId) {
                case "site-workspace" -> true;
                default -> false;
            };
        }
        return "role-workspace".equals(normalizedViewId);
    }

    public static String defaultViewId(AuthenticatedUser user) {
        return defaultViewId(RoleType.from(user));
    }

    public static String defaultViewId(RoleType roleType) {
        if (roleType.isAdminRole()) {
            return "account-management";
        }
        if (roleType.isOrderingRole()) {
            return "home";
        }
        if (roleType.isSalesRole()) {
            return "sales-requests";
        }
        if (roleType.isWarehouseRole()) {
            return "warehouse-order-confirm-arrival";
        }
        if (roleType.isSiteRole()) {
            return "site-workspace";
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
        if (viewId.startsWith("sales-request-update:")) {
            return "sales-request-update";
        }
        if (viewId.startsWith("sales-request-detail:")) {
            return "sales-request-detail";
        }
        if (viewId.startsWith("ordering-order-handle-cancellation:")) {
            return "ordering-order-handle-cancellation";
        }
        if (viewId.startsWith("warehouse-order-confirm-arrival:")) {
            return "warehouse-order-confirm-arrival";
        }
        return viewId;
    }
}
