package org.itss.prj_itss.model.request.application.sales.shared;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesRequestItemSubmission(
    int merchandiseId,
    BigDecimal quantityOrdered,
    LocalDate desiredDeliveryDate
) {
}
