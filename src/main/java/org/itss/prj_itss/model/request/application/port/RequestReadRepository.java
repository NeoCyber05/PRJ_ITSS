package org.itss.prj_itss.model.request.application.port;

import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.time.LocalDate;
import java.util.List;

public interface RequestReadRepository {
    List<Request> findAll();

    Request findById(int id);

    List<RequestMerchandise> findItemsByRequestId(int requestId);

    int countItemTypes(int requestId);

    LocalDate getEarliestDeliveryDate(int requestId);
}
