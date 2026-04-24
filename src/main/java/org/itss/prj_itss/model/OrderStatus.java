package org.itss.prj_itss.model;

public enum OrderStatus {
    PENDING_CONFIRMATION("Chờ xác nhận"),
    SHIPPING("Đang giao"),
    DELIVERED("Hoàn thành");

    private final String displayValue;

    OrderStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String displayValue() {
        return displayValue;
    }
}
