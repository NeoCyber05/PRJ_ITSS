package org.itss.prj_itss.model.request.application.processing;

import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.suggestion.AllocationSuggester;
import org.itss.prj_itss.model.request.domain.processing.allocation.validator.AllocationValidator;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.RequestProcessingData;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RequestProcessingUseCase {
    private final RequestProcessingGateway gateway;
    private final AllocationValidator allocationValidator;
    private final AllocationSuggester allocationSuggester;

    public RequestProcessingUseCase(
            RequestProcessingGateway gateway,
        AllocationValidator allocationValidator,
        AllocationSuggester allocationSuggester
    ) {
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.allocationValidator = Objects.requireNonNull(allocationValidator, "allocationValidator");
        this.allocationSuggester = Objects.requireNonNull(allocationSuggester, "allocationSuggester");
    }

    public RequestProcessingData loadProcessingData(int requestId) {
        try {
            return gateway.loadProcessingData(requestId);
        } catch (RequestProcessingGatewayException exception) {
            return new RequestProcessingData(requestId, null, 14, List.of(), List.of(), Map.of());
        }
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

    public List<RequestProcessingPreviewBuilder.PreviewOrder> buildPreviewOrders(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        Map<Integer, LocalDate> desiredDeliveryDates
    ) {
        return new RequestProcessingPreviewBuilder()
            .items(items)
            .sites(allSites)
            .allocations(allocations)
            .desiredDeliveryDates(desiredDeliveryDates)
            .getProduct();
    }

    public void createAllocatedOrders(
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws RequestProcessingException {
        try {
            gateway.createAllocatedOrders(requestId, allocations);
        } catch (RequestProcessingGatewayException exception) {
            throw new RequestProcessingException(
                "Cannot create allocated orders for request " + requestId,
                exception
            );
        }
    }

    public AllocationSuggester allocationSuggester() {
        return allocationSuggester;
    }
}
