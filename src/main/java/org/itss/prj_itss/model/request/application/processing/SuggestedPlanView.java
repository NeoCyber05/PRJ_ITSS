package org.itss.prj_itss.model.request.application.processing;

public record SuggestedPlanView(
    String signature,
    int totalQuantity,
    int totalLineCount,
    int siteCount,
    int prioritySiteCount,
    int totalDeliveryDays
) {
}
