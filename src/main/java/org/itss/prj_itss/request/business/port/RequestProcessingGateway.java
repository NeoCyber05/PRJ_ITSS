package org.itss.prj_itss.request.business.port;

import org.itss.prj_itss.request.business.model.Allocation;
import org.itss.prj_itss.request.business.model.RequestProcessingData;

import java.sql.SQLException;
import java.util.Map;

public interface RequestProcessingGateway {
    RequestProcessingData loadProcessingData(int requestId);

    void createAllocatedOrders(
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) throws SQLException;
}
