package org.itss.prj_itss.model.request.application.processing;

public class RequestProcessingGatewayException extends RuntimeException {
    public RequestProcessingGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestProcessingGatewayException(String message) {
        super(message);
    }
}
