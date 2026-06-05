package org.itss.prj_itss.view.ordering.request.process.preview;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.controller.ordering.request.process.state.ProcessingPreviewOrder;

import java.io.IOException;
import java.util.Objects;

public final class PreviewOrderCardView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/process/preview/preview-order-card-view.fxml";

    @FXML
    private Label orderTitleLabel;
    @FXML
    private Label siteLabel;
    @FXML
    private Label qtyBadge;
    @FXML
    private VBox linesBox;

    public static VBox load(ProcessingPreviewOrder order, int index) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                PreviewOrderCardView.class.getResource(VIEW_RESOURCE),
                "Missing preview order card FXML"
            ));
            VBox root = loader.load();
            PreviewOrderCardView controller = loader.getController();
            controller.init(order, index);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load preview order card view", exception);
        }
    }

    private void init(ProcessingPreviewOrder order, int index) {
        orderTitleLabel.setText("Đơn hàng dự kiến " + index);
        siteLabel.setText(order.siteName() + " • " + order.siteCode());

        int totalQuantity = order.lines().stream()
            .mapToInt(ProcessingPreviewOrder.ProcessingPreviewLine::quantity)
            .sum();
        qtyBadge.setText(totalQuantity + " chiếc");

        linesBox.getChildren().clear();
        for (ProcessingPreviewOrder.ProcessingPreviewLine line : order.lines()) {
            linesBox.getChildren().add(PreviewTableRowView.load(line));
        }
    }
}
