package org.itss.prj_itss.model.request.application.lock;

import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.model.request.domain.lock.LockResult;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;

import java.util.Collection;
import java.util.Map;

public interface RequestLockGateway {
    /**
     * Atomically acquire or renew. Returns the outcome.
     * If the row is owned by someone else and not expired, returns blocked with the current holder.
     */
    LockResult acquireOrRenew(int requestId, LockOwner owner, int ttlSeconds) throws RequestLockException;

    void release(int requestId, String ownerUsername) throws RequestLockException;

    /** Release every lock currently held by this owner. Used on logout / app exit cleanup. */
    void releaseAllForOwner(String ownerUsername) throws RequestLockException;

    Map<Integer, RequestLock> findActiveForRequests(Collection<Integer> requestIds) throws RequestLockException;
}
