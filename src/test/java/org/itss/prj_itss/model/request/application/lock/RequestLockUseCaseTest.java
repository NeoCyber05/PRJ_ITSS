package org.itss.prj_itss.model.request.application.lock;

import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.model.request.domain.lock.LockResult;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RequestLockUseCaseTest {

    private static RequestLock fakeLock(int reqId, String username) {
        return new RequestLock(reqId, username, "Bộ phận bán hàng", username,
            LocalDateTime.now(), LocalDateTime.now().plusMinutes(15));
    }

    @Test
    void acquire_success_returnsAcquired() throws RequestLockException {
        LockOwner owner = new LockOwner("alice", "Bộ phận bán hàng", "Alice");
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            (id, o, ttl) -> LockResult.acquired(fakeLock(id, o.username()))
        ));
        LockResult result = uc.acquire(1, owner);
        assertTrue(result.acquired());
        assertEquals("alice", result.holder().ownerUsername());
    }

    @Test
    void acquire_blocked_returnsBlockedWithHolder() throws RequestLockException {
        LockOwner owner = new LockOwner("bob", "Bộ phận đặt hàng quốc tế", "Bob");
        RequestLock existingHolder = fakeLock(1, "alice");
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            (id, o, ttl) -> LockResult.blocked(existingHolder)
        ));
        LockResult result = uc.acquire(1, owner);
        assertFalse(result.acquired());
        assertEquals("alice", result.holder().ownerUsername());
    }

    @Test
    void acquire_reentrant_sameName_returnsAcquired() throws RequestLockException {
        LockOwner owner = new LockOwner("alice", "Bộ phận bán hàng", "Alice");
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            (id, o, ttl) -> LockResult.acquired(fakeLock(id, o.username()))
        ));
        uc.acquire(1, owner);
        LockResult renew = uc.acquire(1, owner);
        assertTrue(renew.acquired());
    }

    @Test
    void release_callsGateway() throws RequestLockException {
        List<String> released = new ArrayList<>();
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            (id, o, ttl) -> LockResult.acquired(fakeLock(id, o.username())),
            (id, user) -> released.add(user + ":" + id)
        ));
        uc.release(1, "alice");
        assertEquals(List.of("alice:1"), released);
    }

    @Test
    void activeLocks_returnsOnlyActiveEntries() throws RequestLockException {
        RequestLock lock = fakeLock(5, "alice");
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(
            null, null,
            ids -> ids.contains(5) ? Map.of(5, lock) : Map.of()
        ));
        Map<Integer, RequestLock> active = uc.activeLocks(List.of(5, 6));
        assertTrue(active.containsKey(5));
        assertFalse(active.containsKey(6));
    }

    @Test
    void activeLocks_emptyInput_returnsEmpty() throws RequestLockException {
        RequestLockUseCase uc = new RequestLockUseCase(new FakeGateway(null, null, ids -> Map.of()));
        assertTrue(uc.activeLocks(List.of()).isEmpty());
    }

    // ---- Fake gateway ----

    @FunctionalInterface
    interface AcquireFn {
        LockResult acquire(int id, LockOwner owner, int ttl) throws RequestLockException;
    }

    @FunctionalInterface
    interface ReleaseFn {
        void release(int id, String user) throws RequestLockException;
    }

    @FunctionalInterface
    interface FindFn {
        Map<Integer, RequestLock> find(Collection<Integer> ids) throws RequestLockException;
    }

    static class FakeGateway implements RequestLockGateway {
        private final AcquireFn acquireFn;
        private final ReleaseFn releaseFn;
        private final FindFn findFn;

        FakeGateway(AcquireFn acquireFn) { this(acquireFn, null, null); }

        FakeGateway(AcquireFn acquireFn, ReleaseFn releaseFn) {
            this(acquireFn, releaseFn, null);
        }

        FakeGateway(AcquireFn acquireFn, ReleaseFn releaseFn, FindFn findFn) {
            this.acquireFn = acquireFn;
            this.releaseFn = releaseFn;
            this.findFn = findFn;
        }

        @Override
        public LockResult acquireOrRenew(int requestId, LockOwner owner, int ttlSeconds) throws RequestLockException {
            if (acquireFn == null) return LockResult.acquired(new RequestLock(requestId, owner.username(), owner.role(), owner.display(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(15)));
            return acquireFn.acquire(requestId, owner, ttlSeconds);
        }

        @Override
        public void release(int requestId, String ownerUsername) throws RequestLockException {
            if (releaseFn != null) releaseFn.release(requestId, ownerUsername);
        }

        @Override
        public void releaseAllForOwner(String ownerUsername) throws RequestLockException {
            if (releaseFn != null) releaseFn.release(-1, ownerUsername);
        }

        @Override
        public Map<Integer, RequestLock> findActiveForRequests(Collection<Integer> requestIds) throws RequestLockException {
            if (findFn == null) return Map.of();
            return findFn.find(requestIds);
        }
    }
}
