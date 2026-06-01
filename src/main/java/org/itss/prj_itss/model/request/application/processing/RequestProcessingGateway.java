package org.itss.prj_itss.model.request.application.processing;

import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.RequestProcessingData;

import java.util.Map;

public interface RequestProcessingGateway {
    RequestProcessingData loadProcessingData(int requestId);

    void createAllocatedOrders(
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws RequestProcessingGatewayException;
}
