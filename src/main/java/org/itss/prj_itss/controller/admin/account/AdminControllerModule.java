package org.itss.prj_itss.controller.admin.account;

import org.itss.prj_itss.model.auth.AuthModule;

public final class AdminControllerModule {

    private final AccountManagementController accountManagementController;

    public AdminControllerModule(AuthModule authModule) {
        this.accountManagementController =
            new AccountManagementController(authModule.accountManagementService());
    }

    public AccountManagementController accountManagementController() {
        return accountManagementController;
    }
}
