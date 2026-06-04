package org.itss.prj_itss.model.request.application.sales.create;

import java.time.LocalDate;

public record SalesRequestCreationItemDraft(
        String merchandiseCode,
        String quantityText,
        LocalDate desiredDate
) {
}
