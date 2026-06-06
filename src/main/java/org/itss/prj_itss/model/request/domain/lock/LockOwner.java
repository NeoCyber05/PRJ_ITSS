package org.itss.prj_itss.model.request.domain.lock;

public record LockOwner(String username, String role, String display) {
    public LockOwner {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
    }
}
