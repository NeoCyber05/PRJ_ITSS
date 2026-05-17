package org.itss.prj_itss.request.business.service;

import org.itss.prj_itss.request.business.allocation.DefaultAllocationSuggester;
import org.itss.prj_itss.request.business.allocation.DefaultAllocationValidator;
import org.itss.prj_itss.request.business.model.Allocation;
import org.itss.prj_itss.request.business.model.ItemRequirement;
import org.itss.prj_itss.request.business.model.RequestProcessingData;
import org.itss.prj_itss.request.business.model.SiteStockOption;
import org.itss.prj_itss.request.business.port.AllocationSuggester;
import org.itss.prj_itss.request.business.port.AllocationValidator;
import org.itss.prj_itss.request.business.port.RequestProcessingGateway;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RequestProcessingUseCase {
    private final RequestProcessingGateway gateway;
    private final AllocationValidator allocationValidator;
    private final AllocationSuggester allocationSuggester;
    private final RequestProcessingPreviewBuilder previewBuilder = new RequestProcessingPreviewBuilder();

    public RequestProcessingUseCase(RequestProcessingGateway gateway) {
        this(gateway, new DefaultAllocationValidator(), new DefaultAllocationSuggester());
    }

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
        return gateway.loadProcessingData(requestId);
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
        Map<Integer, LocalDate> desiredDeliveryDates,
        int deadlineDays
    ) {
        return allocationValidator.validateSubmission(
            items,
            allSites,
            allocations,
            desiredDeliveryDates,
            deadlineDays
        );
    }

    public List<RequestProcessingPreviewBuilder.PreviewOrder> buildPreviewOrders(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        Map<Integer, LocalDate> desiredDeliveryDates
    ) {
        return previewBuilder.build(items, allSites, allocations, desiredDeliveryDates);
    }

    public void createAllocatedOrders(
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws SQLException {
        gateway.createAllocatedOrders(requestId, allocations);
    }

    public AllocationSuggester allocationSuggester() {
        return allocationSuggester;
    }
}
