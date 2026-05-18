package org.itss.prj_itss.request.application.processing;

public record AllocationChangeResultView(
    boolean applied,
    String errorType,
    int stock,
    int deliveryDays,
    int dayDelta,
    boolean deliveryAvailable
) {
}
