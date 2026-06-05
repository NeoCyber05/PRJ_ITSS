package org.itss.prj_itss.view.ordering.order.detail.components;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import org.itss.prj_itss.controller.ordering.order.OrderDetailController;
import org.itss.prj_itss.controller.shared.ActionResult;

public final class OrderCancellationHandler {

    private OrderCancellationHandler() {
    }

    public static Button createCancelButton(int orderId, OrderDetailController controller, Runnable onSuccess) {
        Button cancelBtn = new Button("Hủy đơn hàng");
        cancelBtn.setStyle(
            "-fx-background-color: #FEF2F2; " +
            "-fx-text-fill: #DC2626; " +
            "-fx-border-color: #F87171; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 6 12; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );

        cancelBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận hủy");
            alert.setHeaderText("Bạn có chắc chắn muốn hủy đơn hàng này không?");
            alert.setContentText("Hành động này không thể hoàn tác.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    ActionResult result = controller.cancel(orderId);
                    if (result.success() && onSuccess != null) {
                        onSuccess.run();
                    }
                }
            });
        });

        return cancelBtn;
    }
}
