package org.itss.prj_itss.view.sales.request.update;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditViewState;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class SalesRequestEditItemRow {

    private final int lineId;
    private final ObjectProperty<MerchandiseOption> merchandise = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> quantity = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> desiredDate = new SimpleObjectProperty<>();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private String rawQuantity = "";

    public SalesRequestEditItemRow(SalesRequestEditViewState.Item item) {
        this.lineId = item.lineId();
        this.merchandise.set(item.merchandise());
        this.quantity.set(item.quantity());
        this.desiredDate.set(item.desiredDate());
        this.rawQuantity = item.rawQuantity();
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

    public String rawQuantity() {
        return rawQuantity;
    }

    public void setRawQuantity(String rawQuantity) {
        this.rawQuantity = rawQuantity == null ? "" : rawQuantity;
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
