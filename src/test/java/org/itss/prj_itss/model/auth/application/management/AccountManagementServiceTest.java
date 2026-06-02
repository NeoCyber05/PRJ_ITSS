package org.itss.prj_itss.model.auth.application.management;

import org.itss.prj_itss.model.auth.application.port.AccountManagementRepository;
import org.itss.prj_itss.model.auth.domain.Account;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.Role;
import org.itss.prj_itss.model.auth.domain.RoleType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountManagementServiceTest {

    // -----------------------------------------------------------------------
    // createInternalAccount
    // -----------------------------------------------------------------------

    @Test
    void adminCreateInternalAccountRejectsSiteRole() {
        FakeAccountManagementRepository repository = new FakeAccountManagementRepository();
        AccountManagementService service = new AccountManagementService(repository);

        AccountDraft draft = new AccountDraft("siteuser", "pass", "Site User", RoleType.SITE.id(), 5);
        AccountManagementResult result = service.createInternalAccount(draft);

        assertFalse(result.success());
        assertEquals("Admin không được tạo tài khoản Site.", result.message());
        assertTrue(repository.createdAccounts.isEmpty());
    }

    @Test
    void adminCreateInternalAccountStoresNullSiteId() {
        FakeAccountManagementRepository repository = new FakeAccountManagementRepository();
        AccountManagementService service = new AccountManagementService(repository);

        AccountDraft draft = new AccountDraft("salesuser", "pass", "Sales User", RoleType.SALES.id(), 9);
        AccountManagementResult result = service.createInternalAccount(draft);

        assertTrue(result.success());
        assertEquals(1, repository.createdAccounts.size());
        assertNull(repository.createdAccounts.get(0).siteId());
    }

    // -----------------------------------------------------------------------
    // disableAccount / deleteAccount
    // -----------------------------------------------------------------------

    @Test
    void disabledAccountGetsDisabledStatus() {
        FakeAccountManagementRepository repository = new FakeAccountManagementRepository();
        repository.accounts.add(new Account(7, "user7", "pass", "User Seven", "active", RoleType.SALES.id(), null));
        AccountManagementService service = new AccountManagementService(repository);

        AccountManagementResult result = service.disableAccount(7);

        assertTrue(result.success());
        assertEquals("disabled", repository.statusByAccountId.get(7));
    }

    @Test
    void deleteAccountGetsDeletedStatus() {
        FakeAccountManagementRepository repository = new FakeAccountManagementRepository();
        repository.accounts.add(new Account(3, "user3", "pass", "User Three", "active", RoleType.ORDERING.id(), null));
        AccountManagementService service = new AccountManagementService(repository);

        AccountManagementResult result = service.deleteAccount(3);

        assertTrue(result.success());
        assertEquals("deleted", repository.statusByAccountId.get(3));
    }

    // -----------------------------------------------------------------------
    // load() filtering
    // -----------------------------------------------------------------------

    @Test
    void loadHidesSiteAccounts() {
        FakeAccountManagementRepository repository = new FakeAccountManagementRepository();
        repository.accounts.add(new Account(10, "siteacct", "pass", "Site Acct", "active", RoleType.SITE.id(), 2));
        repository.accounts.add(new Account(11, "salesacct", "pass", "Sales Acct", "active", RoleType.SALES.id(), null));
        AccountManagementService service = new AccountManagementService(repository);

        AccountManagementSnapshot snapshot = service.load();

        assertEquals(1, snapshot.rows().size());
        assertEquals("salesacct", snapshot.rows().get(0).username());
    }

    @Test
    void loadHidesDeletedAccounts() {
        FakeAccountManagementRepository repository = new FakeAccountManagementRepository();
        repository.accounts.add(new Account(20, "deletedacct", "pass", "Deleted Acct", "deleted", RoleType.ADMIN.id(), null));
        repository.accounts.add(new Account(21, "activeacct", "pass", "Active Acct", "active", RoleType.ADMIN.id(), null));
        AccountManagementService service = new AccountManagementService(repository);

        AccountManagementSnapshot snapshot = service.load();

        assertEquals(1, snapshot.rows().size());
        assertEquals("activeacct", snapshot.rows().get(0).username());
    }

    // -----------------------------------------------------------------------
    // Fake repository
    // -----------------------------------------------------------------------

    private static final class FakeAccountManagementRepository implements AccountManagementRepository {

        final List<Account> accounts = new ArrayList<>();
        final List<AccountDraft> createdAccounts = new ArrayList<>();
        final Map<Integer, String> statusByAccountId = new HashMap<>();

        @Override
        public List<AuthenticatedUser> findAllUsers() {
            List<AuthenticatedUser> users = new ArrayList<>();
            for (Account a : accounts) {
                String roleName = RoleType.fromRoleId(a.getRoleId()).name();
                Role role = new Role(a.getRoleId(), roleName);
                users.add(new AuthenticatedUser(a, role));
            }
            return users;
        }

        @Override
        public List<Role> findRoles() {
            return List.of();
        }

        @Override
        public Optional<AuthenticatedUser> findUserById(int id) {
            return accounts.stream()
                .filter(a -> a.getId() == id)
                .map(a -> new AuthenticatedUser(a, new Role(a.getRoleId(), RoleType.fromRoleId(a.getRoleId()).name())))
                .findFirst();
        }

        @Override
        public Optional<AuthenticatedUser> findUserByUsername(String username) {
            return accounts.stream()
                .filter(a -> a.getUsername().equalsIgnoreCase(username))
                .map(a -> new AuthenticatedUser(a, new Role(a.getRoleId(), RoleType.fromRoleId(a.getRoleId()).name())))
                .findFirst();
        }

        @Override
        public int createAccount(AccountDraft draft) {
            createdAccounts.add(draft);
            return 1;
        }

        @Override
        public void updateAccount(int accountId, AccountDraft draft) {
            // no-op
        }

        @Override
        public void updateStatus(int accountId, String status) {
            statusByAccountId.put(accountId, status);
        }
    }
}
