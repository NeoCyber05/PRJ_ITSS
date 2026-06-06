package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesRequestEditItemDraft(
        int lineId,
        MerchandiseOption merchandise,
        BigDecimal quantity,
        LocalDate desiredDate,
        String rawQuantity
) {

    public SalesRequestEditItemDraft(
            int lineId,
            MerchandiseOption merchandise,
            BigDecimal quantity,
            LocalDate desiredDate
    ) {
        this(lineId, merchandise, quantity, desiredDate, formatQuantity(quantity));
    }

    public SalesRequestEditItemDraft {
        rawQuantity = rawQuantity == null ? "" : rawQuantity;
    }

    private static String formatQuantity(BigDecimal quantity) {
        return quantity == null ? "" : OrderingFormatters.formatQuantity(quantity);
    }
}
