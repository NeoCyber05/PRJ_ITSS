package org.itss.prj_itss.view.sales.request.create;

import java.time.LocalDate;

record SalesRequestCreationItemCandidate(String merchandiseCode, String quantityText, LocalDate desiredDate) {
}
