package org.itss.prj_itss.controller.ordering.order.cancellation.state;

import org.itss.prj_itss.view.ordering.request.process.state.ProcessingItemView;
import org.itss.prj_itss.view.ordering.request.process.state.ProcessingSiteView;

import java.util.List;
import java.util.Map;

public record CancelledOrderProcessingViewModel(
    int cancelledOrderId,
    String cancelledOrderCode,
    int requestId,
    String requestCode,
    String desiredDeliveryDate,
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
