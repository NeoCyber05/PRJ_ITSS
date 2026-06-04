package org.itss.prj_itss.controller.sales.request.create;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.util.List;

public record SalesRequestCreationViewState(List<MerchandiseOption> merchandiseOptions) {

    public SalesRequestCreationViewState {
        merchandiseOptions = List.copyOf(merchandiseOptions);
    }
}
