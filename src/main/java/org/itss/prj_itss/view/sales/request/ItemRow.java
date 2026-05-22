package org.itss.prj_itss.view.sales.request;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.itss.prj_itss.model.request.application.sales.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ItemRow {

    public final SimpleObjectProperty<MerchandiseOption> merchandise = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<BigDecimal>  quantity    = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<LocalDate>   desiredDate = new SimpleObjectProperty<>();
    public final BooleanProperty                   selected    = new SimpleBooleanProperty(false);

    public ItemRow() {}

    public ItemRow(MerchandiseOption merchandise, BigDecimal quantity, LocalDate desiredDate) {
        this.merchandise.set(merchandise);
        this.quantity.set(quantity);
        this.desiredDate.set(desiredDate);
    }
}
