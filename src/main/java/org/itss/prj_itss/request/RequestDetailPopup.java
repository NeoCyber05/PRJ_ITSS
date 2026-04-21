package org.itss.prj_itss.request;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.itss.prj_itss.order.OrderDetailPanel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RequestDetailPopup {

    public record RequestItem(
        String code,
        String name,
        String quantity,
        String unit,
        String dueDate
    ) { }

    public record RequestDetailData(
        String requestId,
        String createdAt,
        String earliestDue,
        String status,
        List<RequestItem> items,
        List<String> allocatedOrderIds
    ) { }

    private static final Map<String, RequestDetailData> SAMPLE_DATA = buildSampleData();

    private final RequestDetailData detail;
    private VBox orderSideCard;
    private StackPane orderSideContent;
    private double dialogHeight;
    private double requestCardWidth;
    private double orderCardWidth;

    private RequestDetailPopup(String requestId) {
        this.detail = SAMPLE_DATA.getOrDefault(requestId, SAMPLE_DATA.get("YC-2026-003"));
    }

    public static void show(Window owner, String requestId) {
        new RequestDetailPopup(requestId).showPopup(owner);
    }

    private void showPopup(Window owner) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }

        double sceneWidth = resolveSceneWidth(owner);
        double sceneHeight = resolveSceneHeight(owner);
        configureLayout(sceneWidth);

        StackPane overlay = new StackPane();
        overlay.setPadding(new Insets(18));
        overlay.setStyle("-fx-background-color: transparent;");

        HBox dialogShell = buildDialogShell(stage);
        overlay.getChildren().add(dialogShell);
        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                stage.close();
            }
        });

        Scene scene = new Scene(overlay, sceneWidth, sceneHeight);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.showAndWait();
    }

    private void configureLayout(double sceneWidth) {
        double availableWidth = sceneWidth - 36;
        double desiredOrderWidth = Math.min(420, Math.max(340, availableWidth * 0.32));
        double desiredRequestWidth = Math.min(860, availableWidth - desiredOrderWidth - 16);

        requestCardWidth = Math.max(740, desiredRequestWidth);
        orderCardWidth = Math.max(340, Math.min(desiredOrderWidth, availableWidth - requestCardWidth - 16));
        dialogHeight = Math.max(720, Math.min(820, resolveDialogHeight(availableWidth)));
    }

    private double resolveDialogHeight(double availableWidth) {
        if (availableWidth < 1240) {
            return 760;
        }
        return 800;
    }

    private HBox buildDialogShell(Stage stage) {
        HBox shell = new HBox(16);
        shell.setAlignment(Pos.CENTER);

        VBox requestCard = buildRequestCard(stage);
        requestCard.setPrefWidth(requestCardWidth);
        requestCard.setMaxWidth(requestCardWidth);
        requestCard.setPrefHeight(dialogHeight);
        requestCard.setMaxHeight(dialogHeight);

        orderSideCard = buildOrderSideCard();

        shell.getChildren().addAll(requestCard, orderSideCard);
        return shell;
    }

    private VBox buildRequestCard(Stage stage) {
        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 26;" +
            "-fx-border-radius: 26;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.20), 24, 0, 0, 8);"
        );

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(22, 24, 22, 24));

        Label title = new Label("Chi tiết " + detail.requestId());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("×");
        closeButton.setOnAction(event -> stage.close());
        closeButton.setStyle(
            "-fx-background-color: #F8FAFC;" +
            "-fx-border-color: #D8E2EF;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #64748B;" +
            "-fx-padding: 4 14;"
        );

        header.getChildren().addAll(title, spacer, closeButton);

        VBox body = new VBox(22);
        body.setPadding(new Insets(0, 24, 24, 24));
        body.getChildren().addAll(
            buildSummaryGrid(),
            buildItemsSection(),
            buildAllocatedOrdersSection()
        );

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        card.getChildren().addAll(header, buildDivider(), scrollPane);
        return card;
    }

    private VBox buildOrderSideCard() {
        orderSideContent = new StackPane();
        orderSideContent.setStyle("-fx-background-color: white;");

        VBox card = new VBox(orderSideContent);
        card.setPrefWidth(orderCardWidth);
        card.setMaxWidth(orderCardWidth);
        card.setPrefHeight(dialogHeight);
        card.setMaxHeight(dialogHeight);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 26;" +
            "-fx-border-radius: 26;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.16), 22, 0, 0, 8);"
        );
        VBox.setVgrow(orderSideContent, Priority.ALWAYS);

        card.setVisible(false);
        card.setManaged(false);
        return card;
    }

    private GridPane buildSummaryGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(14, 0, 4, 0));
        grid.setHgap(20);
        grid.setVgap(18);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        left.setHgrow(Priority.ALWAYS);

        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        right.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(left, right);

        grid.add(buildSummaryMetric("MÃ YÊU CẦU", detail.requestId(), "#1E293B"), 0, 0);
        grid.add(buildSummaryMetric("NGÀY TẠO", detail.createdAt(), "#1E293B"), 1, 0);
        grid.add(buildSummaryMetric("NGÀY ĐẾN HẠN SỚM NHẤT", detail.earliestDue(), "#EF4444"), 0, 1);
        grid.add(buildStatusMetric("TRẠNG THÁI", detail.status()), 1, 1);
        return grid;
    }

    private VBox buildSummaryMetric(String labelText, String valueText, String valueColor) {
        VBox metric = new VBox(8);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        Label value = new Label(valueText);
        value.setWrapText(true);
        value.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + valueColor + ";"
        );

        metric.getChildren().addAll(label, value);
        return metric;
    }

    private VBox buildStatusMetric(String labelText, String status) {
        VBox metric = new VBox(8);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        metric.getChildren().addAll(label, buildStatusBadge(status));
        return metric;
    }

    private VBox buildItemsSection() {
        VBox section = new VBox(14);
        section.getChildren().addAll(
            buildSectionTitle("Danh sách mặt hàng"),
            buildRequestItemsTable()
        );
        return section;
    }

    private VBox buildAllocatedOrdersSection() {
        VBox section = new VBox(14);
        section.getChildren().add(buildSectionTitle("Danh sách Đơn hàng đã phân bổ"));

        if (detail.allocatedOrderIds().isEmpty()) {
            VBox emptyState = new VBox(8);
            emptyState.setAlignment(Pos.CENTER_LEFT);
            emptyState.setPadding(new Insets(18, 20, 18, 20));
            emptyState.setStyle(
                "-fx-background-color: #F8FAFC;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 16;" +
                "-fx-background-radius: 16;"
            );

            Label title = new Label("Chưa có đơn hàng được phân bổ");
            title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

            Label subtitle = new Label("Yêu cầu này vẫn đang chờ phân bổ hoặc chưa tạo đơn hàng.");
            subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

            emptyState.getChildren().addAll(title, subtitle);
            section.getChildren().add(emptyState);
            return section;
        }

        section.getChildren().add(buildAllocatedOrdersTable());
        return section;
    }

    private VBox buildRequestItemsTable() {
        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #E2E8F0;" +
            "-fx-border-radius: 18;" +
            "-fx-background-radius: 18;"
        );

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle(
            "-fx-background-color: #F8FAF6;" +
            "-fx-background-radius: 18 18 0 0;" +
            "-fx-border-color: transparent transparent #E2E8F0 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        header.getChildren().addAll(
            tableHeader("MÃ HÀNG", 110),
            tableHeader("TÊN", 220),
            tableHeader("SỐ LƯỢNG", 90),
            tableHeader("ĐƠN VỊ", 80),
            tableHeader("NGÀY NHẬN RIÊNG", 120)
        );

        table.getChildren().add(header);
        for (RequestItem item : detail.items()) {
            table.getChildren().add(buildRequestItemRow(item));
        }

        return table;
    }

    private HBox buildRequestItemRow(RequestItem item) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 16, 14, 16));
        row.setStyle("-fx-border-color: transparent transparent #EDF2F7 transparent; -fx-border-width: 0 0 1 0;");

        row.getChildren().addAll(
            tableValue(item.code(), 110, true),
            tableValue(item.name(), 220, false),
            tableValue(item.quantity(), 90, true),
            tableValue(item.unit(), 80, false),
            tableValue(item.dueDate(), 120, false)
        );

        return row;
    }

    private VBox buildAllocatedOrdersTable() {
        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #E2E8F0;" +
            "-fx-border-radius: 18;" +
            "-fx-background-radius: 18;"
        );

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle(
            "-fx-background-color: #F8FAF6;" +
            "-fx-background-radius: 18 18 0 0;" +
            "-fx-border-color: transparent transparent #E2E8F0 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        header.getChildren().addAll(
            tableHeader("MÃ ĐƠN", 104),
            tableHeader("SITE", 170),
            tableHeader("VẬN CHUYỂN", 114),
            tableHeader("NGÀY TẠO", 96),
            tableHeader("TRẠNG THÁI", 110),
            tableHeader("CHI TIẾT", 96)
        );

        table.getChildren().add(header);
        for (String orderId : detail.allocatedOrderIds()) {
            table.getChildren().add(buildAllocatedOrderRow(orderId));
        }

        return table;
    }

    private HBox buildAllocatedOrderRow(String orderId) {
        OrderDetailPanel.OrderDetailData orderData = OrderDetailPanel.getOrderDetailData(orderId);

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 16, 14, 16));
        row.setStyle("-fx-border-color: transparent transparent #EDF2F7 transparent; -fx-border-width: 0 0 1 0;");

        HBox transportCell = new HBox(buildTransportBadge(resolveTransportSummary(orderData)));
        transportCell.setAlignment(Pos.CENTER_LEFT);
        transportCell.setMinWidth(114);
        transportCell.setPrefWidth(114);

        HBox statusCell = new HBox(buildStatusBadge(orderData.status()));
        statusCell.setAlignment(Pos.CENTER_LEFT);
        statusCell.setMinWidth(110);
        statusCell.setPrefWidth(110);

        Button viewButton = new Button("Xem đơn");
        viewButton.setOnAction(event -> showOrderDetail(orderId));
        viewButton.setStyle(
            "-fx-background-color: #0F172A;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 999;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 7 12;"
        );

        HBox actionCell = new HBox(viewButton);
        actionCell.setAlignment(Pos.CENTER_LEFT);
        actionCell.setMinWidth(96);
        actionCell.setPrefWidth(96);

        row.getChildren().addAll(
            tableValue(orderId, 104, true),
            tableValue(orderData.siteName(), 170, false),
            transportCell,
            tableValue(resolveCreatedDate(orderData.createdAt()), 96, false),
            statusCell,
            actionCell
        );

        return row;
    }

    private void showOrderDetail(String orderId) {
        Node content = new OrderDetailPanel(orderId, this::hideOrderDetail, OrderDetailPanel.LayoutMode.COMPACT).getView();

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");

        orderSideContent.getChildren().setAll(scrollPane);
        orderSideCard.setVisible(true);
        orderSideCard.setManaged(true);
    }

    private void hideOrderDetail() {
        orderSideContent.getChildren().clear();
        orderSideCard.setVisible(false);
        orderSideCard.setManaged(false);
    }

    private Label buildSectionTitle(String text) {
        Label title = new Label(text);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        return title;
    }

    private Region buildDivider() {
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #E5EAF2;");
        return divider;
    }

    private Label tableHeader(String text, double width) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #64748B;");
        return label;
    }

    private Label tableValue(String text, double width, boolean highlight) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-font-weight: " + (highlight ? "bold" : "normal") + ";" +
            "-fx-text-fill: #334155;"
        );
        return label;
    }

    private Label buildStatusBadge(String status) {
        String background;
        String foreground;

        switch (status) {
            case "Chờ xử lý":
            case "Chờ xác nhận":
                background = "#FFF4E5";
                foreground = "#D97706";
                break;
            case "Đang xử lý":
                background = "#E8F1FF";
                foreground = "#2563EB";
                break;
            case "Đang giao":
                background = "#F2EAFF";
                foreground = "#7C3AED";
                break;
            case "Đã hoàn thành":
            case "Hoàn thành":
                background = "#EAF8EF";
                foreground = "#15803D";
                break;
            default:
                background = "#F3F4F6";
                foreground = "#6B7280";
                break;
        }

        Label badge = new Label("● " + status);
        badge.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + foreground + ";" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 7 12;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );
        return badge;
    }

    private Label buildTransportBadge(String transport) {
        String icon = "Đường biển".equals(transport) ? "🚢 " : "✈ ";
        String background = "Đường biển".equals(transport) ? "#E8F1FF" : "#FFF4E5";
        String foreground = "Đường biển".equals(transport) ? "#2563EB" : "#D97706";

        Label badge = new Label(icon + transport);
        badge.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + foreground + ";" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 7 10;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );
        return badge;
    }

    private String resolveCreatedDate(String createdAt) {
        int splitIndex = createdAt.indexOf(' ');
        if (splitIndex < 0 || splitIndex == createdAt.length() - 1) {
            return createdAt;
        }
        return createdAt.substring(splitIndex + 1);
    }

    private String resolveTransportSummary(OrderDetailPanel.OrderDetailData orderData) {
        if (orderData.items().isEmpty()) {
            return "Đường biển";
        }
        return orderData.items().get(0).transport();
    }

    private double resolveSceneWidth(Window owner) {
        if (owner != null && owner.getWidth() > 0) {
            return Math.max(1160, Math.min(1500, owner.getWidth() - 24));
        }
        return 1360;
    }

    private double resolveSceneHeight(Window owner) {
        if (owner != null && owner.getHeight() > 0) {
            return Math.max(760, Math.min(900, owner.getHeight() - 24));
        }
        return 820;
    }

    private static Map<String, RequestDetailData> buildSampleData() {
        Map<String, RequestDetailData> data = new LinkedHashMap<>();

        data.put("YC-2026-001", new RequestDetailData(
            "YC-2026-001",
            "28/03/2026",
            "15/04/2026",
            "Chờ xử lý",
            List.of(
                new RequestItem("MH001", "iPhone 16 Pro Max", "40", "Chiếc", "15/04/2026"),
                new RequestItem("MH002", "Samsung Galaxy S25 Ultra", "60", "Chiếc", "15/04/2026")
            ),
            List.of()
        ));

        data.put("YC-2026-002", new RequestDetailData(
            "YC-2026-002",
            "30/03/2026",
            "20/04/2026",
            "Đang xử lý",
            List.of(
                new RequestItem("MH002", "Samsung Galaxy S25 Ultra", "120", "Chiếc", "20/04/2026"),
                new RequestItem("MH004", "iPad Air M3", "40", "Chiếc", "20/04/2026"),
                new RequestItem("MH005", "Sony WH-1000XM5", "50", "Chiếc", "20/04/2026")
            ),
            List.of("DH-2026-004")
        ));

        data.put("YC-2026-003", new RequestDetailData(
            "YC-2026-003",
            "01/04/2026",
            "25/04/2026",
            "Đang giao",
            List.of(
                new RequestItem("MH004", "iPad Air M3", "100", "Chiếc", "25/04/2026"),
                new RequestItem("MH006", "Apple Watch Ultra 3", "80", "Chiếc", "25/04/2026")
            ),
            List.of("DH-2026-002", "DH-2026-003")
        ));

        data.put("YC-2026-004", new RequestDetailData(
            "YC-2026-004",
            "02/04/2026",
            "18/04/2026",
            "Đã hoàn thành",
            List.of(
                new RequestItem("MH003", "MacBook Pro M4", "50", "Chiếc", "18/04/2026")
            ),
            List.of("DH-2026-001")
        ));

        data.put("YC-2026-005", new RequestDetailData(
            "YC-2026-005",
            "03/04/2026",
            "22/04/2026",
            "Đã hủy",
            List.of(
                new RequestItem("MH007", "AirPods Pro 3", "30", "Chiếc", "22/04/2026"),
                new RequestItem("MH008", "Kindle Paperwhite", "20", "Chiếc", "22/04/2026")
            ),
            List.of()
        ));

        data.put("YC-2026-006", new RequestDetailData(
            "YC-2026-006",
            "04/04/2026",
            "28/04/2026",
            "Chờ xử lý",
            List.of(
                new RequestItem("MH001", "iPhone 16 Pro Max", "70", "Chiếc", "28/04/2026"),
                new RequestItem("MH004", "iPad Air M3", "45", "Chiếc", "28/04/2026"),
                new RequestItem("MH006", "Apple Watch Ultra 3", "20", "Chiếc", "28/04/2026")
            ),
            List.of()
        ));

        data.put("YC-2026-007", new RequestDetailData(
            "YC-2026-007",
            "05/04/2026",
            "30/04/2026",
            "Chờ xử lý",
            List.of(
                new RequestItem("MH002", "Samsung Galaxy S25 Ultra", "50", "Chiếc", "30/04/2026"),
                new RequestItem("MH009", "DJI Osmo Pocket 3", "12", "Chiếc", "30/04/2026")
            ),
            List.of()
        ));

        return data;
    }
}
