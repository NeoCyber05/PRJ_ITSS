package org.itss.prj_itss.controller.sales.request.update;

import java.util.List;

public interface ISalesRequestEditViewPort {

    void bindEvents(ISalesRequestEditActions events);

    void render(SalesRequestEditViewState viewModel);

    void renderItems(List<SalesRequestEditItemView> items);

    void renderValidation(SalesRequestEditValidationView validation);

    void focusFirstViolation(List<SalesRequestEditFieldViolationView> violations);

    void showSuccess(String message);

    void showError(String message);

    void close();
}
