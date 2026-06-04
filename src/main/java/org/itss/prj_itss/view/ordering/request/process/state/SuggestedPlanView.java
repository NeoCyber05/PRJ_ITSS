package org.itss.prj_itss.view.ordering.request.process.state;

public record SuggestedPlanView(
    String signature,
    int totalQuantity,
    int totalLineCount,
    int siteCount,
    int totalDeliveryDays
) {
}
