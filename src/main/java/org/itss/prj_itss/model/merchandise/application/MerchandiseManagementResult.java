package org.itss.prj_itss.model.merchandise.application;

public record MerchandiseManagementResult(
    boolean success,
    String message,
    Integer merchandiseId
) {

    public static MerchandiseManagementResult success(String message, Integer merchandiseId) {
        return new MerchandiseManagementResult(true, message, merchandiseId);
    }

    public static MerchandiseManagementResult success(String message) {
        return new MerchandiseManagementResult(true, message, null);
    }

    public static MerchandiseManagementResult failure(String message) {
        return new MerchandiseManagementResult(false, message, null);
    }
}
