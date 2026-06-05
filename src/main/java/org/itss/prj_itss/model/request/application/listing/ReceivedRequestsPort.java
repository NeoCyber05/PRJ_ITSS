package org.itss.prj_itss.model.request.application.listing;

import org.itss.prj_itss.model.request.domain.request.Request;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReceivedRequestsPort {
    List<Request> findAll();
    int countItemTypes(int requestId);
    LocalDate getEarliestDeliveryDate(int requestId);
    boolean deleteById(int requestId);

    Map<Integer, Integer> countItemTypesByRequestIds(Set<Integer> requestIds);
    Map<Integer, LocalDate> findEarliestDeliveryDatesByRequestIds(Set<Integer> requestIds);
}
