package org.itss.prj_itss.model.auth.application;

import org.itss.prj_itss.model.auth.domain.Account;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.Role;
import org.itss.prj_itss.model.auth.domain.RoleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleAccessPolicyTest {

    @Test
    void orderingRoleCanAccessOrderingViewsAndDynamicRoutes() {
        assertEquals("home", RoleAccessPolicy.defaultViewId(RoleType.ORDERING));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.ORDERING, "home"));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.ORDERING, "site-management"));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.ORDERING, "request-processing:42"));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.ORDERING, "order-detail:7"));
        assertFalse(RoleAccessPolicy.canAccess(RoleType.ORDERING, "sales-requests"));
    }

    @Test
    void salesRoleCanAccessSalesViewsAndDynamicRoutes() {
        assertEquals("sales-requests", RoleAccessPolicy.defaultViewId(RoleType.SALES));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.SALES, "sales-requests"));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.SALES, "sales-request-create"));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.SALES, "sales-request-update:9"));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.SALES, "sales-request-detail:9"));
        assertFalse(RoleAccessPolicy.canAccess(RoleType.SALES, "orders"));
    }

    @Test
    void warehouseRoleOnlyGetsWarehouseArrivalView() {
        assertEquals("warehouse-order-confirm-arrival", RoleAccessPolicy.defaultViewId(RoleType.WAREHOUSE));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.WAREHOUSE, "warehouse-order-confirm-arrival"));
        assertFalse(RoleAccessPolicy.canAccess(RoleType.WAREHOUSE, "home"));
    }

    @Test
    void unknownAndUnsupportedRolesUseRoleWorkspaceOnly() {
        assertEquals("role-workspace", RoleAccessPolicy.defaultViewId(RoleType.ADMIN));
        assertEquals("role-workspace", RoleAccessPolicy.defaultViewId(RoleType.SITE));
        assertEquals("role-workspace", RoleAccessPolicy.defaultViewId(RoleType.UNKNOWN));
        assertTrue(RoleAccessPolicy.canAccess(RoleType.UNKNOWN, "role-workspace"));
        assertFalse(RoleAccessPolicy.canAccess(RoleType.UNKNOWN, "home"));
        assertFalse(RoleAccessPolicy.canAccess(RoleType.UNKNOWN, null));
    }

    @Test
    void resolvesRoleTypeFromAccountRoleIdBeforeRoleName() {
        AuthenticatedUser user = user(3, "Quan ly kho");

        assertEquals(RoleType.ORDERING, RoleType.from(user));
        assertEquals("home", RoleAccessPolicy.defaultViewId(user));
    }

    private AuthenticatedUser user(int roleId, String roleName) {
        Account account = new Account(1, "alice", "secret", "Alice Nguyen", "active", roleId, null);
        Role role = new Role(roleId, roleName);
        return new AuthenticatedUser(account, role);
    }
}
