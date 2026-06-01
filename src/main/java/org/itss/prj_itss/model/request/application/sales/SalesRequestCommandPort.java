package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.util.List;

public interface SalesRequestCommandPort {
    int createRequest(List<RequestMerchandise> items, String note) throws Exception;
    void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) throws Exception;
    boolean deleteById(int requestId);
}
