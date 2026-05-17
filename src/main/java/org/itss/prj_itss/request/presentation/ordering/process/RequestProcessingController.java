package org.itss.prj_itss.request.presentation.ordering.process;

import org.itss.prj_itss.request.business.model.Allocation;
import org.itss.prj_itss.request.business.model.ItemRequirement;
import org.itss.prj_itss.request.business.model.RequestProcessingData;
import org.itss.prj_itss.request.business.model.SiteStockOption;
import org.itss.prj_itss.request.business.allocation.AllocationControl;
import org.itss.prj_itss.request.business.allocation.algo.AllSuggestAlgo.SuggestedPlan;
import org.itss.prj_itss.request.business.model.AllocationPlan;
import org.itss.prj_itss.request.business.service.RequestProcessingPreviewBuilder.PreviewOrder;
import org.itss.prj_itss.request.business.service.RequestProcessingUseCase;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class RequestProcessingController {

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

    public RequestProcessingController(RequestProcessingUseCase requestProcessingUseCase) {
        this.requestProcessingUseCase = Objects.requireNonNull(requestProcessingUseCase, "requestProcessingUseCase");
    }

    public void setRequestId(int requestId) {
        if (requestId <= 0) {
            return;
        }
        this.requestId = requestId;
        resetProcessingState();
        loadProcessingData();
        rebuildAllocationSection();
    }

    public ProcessingSnapshot snapshot() {
        return new ProcessingSnapshot(
            requestId,
            earliestDeliveryDate,
            deadlineDays,
            List.copyOf(items),
            List.copyOf(allSites),
            Collections.unmodifiableMap(new LinkedHashMap<>(desiredDeliveryDates)),
            Set.copyOf(excludedSiteIds),
            Set.copyOf(prioritySiteIds),
            expandedItemIndex,
            allocationControl
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

    public List<SuggestedPlan> handleShowAllPlans() {
        return allocationControl.buildSuggestedPlans();
    }

    public void applySelectedPlan(SuggestedPlan plan) {
        allocationControl.applySelectedPlan(plan);
    }

    public AllocationControl.AllocationChangeResult handleAllocationInputChanged(
        AllocationControl.AllocationChangeRequest request
    ) {
        return allocationControl.applyAllocationChange(request);
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
            desiredDeliveryDates,
            deadlineDays
        );
    }

    public List<PreviewOrder> buildPreviewOrders() {
        return requestProcessingUseCase.buildPreviewOrders(items, allSites, allocations, desiredDeliveryDates);
    }

    public void submitAllocatedOrders() throws SQLException {
        requestProcessingUseCase.createAllocatedOrders(requestId, allocations);
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

    private Set<Integer> copyIds(Set<Integer> ids) {
        return ids == null ? new LinkedHashSet<>() : new LinkedHashSet<>(ids);
    }

    public record ProcessingSnapshot(
        int requestId,
        LocalDate earliestDeliveryDate,
        int deadlineDays,
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, LocalDate> desiredDeliveryDates,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        int expandedItemIndex,
        AllocationControl allocationControl
    ) {
    }

    public record ConfirmResult(String validationMessage, List<PreviewOrder> previewOrders) {
        public static ConfirmResult invalid(String validationMessage) {
            return new ConfirmResult(validationMessage, List.of());
        }

        public static ConfirmResult valid(List<PreviewOrder> previewOrders) {
            return new ConfirmResult(null, List.copyOf(previewOrders));
        }

        public boolean valid() {
            return validationMessage == null;
        }
    }
}

