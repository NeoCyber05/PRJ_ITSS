package org.itss.prj_itss.model.request.application.international.detail;

public record ReceivedRequestDetailItemRow(
    String code,
    String name,
    String quantity,
    String unit,
    String desiredDate
) {
}
