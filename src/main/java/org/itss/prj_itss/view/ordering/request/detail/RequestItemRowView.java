package org.itss.prj_itss.view.ordering.request.detail;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.itss.prj_itss.model.request.application.sales.view.RequestDetailItemRow;

import java.io.IOException;
import java.util.Objects;

public final class RequestItemRowView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/ordering/request/detail/request-item-row.fxml";

    @FXML
    private Label codeCell;
    @FXML
    private Label nameCell;
    @FXML
    private Label quantityCell;
    @FXML
    private Label unitCell;
    @FXML
    private Label desiredDateCell;

    public static HBox load(RequestDetailItemRow item) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                RequestItemRowView.class.getResource(VIEW_RESOURCE),
                "Missing request item row FXML"
            ));
            HBox root = loader.load();
            RequestItemRowView controller = loader.getController();
            controller.init(item);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load request item row view", exception);
        }
    }

    private void init(RequestDetailItemRow item) {
        codeCell.setText(item.code());
        nameCell.setText(item.name());
        quantityCell.setText(item.quantity());
        unitCell.setText(item.unit());
        desiredDateCell.setText(item.desiredDate() != null && !item.desiredDate().isBlank() ? item.desiredDate() : "N/A");
    }
}
