package org.itss.prj_itss.model.auth;

import org.itss.prj_itss.common.config.SharedInfrastructure;
import org.itss.prj_itss.model.auth.application.AuthSession;
import org.itss.prj_itss.model.auth.application.AuthenticationService;
import org.itss.prj_itss.model.auth.application.port.AccountRepository;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.infrastructure.persistence.JdbcAccountRepository;

import java.util.function.Supplier;

public final class AuthModule {

    private final AccountRepository accountRepository;
    private final AuthenticationService authenticationService;
    private final AuthSession authSession = new AuthSession();

    public AuthModule(SharedInfrastructure infrastructure) {
        this.accountRepository = new JdbcAccountRepository(infrastructure.connectionProvider());
        this.authenticationService = new AuthenticationService(accountRepository);
    }

    public AuthenticationService authenticationService() {
        return authenticationService;
    }

    public AuthSession authSession() {
        return authSession;
    }

    public Supplier<AuthenticatedUser> currentUserSupplier() {
        return authSession::currentAuthenticatedUser;
    }

    public void setAuthenticatedUser(AuthenticatedUser user) {
        if (user == null) {
            authSession.clear();
        } else {
            authSession.start(user);
        }
    }

    public AuthenticatedUser currentAuthenticatedUser() {
        return authSession.currentAuthenticatedUser();
    }
}
