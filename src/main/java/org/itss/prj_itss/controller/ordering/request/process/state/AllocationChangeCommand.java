package org.itss.prj_itss.controller.ordering.request.process.state;

public record AllocationChangeCommand(
    int itemMerchandiseId,
    int siteId,
    String quantityText,
    String transportLabel
) {
}

