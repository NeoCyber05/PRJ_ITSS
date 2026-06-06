package org.itss.prj_itss.model.request.domain.lock;

import java.time.LocalDateTime;

public record RequestLock(
    int requestId,
    String ownerUsername,
    String ownerRole,
    String ownerDisplay,
    LocalDateTime lockedAt,
    LocalDateTime expiresAt
) {}
