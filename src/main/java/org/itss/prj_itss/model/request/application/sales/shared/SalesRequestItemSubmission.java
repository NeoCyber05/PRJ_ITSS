package org.itss.prj_itss.model.request.application.sales.shared;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesRequestItemSubmission(
    String merchandiseCode,
    BigDecimal quantityOrdered,
    LocalDate desiredDeliveryDate
) {
}
