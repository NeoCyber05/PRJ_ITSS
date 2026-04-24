package org.itss.prj_itss.auth.login;

import org.itss.prj_itss.auth.session.UserSession;

public record LoginResult(boolean success, String message, UserSession session) {

    public static LoginResult success(UserSession session) {
        return new LoginResult(true, "", session);
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, message, null);
    }
}
