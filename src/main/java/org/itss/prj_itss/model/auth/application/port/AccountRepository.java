package org.itss.prj_itss.model.auth.application.port;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;

import java.util.Optional;

public interface AccountRepository {
    Optional<AuthenticatedUser> findByCredentials(String username, String password);
}
