package org.itss.prj_itss.model.auth.application.management;

import org.itss.prj_itss.model.auth.domain.Role;

import java.util.List;

public record AccountManagementSnapshot(List<AccountRow> rows, List<Role> assignableRoles, int activeCount, int disabledCount) {

    public AccountManagementSnapshot {
        rows = rows == null ? List.of() : List.copyOf(rows);
        assignableRoles = assignableRoles == null ? List.of() : List.copyOf(assignableRoles);
    }
}
