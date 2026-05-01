package org.itss.prj_itss.repository;

import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.entity.RequestMerchandise;

import java.time.LocalDate;
import java.util.List;

public interface IRequestRepository {
    List<Request> findAll();
    Request findById(int id);
    List<RequestMerchandise> findItemsByRequestId(int requestId);
    int countItemTypes(int requestId);
    LocalDate getEarliestDeliveryDate(int requestId);
    boolean updateStatus(int requestId, String newStatus);
    void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) throws Exception;
    boolean deleteById(int requestId);
}
