package org.itss.prj_itss.model.request.domain.processing.suggestion;

import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationDraft;

import java.util.Map;

public record ItemVariant(
    Map<Integer, AllocationDraft> allocationsBySite,
    int siteCount,
    int totalDeliveryDays,
    String signature
) {
}
