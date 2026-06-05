package org.itss.prj_itss.model.request.application.sales.update;

import java.math.BigDecimal;
import java.util.List;

public record ValidatedSalesRequestEditDraft(
        int requestId,
        String requestCode,
        List<SalesRequestEditItemDraft> items
) {

    public ValidatedSalesRequestEditDraft {
        items = List.copyOf(items);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Validated request edit draft must contain at least one item.");
        }
        for (SalesRequestEditItemDraft item : items) {
            if (item.merchandise() == null || item.quantity() == null || item.desiredDate() == null) {
                throw new IllegalArgumentException("Validated request edit draft contains an incomplete item.");
            }
            if (item.quantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Validated request edit draft contains non-positive quantity.");
            }
        }
    }
}
