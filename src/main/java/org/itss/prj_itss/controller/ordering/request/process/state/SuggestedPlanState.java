package org.itss.prj_itss.controller.ordering.request.process.state;

public record SuggestedPlanState(
    String signature,
    int totalQuantity,
    int totalLineCount,
    int siteCount,
    int totalDeliveryDays
) {
}
