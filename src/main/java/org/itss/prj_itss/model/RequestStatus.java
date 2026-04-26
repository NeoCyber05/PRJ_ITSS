package org.itss.prj_itss.model;

public enum RequestStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    COMPLETED("completed");

    private final String displayValue;

    RequestStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String displayValue() {
        return displayValue;
    }
}
