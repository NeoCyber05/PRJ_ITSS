package org.itss.prj_itss.view.ordering.request.detail;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.itss.prj_itss.controller.ordering.request.RequestDetailPopupController;
import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.request.application.international.detail.AllocatedOrderRow;
import org.itss.prj_itss.view.shared.ui.StatusBadgeFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class AllocatedOrderRowView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/detail/allocated-order-row.fxml";

    @FXML
    private HBox rowContainer;
    @FXML
    private Label orderCodeCell;
    @FXML
    private Label siteNameCell;
    @FXML
    private Label itemCountCell;
    @FXML
    private Label createdAtCell;
    @FXML
    private HBox statusBox;
    @FXML
    private HBox actionBox;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button openButton;

    public static HBox load(
        AllocatedOrderRow order,
        RequestDetailPopupController controller,
        BiConsumer<AllocatedOrderRow, HBox> onOrderSelected
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                AllocatedOrderRowView.class.getResource(VIEW_RESOURCE),
                "Missing allocated order row FXML"
            ));
            HBox root = loader.load();
            AllocatedOrderRowView controllerInstance = loader.getController();
            controllerInstance.init(order, controller, onOrderSelected);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load allocated order row view", exception);
        }
    }

    private void init(
        AllocatedOrderRow order,
        RequestDetailPopupController controller,
        BiConsumer<AllocatedOrderRow, HBox> onOrderSelected
    ) {
        orderCodeCell.setText(order.orderCode());
        siteNameCell.setText(order.siteName());

        itemCountCell.setText(order.deliveryMethod());
        createdAtCell.setText(order.createdAt() != null && !order.createdAt().isBlank() ? order.createdAt() : "N/A");
        statusBox.getChildren().setAll(StatusBadgeFactory.statusBadge(order.status(), false));

        rowContainer.setOnMouseClicked(event -> onOrderSelected.accept(order, rowContainer));
        openButton.setOnMouseClicked(event -> event.consume());
        openButton.setOnAction(event -> onOrderSelected.accept(order, rowContainer));

        if (order.cancellable()) {
            cancelBtn.setVisible(true);
            cancelBtn.setManaged(true);
            cancelBtn.setOnAction(event -> {
                event.consume();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Xác nhận hủy");
                alert.setHeaderText("Bạn có chắc chắn muốn hủy đơn hàng này không?");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        OrderCancellationApplicationService.CancellationResult result = controller.cancel(order.orderId());
                        if (result.success()) {
                            statusBox.getChildren().setAll(StatusBadgeFactory.statusBadge("cancelled", false));
                            cancelBtn.setVisible(false);
                            cancelBtn.setManaged(false);
                        }
                    }
                });
            });
        }
    }
}
