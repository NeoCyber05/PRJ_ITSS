package org.itss.prj_itss.request.application.sales;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestItemInput(
    int merchandiseId,
    BigDecimal quantityOrdered,
    LocalDate desiredDeliveryDate
) {
}
