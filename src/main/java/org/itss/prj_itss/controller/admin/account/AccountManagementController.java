package org.itss.prj_itss.controller.admin.account;

import org.itss.prj_itss.model.auth.application.management.AccountDraft;
import org.itss.prj_itss.model.auth.application.management.AccountManagementResult;
import org.itss.prj_itss.model.auth.application.management.AccountManagementService;
import org.itss.prj_itss.model.auth.application.management.AccountManagementSnapshot;
import org.itss.prj_itss.model.auth.application.management.AccountRow;

import java.util.List;
import java.util.Objects;

public final class AccountManagementController {

    private final AccountManagementService accountManagementService;

    public AccountManagementController(AccountManagementService accountManagementService) {
        this.accountManagementService = Objects.requireNonNull(accountManagementService, "accountManagementService");
    }

    public AccountManagementSnapshot load() {
        return accountManagementService.load();
    }

    public List<AccountRow> filterRows(List<AccountRow> rows, String keyword) {
        return accountManagementService.filterRows(rows, keyword);
    }

    public AccountManagementResult create(AccountDraft draft) {
        return accountManagementService.createInternalAccount(draft);
    }

    public AccountManagementResult update(int accountId, AccountDraft draft) {
        return accountManagementService.updateInternalAccount(accountId, draft);
    }

    public AccountManagementResult disable(int accountId) {
        return accountManagementService.disableAccount(accountId);
    }

    public AccountManagementResult delete(int accountId) {
        return accountManagementService.deleteAccount(accountId);
    }
}
