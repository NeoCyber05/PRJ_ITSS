package org.itss.prj_itss.request.domain.suggestion;

import org.itss.prj_itss.request.domain.allocation.AllocationDraft;

import java.util.List;
import java.util.Map;

public record SuggestedPlan(
    Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem,
    List<SiteOrderSuggestion> siteOrders,
    int totalQuantity,
    int totalLineCount,
    int siteCount,
    int prioritySiteCount,
    int totalDeliveryDays,
    String signature
) {
}
