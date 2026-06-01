package org.itss.prj_itss.view.ordering.request.process.state;

public record ProcessingItemView(
    int merchandiseId,
    String code,
    String name,
    int required
) {
}
