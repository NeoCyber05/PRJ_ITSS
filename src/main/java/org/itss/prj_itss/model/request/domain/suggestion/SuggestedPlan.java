package org.itss.prj_itss.model.request.domain.suggestion;

import org.itss.prj_itss.model.request.domain.allocation.model.AllocationDraft;

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
