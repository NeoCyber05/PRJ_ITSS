package org.itss.prj_itss.model.order.application.cancellation;

import org.itss.prj_itss.model.order.application.port.CancelledOrderProcessingGateway;
import org.itss.prj_itss.model.order.application.port.CancelledOrderProcessingGatewayException;
import org.itss.prj_itss.model.request.domain.processing.allocation.policy.FastDeliveryObjective;
import org.itss.prj_itss.model.order.domain.cancellation.CancelledOrderProcessingData;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.suggestion.AllocationSuggester;
import org.itss.prj_itss.model.request.domain.processing.suggestion.DefaultAllocationSuggester;
import org.itss.prj_itss.model.request.domain.processing.allocation.validator.AllocationValidator;
import org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidator;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CancelledOrderProcessingUseCase {

    private final CancelledOrderProcessingGateway gateway;
    private final AllocationValidator allocationValidator;
    private final AllocationSuggester allocationSuggester;
    private final CancelledOrderProcessingPreviewBuilder previewBuilder = new CancelledOrderProcessingPreviewBuilder();

    public CancelledOrderProcessingUseCase(CancelledOrderProcessingGateway gateway) {
        this(gateway, new DefaultAllocationValidator(), new DefaultAllocationSuggester(new FastDeliveryObjective()));
    }

    public CancelledOrderProcessingUseCase(
        CancelledOrderProcessingGateway gateway,
        AllocationValidator allocationValidator,
        AllocationSuggester allocationSuggester
    ) {
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.allocationValidator = Objects.requireNonNull(allocationValidator, "allocationValidator");
        this.allocationSuggester = Objects.requireNonNull(allocationSuggester, "allocationSuggester");
    }

    public CancelledOrderProcessingData loadProcessingData(int cancelledOrderId) {
        return gateway.loadProcessingData(cancelledOrderId);
    }

    public List<String> validateAllocations(
        List<ItemRequirement> items,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) {
        return allocationValidator.validateAllocations(items, allocations);
    }

    public String validateSubmission(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        Map<Integer, LocalDate> desiredDeliveryDates
    ) {
        return allocationValidator.validateSubmission(
            items,
            allSites,
            allocations,
            desiredDeliveryDates
        );
    }

    public List<CancelledOrderProcessingPreviewBuilder.PreviewOrder> buildPreviewOrders(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        LocalDate desiredDeliveryDate
    ) {
        return previewBuilder.build(items, allSites, allocations, desiredDeliveryDate);
    }

    public void createAllocatedOrders(
        int cancelledOrderId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws CancelledOrderProcessingException {
        try {
            gateway.createAllocatedOrders(cancelledOrderId, allocations);
        } catch (CancelledOrderProcessingGatewayException exception) {
            throw new CancelledOrderProcessingException(
                "Cannot create allocated orders for cancelled order " + cancelledOrderId,
                exception
            );
        }
    }

    public AllocationSuggester allocationSuggester() {
        return allocationSuggester;
    }
}
