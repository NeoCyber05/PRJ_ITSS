package org.itss.prj_itss.model.order.application.cancellation;

public class CancelledOrderProcessingException extends Exception {
    public CancelledOrderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelledOrderProcessingException(String message) {
        super(message);
    }
}
