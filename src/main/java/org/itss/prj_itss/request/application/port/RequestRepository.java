package org.itss.prj_itss.request.application.port;

import org.itss.prj_itss.request.domain.request.Request;
import org.itss.prj_itss.request.domain.request.RequestMerchandise;

import java.time.LocalDate;
import java.util.List;

public interface RequestRepository {
    List<Request> findAll();
    Request findById(int id);
    List<RequestMerchandise> findItemsByRequestId(int requestId);
    int countItemTypes(int requestId);
    LocalDate getEarliestDeliveryDate(int requestId);
    boolean updateStatus(int requestId, String newStatus);
    void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) throws Exception;
    int createRequest(List<RequestMerchandise> items, String note) throws Exception;
    boolean deleteById(int requestId);
}
