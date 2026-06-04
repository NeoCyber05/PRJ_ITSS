package org.itss.prj_itss.model.request.application.sales.create;

import java.util.List;

public record SalesRequestCreationValidationResult(
        List<SalesRequestCreationFieldViolation> violations,
        List<SalesRequestCreationValidatedItem> validItems
) {

    public SalesRequestCreationValidationResult {
        violations = List.copyOf(violations);
        validItems = List.copyOf(validItems);
    }

    public boolean validForm() {
        return violations.isEmpty();
    }

    public String firstMessage() {
        return violations.isEmpty() ? "" : violations.get(0).message();
    }
}
