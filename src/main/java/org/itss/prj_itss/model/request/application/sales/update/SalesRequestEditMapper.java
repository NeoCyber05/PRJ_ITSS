package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SalesRequestEditMapper {

    public SalesRequestEditState toState(RequestFormView form) {
        SalesRequestEditState state = new SalesRequestEditState(
            form.id(),
            form.requestCode(),
            form.createdAt(),
            form.status()
        );

        List<SalesRequestEditItemDraft> items = new ArrayList<>();
        int lineId = 1;
        for (RequestFormView.RequestItemFormRow row : form.items()) {
            if (row.merchandise() == null) {
                continue;
            }
            items.add(new SalesRequestEditItemDraft(
                lineId++,
                row.merchandise(),
                parseQuantity(row.quantity()),
                parseDate(row.desiredDate())
            ));
        }
        state.replaceItems(items);
        return state;
    }

    public List<SalesRequestItemSubmission> toInput(SalesRequestEditDraft draft) {
        return draft.items().stream()
            .map(item -> new SalesRequestItemSubmission(
                item.merchandise().code(),
                item.quantity(),
                item.desiredDate()
            ))
            .toList();
    }

    private BigDecimal parseQuantity(String rawQuantity) {
        if (rawQuantity == null || rawQuantity.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(rawQuantity.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank() || "N/A".equalsIgnoreCase(rawDate.trim())) {
            return null;
        }
        try {
            return LocalDate.parse(rawDate.trim(), OrderingFormatters.DATE_FORMAT);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
