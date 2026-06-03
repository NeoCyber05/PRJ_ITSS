package org.itss.prj_itss.model.auth.application.management;

import org.itss.prj_itss.model.auth.application.port.AccountManagementRepository;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.Role;
import org.itss.prj_itss.model.auth.domain.RoleType;

import java.util.List;
import java.util.Objects;

public final class AccountManagementService {

    private final AccountManagementRepository repository;

    public AccountManagementService(AccountManagementRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public AccountManagementSnapshot load() {
        List<AuthenticatedUser> allUsers = repository.findAllUsers();
        List<AccountRow> rows = allUsers.stream()
            .filter(u -> !RoleType.fromRoleId(u.account().getRoleId()).isSiteRole())
            .filter(u -> !"deleted".equalsIgnoreCase(u.account().getStatus()))
            .map(this::toRow)
            .toList();
        List<Role> assignableRoles = repository.findRoles();
        long activeCount = rows.stream().filter(r -> "active".equalsIgnoreCase(r.status())).count();
        long disabledCount = rows.stream().filter(r -> "disabled".equalsIgnoreCase(r.status())).count();
        return new AccountManagementSnapshot(rows, assignableRoles, (int) activeCount, (int) disabledCount);
    }

    public List<AccountRow> filterRows(List<AccountRow> rows, String keyword) {
        return rows.stream().filter(r -> r.matchesKeyword(keyword)).toList();
    }

    public AccountManagementResult createInternalAccount(AccountDraft draft) {
        if (RoleType.fromRoleId(draft.roleId()).isSiteRole()) {
            return AccountManagementResult.failure("Admin không được tạo tài khoản Site.");
        }
        AccountDraft sanitized = new AccountDraft(draft.username(), draft.password(), draft.fullName(), draft.roleId(), null);
        int id = repository.createAccount(sanitized);
        return AccountManagementResult.success("Tạo tài khoản thành công.", id);
    }

    public AccountManagementResult updateInternalAccount(int accountId, AccountDraft draft) {
        if (RoleType.fromRoleId(draft.roleId()).isSiteRole()) {
            return AccountManagementResult.failure("Không được cập nhật tài khoản thành role Site.");
        }
        AccountDraft sanitized = new AccountDraft(draft.username(), draft.password(), draft.fullName(), draft.roleId(), null);
        repository.updateAccount(accountId, sanitized);
        return AccountManagementResult.success("Cập nhật tài khoản thành công.", accountId);
    }

    public AccountManagementResult disableAccount(int accountId) {
        repository.updateStatus(accountId, "disabled");
        return AccountManagementResult.success("Tài khoản đã bị vô hiệu hóa.", accountId);
    }

    public AccountManagementResult deleteAccount(int accountId) {
        repository.updateStatus(accountId, "deleted");
        return AccountManagementResult.success("Tài khoản đã bị hủy.", accountId);
    }

    private AccountRow toRow(AuthenticatedUser user) {
        return new AccountRow(
            user.account().getId(),
            user.account().getUsername(),
            user.account().getFullName(),
            user.roleName(),
            user.account().getStatus()
        );
    }
}
