package org.itss.prj_itss.model.site.application.self;

public record SiteOrderItemRow(
    int merchandiseId,
    String merchandiseCode,
    String merchandiseName,
    String unit,
    String quantity,
    String deliveryMethod
) {}
