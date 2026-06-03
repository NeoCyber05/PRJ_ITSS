package org.itss.prj_itss.model.order.application.port;

public class CancelledOrderProcessingGatewayException extends RuntimeException {
    public CancelledOrderProcessingGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelledOrderProcessingGatewayException(String message) {
        super(message);
    }
}
