package org.itss.prj_itss.model.request.application.sales.update;

import java.util.List;

public record SalesRequestEditValidationResult(List<SalesRequestEditFieldViolation> violations) {

    public SalesRequestEditValidationResult {
        violations = List.copyOf(violations);
    }

    public static SalesRequestEditValidationResult valid() {
        return new SalesRequestEditValidationResult(List.of());
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
