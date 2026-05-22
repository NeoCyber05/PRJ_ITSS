package org.itss.prj_itss.model.request.application.processing;

public record AllocationChangeResultView(
    boolean applied,
    String errorType,
    int stock,
    int deliveryDays,
    int dayDelta,
    boolean deliveryAvailable
) {
}
