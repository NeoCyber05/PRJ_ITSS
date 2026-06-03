package org.itss.prj_itss.view.ordering.request.process.state;

public record AllocationChangeResultView(
    boolean applied,
    String errorType,
    int stock,
    int deliveryDays,
    int dayDelta,
    boolean deliveryAvailable,
    String deliveryStatusText,
    String deliveryStatusClass
) {
}
