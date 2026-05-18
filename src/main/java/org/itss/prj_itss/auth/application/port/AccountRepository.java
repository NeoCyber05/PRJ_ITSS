package org.itss.prj_itss.auth.application.port;

import org.itss.prj_itss.auth.domain.AuthenticatedUser;

import java.util.Optional;

public interface AccountRepository {
    Optional<AuthenticatedUser> findByCredentials(String username, String password);
}
