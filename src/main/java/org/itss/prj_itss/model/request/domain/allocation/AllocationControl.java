package org.itss.prj_itss.model.request.domain.allocation;

import org.itss.prj_itss.model.request.domain.allocation.algo.ApplyPlan;
import org.itss.prj_itss.model.request.domain.allocation.model.Allocation;
import org.itss.prj_itss.model.request.domain.allocation.suggester.AllocationSuggester;
import org.itss.prj_itss.model.request.domain.allocation.suggester.DefaultAllocationSuggester;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryOptions;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.suggestion.SuggestedPlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AllocationControl {

    private static final int MAX_SUGGESTED_PLANS = 10;
    private static final int MAX_ITEM_VARIANTS = 12;

    private final List<ItemRequirement> items;
    private final List<SiteStockOption> allSites;
    private final Set<Integer> excludedSiteIds;
    private final Set<Integer> prioritySiteIds;
    private final Map<Integer, Map<Integer, Allocation>> allocations;
    private final int deadlineDays;
    private final ApplyPlan applyPlan;
    private final AllocationSuggester allocationSuggester;

    public AllocationControl(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        Map<Integer, Map<Integer, Allocation>> allocations,
        int deadlineDays
    ) {
        this(
            items,
            allSites,
            excludedSiteIds,
            prioritySiteIds,
            allocations,
            deadlineDays,
            new DefaultAllocationSuggester()
        );
    }

    public AllocationControl(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        Map<Integer, Map<Integer, Allocation>> allocations,
        int deadlineDays,
        AllocationSuggester allocationSuggester
    ) {
        this.items = Objects.requireNonNull(items, "items");
        this.allSites = Objects.requireNonNull(allSites, "allSites");
        this.excludedSiteIds = Objects.requireNonNull(excludedSiteIds, "excludedSiteIds");
        this.prioritySiteIds = Objects.requireNonNull(prioritySiteIds, "prioritySiteIds");
        this.allocations = Objects.requireNonNull(allocations, "allocations");
        this.deadlineDays = deadlineDays;
        this.applyPlan = new ApplyPlan(items, allocations);
        this.allocationSuggester = Objects.requireNonNull(allocationSuggester, "allocationSuggester");
    }

    public int getAllocated(int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Collections.emptyMap())
            .values()
            .stream()
            .mapToInt(Allocation::getQuantity)
            .sum();
    }

    public ItemAllocationSummary allocationSummary(ItemRequirement item) {
        int allocated = getAllocated(item.merchandiseId);
        ItemAllocationState state = ItemAllocationState.NONE;
        if (allocated > item.required) {
            state = ItemAllocationState.OVER;
        } else if (allocated == item.required) {
            state = ItemAllocationState.COMPLETE;
        } else if (allocated > 0) {
            state = ItemAllocationState.PARTIAL;
        }
        return new ItemAllocationSummary(allocated, item.required, state);
    }

    public AllocationSiteRowState siteRowState(ItemRequirement item, SiteStockOption site) {
        Allocation existing = allocations.getOrDefault(item.merchandiseId, Collections.emptyMap()).get(site.id);
        String selectedTransport = DeliveryOptions.resolveStorageValue(
            site,
            existing == null ? null : existing.transport,
            deadlineDays
        );
        int quantity = existing == null ? 0 : existing.getQuantity();
        return new AllocationSiteRowState(
            siteName(site),
            siteDetail(site),
            site.stock.getOrDefault(item.merchandiseId, 0),
            quantity,
            DeliveryMethod.displayLabelOf(selectedTransport),
            transportLabels(site),
            deliveryStatus(site, selectedTransport)
        );
    }

    public AllocationChangeResult applyAllocationChange(AllocationChangeRequest request) {
        Integer quantity = parseQuantity(request.quantityText());
        DeliveryStatus deliveryStatus = deliveryStatus(request.site(), request.transportLabel());
        if (quantity == null) {
            return AllocationChangeResult.rejected(AllocationInputError.INVALID_INTEGER, 0, deliveryStatus);
        }
        if (quantity < 0) {
            return AllocationChangeResult.rejected(AllocationInputError.NEGATIVE_QUANTITY, 0, deliveryStatus);
        }

        int stock = request.site().stock.getOrDefault(request.item().merchandiseId, 0);
        if (quantity > stock) {
            return AllocationChangeResult.rejected(AllocationInputError.EXCEEDS_STOCK, stock, deliveryStatus);
        }

        String transport = DeliveryOptions.resolveStorageValue(request.site(), request.transportLabel(), deadlineDays);
        updateAllocationsState(request.item(), request.site(), quantity, transport);
        return AllocationChangeResult.applied(deliveryStatus(request.site(), transport));
    }

    public void applyOptimalAllocation() {
        applyPlan.apply(allocationSuggester.buildOptimalDrafts(
            items,
            allSites,
            excludedSiteIds,
            deadlineDays
        ));
    }

    public List<SuggestedPlan> buildSuggestedPlans() {
        return allocationSuggester.buildSuggestedPlans(
            items,
            allSites,
            excludedSiteIds,
            prioritySiteIds,
            deadlineDays,
            MAX_SUGGESTED_PLANS,
            MAX_ITEM_VARIANTS
        );
    }

    public void applySelectedPlan(SuggestedPlan plan) {
        if (plan != null) {
            applyPlan.apply(plan.allocationsByItem());
        }
    }

    private List<String> transportLabels(SiteStockOption site) {
        List<String> labels = new ArrayList<>();
        if (site.shipDays < 999) {
            labels.add(DeliveryMethod.SHIP.displayLabel());
        }
        if (site.airDays < 999) {
            labels.add(DeliveryMethod.AIR.displayLabel());
        }
        if (labels.isEmpty()) {
            labels.add("KhÃ´ng kháº£ dá»¥ng");
        }
        return labels;
    }

    private DeliveryStatus deliveryStatus(SiteStockOption site, String transport) {
        int deliveryDays = DeliveryOptions.deliveryDays(
            site,
            DeliveryOptions.resolve(site, transport, deadlineDays)
        );
        return new DeliveryStatus(deliveryDays, deadlineDays - deliveryDays);
    }

    private Integer parseQuantity(String rawText) {
        String rawValue = rawText == null ? "" : rawText.trim();
        try {
            return rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void updateAllocationsState(ItemRequirement item, SiteStockOption site, int quantity, String transport) {
        if (quantity > 0) {
            Allocation allocation = allocations
                .computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>())
                .computeIfAbsent(site.id, key -> new Allocation(site.id, item.merchandiseId, 0, transport));
            allocation.setQuantity(quantity);
            allocation.transport = transport;
            return;
        }

        Map<Integer, Allocation> itemAllocations = allocations.get(item.merchandiseId);
        if (itemAllocations != null) {
            itemAllocations.remove(site.id);
        }
    }

    private static String siteName(SiteStockOption site) {
        return site.name == null ? "" : site.name;
    }

    private static String siteDetail(SiteStockOption site) {
        String detail = site.siteCode == null ? "" : site.siteCode;
        if (site.description != null && !site.description.isBlank()) {
            detail += " - " + site.description;
        }
        return detail;
    }

    public enum ItemAllocationState {
        NONE,
        PARTIAL,
        COMPLETE,
        OVER
    }

    public enum AllocationInputError {
        NONE,
        INVALID_INTEGER,
        NEGATIVE_QUANTITY,
        EXCEEDS_STOCK
    }

    public record ItemAllocationSummary(int allocated, int required, ItemAllocationState state) {
    }

    public record AllocationSiteRowState(
        String siteName,
        String siteDetail,
        int stock,
        int quantity,
        String selectedTransportLabel,
        List<String> transportLabels,
        DeliveryStatus deliveryStatus
    ) {
        public boolean transportDisabled() {
            return transportLabels.size() == 1 && "KhÃ´ng kháº£ dá»¥ng".equals(transportLabels.get(0));
        }
    }

    public record AllocationChangeRequest(
        ItemRequirement item,
        SiteStockOption site,
        String quantityText,
        String transportLabel
    ) {
    }

    public record AllocationChangeResult(
        boolean applied,
        AllocationInputError error,
        int stock,
        DeliveryStatus deliveryStatus
    ) {
        public static AllocationChangeResult applied(DeliveryStatus deliveryStatus) {
            return new AllocationChangeResult(true, AllocationInputError.NONE, 0, deliveryStatus);
        }

        public static AllocationChangeResult rejected(
            AllocationInputError error,
            int stock,
            DeliveryStatus deliveryStatus
        ) {
            return new AllocationChangeResult(false, error, stock, deliveryStatus);
        }
    }

    public record DeliveryStatus(int deliveryDays, int dayDelta) {
        public boolean available() {
            return deliveryDays < 999;
        }
    }
}

