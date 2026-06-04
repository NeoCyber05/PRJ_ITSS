package org.itss.prj_itss.model.order.application.cancellation;

import java.util.List;
import java.util.Map;

public record CancelledOrderProcessingViewModel(
    int cancelledOrderId,
    String cancelledOrderCode,
    int requestId,
    String requestCode,
    String desiredDeliveryDate,
    int deadlineDays,
    List<ItemViewModel> items,
    List<SiteViewModel> sites,
    Map<Integer, String> desiredDeliveryDates,
    List<AllocationItemViewModel> allocationItems
) {

    public record ItemViewModel(
        int merchandiseId,
        String code,
        String name,
        int required
    ) {
    }

    public record SiteViewModel(
        int id,
        String siteCode,
        String name,
        String description,
        int shipDays,
        int airDays,
        Map<Integer, Integer> stock
    ) {
    }

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
