package org.itss.prj_itss.model.warehouse.application;

public record IncomingOrderItemRow(
    int merchandiseId,
    String merchandiseCode,
    String merchandiseName,
    String unit,
    String orderedQuantity,
    String deliveryMethod
) {
}
