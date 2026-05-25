package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.model.request.application.sales.update.FieldViolation;
import org.itss.prj_itss.model.request.application.sales.update.UpdateOrderRequestItemDraft;
import org.itss.prj_itss.model.request.application.sales.update.ValidationResult;

import java.util.List;

public interface UpdateOrderRequestScreen {

    void bindEvents(UpdateOrderRequestEvents events);

    void render(UpdateOrderRequestViewModel viewModel);

    void renderItems(List<UpdateOrderRequestItemDraft> items);

    void renderValidation(ValidationResult validationResult);

    void focusFirstViolation(List<FieldViolation> violations);

    void showSuccess(String message);

    void showError(String message);

    void close();
}
