package org.itss.prj_itss.controller.ordering.request.process.state;

import java.util.List;
import java.util.Map;

public record RequestProcessingState(
    int requestId,
    String requestCode,
    String earliestDeliveryDate,
    int deadlineDays,
    List<ProcessingItemState> items,
    List<ProcessingSiteState> sites,
    Map<Integer, String> desiredDeliveryDates,
    List<AllocationItemState> allocationItems
) {

    public record AllocationItemState(
        int merchandiseId,
        String code,
        String name,
        int required,
        int allocated,
        int totalStock,
        String allocationStatusText,
        String allocationFractionText,
        boolean expanded,
        List<AllocationSiteRowState> siteRows
    ) {
    }

    public record AllocationSiteRowState(
        int itemMerchandiseId,
        int siteId,
        String siteName,
        String siteDetail,
        int stock,
        int quantity,
        String selectedTransportLabel,
        List<String> transportLabels,
        boolean transportDisabled,
        String deliveryStatusText,
        String deliveryStatusClass
    ) {
    }
}

