package org.itss.prj_itss.warehouse.domain;

public enum InspectionResult {
    ENOUGH("enough", "Đủ hàng"),
    SHORTAGE("shortage", "Thiếu hàng"),
    WRONG_ITEM("wrong_item", "Sai hàng"),
    DAMAGED("damaged", "Hàng lỗi");

    private final String storedValue;
    private final String displayValue;

    InspectionResult(String storedValue, String displayValue) {
        this.storedValue = storedValue;
        this.displayValue = displayValue;
    }

    public String storedValue() {
        return storedValue;
    }

    public String displayValue() {
        return displayValue;
    }

    public boolean indicatesDiscrepancy() {
        return this != ENOUGH;
    }

    @Override
    public String toString() {
        return displayValue;
    }
}
