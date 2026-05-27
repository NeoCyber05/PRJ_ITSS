package org.itss.prj_itss.view.sales.request.create;

import java.math.BigDecimal;
import java.time.LocalDate;

record SalesRequestCreationItemCandidate(String merchandiseCode, BigDecimal quantity, LocalDate desiredDate) {

    boolean complete() {
        return merchandiseCode != null
            && !merchandiseCode.isBlank()
            && quantity != null
            && desiredDate != null;
    }
}
