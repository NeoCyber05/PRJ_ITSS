package org.itss.prj_itss.request.application.processing;

import java.util.List;
import java.util.Map;

public record RequestProcessingViewModel(
    int requestId,
    String requestCode,
    String earliestDeliveryDate,
    int deadlineDays,
    List<ProcessingItemView> items,
    List<ProcessingSiteView> sites,
    Map<Integer, String> desiredDeliveryDates,
    List<AllocationItemViewModel> allocationItems
) {

    public record AllocationItemViewModel(
        int merchandiseId,
        String code,
        String name,
        int required,
        int allocated,
        int totalStock,
        String allocationStatusText,
        String allocationFractionText,
        boolean expanded,
        List<AllocationSiteRowViewModel> siteRows
    ) {
    }

    public record AllocationSiteRowViewModel(
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
