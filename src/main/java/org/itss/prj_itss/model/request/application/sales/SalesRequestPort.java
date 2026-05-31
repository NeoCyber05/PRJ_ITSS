package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.util.List;

public interface SalesRequestPort {
    Request findById(int id);
    List<RequestMerchandise> findItemsByRequestId(int requestId);
    int createRequest(List<RequestMerchandise> items, String note) throws Exception;
    void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) throws Exception;
    boolean deleteById(int requestId);
}
