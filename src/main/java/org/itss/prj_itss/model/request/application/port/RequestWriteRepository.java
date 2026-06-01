package org.itss.prj_itss.model.request.application.port;

import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;

import java.util.List;

public interface RequestWriteRepository {
    boolean updateStatus(int requestId, RequestStatus newStatus);

    void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) throws Exception;

    int createRequest(Request request) throws Exception;

    boolean deleteById(int requestId);
}
