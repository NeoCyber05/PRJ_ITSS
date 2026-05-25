package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.update.UpdateOrderRequestDraft;
import org.itss.prj_itss.model.request.application.sales.update.ValidationResult;

import java.util.List;

public record UpdateOrderRequestViewModel(
        String requestCode,
        String createdAt,
        String status,
        List<MerchandiseOption> merchandiseOptions,
        UpdateOrderRequestDraft draft,
        ValidationResult validationResult
) {

    public UpdateOrderRequestViewModel {
        merchandiseOptions = List.copyOf(merchandiseOptions);
    }
}
