package org.itss.prj_itss.auth.login;

import org.itss.prj_itss.auth.AuthenticatedUser;

public record LoginResult(boolean success, String message, AuthenticatedUser user) {

    public static LoginResult success(AuthenticatedUser user) {
        return new LoginResult(true, "", user);
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, message, null);
    }
}
