package org.itss.prj_itss.model.request.domain.request;

public enum RequestStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    COMPLETED("completed");

    private final String displayValue;

    RequestStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    /** Giá trị ngắn gọn, thân thiện để hiển thị (vd: "pending"). */
    public String displayValue() {
        return displayValue;
    }

    /**
     * Giá trị lưu trong DB / truyền qua API (lowercase).
     * Giống displayValue() — giữ riêng để code rõ ý đồ.
     */
    public String storageValue() {
        return displayValue;
    }

    /**
     * Parse từ chuỗi lấy ra DB / API.
     * Trả về {@code null} nếu không khớp enum nào (thay vì ném exception).
     */
    public static RequestStatus fromStorageValue(String raw) {
        if (raw == null) {
            return null;
        }
        for (RequestStatus s : values()) {
            if (s.storageValue().equalsIgnoreCase(raw.trim())) {
                return s;
            }
        }
        return null;
    }
}
