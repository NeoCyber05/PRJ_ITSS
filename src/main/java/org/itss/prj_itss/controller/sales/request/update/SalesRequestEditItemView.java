package org.itss.prj_itss.controller.sales.request.update;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesRequestEditItemView(
        int lineId,
        SalesRequestEditMerchandiseOptionView merchandise,
        BigDecimal quantity,
        LocalDate desiredDate
) {
}
