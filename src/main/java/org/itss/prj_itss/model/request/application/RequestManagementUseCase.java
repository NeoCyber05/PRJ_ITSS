package org.itss.prj_itss.model.request.application;

import org.itss.prj_itss.model.request.application.port.RequestReadRepository;
import org.itss.prj_itss.model.request.application.port.RequestWriteRepository;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.time.LocalDate;
import java.util.List;

public final class RequestManagementUseCase {

    private final RequestReadRepository readRepository;
    private final RequestWriteRepository writeRepository;

    public RequestManagementUseCase(RequestReadRepository readRepository, RequestWriteRepository writeRepository) {
        this.readRepository = readRepository;
        this.writeRepository = writeRepository;
    }

    public List<Request> findAll() {
        return readRepository.findAll();
    }

    public Request findById(int id) {
        return readRepository.findById(id);
    }

    public List<RequestMerchandise> findItemsByRequestId(int requestId) {
        return readRepository.findItemsByRequestId(requestId);
    }

    public int countItemTypes(int requestId) {
        return readRepository.countItemTypes(requestId);
    }

    public LocalDate getEarliestDeliveryDate(int requestId) {
        return readRepository.getEarliestDeliveryDate(requestId);
    }

    public void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) throws Exception {
        writeRepository.updateRequestItems(requestId, items, note);
    }

    public int createRequest(Request request) throws Exception {
        return writeRepository.createRequest(request);
    }

    public boolean deleteRequest(int requestId) {
        return writeRepository.deleteById(requestId);
    }
}
