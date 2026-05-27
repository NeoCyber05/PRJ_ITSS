package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesRequestEditItemDraft(
        int lineId,
        MerchandiseOption merchandise,
        BigDecimal quantity,
        LocalDate desiredDate
) {
}
