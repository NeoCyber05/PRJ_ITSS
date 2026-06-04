package org.itss.prj_itss.model.order.application.port;

import org.itss.prj_itss.model.order.domain.cancellation.CancelledOrderProcessingData;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;

import java.util.Map;

public interface CancelledOrderProcessingGateway {
    CancelledOrderProcessingData loadProcessingData(int cancelledOrderId)
        throws CancelledOrderProcessingGatewayException;

    void createAllocatedOrders(
        int cancelledOrderId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws CancelledOrderProcessingGatewayException;
}
