package org.itss.prj_itss.model.request.application.processing;

import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;

import java.time.LocalDate;
import java.util.List;

public interface ProcessingRequestPort {
    List<RequestMerchandise> findItemsByRequestId(int requestId);
    LocalDate getEarliestDeliveryDate(int requestId);
    boolean updateStatus(int requestId, RequestStatus newStatus);
}
