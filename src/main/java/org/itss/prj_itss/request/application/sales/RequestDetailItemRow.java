package org.itss.prj_itss.request.application.sales;

public record RequestDetailItemRow(
    String code,
    String name,
    String quantity,
    String unit,
    String desiredDate
) {
}
