package org.itss.prj_itss.request.application.processing;

public record AllocationChangeCommand(
    int itemMerchandiseId,
    int siteId,
    String quantityText,
    String transportLabel
) {
}
