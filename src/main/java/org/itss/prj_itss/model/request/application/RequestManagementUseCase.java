package org.itss.prj_itss.model.request.application;

import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.application.port.RequestRepository;

import java.time.LocalDate;
import java.util.List;

public final class RequestManagementUseCase {

    private final RequestRepository requestRepository;

    public RequestManagementUseCase(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public List<Request> findAll() {
        return requestRepository.findAll();
    }

    public Request findById(int id) {
        return requestRepository.findById(id);
    }

    public List<RequestMerchandise> findItemsByRequestId(int requestId) {
        return requestRepository.findItemsByRequestId(requestId);
    }

    public int countItemTypes(int requestId) {
        return requestRepository.countItemTypes(requestId);
    }

    public LocalDate getEarliestDeliveryDate(int requestId) {
        return requestRepository.getEarliestDeliveryDate(requestId);
    }

    public void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) throws Exception {
        requestRepository.updateRequestItems(requestId, items, note);
    }

    public int createRequest(List<RequestMerchandise> items, String note) throws Exception {
        return requestRepository.createRequest(items, note);
    }

    public boolean deleteRequest(int requestId) {
        return requestRepository.deleteById(requestId);
    }
}
