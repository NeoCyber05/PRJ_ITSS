package org.itss.prj_itss.model.request.application.sales.update;

public record SalesRequestEditSaveResult(
        boolean success,
        String message,
        SalesRequestEditDraft draft,
        SalesRequestEditValidationResult validationResult
) {

    public static SalesRequestEditSaveResult validationFailed(
            SalesRequestEditDraft draft,
            SalesRequestEditValidationResult validationResult
    ) {
        return new SalesRequestEditSaveResult(false, validationResult.firstMessage(), draft, validationResult);
    }

    public static SalesRequestEditSaveResult saved(SalesRequestEditDraft draft, String message) {
        return new SalesRequestEditSaveResult(true, message, draft, SalesRequestEditValidationResult.valid());
    }

    public static SalesRequestEditSaveResult failed(
            SalesRequestEditDraft draft,
            SalesRequestEditValidationResult validationResult,
            String message
    ) {
        return new SalesRequestEditSaveResult(false, message, draft, validationResult);
    }
}
