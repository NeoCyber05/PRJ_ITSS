package org.itss.prj_itss.request.domain.suggestion;

import org.itss.prj_itss.request.domain.processing.SiteStockOption;

import java.util.List;

public record SiteOrderSuggestion(
    SiteStockOption site,
    List<OrderLineSuggestion> lines,
    int totalQuantity,
    int deliveryDays,
    String transportSummary
) {
}
