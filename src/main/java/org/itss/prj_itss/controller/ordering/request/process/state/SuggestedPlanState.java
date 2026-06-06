package org.itss.prj_itss.controller.ordering.request.process.state;

import java.util.List;

public record SuggestedPlanState(
    String signature,
    int totalQuantity,
    int totalLineCount,
    int siteCount,
    int totalDeliveryDays,
    int longestDeliveryDays,
    List<SuggestedSiteState> siteAllocations
) {
    public SuggestedPlanState {
        siteAllocations = siteAllocations == null ? List.of() : List.copyOf(siteAllocations);
    }

    public record SuggestedSiteState(
        String siteCode,
        String siteName,
        int totalQuantity,
        int lineCount,
        int deliveryDays,
        String transportSummary,
        List<SuggestedLineState> lines
    ) {
        public SuggestedSiteState {
            lines = lines == null ? List.of() : List.copyOf(lines);
        }
    }

    public record SuggestedLineState(
        String itemCode,
        String itemName,
        int quantity,
        String transportLabel,
        int deliveryDays
    ) {
    }
}
