package org.itss.prj_itss.model.request.application.processing;

import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.shared.formatting.DeliveryStatusFormatter;
import org.itss.prj_itss.model.request.domain.allocation.AllocationControl;
import org.itss.prj_itss.model.request.domain.allocation.model.Allocation;
import org.itss.prj_itss.model.request.domain.allocation.model.AllocationPlan;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.RequestProcessingData;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.suggestion.SuggestedPlan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class RequestProcessingSession {

    private final RequestProcessingUseCase requestProcessingUseCase;

    private final List<ItemRequirement> items = new ArrayList<>();
    private final List<SiteStockOption> allSites = new ArrayList<>();
    private final Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();
    private final Map<Integer, LocalDate> desiredDeliveryDates = new LinkedHashMap<>();
    private Set<Integer> excludedSiteIds = new LinkedHashSet<>();
    private Set<Integer> prioritySiteIds = new LinkedHashSet<>();

    private int requestId = -1;
    private int deadlineDays = 14;
    private int expandedItemIndex = -1;
    private LocalDate earliestDeliveryDate;
    private AllocationControl allocationControl;
    private List<SuggestedPlan> currentSuggestedPlans = List.of();

    public RequestProcessingSession(RequestProcessingUseCase requestProcessingUseCase) {
        this.requestProcessingUseCase = Objects.requireNonNull(requestProcessingUseCase, "requestProcessingUseCase");
    }

    public void start(int requestId) {
        if (requestId <= 0) {
            return;
        }
        this.requestId = requestId;
        resetProcessingState();
        loadProcessingData();
        rebuildAllocationSection();
    }

    public int requestId() {
        return requestId;
    }

    public String requestCode() {
        return OrderingFormatters.formatRequestCode(requestId);
    }

    public RequestProcessingViewModel buildViewModel() {
        List<RequestProcessingViewModel.AllocationItemViewModel> allocationItems = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            ItemRequirement item = items.get(index);
            int allocated = getAllocated(item.merchandiseId);
            AllocationControl.ItemAllocationState state;
            if (allocated > item.required) {
                state = AllocationControl.ItemAllocationState.OVER;
            } else if (allocated == item.required) {
                state = AllocationControl.ItemAllocationState.COMPLETE;
            } else if (allocated > 0) {
                state = AllocationControl.ItemAllocationState.PARTIAL;
            } else {
                state = AllocationControl.ItemAllocationState.NONE;
            }

            String statusText = switch (state) {
                case OVER -> "Vượt mức";
                case COMPLETE -> "Đủ";
                case PARTIAL -> "Chưa đủ";
                case NONE -> "Chưa có phương án";
            };
            String statusClass = switch (state) {
                case OVER -> "allocation-fraction-over";
                case COMPLETE -> "allocation-fraction-complete";
                case PARTIAL -> "allocation-fraction-partial";
                case NONE -> "allocation-fraction-muted";
            };

            int totalStock = allSites.stream()
                .filter(site -> !excludedSiteIds.contains(site.id))
                .mapToInt(site -> site.stock.getOrDefault(item.merchandiseId, 0))
                .sum();

            boolean expanded = expandedItemIndex == index;
            List<RequestProcessingViewModel.AllocationSiteRowViewModel> siteRows = new ArrayList<>();
            if (expanded) {
                for (SiteStockOption site : allSites) {
                    if (excludedSiteIds.contains(site.id)) continue;
                    if (site.stock.getOrDefault(item.merchandiseId, 0) <= 0) continue;
                    AllocationControl.AllocationSiteRowState stateRow = allocationControl.siteRowState(item, site);
                    var deliveryView = DeliveryStatusFormatter.format(stateRow.deliveryStatus().dayDelta(), stateRow.deliveryStatus().available());
                    siteRows.add(new RequestProcessingViewModel.AllocationSiteRowViewModel(
                        item.merchandiseId,
                        site.id,
                        stateRow.siteName(),
                        stateRow.siteDetail(),
                        stateRow.stock(),
                        stateRow.quantity(),
                        stateRow.selectedTransportLabel(),
                        stateRow.transportLabels(),
                        stateRow.transportDisabled(),
                        deliveryView.text(),
                        deliveryView.styleClass()
                    ));
                }
            }

            allocationItems.add(new RequestProcessingViewModel.AllocationItemViewModel(
                item.merchandiseId,
                item.code,
                item.name,
                item.required,
                allocated,
                totalStock,
                statusText,
                allocated + "/" + item.required,
                expanded,
                siteRows
            ));
        }

        List<ProcessingItemView> itemViews = items.stream()
            .map(i -> new ProcessingItemView(i.merchandiseId, i.code, i.name, i.required))
            .toList();

        List<ProcessingSiteView> siteViews = allSites.stream()
            .map(s -> new ProcessingSiteView(s.id, s.siteCode, s.name, s.description, s.shipDays, s.airDays, s.stock))
            .toList();

        Map<Integer, String> desiredDateViews = new LinkedHashMap<>();
        desiredDeliveryDates.forEach((k, v) -> desiredDateViews.put(k, OrderingFormatters.formatDate(v)));

        return new RequestProcessingViewModel(
            requestId,
            requestCode(),
            OrderingFormatters.formatDate(earliestDeliveryDate),
            deadlineDays,
            itemViews,
            siteViews,
            desiredDateViews,
            allocationItems
        );
    }

    public void handleSiteFilterChanged(Set<Integer> excludedSiteIds, Set<Integer> prioritySiteIds) {
        this.excludedSiteIds = copyIds(excludedSiteIds);
        this.prioritySiteIds = copyIds(prioritySiteIds);
        AllocationPlan.using(allocations).removeSites(this.excludedSiteIds);
        rebuildAllocationSection();
    }

    public void handleOptimizeAllocation() {
        allocationControl.applyOptimalAllocation();
    }

    public List<SuggestedPlanView> handleShowAllPlans() {
        currentSuggestedPlans = allocationControl.buildSuggestedPlans();
        return currentSuggestedPlans.stream()
            .map(p -> new SuggestedPlanView(
                p.signature(),
                p.totalQuantity(),
                p.totalLineCount(),
                p.siteCount(),
                p.prioritySiteCount(),
                p.totalDeliveryDays()
            ))
            .toList();
    }

    public void applySelectedPlanBySignature(String signature) {
        currentSuggestedPlans.stream()
            .filter(p -> p.signature().equals(signature))
            .findFirst()
            .ifPresent(allocationControl::applySelectedPlan);
    }

    public AllocationChangeResultView handleAllocationInputChanged(AllocationChangeCommand command) {
        ItemRequirement item = items.stream()
            .filter(i -> i.merchandiseId == command.itemMerchandiseId())
            .findFirst()
            .orElse(null);
        SiteStockOption site = allSites.stream()
            .filter(s -> s.id == command.siteId())
            .findFirst()
            .orElse(null);
        if (item == null || site == null) {
            var deliveryView = DeliveryStatusFormatter.format(0, false);
            return new AllocationChangeResultView(
                false,
                "INVALID",
                0,
                0,
                0,
                false,
                deliveryView.text(),
                deliveryView.styleClass()
            );
        }

        AllocationControl.AllocationChangeResult result = allocationControl.applyAllocationChange(
            new AllocationControl.AllocationChangeRequest(item, site, command.quantityText(), command.transportLabel())
        );

        String errorType = result.error() == AllocationControl.AllocationInputError.NONE ? null :
            switch (result.error()) {
                case INVALID_INTEGER -> "INVALID_INTEGER";
                case NEGATIVE_QUANTITY -> "NEGATIVE_QUANTITY";
                case EXCEEDS_STOCK -> "EXCEEDS_STOCK";
                case NONE -> null;
            };

        var deliveryView = DeliveryStatusFormatter.format(result.deliveryStatus().dayDelta(), result.deliveryStatus().available());
        return new AllocationChangeResultView(
            result.applied(),
            errorType,
            result.stock(),
            result.deliveryStatus().deliveryDays(),
            result.deliveryStatus().dayDelta(),
            result.deliveryStatus().available(),
            deliveryView.text(),
            deliveryView.styleClass()
        );
    }

    public void toggleExpandedItem(int index) {
        expandedItemIndex = expandedItemIndex == index ? -1 : index;
    }

    public ConfirmResult handleConfirm() {
        String validationMessage = validateCurrentSubmission();
        if (validationMessage != null) {
            return ConfirmResult.invalid(validationMessage);
        }
        return ConfirmResult.valid(buildPreviewOrderViews());
    }

    public String validateCurrentSubmission() {
        return requestProcessingUseCase.validateSubmission(
            items,
            allSites,
            allocations,
            desiredDeliveryDates,
            deadlineDays
        );
    }

    public List<ProcessingPreviewOrderView> buildPreviewOrderViews() {
        var previewOrders = requestProcessingUseCase.buildPreviewOrders(items, allSites, allocations, desiredDeliveryDates);
        return previewOrders.stream().map(po -> new ProcessingPreviewOrderView(
            po.site().name,
            po.site().siteCode,
            po.lines().stream().map(line -> new ProcessingPreviewOrderView.ProcessingPreviewLineView(
                line.item().code,
                line.item().name,
                line.quantity(),
                line.transport(),
                OrderingFormatters.formatDate(line.desiredDate()),
                OrderingFormatters.formatDate(line.estimatedDate())
            )).toList()
        )).toList();
    }

    public void submitAllocatedOrders() throws RequestProcessingException {
        requestProcessingUseCase.createAllocatedOrders(requestId, allocations);
    }

    public boolean isSiteExcluded(int siteId) {
        return excludedSiteIds.contains(siteId);
    }

    public boolean isSitePriority(int siteId) {
        return prioritySiteIds.contains(siteId);
    }

    public Set<Integer> excludedSiteIds() {
        return Set.copyOf(excludedSiteIds);
    }

    public Set<Integer> prioritySiteIds() {
        return Set.copyOf(prioritySiteIds);
    }

    public int expandedItemIndex() {
        return expandedItemIndex;
    }

    private void resetProcessingState() {
        items.clear();
        allSites.clear();
        allocations.clear();
        desiredDeliveryDates.clear();
        excludedSiteIds = new LinkedHashSet<>();
        prioritySiteIds = new LinkedHashSet<>();
        earliestDeliveryDate = null;
        deadlineDays = 14;
        expandedItemIndex = -1;
        allocationControl = null;
        currentSuggestedPlans = List.of();
    }

    private void loadProcessingData() {
        RequestProcessingData data = requestProcessingUseCase.loadProcessingData(requestId);
        items.addAll(data.items());
        allSites.addAll(data.sites());
        desiredDeliveryDates.putAll(data.desiredDeliveryDates());
        earliestDeliveryDate = data.earliestDeliveryDate();
        deadlineDays = data.deadlineDays();

        for (ItemRequirement item : items) {
            allocations.put(item.merchandiseId, new LinkedHashMap<>());
        }
    }

    private void rebuildAllocationSection() {
        allocationControl = createAllocationControl();
    }

    private AllocationControl createAllocationControl() {
        return new AllocationControl(
            items,
            allSites,
            excludedSiteIds,
            prioritySiteIds,
            allocations,
            deadlineDays,
            requestProcessingUseCase.allocationSuggester()
        );
    }

    private int getAllocated(int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Collections.emptyMap())
            .values()
            .stream()
            .mapToInt(Allocation::getQuantity)
            .sum();
    }



    private Set<Integer> copyIds(Set<Integer> ids) {
        return ids == null ? new LinkedHashSet<>() : new LinkedHashSet<>(ids);
    }

    public record ConfirmResult(String validationMessage, List<ProcessingPreviewOrderView> previewOrders) {
        public static ConfirmResult invalid(String validationMessage) {
            return new ConfirmResult(validationMessage, List.of());
        }

        public static ConfirmResult valid(List<ProcessingPreviewOrderView> previewOrders) {
            return new ConfirmResult(null, List.copyOf(previewOrders));
        }

        public boolean valid() {
            return validationMessage == null;
        }
    }
}
