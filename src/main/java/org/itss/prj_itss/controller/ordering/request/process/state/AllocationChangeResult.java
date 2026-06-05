package org.itss.prj_itss.controller.ordering.request.process.state;

public record AllocationChangeResult(
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

