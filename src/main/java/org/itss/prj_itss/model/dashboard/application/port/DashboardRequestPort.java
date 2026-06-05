package org.itss.prj_itss.model.dashboard.application.port;

import org.itss.prj_itss.model.request.domain.request.Request;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DashboardRequestPort {
    List<Request> findAll();
    LocalDate getEarliestDeliveryDate(int requestId);
    int countItemTypes(int requestId);

    Map<Integer, Integer> countItemTypesByRequestIds(Set<Integer> requestIds);
    Map<Integer, LocalDate> findEarliestDeliveryDatesByRequestIds(Set<Integer> requestIds);
}
