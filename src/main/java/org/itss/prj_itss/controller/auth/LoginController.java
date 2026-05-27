package org.itss.prj_itss.controller.auth;

import org.itss.prj_itss.model.auth.application.AuthenticationService;
import org.itss.prj_itss.model.auth.application.LoginResult;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import java.util.function.Consumer;

public class LoginController {
    private final AuthenticationService authenticationService;
    private final Consumer<AuthenticatedUser> loginHandler;

    public LoginController(AuthenticationService authenticationService, Consumer<AuthenticatedUser> loginHandler) {
        this.authenticationService = authenticationService;
        this.loginHandler = loginHandler;
    }

    public LoginResult login(String username, String password) {
        if (authenticationService == null) {
            return new LoginResult(false, "Hệ thống chưa sẵn sàng để đăng nhập.", null);
        }
        return authenticationService.authenticate(username, password);
    }

    public void handleSuccessfulLogin(AuthenticatedUser user) {
        if (loginHandler != null) {
            loginHandler.accept(user);
        }
    }
}
