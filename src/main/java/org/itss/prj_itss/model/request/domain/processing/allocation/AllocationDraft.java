package org.itss.prj_itss.model.request.domain.processing.allocation;

public record AllocationDraft(int siteId, int merchandiseId, int quantity, String transport) {
}
