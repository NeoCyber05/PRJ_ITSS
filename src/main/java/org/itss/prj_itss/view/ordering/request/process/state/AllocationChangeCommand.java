package org.itss.prj_itss.view.ordering.request.process.state;

public record AllocationChangeCommand(
    int itemMerchandiseId,
    int siteId,
    String quantityText,
    String transportLabel
) {
}
