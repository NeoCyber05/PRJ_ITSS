package org.itss.prj_itss.model.request.application.sales;

import java.util.List;

public record RequestFormView(
    int id,
    String requestCode,
    String createdAt,
    String status,
    String statusText,
    String note,
    List<RequestItemFormRow> items
) {

    public record RequestItemFormRow(
        MerchandiseOption merchandise,
        String quantity,
        String desiredDate
    ) {
    }
}
