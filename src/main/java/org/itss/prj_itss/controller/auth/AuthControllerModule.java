package org.itss.prj_itss.controller.auth;

import org.itss.prj_itss.controller.navigation.SimpleNavigator;

public final class AuthControllerModule {

    private final RoleWorkspaceController roleWorkspaceController;

    public AuthControllerModule(SimpleNavigator navigator) {
        this.roleWorkspaceController = new RoleWorkspaceController(navigator);
    }

    public RoleWorkspaceController roleWorkspaceController() {
        return roleWorkspaceController;
    }
}
