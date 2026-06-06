package org.itss.prj_itss.model.request.application.lock;

import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.model.request.domain.lock.LockResult;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class RequestLockUseCase {

    public static final int LOCK_TTL_SECONDS = 900;   // 15 min
    public static final int HEARTBEAT_SECONDS = 300;  // 5 min

    private final RequestLockGateway gateway;

    public RequestLockUseCase(RequestLockGateway gateway) {
        this.gateway = Objects.requireNonNull(gateway, "gateway");
    }

    public LockResult acquire(int requestId, LockOwner owner) throws RequestLockException {
        return gateway.acquireOrRenew(requestId, owner, LOCK_TTL_SECONDS);
    }

    public void renew(int requestId, LockOwner owner) throws RequestLockException {
        gateway.acquireOrRenew(requestId, owner, LOCK_TTL_SECONDS);
    }

    public void release(int requestId, String ownerUsername) throws RequestLockException {
        gateway.release(requestId, ownerUsername);
    }

    public Map<Integer, RequestLock> activeLocks(Collection<Integer> requestIds) throws RequestLockException {
        if (requestIds == null || requestIds.isEmpty()) return Map.of();
        return gateway.findActiveForRequests(requestIds);
    }
}
