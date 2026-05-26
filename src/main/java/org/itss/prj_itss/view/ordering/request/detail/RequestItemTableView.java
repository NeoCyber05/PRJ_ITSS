package org.itss.prj_itss.view.ordering.request.detail;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.model.request.application.sales.view.RequestDetailItemRow;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class RequestItemTableView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/detail/request-item-table.fxml";

    @FXML
    private VBox rowsContainer;

    public static VBox load(List<RequestDetailItemRow> items) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                RequestItemTableView.class.getResource(VIEW_RESOURCE),
                "Missing request item table FXML"
            ));
            VBox root = loader.load();
            RequestItemTableView controller = loader.getController();
            controller.init(items);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load request item table view", exception);
        }
    }

    private void init(List<RequestDetailItemRow> items) {
        rowsContainer.getChildren().clear();
        if (items.isEmpty()) {
            Label emptyLabel = new Label("Không có mặt hàng.");
            emptyLabel.getStyleClass().add("request-detail-empty-label");
            rowsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (RequestDetailItemRow item : items) {
            rowsContainer.getChildren().add(RequestItemRowView.load(item));
        }
    }
}
