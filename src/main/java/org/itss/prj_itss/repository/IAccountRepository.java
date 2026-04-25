package org.itss.prj_itss.repository;

import org.itss.prj_itss.auth.AuthenticatedUser;

import java.util.Optional;

public interface IAccountRepository {
    Optional<AuthenticatedUser> findByCredentials(String username, String password);
}
