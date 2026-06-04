package org.itss.prj_itss.view.sales.request.shared;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditItemView;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditMerchandiseOptionView;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ItemRow {

    private final int lineId;
    private final ObjectProperty<SalesRequestEditMerchandiseOptionView> merchandise = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> quantity = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> desiredDate = new SimpleObjectProperty<>();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public ItemRow(SalesRequestEditItemView item) {
        this.lineId = item.lineId();
        this.merchandise.set(item.merchandise());
        this.quantity.set(item.quantity());
        this.desiredDate.set(item.desiredDate());
    }

    public int lineId() {
        return lineId;
    }

    public SalesRequestEditMerchandiseOptionView merchandise() {
        return merchandise.get();
    }

    public ObjectProperty<SalesRequestEditMerchandiseOptionView> merchandiseProperty() {
        return merchandise;
    }

    public void setMerchandise(SalesRequestEditMerchandiseOptionView value) {
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
