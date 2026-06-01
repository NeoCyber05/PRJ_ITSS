package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.util.List;

public interface SalesRequestQueryPort {
    Request findById(int id);
    List<RequestMerchandise> findItemsByRequestId(int requestId);
}
