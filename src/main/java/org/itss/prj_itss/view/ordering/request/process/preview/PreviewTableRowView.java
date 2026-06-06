package org.itss.prj_itss.view.ordering.request.process.preview;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import org.itss.prj_itss.controller.ordering.request.process.state.ProcessingPreviewOrder;
import org.itss.prj_itss.view.shared.ui.StatusBadgeFactory;

import java.io.IOException;
import java.util.Objects;

public final class PreviewTableRowView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/process/preview/preview-table-row-view.fxml";

    @FXML
    private Label codeCell;
    @FXML
    private Label nameCell;
    @FXML
    private Label quantityCell;
    @FXML
    private HBox transportBox;
    @FXML
    private Label estimatedCell;

    public static HBox load(ProcessingPreviewOrder.ProcessingPreviewLine line) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                PreviewTableRowView.class.getResource(VIEW_RESOURCE),
                "Missing preview table row FXML"
            ));
            HBox root = loader.load();
            PreviewTableRowView controller = loader.getController();
            controller.init(line);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load preview table row view", exception);
        }
    }

    private void init(ProcessingPreviewOrder.ProcessingPreviewLine line) {
        codeCell.setText(line.merchandiseCode());
        nameCell.setText(line.merchandiseName());
        quantityCell.setText(String.valueOf(line.quantity()));
        transportBox.getChildren().setAll(StatusBadgeFactory.transportBadge(line.transport()));
        estimatedCell.setText(line.estimatedDate() != null ? line.estimatedDate() : "N/A");
    }
}
