package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditDraft;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidationResult;

import java.util.List;

public record SalesRequestEditViewState(
        String requestCode,
        String createdAt,
        String status,
        List<MerchandiseOption> merchandiseOptions,
        SalesRequestEditDraft draft,
        SalesRequestEditValidationResult validationResult
) {

    public SalesRequestEditViewState {
        merchandiseOptions = List.copyOf(merchandiseOptions);
    }
}
