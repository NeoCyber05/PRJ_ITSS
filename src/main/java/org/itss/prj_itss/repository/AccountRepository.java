package org.itss.prj_itss.repository;

import org.itss.prj_itss.auth.session.UserSession;

import java.util.Optional;

public interface AccountRepository {
    Optional<UserSession> findByCredentials(String username, String password);
}
