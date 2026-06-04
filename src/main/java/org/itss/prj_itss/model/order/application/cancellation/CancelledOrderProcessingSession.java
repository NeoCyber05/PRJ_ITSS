package org.itss.prj_itss.model.order.application.cancellation;

import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationControl;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationDraft;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.shared.formatting.DeliveryStatusFormatter;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.order.domain.cancellation.CancelledOrderProcessingData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CancelledOrderProcessingSession {

    private final CancelledOrderProcessingUseCase useCase;

    private final List<ItemRequirement> items = new ArrayList<>();
    private final List<SiteStockOption> allSites = new ArrayList<>();
    private final Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();
    private final Set<Integer> excludedSiteIds = new LinkedHashSet<>();

    private int cancelledOrderId = -1;
    private int requestId = -1;
    private int cancelledSiteId = -1;
    private int deadlineDays = 14;
    private LocalDate desiredDeliveryDate;
    private String requestCode = "";
    private int expandedItemIndex = -1;
    private AllocationControl allocationControl;

    public CancelledOrderProcessingSession(CancelledOrderProcessingUseCase useCase) {
        this.useCase = Objects.requireNonNull(useCase, "useCase");
    }

    public void start(int cancelledOrderId) {
        if (cancelledOrderId <= 0) {
            return;
        }
        this.cancelledOrderId = cancelledOrderId;
        resetProcessingState();
        loadProcessingData();
        rebuildAllocationSection();
    }

    public int cancelledOrderId() {
        return cancelledOrderId;
    }

    public String cancelledOrderCode() {
        return OrderingFormatters.formatOrderCode(cancelledOrderId);
    }

    public int requestId() {
        return requestId;
    }

    public String requestCode() {
        return requestCode;
    }

    public CancelledOrderProcessingViewModel buildViewModel() {
        List<CancelledOrderProcessingViewModel.AllocationItemViewModel> allocationItems = new ArrayList<>();
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

            int totalStock = allSites.stream()
                .filter(site -> !excludedSiteIds.contains(site.id))
                .mapToInt(site -> site.stock.getOrDefault(item.merchandiseId, 0))
                .sum();

            boolean expanded = expandedItemIndex == index;
            List<CancelledOrderProcessingViewModel.AllocationSiteRowViewModel> siteRows = new ArrayList<>();
            if (expanded) {
                for (SiteStockOption site : allSites) {
                    if (excludedSiteIds.contains(site.id)) continue;
                    if (site.stock.getOrDefault(item.merchandiseId, 0) <= 0) continue;
                    AllocationControl.AllocationSiteRowState stateRow = allocationControl.siteRowState(item, site);
                    var deliveryView = DeliveryStatusFormatter.format(stateRow.deliveryStatus().dayDelta(), stateRow.deliveryStatus().available());
                    siteRows.add(new CancelledOrderProcessingViewModel.AllocationSiteRowViewModel(
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

            allocationItems.add(new CancelledOrderProcessingViewModel.AllocationItemViewModel(
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

        List<CancelledOrderProcessingViewModel.ItemViewModel> itemViews = items.stream()
            .map(i -> new CancelledOrderProcessingViewModel.ItemViewModel(i.merchandiseId, i.code, i.name, i.required))
            .toList();

        List<CancelledOrderProcessingViewModel.SiteViewModel> siteViews = allSites.stream()
            .map(s -> new CancelledOrderProcessingViewModel.SiteViewModel(
                s.id,
                s.siteCode,
                s.name,
                s.description,
                s.shipDays,
                s.airDays,
                s.stock
            ))
            .toList();

        Map<Integer, String> desiredDateViews = new LinkedHashMap<>();
        for (ItemRequirement item : items) {
            desiredDateViews.put(item.merchandiseId, OrderingFormatters.formatDate(desiredDeliveryDate));
        }

        return new CancelledOrderProcessingViewModel(
            cancelledOrderId,
            cancelledOrderCode(),
            requestId,
            requestCode,
            OrderingFormatters.formatDate(desiredDeliveryDate),
            deadlineDays,
            itemViews,
            siteViews,
            desiredDateViews,
            allocationItems
        );
    }

    public void handleSuggestAllocation(int optionId) {
        OrderCancellationSuggester suggester = new OrderCancellationSuggester();
        Map<Integer, Map<Integer, AllocationDraft>> drafts = suggester.suggest(
            optionId,
            items,
            allSites,
            excludedSiteIds
        );
        new org.itss.prj_itss.model.request.domain.processing.allocation.ApplyPlan(items, allocations).apply(drafts);
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
            var deliveryView = DeliveryStatusFormatter.format(0, false);
            return new AllocationChangeResult(
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
        return new AllocationChangeResult(
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
        Map<Integer, LocalDate> desiredDeliveryDates = new LinkedHashMap<>();
        for (ItemRequirement item : items) {
            desiredDeliveryDates.put(item.merchandiseId, desiredDeliveryDate);
        }
        String validationMessage = useCase.validateSubmission(
            items,
            allSites,
            allocations,
            desiredDeliveryDates,
            deadlineDays
        );
        if (validationMessage != null) {
            return ConfirmResult.invalid(validationMessage);
        }

        var previewOrders = useCase.buildPreviewOrders(items, allSites, allocations, desiredDeliveryDate);
        List<PreviewOrderView> previewViews = previewOrders.stream().map(po -> new PreviewOrderView(
            po.site().name,
            po.site().siteCode,
            po.lines().stream().map(line -> new PreviewOrderView.PreviewLineView(
                line.item().code,
                line.item().name,
                line.quantity(),
                line.transport(),
                OrderingFormatters.formatDate(line.desiredDate()),
                OrderingFormatters.formatDate(line.estimatedDate())
            )).toList()
        )).toList();

        return ConfirmResult.valid(previewViews);
    }

    public void submitAllocatedOrders() throws CancelledOrderProcessingException {
        useCase.createAllocatedOrders(cancelledOrderId, allocations);
    }

    private void resetProcessingState() {
        items.clear();
        allSites.clear();
        allocations.clear();
        excludedSiteIds.clear();
        requestId = -1;
        cancelledSiteId = -1;
        desiredDeliveryDate = null;
        deadlineDays = 14;
        requestCode = "";
        expandedItemIndex = -1;
        allocationControl = null;
    }

    private void loadProcessingData() {
        CancelledOrderProcessingData data = useCase.loadProcessingData(cancelledOrderId);
        requestId = data.requestId();
        cancelledSiteId = data.cancelledSiteId();
        requestCode = data.requestCode();
        desiredDeliveryDate = data.desiredDeliveryDate();
        deadlineDays = data.deadlineDays();
        items.addAll(data.items());
        allSites.addAll(data.sites());
        excludedSiteIds.add(cancelledSiteId);

        for (ItemRequirement item : items) {
            allocations.put(item.merchandiseId, new LinkedHashMap<>());
        }
    }

    private void rebuildAllocationSection() {
        allocationControl = new AllocationControl(
            items,
            allSites,
            excludedSiteIds,
            new LinkedHashSet<>(),
            allocations,
            deadlineDays,
            useCase.allocationSuggester()
        );
    }

    private int getAllocated(int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Collections.emptyMap())
            .values()
            .stream()
            .mapToInt(Allocation::getQuantity)
            .sum();
    }

    public record AllocationChangeCommand(
        int itemMerchandiseId,
        int siteId,
        String quantityText,
        String transportLabel
    ) {
    }

    public record AllocationChangeResult(
        boolean applied,
        String errorType,
        int stock,
        int deliveryDays,
        int dayDelta,
        boolean deliveryAvailable,
        String deliveryStatusText,
        String deliveryStatusClass
    ) {
    }

    public record PreviewOrderView(
        String siteName,
        String siteCode,
        List<PreviewLineView> lines
    ) {

        public record PreviewLineView(
            String merchandiseCode,
            String merchandiseName,
            int quantity,
            String transport,
            String desiredDate,
            String estimatedDate
        ) {
        }
    }

    public record ConfirmResult(String validationMessage, List<PreviewOrderView> previewOrders) {
        public static ConfirmResult invalid(String validationMessage) {
            return new ConfirmResult(validationMessage, List.of());
        }

        public static ConfirmResult valid(List<PreviewOrderView> previewOrders) {
            return new ConfirmResult(null, List.copyOf(previewOrders));
        }

        public boolean valid() {
            return validationMessage == null;
        }
    }
}
