package org.itss.prj_itss.model.request.application.sales.update;

import java.util.List;

public record ValidationResult(List<FieldViolation> violations) {

    public ValidationResult {
        violations = List.copyOf(violations);
    }

    public static ValidationResult valid() {
        return new ValidationResult(List.of());
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
