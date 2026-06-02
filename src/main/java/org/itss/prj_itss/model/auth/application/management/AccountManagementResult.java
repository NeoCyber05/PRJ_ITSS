package org.itss.prj_itss.model.auth.application.management;

public record AccountManagementResult(boolean success, String message, Integer accountId) {

    public static AccountManagementResult success(String message, Integer accountId) {
        return new AccountManagementResult(true, message, accountId);
    }

    public static AccountManagementResult failure(String message) {
        return new AccountManagementResult(false, message, null);
    }
}
