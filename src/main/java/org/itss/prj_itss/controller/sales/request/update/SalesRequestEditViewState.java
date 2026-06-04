package org.itss.prj_itss.controller.sales.request.update;

import java.util.List;

public record SalesRequestEditViewState(
        String requestCode,
        String createdAt,
        String status,
        List<SalesRequestEditMerchandiseOptionView> merchandiseOptions,
        List<SalesRequestEditItemView> items,
        SalesRequestEditValidationView validation
) {

    public SalesRequestEditViewState {
        merchandiseOptions = List.copyOf(merchandiseOptions);
        items = List.copyOf(items);
    }
}
