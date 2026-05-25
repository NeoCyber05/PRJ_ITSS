package org.itss.prj_itss.model.request.application.sales.update;

import java.util.List;

public record UpdateOrderRequestDraft(
        int requestId,
        String requestCode,
        String createdAt,
        String status,
        List<UpdateOrderRequestItemDraft> items
) {

    public UpdateOrderRequestDraft {
        items = List.copyOf(items);
    }
}
