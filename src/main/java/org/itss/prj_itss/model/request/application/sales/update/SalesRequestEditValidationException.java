package org.itss.prj_itss.model.request.application.sales.update;

public final class SalesRequestEditValidationException extends RuntimeException {

    private final SalesRequestEditValidationResult validationResult;

    public SalesRequestEditValidationException(SalesRequestEditValidationResult validationResult) {
        super(validationResult.firstMessage());
        this.validationResult = validationResult;
    }

    public SalesRequestEditValidationResult validationResult() {
        return validationResult;
    }
}
