package org.itss.prj_itss.model.request.application.processing;

public record AllocationChangeCommand(
    int itemMerchandiseId,
    int siteId,
    String quantityText,
    String transportLabel
) {
}
