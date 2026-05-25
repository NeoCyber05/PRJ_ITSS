package org.itss.prj_itss.view.ordering.request.detail;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.controller.ordering.request.RequestDetailPopupController;
import org.itss.prj_itss.model.request.application.sales.detail.AllocatedOrderRow;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class AllocatedOrderTableView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/ordering/request/detail/allocated-order-table.fxml";

    @FXML
    private VBox rowsContainer;

    public static VBox load(
        List<AllocatedOrderRow> orders,
        RequestDetailPopupController controller,
        BiConsumer<AllocatedOrderRow, HBox> onOrderSelected
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                AllocatedOrderTableView.class.getResource(VIEW_RESOURCE),
                "Missing allocated order table FXML"
            ));
            VBox root = loader.load();
            AllocatedOrderTableView controllerInstance = loader.getController();
            controllerInstance.init(orders, controller, onOrderSelected);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load allocated order table view", exception);
        }
    }

    private void init(
        List<AllocatedOrderRow> orders,
        RequestDetailPopupController controller,
        BiConsumer<AllocatedOrderRow, HBox> onOrderSelected
    ) {
        rowsContainer.getChildren().clear();
        if (orders.isEmpty()) {
            Label emptyLabel = new Label("Chưa có đơn hàng nào được phân bổ.");
            emptyLabel.getStyleClass().add("request-detail-empty-label");
            rowsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (AllocatedOrderRow order : orders) {
            rowsContainer.getChildren().add(AllocatedOrderRowView.load(order, controller, onOrderSelected));
        }
    }
}
