package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.util.List;

public record SalesRequestEditLoadResult(
        SalesRequestEditDraft draft,
        List<MerchandiseOption> merchandiseOptions,
        SalesRequestEditValidationResult validationResult
) {

    public SalesRequestEditLoadResult {
        merchandiseOptions = List.copyOf(merchandiseOptions);
    }
}
