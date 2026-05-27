package org.itss.prj_itss.view.ordering.request.process.preview;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.itss.prj_itss.model.request.application.processing.ProcessingPreviewOrderView;
import org.itss.prj_itss.controller.ordering.request.process.preview.RequestProcessingPreviewDialogController;

import java.util.List;

import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.showToast;

public final class RequestProcessingPreviewDialogView {

    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox ordersBox;

    @FXML
    private Button backButton;

    @FXML
    private Button sendButton;

    private Stage dialog;
    private Runnable onOrdersRequested;
    private RequestProcessingPreviewDialogController controller;

    void init(
        Stage dialog,
        Runnable onOrdersRequested,
        RequestProcessingPreviewDialogController controller
    ) {
        this.dialog = dialog;
        this.onOrdersRequested = onOrdersRequested == null ? () -> {} : onOrdersRequested;
        this.controller = controller;

        render(controller.previewOrders());
        backButton.setOnAction(event -> dialog.close());
        sendButton.setOnAction(event -> submit());
    }

    private void render(List<ProcessingPreviewOrderView> previewOrders) {
        int totalQuantity = previewOrders.stream()
            .flatMap(order -> order.lines().stream())
            .mapToInt(ProcessingPreviewOrderView.ProcessingPreviewLineView::quantity)
            .sum();
        int totalLines = previewOrders.stream()
            .mapToInt(order -> order.lines().size())
            .sum();

        subtitleLabel.setText(
            previewOrders.size() + " đơn hàng dự kiến"
                + " • " + totalLines + " dòng phân bổ"
                + " • " + totalQuantity + " chiếc"
        );

        ordersBox.getChildren().clear();
        for (int index = 0; index < previewOrders.size(); index++) {
            ordersBox.getChildren().add(PreviewOrderCardView.load(previewOrders.get(index), index + 1));
        }
    }

    private void submit() {
        RequestProcessingPreviewDialogController.SubmitResult result = controller.submit();
        if (!result.success()) {
            showCreationError();
            return;
        }

        dialog.close();
        onOrdersRequested.run();
        showToast("Đã tạo đơn hàng thành công.");
    }

    private void showCreationError() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Không thể tạo đơn");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText("Không thể tạo các đơn hàng đã phân bổ.");
        DialogPane dialogPane = errorAlert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/org/itss/prj_itss/styles/main-style.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog-custom");
        errorAlert.showAndWait();
    }
}
