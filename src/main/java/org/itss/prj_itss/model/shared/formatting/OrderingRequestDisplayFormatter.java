package org.itss.prj_itss.model.shared.formatting;

import org.itss.prj_itss.model.request.application.port.RequestDisplayFormatter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class OrderingRequestDisplayFormatter implements RequestDisplayFormatter {

    @Override
    public String pendingStatusKey() {
        return OrderingFormatters.STATUS_PENDING;
    }

    @Override
    public String formatRequestCode(int requestId) {
        return OrderingFormatters.formatRequestCode(requestId);
    }

    @Override
    public String formatOrderCode(int orderId) {
        return OrderingFormatters.formatOrderCode(orderId);
    }

    @Override
    public int parseEntityId(String raw, int fallback) {
        return OrderingFormatters.parseEntityId(raw, fallback);
    }

    @Override
    public String formatQuantity(BigDecimal quantity) {
        return OrderingFormatters.formatQuantity(quantity);
    }

    @Override
    public String formatDate(LocalDate date) {
        return OrderingFormatters.formatDate(date);
    }

    @Override
    public String formatDateOrEmpty(LocalDateTime dateTime) {
        return OrderingFormatters.formatDateOrEmpty(dateTime);
    }

    @Override
    public String normalizeStatusKey(String status) {
        return OrderingFormatters.normalizeStatusKey(status);
    }

    @Override
    public boolean statusMatches(String rawStatus, String selectedStatusKey) {
        return OrderingFormatters.statusMatches(rawStatus, selectedStatusKey);
    }

    @Override
    public String requestStatusText(String status) {
        return OrderingFormatters.requestStatusText(status);
    }

    @Override
    public String orderStatusText(String status) {
        return OrderingFormatters.orderStatusText(status);
    }

    @Override
    public String deliveryMethodText(String deliveryMethod) {
        return OrderingFormatters.deliveryMethodText(deliveryMethod);
    }

    @Override
    public LocalDate parseDisplayDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank() || "N/A".equalsIgnoreCase(rawDate.trim())) {
            return null;
        }
        return LocalDate.parse(rawDate.trim(), OrderingFormatters.DATE_FORMAT);
    }
}
