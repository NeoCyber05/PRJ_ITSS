package org.itss.prj_itss.model.site.application.self;

public record SiteProfileDraft(
    String name,
    String description,
    Integer shipDeliveryDays,
    Integer airDeliveryDays
) {}
