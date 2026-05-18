package org.itss.prj_itss.order.domain;

public enum DeliveryMethod {
    SHIP("ship", "\u0110\u01b0\u1eddng bi\u1ec3n"),
    AIR("air", "H\u00e0ng kh\u00f4ng");

    private final String storageValue;
    private final String displayLabel;

    DeliveryMethod(String storageValue, String displayLabel) {
        this.storageValue = storageValue;
        this.displayLabel = displayLabel;
    }

    public String storageValue() {
        return storageValue;
    }

    public String displayLabel() {
        return displayLabel;
    }

    public static DeliveryMethod fromRaw(String rawTransport) {
        if (rawTransport == null) {
            return null;
        }

        String normalized = rawTransport.trim().toLowerCase();
        if (normalized.contains("air")
            || normalized.contains("hang khong")
            || normalized.contains("h\u00e0ng kh\u00f4ng")
            || normalized.contains("may")
            || normalized.contains("m\u00e1y")) {
            return AIR;
        }
        if (normalized.contains("ship")
            || normalized.contains("duong bien")
            || normalized.contains("\u0111\u01b0\u1eddng bi\u1ec3n")
            || normalized.contains("tau")
            || normalized.contains("t\u00e0u")) {
            return SHIP;
        }
        return null;
    }

    public static String displayLabelOf(String rawTransport) {
        DeliveryMethod method = fromRaw(rawTransport);
        return (method == null ? SHIP : method).displayLabel();
    }
}
