package org.itss.prj_itss.model.request.application.sales.create;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestItemInput(
    int merchandiseId,
    BigDecimal quantityOrdered,
    LocalDate desiredDeliveryDate
) {
}
