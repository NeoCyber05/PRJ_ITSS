package org.itss.prj_itss.request.processing;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.layout.Navigator;
import org.itss.prj_itss.service.RequestProcessingService;
import org.itss.prj_itss.ui.Notifications;
import org.itss.prj_itss.ui.StatusNodes;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

final class RequestProcessingPreviewDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Navigator navigator;
    private final RequestProcessingService requestProcessingService;
    private final int requestId;
    private final Map<Integer, Map<Integer, Allocation>> allocations;

    RequestProcessingPreviewDialog(
        Navigator navigator,
        RequestProcessingService requestProcessingService,
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) {
        this.navigator = navigator;
        this.requestProcessingService = requestProcessingService;
        this.requestId = requestId;
        this.allocations = allocations;
    }

    void show(Node ownerNode, List<RequestProcessingPreviewBuilder.PreviewOrder> previewOrders) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        Window ownerWindow = resolveOwnerWindow(ownerNode);
        if (ownerWindow != null) {
            dialog.initOwner(ownerWindow);
        }

        dialog.setTitle("Chi tiết phân bổ đơn hàng");
        dialog.setResizable(true);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color:#F5F9F6;");
        root.setPrefWidth(980);
        root.setPrefHeight(760);

        Label titleLabel = new Label("Chi tiết phân bổ đơn hàng");
        titleLabel.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#1a2e22;");

        int totalQuantity = previewOrders.stream()
            .flatMap(order -> order.lines().stream())
            .mapToInt(RequestProcessingPreviewBuilder.PreviewLine::quantity)
            .sum();
        int totalLines = previewOrders.stream()
            .mapToInt(order -> order.lines().size())
            .sum();

        Label subtitleLabel = new Label(
            previewOrders.size() + " đơn hàng dự kiến"
                + " • " + totalLines + " dòng phân bổ"
                + " • " + totalQuantity + " chiếc"
        );
        subtitleLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#6B7C72;");

        VBox ordersBox = new VBox(14);
        for (int index = 0; index < previewOrders.size(); index++) {
            ordersBox.getChildren().add(buildPreviewOrderCard(previewOrders.get(index), index + 1));
        }

        ScrollPane scrollPane = new ScrollPane(ordersBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(620);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button backButton = new Button("Quay lại");
        backButton.getStyleClass().add("forest-secondary-button");
        backButton.setOnAction(event -> dialog.close());

        Button sendButton = new Button("Gửi đơn hàng");
        sendButton.getStyleClass().add("request-confirm-button");
        sendButton.setOnAction(event -> {
            try {
                requestProcessingService.createAllocatedOrders(requestId, allocations);
                dialog.close();
                navigator.showView("orders");
                Notifications.showToast("Đã tạo đơn hàng thành công.");
            } catch (SQLException exception) {
                showCreationError();
            }
        });

        HBox footer = new HBox(12, backButton, sendButton);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titleLabel, subtitleLabel, scrollPane, footer);

        Scene scene = new Scene(root);
        RequestProcessingUiSupport.applyMainStylesheet(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private VBox buildPreviewOrderCard(RequestProcessingPreviewBuilder.PreviewOrder order, int index) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color:white;"
                + "-fx-background-radius:14;"
                + "-fx-border-radius:14;"
                + "-fx-border-color:#D8E8DD;"
                + "-fx-border-width:1;"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label orderLabel = new Label("Đơn hàng dự kiến " + index);
        orderLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1a2e22;");
        Label siteLabel = new Label(order.site().name + " • " + order.site().siteCode);
        siteLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#6B7C72;");
        titleBox.getChildren().addAll(orderLabel, siteLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int totalQuantity = order.lines().stream().mapToInt(RequestProcessingPreviewBuilder.PreviewLine::quantity).sum();
        Label qtyBadge = new Label(totalQuantity + " chiếc");
        qtyBadge.setStyle(
            "-fx-background-color:#EEF4FF;"
                + "-fx-text-fill:#2456C2;"
                + "-fx-background-radius:999;"
                + "-fx-padding:6 12;"
                + "-fx-font-size:11px;"
                + "-fx-font-weight:bold;"
        );

        header.getChildren().addAll(titleBox, spacer, qtyBadge);

        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color:#FCFEFD;"
                + "-fx-background-radius:12;"
                + "-fx-border-radius:12;"
                + "-fx-border-color:#E5ECE7;"
                + "-fx-border-width:1;"
        );
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
        header.setStyle("-fx-background-color:#F7FAF8; -fx-background-radius:12 12 0 0;");
        header.getChildren().addAll(
            previewHeaderCell("MÃ HÀNG", 120),
            previewHeaderCell("TÊN MẶT HÀNG", 250),
            previewHeaderCell("SỐ LƯỢNG", 110),
            previewHeaderCell("VẬN CHUYỂN", 150),
            previewHeaderCell("DỰ KIẾN NHẬN", 140),
            previewHeaderCell("HẠN NHẬN", 140)
        );
        return header;
    }

    private HBox buildPreviewTableRow(RequestProcessingPreviewBuilder.PreviewLine line) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle("-fx-border-color:transparent transparent #EEF3EF transparent; -fx-border-width:0 0 1 0;");

        row.getChildren().add(previewValueCell(line.item().code, 120, true));
        row.getChildren().add(previewValueCell(line.item().name, 250, false));
        row.getChildren().add(previewValueCell(String.valueOf(line.quantity()), 110, true));

        HBox transportBox = new HBox(StatusNodes.buildTransportBadgeCompact(
            RequestProcessingAllocationSupport.toDisplayDeliveryMethod(line.transport())
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
        label.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#6B7F95;");
        return label;
    }

    private Label previewValueCell(String text, double width, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(
            "-fx-font-size:12px;"
                + "-fx-text-fill:#1a2e22;"
                + (bold ? "-fx-font-weight:bold;" : "")
        );
        return label;
    }

    private Window resolveOwnerWindow(Node ownerNode) {
        if (ownerNode == null || ownerNode.getScene() == null) {
            return null;
        }
        return ownerNode.getScene().getWindow();
    }

    private void showCreationError() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Không thể tạo đơn");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText("Không thể tạo các đơn hàng đã phân bổ.");
        Notifications.styleDialog(errorAlert);
        errorAlert.showAndWait();
    }
}
