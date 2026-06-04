package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditLoadResult;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditSaveResult;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidationResult;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditDraft;

public interface SalesRequestEditPresenter {

    SalesRequestEditViewState present(SalesRequestEditLoadResult result);

    SalesRequestEditValidationView presentValidation(SalesRequestEditValidationResult validationResult);

    SalesRequestEditValidationView presentValidation(SalesRequestEditSaveResult result);

    String presentRequestCode(SalesRequestEditDraft draft);
}
