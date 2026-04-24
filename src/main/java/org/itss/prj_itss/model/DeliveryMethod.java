package org.itss.prj_itss.model;

public enum DeliveryMethod {
    SHIP("ship"),
    AIR("air");

    private final String storageValue;

    DeliveryMethod(String storageValue) {
        this.storageValue = storageValue;
    }

    public String storageValue() {
        return storageValue;
    }
}
