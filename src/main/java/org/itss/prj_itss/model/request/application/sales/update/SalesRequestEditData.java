package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;

import java.util.List;

public record SalesRequestEditData(
        int requestId,
        RequestFormView form,
        List<MerchandiseOption> merchandiseOptions
) {

    public SalesRequestEditData {
        merchandiseOptions = List.copyOf(merchandiseOptions);
    }

    public static SalesRequestEditData empty(int requestId) {
        return new SalesRequestEditData(requestId, null, List.of());
    }

    public boolean found() {
        return form != null;
    }
}
