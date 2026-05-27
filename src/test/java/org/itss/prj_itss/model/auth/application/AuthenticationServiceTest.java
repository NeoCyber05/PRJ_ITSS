package org.itss.prj_itss.model.auth.application;

import org.itss.prj_itss.model.auth.domain.Account;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.Role;
import org.itss.prj_itss.model.auth.application.port.AccountRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServiceTest {

    @Test
    void rejectsBlankCredentialsWithoutQueryingRepository() {
        RecordingAccountRepository repository = new RecordingAccountRepository(Optional.empty());
        AuthenticationService service = new AuthenticationService(repository);

        LoginResult result = service.authenticate("  ", "secret");

        assertFalse(result.success());
        assertNull(result.user());
        assertFalse(repository.called);
    }

    @Test
    void trimsUsernameAndAuthenticatesActiveAccount() {
        AuthenticatedUser user = userWithStatus("active");
        RecordingAccountRepository repository = new RecordingAccountRepository(Optional.of(user));
        AuthenticationService service = new AuthenticationService(repository);

        LoginResult result = service.authenticate(" alice ", "secret");

        assertTrue(result.success());
        assertSame(user, result.user());
        assertEquals("alice", repository.username);
        assertEquals("secret", repository.password);
    }

    @Test
    void rejectsUnknownCredentials() {
        RecordingAccountRepository repository = new RecordingAccountRepository(Optional.empty());
        AuthenticationService service = new AuthenticationService(repository);

        LoginResult result = service.authenticate("alice", "wrong");

        assertFalse(result.success());
        assertTrue(repository.called);
        assertNull(result.user());
    }

    @Test
    void rejectsInactiveAccountStatuses() {
        RecordingAccountRepository repository = new RecordingAccountRepository(Optional.of(userWithStatus("locked")));
        AuthenticationService service = new AuthenticationService(repository);

        LoginResult result = service.authenticate("alice", "secret");

        assertFalse(result.success());
        assertNull(result.user());
    }

    @Test
    void treatsBlankStatusAsActive() {
        AuthenticatedUser user = userWithStatus(" ");
        RecordingAccountRepository repository = new RecordingAccountRepository(Optional.of(user));
        AuthenticationService service = new AuthenticationService(repository);

        LoginResult result = service.authenticate("alice", "secret");

        assertTrue(result.success());
        assertSame(user, result.user());
    }

    private AuthenticatedUser userWithStatus(String status) {
        Account account = new Account(1, "alice", "secret", "Alice Nguyen", status, 3, null);
        Role role = new Role(3, "Ordering");
        return new AuthenticatedUser(account, role);
    }

    private static final class RecordingAccountRepository implements AccountRepository {
        private final Optional<AuthenticatedUser> result;
        private boolean called;
        private String username;
        private String password;

        private RecordingAccountRepository(Optional<AuthenticatedUser> result) {
            this.result = result;
        }

        @Override
        public Optional<AuthenticatedUser> findByCredentials(String username, String password) {
            this.called = true;
            this.username = username;
            this.password = password;
            return result;
        }
    }
}
