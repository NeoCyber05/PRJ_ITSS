package org.itss.prj_itss.model.shared;

import java.util.Locale;

public final class OrderingFormatters {

    private OrderingFormatters() {
    }

    public static String renderStatusVietnamese(String status) {
        if (status == null || status.isBlank()) {
            return "Không xác định";
        }
        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "pending" -> "Chờ xác nhận";
            case "shipping" -> "Đang giao";
            case "completed" -> "Hoàn thành";
            case "cancelled" -> "Đã hủy";
            default -> status;
        };
    }

    public static String renderDeliveryMethodVietnamese(String deliveryMethod) {
        if (deliveryMethod == null || deliveryMethod.isBlank()) {
            return "Không xác định";
        }
        return switch (deliveryMethod.trim().toLowerCase(Locale.ROOT)) {
            case "air" -> "Máy bay";
            case "ship" -> "Tàu biển";
            default -> deliveryMethod;
        };
    }

    public static String formatOrderCode(int orderId) {
        return String.format("ĐH-2026-%03d", orderId);
    }

    public static String formatRequestCode(int requestId) {
        return String.format("YC-2026-%03d", requestId);
    }
}
