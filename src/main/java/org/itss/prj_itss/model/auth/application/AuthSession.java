package org.itss.prj_itss.model.auth.application;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class AuthSession {

    private final AtomicReference<AuthenticatedUser> currentUser = new AtomicReference<>();

    public void start(AuthenticatedUser user) {
        currentUser.set(Objects.requireNonNull(user, "user must not be null"));
    }

    public void setAuthenticatedUser(AuthenticatedUser user) {
        currentUser.set(user);
    }

    public Optional<AuthenticatedUser> currentUser() {
        return Optional.ofNullable(currentUser.get());
    }

    public AuthenticatedUser currentAuthenticatedUser() {
        return currentUser.get();
    }

    public boolean isAuthenticated() {
        return currentUser.get() != null;
    }

    public void clear() {
        currentUser.set(null);
    }
}
