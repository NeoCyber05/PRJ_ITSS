package org.itss.prj_itss.controller.sales.request.update;

import java.util.List;

public interface SalesRequestEditViewPort {

    void setActionHandler(SalesRequestEditActionHandler actionHandler);

    void render(SalesRequestEditViewState viewModel);

    void renderValidation(SalesRequestEditViewState.Validation validationResult);

    void focusFirstViolation(List<SalesRequestEditViewState.FieldViolation> violations);

    void showSuccess(String message);

    void showError(String message);

    void close();
}
