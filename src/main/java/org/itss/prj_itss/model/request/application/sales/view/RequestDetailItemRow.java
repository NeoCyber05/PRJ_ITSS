package org.itss.prj_itss.model.request.application.sales.view;

public record RequestDetailItemRow(
    String code,
    String name,
    String quantity,
    String unit,
    String desiredDate
) {
}
