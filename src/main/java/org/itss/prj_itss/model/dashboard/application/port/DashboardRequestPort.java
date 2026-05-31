package org.itss.prj_itss.model.dashboard.application.port;

import org.itss.prj_itss.model.request.domain.request.Request;

import java.time.LocalDate;
import java.util.List;

public interface DashboardRequestPort {
    List<Request> findAll();
    LocalDate getEarliestDeliveryDate(int requestId);
    int countItemTypes(int requestId);
}
