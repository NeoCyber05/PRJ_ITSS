package org.itss.prj_itss.request.application.port;

public class RequestProcessingGatewayException extends RuntimeException {
    public RequestProcessingGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestProcessingGatewayException(String message) {
        super(message);
    }
}
