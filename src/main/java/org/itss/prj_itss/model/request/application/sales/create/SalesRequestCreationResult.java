package org.itss.prj_itss.model.request.application.sales.create;

public record SalesRequestCreationResult(boolean success, String message, int requestId) {

    public static SalesRequestCreationResult validationFailed(SalesRequestCreationValidationResult validationResult) {
        return new SalesRequestCreationResult(false, validationResult.firstMessage(), 0);
    }

    public static SalesRequestCreationResult created(int requestId) {
        return new SalesRequestCreationResult(true, "Yêu cầu nhập hàng đã được gửi thành công.", requestId);
    }

    public static SalesRequestCreationResult failed(String message) {
        return new SalesRequestCreationResult(false, message, 0);
    }
}
