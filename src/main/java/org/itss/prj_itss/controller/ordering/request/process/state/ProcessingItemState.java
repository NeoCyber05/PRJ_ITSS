package org.itss.prj_itss.controller.ordering.request.process.state;

public record ProcessingItemState(
    int merchandiseId,
    String code,
    String name,
    int required
) {
}

