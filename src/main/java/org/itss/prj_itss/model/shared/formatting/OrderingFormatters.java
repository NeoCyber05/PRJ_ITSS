package org.itss.prj_itss.model.shared.formatting;

import org.itss.prj_itss.model.order.domain.DeliveryMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OrderingFormatters {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final String STATUS_ALL = "all";
    public static final String STATUS_OTHER = "other";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_SHIPPING = "shipping";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    private static final Pattern NUMBER_GROUP = Pattern.compile("(\\d+)");

    private OrderingFormatters() {
    }

    public static String formatOrderCode(int orderId) {
        return String.format("DH-2026-%03d", orderId);
    }

    public static String formatRequestCode(int requestId) {
        return String.format("YC-2026-%03d", requestId);
    }

    public static int parseEntityId(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }

        Matcher matcher = NUMBER_GROUP.matcher(raw);
        int parsed = fallback;
        while (matcher.find()) {
            try {
                parsed = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                parsed = fallback;
            }
        }
        return parsed > 0 ? parsed : fallback;
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "N/A" : date.format(DATE_FORMAT);
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "N/A" : dateTime.toLocalDate().format(DATE_FORMAT);
    }

    public static String formatDateOrEmpty(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.toLocalDate().format(DATE_FORMAT);
    }

    public static String formatDateTimeMultiline(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.toLocalTime().withSecond(0).withNano(0) + "\n" + dateTime.toLocalDate().format(DATE_FORMAT);
    }

    public static String formatDays(Integer days) {
        return days == null ? "N/A" : days + " ng\u00e0y";
    }

    public static String formatItemTypes(int count) {
        return count + " lo\u1ea1i";
    }

    public static String blankToFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public static String normalizeStatusKey(String status) {
        if (status == null) {
            return STATUS_OTHER;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? STATUS_OTHER : normalized;
    }

    public static boolean statusMatches(String rawStatus, String selectedStatusKey) {
        if (selectedStatusKey == null || STATUS_ALL.equalsIgnoreCase(selectedStatusKey)) {
            return true;
        }
        return selectedStatusKey.equalsIgnoreCase(normalizeStatusKey(rawStatus));
    }

    public static String requestStatusText(String status) {
        return switch (normalizeStatusKey(status)) {
            case STATUS_PENDING -> "Ch\u1edd x\u1eed l\u00fd";
            case STATUS_PROCESSING -> "\u0110ang x\u1eed l\u00fd";
            case STATUS_SHIPPING -> "\u0110ang giao";
            case STATUS_COMPLETED -> "\u0110\u00e3 ho\u00e0n th\u00e0nh";
            case STATUS_CANCELLED -> "\u0110\u00e3 h\u1ee7y";
            default -> status == null || status.isBlank() ? "N/A" : status.trim();
        };
    }

    public static String orderStatusText(String status) {
        return switch (normalizeStatusKey(status)) {
            case STATUS_PENDING -> "Ch\u1edd x\u00e1c nh\u1eadn";
            case STATUS_PROCESSING -> "\u0110ang x\u1eed l\u00fd";
            case STATUS_SHIPPING -> "\u0110ang giao";
            case STATUS_COMPLETED -> "\u0110\u00e3 ho\u00e0n th\u00e0nh";
            case STATUS_CANCELLED -> "\u0110\u00e3 h\u1ee7y";
            default -> status == null || status.isBlank() ? "N/A" : status.trim();
        };
    }

    public static String toStatusKey(String selectedStatus) {
        if (selectedStatus == null || selectedStatus.isBlank()) {
            return STATUS_ALL;
        }

        return switch (selectedStatus.trim()) {
            case "M\u1ecdi tr\u1ea1ng th\u00e1i", "Moi trang thai" -> STATUS_ALL;
            case "Ch\u1edd x\u00e1c nh\u1eadn", "Cho xac nhan", "Ch\u1edd x\u1eed l\u00fd", "Cho xu ly" -> STATUS_PENDING;
            case "\u0110ang x\u1eed l\u00fd", "Dang xu ly" -> STATUS_PROCESSING;
            case "\u0110ang giao", "Dang giao" -> STATUS_SHIPPING;
            case "\u0110\u00e3 ho\u00e0n th\u00e0nh", "Da hoan thanh" -> STATUS_COMPLETED;
            case "\u0110\u00e3 h\u1ee7y", "Da huy" -> STATUS_CANCELLED;
            default -> normalizeStatusKey(selectedStatus);
        };
    }

    public static String toOrderStatusKey(String selectedStatus) {
        return toStatusKey(selectedStatus);
    }

    public static String toRequestStatusKey(String selectedStatus) {
        return toStatusKey(selectedStatus);
    }

    public static String deliveryMethodText(String deliveryMethod) {
        if (deliveryMethod == null || deliveryMethod.isBlank()) {
            return "N/A";
        }
        DeliveryMethod method = DeliveryMethod.fromRaw(deliveryMethod);
        return method == null ? deliveryMethod.trim() : method.displayLabel();
    }

    public static boolean isSeaTransport(String deliveryMethod) {
        return DeliveryMethod.SHIP == DeliveryMethod.fromRaw(deliveryMethod);
    }
}
