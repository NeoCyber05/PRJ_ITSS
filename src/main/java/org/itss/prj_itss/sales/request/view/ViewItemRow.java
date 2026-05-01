package org.itss.prj_itss.sales.request.view;

import javafx.beans.property.SimpleObjectProperty;
import org.itss.prj_itss.entity.Merchandise;

import java.math.BigDecimal;
import java.time.LocalDate;

final class ViewItemRow {

    final SimpleObjectProperty<Merchandise> merchandise = new SimpleObjectProperty<>();
    final BigDecimal quantity;
    final LocalDate desiredDate;

    ViewItemRow(Merchandise merchandise, BigDecimal quantity, LocalDate desiredDate) {
        this.merchandise.set(merchandise);
        this.quantity = quantity;
        this.desiredDate = desiredDate;
    }
}
