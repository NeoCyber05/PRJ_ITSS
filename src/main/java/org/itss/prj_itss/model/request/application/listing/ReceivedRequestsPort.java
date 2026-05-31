package org.itss.prj_itss.model.request.application.listing;

import org.itss.prj_itss.model.request.domain.request.Request;

import java.time.LocalDate;
import java.util.List;

public interface ReceivedRequestsPort {
    List<Request> findAll();
    int countItemTypes(int requestId);
    LocalDate getEarliestDeliveryDate(int requestId);
    boolean deleteById(int requestId);
}
