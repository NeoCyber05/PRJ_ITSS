package org.itss.prj_itss.model.request.application.processing;

import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationPlan;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryOptions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RequestProcessingPreviewBuilder implements PreviewBuilder {

    private List<ItemRequirement> items;
    private List<SiteStockOption> sites;
    private Map<Integer, Map<Integer, Allocation>> allocations;
    private Map<Integer, LocalDate> desiredDeliveryDates;

    public RequestProcessingPreviewBuilder() {
        reset();
    }

    @Override
    public void reset() {
        this.items = null;
        this.sites = null;
        this.allocations = null;
        this.desiredDeliveryDates = null;
    }

    @Override
    public RequestProcessingPreviewBuilder items(List<ItemRequirement> items) {
        this.items = items;
        return this;
    }

    @Override
    public RequestProcessingPreviewBuilder sites(List<SiteStockOption> sites) {
        this.sites = sites;
        return this;
    }

    @Override
    public RequestProcessingPreviewBuilder allocations(Map<Integer, Map<Integer, Allocation>> allocations) {
        this.allocations = allocations;
        return this;
    }

    @Override
    public RequestProcessingPreviewBuilder desiredDeliveryDates(Map<Integer, LocalDate> desiredDeliveryDates) {
        this.desiredDeliveryDates = desiredDeliveryDates;
        return this;
    }

    public List<PreviewOrder> getProduct() {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(sites, "sites");
        Objects.requireNonNull(allocations, "allocations");
        Objects.requireNonNull(desiredDeliveryDates, "desiredDeliveryDates");

        List<PreviewOrder> previewOrders = new ArrayList<>();
        Map<Integer, SiteStockOption> sitesById = sites.stream()
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

        reset();
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
