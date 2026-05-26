package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditFieldViolation;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditItemDraft;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidationResult;

import java.util.List;

public interface SalesRequestEditViewPort {

    void bindEvents(SalesRequestEditActions events);

    void render(SalesRequestEditViewState viewModel);

    void renderItems(List<SalesRequestEditItemDraft> items);

    void renderValidation(SalesRequestEditValidationResult validationResult);

    void focusFirstViolation(List<SalesRequestEditFieldViolation> violations);

    void showSuccess(String message);

    void showError(String message);

    void close();
}
