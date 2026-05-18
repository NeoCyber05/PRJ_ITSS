package org.itss.prj_itss.auth.application;

import org.itss.prj_itss.auth.domain.AuthenticatedUser;

import java.util.Objects;

public record LoginResult(boolean success, String message, AuthenticatedUser user) {

    public LoginResult {
        message = message == null ? "" : message;
    }

    public static LoginResult success(AuthenticatedUser user) {
        return new LoginResult(true, "", Objects.requireNonNull(user, "user must not be null"));
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, message, null);
    }
}
