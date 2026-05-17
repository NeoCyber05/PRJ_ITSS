package org.itss.prj_itss.request.business.service;

import org.itss.prj_itss.request.business.model.Allocation;
import org.itss.prj_itss.request.business.model.ItemRequirement;
import org.itss.prj_itss.request.business.model.SiteStockOption;
import org.itss.prj_itss.request.business.model.DeliveryMethod;
import org.itss.prj_itss.request.business.model.AllocationPlan;
import org.itss.prj_itss.request.business.model.DeliveryOptions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RequestProcessingPreviewBuilder {

    public List<PreviewOrder> build(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        Map<Integer, LocalDate> desiredDeliveryDates
    ) {
        List<PreviewOrder> previewOrders = new ArrayList<>();
        Map<Integer, SiteStockOption> sitesById = allSites.stream()
            .collect(LinkedHashMap::new, (map, site) -> map.put(site.id, site), Map::putAll);
        Map<Integer, ItemRequirement> itemsById = items.stream()
            .collect(LinkedHashMap::new, (map, item) -> map.put(item.merchandiseId, item), Map::putAll);

        for (Map.Entry<Integer, List<Allocation>> siteEntry : AllocationPlan.using(allocations).groupBySite().entrySet()) {
            SiteStockOption site = sitesById.get(siteEntry.getKey());
            if (site == null) {
                continue;
            }

            List<PreviewLine> lines = new ArrayList<>();
            for (Allocation allocation : siteEntry.getValue()) {
                ItemRequirement item = itemsById.get(allocation.merchandiseId);
                if (item == null) {
                    continue;
                }

                LocalDate desiredDate = desiredDeliveryDates.get(item.merchandiseId);
                DeliveryMethod method = DeliveryOptions.resolve(site, allocation.transport, Integer.MAX_VALUE);
                int deliveryDays = DeliveryOptions.deliveryDays(site, method);
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

    public record PreviewOrder(SiteStockOption site, List<PreviewLine> lines) {
    }

    public record PreviewLine(
        ItemRequirement item,
        int quantity,
        String transport,
        LocalDate desiredDate,
        LocalDate estimatedDate
    ) {
    }
}


