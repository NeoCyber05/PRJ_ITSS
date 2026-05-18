package org.itss.prj_itss.request.application.processing;

public record SuggestedPlanView(
    String signature,
    int totalQuantity,
    int totalLineCount,
    int siteCount,
    int prioritySiteCount,
    int totalDeliveryDays
) {
}
