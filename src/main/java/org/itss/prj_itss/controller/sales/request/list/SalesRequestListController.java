package org.itss.prj_itss.controller.sales.request.list;

import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.model.request.application.listing.RequestRow;
import org.itss.prj_itss.model.request.application.lock.RequestLockException;
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SalesRequestListController {

    private final ReceivedRequestsApplicationService receivedRequestsService;
    private final RequestLockUseCase lockUseCase;

    public SalesRequestListController(
            ReceivedRequestsApplicationService receivedRequestsService,
            RequestLockUseCase lockUseCase
    ) {
        this.receivedRequestsService = receivedRequestsService;
        this.lockUseCase = Objects.requireNonNull(lockUseCase, "lockUseCase");
    }

    public List<RequestRow> getRequests() {
        return receivedRequestsService.findRows();
    }

    public boolean deleteRequest(int requestId) {
        return receivedRequestsService.deleteRequest(requestId);
    }

    public Map<Integer, RequestLock> activeLocks(Collection<Integer> requestIds) {
        try {
            return lockUseCase.activeLocks(requestIds);
        } catch (RequestLockException e) {
            return Map.of();
        }
    }
}
