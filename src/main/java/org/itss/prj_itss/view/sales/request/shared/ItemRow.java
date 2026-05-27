package org.itss.prj_itss.view.sales.request.shared;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditItemDraft;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ItemRow {

    private final int lineId;
    private final ObjectProperty<MerchandiseOption> merchandise = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> quantity = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> desiredDate = new SimpleObjectProperty<>();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public ItemRow(SalesRequestEditItemDraft draft) {
        this.lineId = draft.lineId();
        this.merchandise.set(draft.merchandise());
        this.quantity.set(draft.quantity());
        this.desiredDate.set(draft.desiredDate());
    }

    public int lineId() {
        return lineId;
    }

    public MerchandiseOption merchandise() {
        return merchandise.get();
    }

    public ObjectProperty<MerchandiseOption> merchandiseProperty() {
        return merchandise;
    }

    public void setMerchandise(MerchandiseOption value) {
        merchandise.set(value);
    }

    public BigDecimal quantity() {
        return quantity.get();
    }

    public ObjectProperty<BigDecimal> quantityProperty() {
        return quantity;
    }

    public void setQuantity(BigDecimal value) {
        quantity.set(value);
    }

    public LocalDate desiredDate() {
        return desiredDate.get();
    }

    public ObjectProperty<LocalDate> desiredDateProperty() {
        return desiredDate;
    }

    public void setDesiredDate(LocalDate value) {
        desiredDate.set(value);
    }

    public boolean selected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
}
