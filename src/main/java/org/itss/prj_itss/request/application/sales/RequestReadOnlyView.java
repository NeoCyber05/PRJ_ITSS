package org.itss.prj_itss.request.application.sales;

import java.util.List;

public record RequestReadOnlyView(
    int id,
    String requestCode,
    String createdAt,
    String status,
    String statusText,
    String note,
    List<RequestDetailItemRow> items
) {
}
