package org.itss.prj_itss.view.ordering.request.process.preview;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import org.itss.prj_itss.model.request.application.processing.ProcessingPreviewOrderView;

import java.io.IOException;
import java.util.Objects;

public final class PreviewTableRowView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/preview/preview-table-row.fxml";

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
    @FXML
    private Label desiredCell;

    public static HBox load(ProcessingPreviewOrderView.ProcessingPreviewLineView line) {
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

    private void init(ProcessingPreviewOrderView.ProcessingPreviewLineView line) {
        codeCell.setText(line.merchandiseCode());
        nameCell.setText(line.merchandiseName());
        quantityCell.setText(String.valueOf(line.quantity()));
        transportBox.getChildren().setAll(buildTransportBadge(line.transport()));
        estimatedCell.setText(line.estimatedDate() != null ? line.estimatedDate() : "N/A");
        desiredCell.setText(line.desiredDate() != null ? line.desiredDate() : "N/A");
    }

    private Label buildTransportBadge(String transport) {
        boolean seaTransport = isSeaTransport(transport);
        String icon = seaTransport ? "🚢 " : "✈ ";
        String background = seaTransport ? "#E8F1FF" : "#FFF4E5";
        String foreground = seaTransport ? "#2563EB" : "#D97706";

        Label badge = new Label(icon + transport);
        badge.setStyle(
            "-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + foreground + ";"
                + "-fx-background-radius: 999;"
                + "-fx-padding: 5 10;"
                + "-fx-font-size: 11px;"
                + "-fx-font-weight: bold;"
        );
        return badge;
    }

    private boolean isSeaTransport(String transport) {
        if (transport == null) {
            return false;
        }
        return switch (transport.trim()) {
            case "Duong bien", "Tau", "Đường biển", "Tàu" -> true;
            default -> false;
        };
    }
}
