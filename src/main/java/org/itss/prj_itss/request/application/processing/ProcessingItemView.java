package org.itss.prj_itss.request.application.processing;

public record ProcessingItemView(
    int merchandiseId,
    String code,
    String name,
    int required
) {
}
