package org.itss.prj_itss.service;

import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.entity.RequestMerchandise;
import org.itss.prj_itss.repository.IRequestRepository;

import java.time.LocalDate;
import java.util.List;

public final class RequestService {

    private final IRequestRepository requestRepository;

    public RequestService(IRequestRepository requestRepository) {
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

    public boolean deleteRequest(int requestId) {
        return requestRepository.deleteById(requestId);
    }
}
