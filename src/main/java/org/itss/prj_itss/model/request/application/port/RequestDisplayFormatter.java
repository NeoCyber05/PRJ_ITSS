package org.itss.prj_itss.model.request.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface RequestDisplayFormatter {

    String pendingStatusKey();

    String formatRequestCode(int requestId);

    String formatOrderCode(int orderId);

    int parseEntityId(String raw, int fallback);

    String formatQuantity(BigDecimal quantity);

    String formatDate(LocalDate date);

    String formatDateOrEmpty(LocalDateTime dateTime);

    String normalizeStatusKey(String status);

    boolean statusMatches(String rawStatus, String selectedStatusKey);

    String requestStatusText(String status);

    String orderStatusText(String status);

    String deliveryMethodText(String deliveryMethod);

    LocalDate parseDisplayDate(String rawDate);
}
