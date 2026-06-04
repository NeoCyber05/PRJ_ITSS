package org.itss.prj_itss.controller.sales.request.update;

import java.util.List;

public record SalesRequestEditValidationView(List<SalesRequestEditFieldViolationView> violations) {

    public SalesRequestEditValidationView {
        violations = List.copyOf(violations);
    }

    public static SalesRequestEditValidationView valid() {
        return new SalesRequestEditValidationView(List.of());
    }

    public boolean validForm() {
        return violations.isEmpty();
    }

    public String firstMessage() {
        return violations.isEmpty() ? "" : violations.get(0).message();
    }

    public boolean hasViolation(int lineId) {
        return violations.stream().anyMatch(violation -> violation.lineId() == lineId);
    }
}
