package org.itss.prj_itss.model.request.application.sales.update;

import java.time.LocalDateTime;
import java.util.List;

public record SalesRequestEditDraft(
        int requestId,
        LocalDateTime createdAt,
        String status,
        List<SalesRequestEditItemDraft> items
) {

    public SalesRequestEditDraft {
        items = List.copyOf(items);
    }
}
