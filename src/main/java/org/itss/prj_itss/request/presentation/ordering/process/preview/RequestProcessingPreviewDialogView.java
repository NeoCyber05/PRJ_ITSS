package org.itss.prj_itss.request.presentation.ordering.process.preview;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.request.business.model.DeliveryMethod;
import org.itss.prj_itss.request.business.service.RequestProcessingPreviewBuilder;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class RequestProcessingPreviewDialogView {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox ordersBox;

    @FXML
    private Button backButton;

    @FXML
    private Button sendButton;

    private Stage dialog;
    private INavigator navigator;
    private RequestProcessingPreviewDialogController controller;

    void init(
        Stage dialog,
        INavigator navigator,
        RequestProcessingPreviewDialogController controller
    ) {
        this.dialog = dialog;
        this.navigator = navigator;
        this.controller = controller;

        render(controller.previewOrders());
        backButton.setOnAction(event -> dialog.close());
        sendButton.setOnAction(event -> submit());
    }

    private void render(List<RequestProcessingPreviewBuilder.PreviewOrder> previewOrders) {
        int totalQuantity = previewOrders.stream()
            .flatMap(order -> order.lines().stream())
            .mapToInt(RequestProcessingPreviewBuilder.PreviewLine::quantity)
            .sum();
        int totalLines = previewOrders.stream()
            .mapToInt(order -> order.lines().size())
            .sum();

        subtitleLabel.setText(
            previewOrders.size() + " Ä‘Æ¡n hÃ ng dá»± kiáº¿n"
                + " â€¢ " + totalLines + " dÃ²ng phÃ¢n bá»•"
                + " â€¢ " + totalQuantity + " chiáº¿c"
        );

        ordersBox.getChildren().clear();
        for (int index = 0; index < previewOrders.size(); index++) {
            ordersBox.getChildren().add(buildPreviewOrderCard(previewOrders.get(index), index + 1));
        }
    }

    private void submit() {
        RequestProcessingPreviewDialogController.SubmitResult result = controller.submit();
        if (!result.success()) {
            showCreationError();
            return;
        }

        dialog.close();
        navigator.showView("orders");
        showToast("ÄÃ£ táº¡o Ä‘Æ¡n hÃ ng thÃ nh cÃ´ng.");
    }

    private VBox buildPreviewOrderCard(RequestProcessingPreviewBuilder.PreviewOrder order, int index) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        addStyleClass(card, "request-preview-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label orderLabel = new Label("ÄÆ¡n hÃ ng dá»± kiáº¿n " + index);
        addStyleClass(orderLabel, "request-preview-card-title");
        Label siteLabel = new Label(order.site().name + " â€¢ " + order.site().siteCode);
        addStyleClass(siteLabel, "request-preview-subtitle");
        titleBox.getChildren().addAll(orderLabel, siteLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int totalQuantity = order.lines().stream().mapToInt(RequestProcessingPreviewBuilder.PreviewLine::quantity).sum();
        Label qtyBadge = new Label(totalQuantity + " chiáº¿c");
        addStyleClass(qtyBadge, "request-preview-quantity-badge");

        header.getChildren().addAll(titleBox, spacer, qtyBadge);

        VBox table = new VBox(0);
        addStyleClass(table, "request-preview-table");
        table.getChildren().add(buildPreviewTableHeader());
        for (RequestProcessingPreviewBuilder.PreviewLine line : order.lines()) {
            table.getChildren().add(buildPreviewTableRow(line));
        }

        card.getChildren().addAll(header, table);
        return card;
    }

    private HBox buildPreviewTableHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 14, 10, 14));
        addStyleClass(header, "request-preview-table-header");
        header.getChildren().addAll(
            previewHeaderCell("MÃƒ HÃ€NG", 120),
            previewHeaderCell("TÃŠN Máº¶T HÃ€NG", 250),
            previewHeaderCell("Sá» LÆ¯á»¢NG", 110),
            previewHeaderCell("Váº¬N CHUYá»‚N", 150),
            previewHeaderCell("Dá»° KIáº¾N NHáº¬N", 140),
            previewHeaderCell("Háº N NHáº¬N", 140)
        );
        return header;
    }

    private HBox buildPreviewTableRow(RequestProcessingPreviewBuilder.PreviewLine line) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        addStyleClass(row, "request-preview-table-row");

        row.getChildren().add(previewValueCell(line.item().code, 120, true));
        row.getChildren().add(previewValueCell(line.item().name, 250, false));
        row.getChildren().add(previewValueCell(String.valueOf(line.quantity()), 110, true));

        HBox transportBox = new HBox(buildTransportBadgeCompact(
            DeliveryMethod.displayLabelOf(line.transport())
        ));
        transportBox.setAlignment(Pos.CENTER_LEFT);
        transportBox.setMinWidth(150);
        transportBox.setPrefWidth(150);
        row.getChildren().add(transportBox);

        row.getChildren().add(previewValueCell(line.estimatedDate().format(DATE_FORMAT), 140, false));
        row.getChildren().add(previewValueCell(
            line.desiredDate() == null ? "N/A" : line.desiredDate().format(DATE_FORMAT),
            140,
            false
        ));
        return row;
    }

    private Label previewHeaderCell(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        addStyleClass(label, "request-preview-header-cell");
        return label;
    }

    private Label previewValueCell(String text, double width, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        addStyleClass(label, "request-preview-value-cell");
        if (bold) {
            addStyleClass(label, "request-preview-value-cell-strong");
        }
        return label;
    }

    private void showCreationError() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("KhÃ´ng thá»ƒ táº¡o Ä‘Æ¡n");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText("KhÃ´ng thá»ƒ táº¡o cÃ¡c Ä‘Æ¡n hÃ ng Ä‘Ã£ phÃ¢n bá»•.");
        styleDialog(errorAlert);
        errorAlert.showAndWait();
    }

    private void addStyleClass(Node node, String... styleClasses) {
        for (String styleClass : styleClasses) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        }
    }

    private void showToast(String message) {
        Stage toast = new Stage();
        toast.setAlwaysOnTop(true);
        toast.initModality(Modality.NONE);

        Label label = new Label(message);
        label.setStyle(
            "-fx-background-color: #253D2C; -fx-text-fill: white;" +
            "-fx-padding: 14 24; -fx-background-radius: 10;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;"
        );

        Scene scene = new Scene(new StackPane(label));
        scene.setFill(null);
        toast.setScene(scene);
        toast.show();

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(2.5), event -> toast.close())
        );
        timeline.play();
    }

    private void styleDialog(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-size: 13px;");
    }

    private Label buildTransportBadgeCompact(String transport) {
        boolean seaTransport = isSeaTransport(transport);
        String icon = seaTransport ? "\uD83D\uDEA2 " : "\u2708 ";
        String background = seaTransport ? "#E8F1FF" : "#FFF4E5";
        String foreground = seaTransport ? "#2563EB" : "#D97706";

        Label badge = new Label(icon + transport);
        badge.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + foreground + ";" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 5 10;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );
        return badge;
    }

    private boolean isSeaTransport(String transport) {
        if (transport == null) {
            return false;
        }
        return switch (transport.trim()) {
            case "Duong bien", "Tau", "\u0110\u01b0\u1eddng bi\u1ec3n", "T\u00e0u" -> true;
            default -> false;
        };
    }
}

