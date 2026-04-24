package org.itss.prj_itss.request.processing;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class RequestProcessingPreviewBuilder {

    List<PreviewOrder> build(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        Map<Integer, LocalDate> desiredDeliveryDates
    ) {
        List<PreviewOrder> previewOrders = new ArrayList<>();

        for (Map.Entry<Integer, List<Allocation>> siteEntry : RequestProcessingAllocationSupport.groupAllocationsBySite(allocations).entrySet()) {
            SiteStockOption site = RequestProcessingAllocationSupport.findSiteInfo(allSites, siteEntry.getKey());
            if (site == null) {
                continue;
            }

            List<PreviewLine> lines = new ArrayList<>();
            for (Allocation allocation : siteEntry.getValue()) {
                ItemRequirement item = RequestProcessingAllocationSupport.findItem(items, allocation.merchandiseId);
                if (item == null) {
                    continue;
                }

                LocalDate desiredDate = desiredDeliveryDates.get(item.merchandiseId);
                int deliveryDays = RequestProcessingAllocationSupport.getDeliveryDays(site, allocation.transport);
                LocalDate estimatedDate = LocalDate.now().plusDays(deliveryDays);
                lines.add(new PreviewLine(
                    item,
                    allocation.getQuantity(),
                    allocation.transport,
                    desiredDate,
                    estimatedDate
                ));
            }

            lines.sort((left, right) -> left.item().code.compareToIgnoreCase(right.item().code));
            previewOrders.add(new PreviewOrder(site, lines));
        }

        return previewOrders;
    }

    record PreviewOrder(SiteStockOption site, List<PreviewLine> lines) {
    }

    record PreviewLine(
        ItemRequirement item,
        int quantity,
        String transport,
        LocalDate desiredDate,
        LocalDate estimatedDate
    ) {
    }
}
