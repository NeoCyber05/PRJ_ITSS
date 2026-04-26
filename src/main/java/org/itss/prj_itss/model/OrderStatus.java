package org.itss.prj_itss.model;

public enum OrderStatus {
    PENDING_CONFIRMATION("pending"),
    SHIPPING("shipping"),
    DELIVERED("completed");

    private final String displayValue;

    OrderStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String displayValue() {
        return displayValue;
    }
}
