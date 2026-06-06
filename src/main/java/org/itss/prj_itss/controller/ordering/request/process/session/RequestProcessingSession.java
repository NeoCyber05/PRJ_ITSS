package org.itss.prj_itss.controller.ordering.request.process.session;

import org.itss.prj_itss.model.request.application.processing.RequestProcessingException;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.shared.formatting.DeliveryStatusFormatter;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationControl;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationPlan;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.RequestProcessingData;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.suggestion.OrderLineSuggestion;
import org.itss.prj_itss.model.request.domain.processing.suggestion.SiteOrderSuggestion;
import org.itss.prj_itss.model.request.domain.processing.suggestion.SuggestedPlan;
import org.itss.prj_itss.controller.ordering.request.process.state.AllocationChangeCommand;
import org.itss.prj_itss.controller.ordering.request.process.state.AllocationChangeResult;
import org.itss.prj_itss.controller.ordering.request.process.state.ProcessingItemState;
import org.itss.prj_itss.controller.ordering.request.process.state.ProcessingPreviewOrder;
import org.itss.prj_itss.controller.ordering.request.process.state.ProcessingSiteState;
import org.itss.prj_itss.controller.ordering.request.process.state.RequestProcessingState;
import org.itss.prj_itss.controller.ordering.request.process.state.SuggestedPlanState;

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
    private Set<Integer> selectedSiteIds = new LinkedHashSet<>();

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

    public RequestProcessingState buildState() {
        List<RequestProcessingState.AllocationItemState> allocationItems = new ArrayList<>();
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
            List<RequestProcessingState.AllocationSiteRowState> siteRows = new ArrayList<>();
            if (expanded) {
                for (SiteStockOption site : allSites) {
                    if (excludedSiteIds.contains(site.id)) continue;
                    if (site.stock.getOrDefault(item.merchandiseId, 0) <= 0) continue;
                    AllocationControl.AllocationSiteRowState stateRow = allocationControl.siteRowState(item, site);
                    var deliveryStatus = DeliveryStatusFormatter.format(
                        stateRow.deliveryStatus().dayDelta(),
                        stateRow.deliveryStatus().available()
                    );
                    siteRows.add(new RequestProcessingState.AllocationSiteRowState(
                        item.merchandiseId,
                        site.id,
                        stateRow.siteName(),
                        stateRow.siteDetail(),
                        stateRow.stock(),
                        stateRow.quantity(),
                        stateRow.selectedTransportLabel(),
                        stateRow.transportLabels(),
                        stateRow.transportDisabled(),
                        deliveryStatus.text(),
                        deliveryStatus.styleClass()
                    ));
                }
            }

            allocationItems.add(new RequestProcessingState.AllocationItemState(
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

        List<ProcessingItemState> itemStates = items.stream()
            .map(i -> new ProcessingItemState(i.merchandiseId, i.code, i.name, i.required))
            .toList();

        List<ProcessingSiteState> siteStates = allSites.stream()
            .map(s -> new ProcessingSiteState(s.id, s.siteCode, s.name, s.description, s.shipDays, s.airDays, s.stock))
            .toList();

        Map<Integer, String> desiredDeliveryDateTexts = new LinkedHashMap<>();
        desiredDeliveryDates.forEach((k, v) -> desiredDeliveryDateTexts.put(k, OrderingFormatters.formatDate(v)));

        return new RequestProcessingState(
            requestId,
            requestCode(),
            OrderingFormatters.formatDate(earliestDeliveryDate),
            deadlineDays,
            itemStates,
            siteStates,
            desiredDeliveryDateTexts,
            allocationItems
        );
    }

    public void handleSiteFilterChanged(Set<Integer> excludedSiteIds, Set<Integer> selectedSiteIds) {
        this.excludedSiteIds = copyIds(excludedSiteIds);
        this.selectedSiteIds = copyIds(selectedSiteIds);
        AllocationPlan.using(allocations).removeSites(this.excludedSiteIds);
        rebuildAllocationSection();
    }

    public boolean handleOptimizeAllocation() {
        return allocationControl.applyOptimalAllocation();
    }

    public List<SuggestedPlanState> handleShowAllPlans() {
        currentSuggestedPlans = allocationControl.buildSuggestedPlans();
        return currentSuggestedPlans.stream()
            .map(this::toSuggestedPlanState)
            .toList();
    }

    private SuggestedPlanState toSuggestedPlanState(SuggestedPlan plan) {
        List<SuggestedPlanState.SuggestedSiteState> siteAllocations = plan.siteOrders().stream()
            .map(this::toSuggestedSiteState)
            .toList();
        int longestDeliveryDays = siteAllocations.stream()
            .mapToInt(SuggestedPlanState.SuggestedSiteState::deliveryDays)
            .max()
            .orElse(0);
        return new SuggestedPlanState(
            plan.signature(),
            plan.totalQuantity(),
            plan.totalLineCount(),
            plan.siteCount(),
            plan.totalDeliveryDays(),
            longestDeliveryDays,
            siteAllocations
        );
    }

    private SuggestedPlanState.SuggestedSiteState toSuggestedSiteState(SiteOrderSuggestion siteOrder) {
        SiteStockOption site = siteOrder.site();
        List<SuggestedPlanState.SuggestedLineState> lines = siteOrder.lines().stream()
            .map(this::toSuggestedLineState)
            .toList();
        return new SuggestedPlanState.SuggestedSiteState(
            safeText(site.siteCode),
            safeText(site.name),
            siteOrder.totalQuantity(),
            lines.size(),
            siteOrder.deliveryDays(),
            safeText(siteOrder.transportSummary()),
            lines
        );
    }

    private SuggestedPlanState.SuggestedLineState toSuggestedLineState(OrderLineSuggestion line) {
        ItemRequirement item = line.item();
        return new SuggestedPlanState.SuggestedLineState(
            safeText(item.code),
            safeText(item.name),
            line.quantity(),
            DeliveryMethod.displayLabelOf(line.transport()),
            line.deliveryDays()
        );
    }

    public void applySelectedPlanBySignature(String signature) {
        currentSuggestedPlans.stream()
            .filter(p -> p.signature().equals(signature))
            .findFirst()
            .ifPresent(allocationControl::applySelectedPlan);
    }

    public AllocationChangeResult handleAllocationInputChanged(AllocationChangeCommand command) {
        ItemRequirement item = items.stream()
            .filter(i -> i.merchandiseId == command.itemMerchandiseId())
            .findFirst()
            .orElse(null);
        SiteStockOption site = allSites.stream()
            .filter(s -> s.id == command.siteId())
            .findFirst()
            .orElse(null);
        if (item == null || site == null) {
            var deliveryStatus = DeliveryStatusFormatter.format(0, false);
            return new AllocationChangeResult(
                false,
                "INVALID",
                0,
                0,
                0,
                false,
                deliveryStatus.text(),
                deliveryStatus.styleClass()
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

        var deliveryStatus = DeliveryStatusFormatter.format(
            result.deliveryStatus().dayDelta(),
            result.deliveryStatus().available()
        );
        return new AllocationChangeResult(
            result.applied(),
            errorType,
            result.stock(),
            result.deliveryStatus().deliveryDays(),
            result.deliveryStatus().dayDelta(),
            result.deliveryStatus().available(),
            deliveryStatus.text(),
            deliveryStatus.styleClass()
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
        return ConfirmResult.valid(buildPreviewOrders());
    }

    public String validateCurrentSubmission() {
        return requestProcessingUseCase.validateSubmission(
            items,
            allSites,
            allocations,
            desiredDeliveryDates
        );
    }

    public List<ProcessingPreviewOrder> buildPreviewOrders() {
        var previewOrders = requestProcessingUseCase.buildPreviewOrders(items, allSites, allocations, desiredDeliveryDates);
        return previewOrders.stream().map(po -> new ProcessingPreviewOrder(
            po.site().name,
            po.site().siteCode,
            po.lines().stream().map(line -> new ProcessingPreviewOrder.ProcessingPreviewLine(
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

    public boolean isSiteSelected(int siteId) {
        return selectedSiteIds.contains(siteId);
    }

    public Set<Integer> excludedSiteIds() {
        return Set.copyOf(excludedSiteIds);
    }

    public Set<Integer> selectedSiteIds() {
        return Set.copyOf(selectedSiteIds);
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
        selectedSiteIds = new LinkedHashSet<>();
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
            selectedSiteIds,
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

    private static String safeText(String value) {
        return value == null ? "" : value;
    }

    private Set<Integer> copyIds(Set<Integer> ids) {
        return ids == null ? new LinkedHashSet<>() : new LinkedHashSet<>(ids);
    }

    public record ConfirmResult(String validationMessage, List<ProcessingPreviewOrder> previewOrders) {
        public static ConfirmResult invalid(String validationMessage) {
            return new ConfirmResult(validationMessage, List.of());
        }

        public static ConfirmResult valid(List<ProcessingPreviewOrder> previewOrders) {
            return new ConfirmResult(null, List.copyOf(previewOrders));
        }

        public boolean valid() {
            return validationMessage == null;
        }
    }
}
