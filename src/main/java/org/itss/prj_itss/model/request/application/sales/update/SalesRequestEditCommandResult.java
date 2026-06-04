package org.itss.prj_itss.model.request.application.sales.update;

public record SalesRequestEditCommandResult(
        SalesRequestEditDraft draft,
        SalesRequestEditValidationResult validationResult
) {
}
