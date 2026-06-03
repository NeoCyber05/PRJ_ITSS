package org.itss.prj_itss.model.site.application.self;

public record SiteInventoryRow(
    int merchandiseId,
    String merchandiseCode,
    String merchandiseName,
    String unit,
    int stockQuantity
) {}
