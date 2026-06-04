package org.itss.prj_itss.model.auth.application.port;

import org.itss.prj_itss.model.auth.application.management.AccountDraft;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.Role;

import java.util.List;
import java.util.Optional;

public interface AccountManagementRepository {

    List<AuthenticatedUser> findAllUsers();

    List<Role> findRoles();

    Optional<AuthenticatedUser> findUserById(int accountId);

    Optional<AuthenticatedUser> findUserByUsername(String username);

    int createAccount(AccountDraft draft);

    void updateAccount(int accountId, AccountDraft draft);

    void updateStatus(int accountId, String status);
}
