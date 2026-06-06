package org.itss.prj_itss.model.request.domain.lock;

public record LockResult(boolean acquired, RequestLock holder) {
    public static LockResult acquired(RequestLock lock) {
        return new LockResult(true, lock);
    }
    public static LockResult blocked(RequestLock existingHolder) {
        return new LockResult(false, existingHolder);
    }
}
