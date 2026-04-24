package org.itss.prj_itss.auth.role;

import org.itss.prj_itss.auth.session.UserSession;

public final class RoleAccessPolicy {

    private RoleAccessPolicy() {
    }

    public static boolean canAccess(UserSession session, String viewId) {
        return canAccess(RoleType.from(session), viewId);
    }

    public static boolean canAccess(RoleType roleType, String viewId) {
        String normalizedViewId = normalizeViewId(viewId);
        if (roleType.isOrderingRole()) {
            return switch (normalizedViewId) {
                case "home", "site-management", "received-requests", "orders",
                    "request-processing", "order-detail" -> true;
                default -> false;
            };
        }
        return "role-workspace".equals(normalizedViewId);
    }

    public static String defaultViewId(UserSession session) {
        return defaultViewId(RoleType.from(session));
    }

    public static String defaultViewId(RoleType roleType) {
        return roleType.isOrderingRole() ? "home" : "role-workspace";
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
