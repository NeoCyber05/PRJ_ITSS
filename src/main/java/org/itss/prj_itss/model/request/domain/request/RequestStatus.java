package org.itss.prj_itss.model.request.domain.request;

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
