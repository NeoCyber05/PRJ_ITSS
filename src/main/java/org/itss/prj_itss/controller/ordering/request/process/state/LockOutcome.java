package org.itss.prj_itss.controller.ordering.request.process.state;

public record LockOutcome(boolean acquired, String blockedMessage) {
    public boolean blocked() { return !acquired; }
    public static LockOutcome outcomeAcquired() { return new LockOutcome(true, null); }
    public static LockOutcome outcomeBlocked(String message) { return new LockOutcome(false, message); }
}
