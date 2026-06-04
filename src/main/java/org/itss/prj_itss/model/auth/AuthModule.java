package org.itss.prj_itss.model.auth;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.auth.application.AuthSession;
import org.itss.prj_itss.model.auth.application.AuthenticationService;
import org.itss.prj_itss.model.auth.application.management.AccountManagementService;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.infrastructure.persistence.JdbcAccountRepository;
import org.itss.prj_itss.model.site.application.port.SiteAccountProvisioningPort;

import java.util.function.Supplier;

public final class AuthModule {

    private final JdbcAccountRepository accountRepository;
    private final AuthenticationService authenticationService;
    private final AuthSession authSession = new AuthSession();
    private final AccountManagementService accountManagementService;

    public AuthModule(ConnectionProvider connectionProvider) {
        this.accountRepository = new JdbcAccountRepository(connectionProvider);
        this.authenticationService = new AuthenticationService(accountRepository);
        this.accountManagementService = new AccountManagementService(accountRepository);
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

    public AccountManagementService accountManagementService() {
        return accountManagementService;
    }

    public SiteAccountProvisioningPort siteAccountProvisioningPort() {
        return accountRepository;
    }
}
