package org.itss.prj_itss.request.application.port;

import org.itss.prj_itss.request.domain.allocation.model.Allocation;
import org.itss.prj_itss.request.domain.processing.RequestProcessingData;

import java.util.Map;

public interface RequestProcessingGateway {
    RequestProcessingData loadProcessingData(int requestId);

    void createAllocatedOrders(
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws RequestProcessingGatewayException;
}
