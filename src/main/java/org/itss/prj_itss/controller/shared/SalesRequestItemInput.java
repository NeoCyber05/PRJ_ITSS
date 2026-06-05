package org.itss.prj_itss.controller.shared;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesRequestItemInput(
    int merchandiseId,
    BigDecimal quantity,
    LocalDate desiredDate
) {
}
