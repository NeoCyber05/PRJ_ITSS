package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesRequestCreationValidatedItem(
        MerchandiseOption merchandise,
        BigDecimal quantity,
        LocalDate desiredDate
) {
}
