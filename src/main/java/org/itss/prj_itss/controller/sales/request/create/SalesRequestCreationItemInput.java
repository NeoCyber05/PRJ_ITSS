package org.itss.prj_itss.controller.sales.request.create;

import java.time.LocalDate;

public record SalesRequestCreationItemInput(String merchandiseCode, String quantityText, LocalDate desiredDate) {
}
