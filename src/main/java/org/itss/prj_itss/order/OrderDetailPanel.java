package org.itss.prj_itss.order;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailPanel {

    public enum LayoutMode {
        STANDARD,
        COMPACT
    }

    public record OrderItem(
        String index,
        String code,
        String name,
        String quantity,
        String unit,
        String transport
    ) { }

    public record OrderDetailData(
        String orderId,
        String siteCode,
        String siteName,
        String createdAt,
        String status,
        List<OrderItem> items
    ) { }

    private static final Map<String, OrderDetailData> SAMPLE_DATA = buildSampleData();

    private final OrderDetailData detail;
    private final Runnable onBackAction;
    private final LayoutMode layoutMode;
    private final boolean compact;
    private final VBox view;

    public OrderDetailPanel(String orderId, Runnable onBackAction) {
        this(orderId, onBackAction, LayoutMode.STANDARD);
    }

    public OrderDetailPanel(String orderId, Runnable onBackAction, LayoutMode layoutMode) {
        this.detail = getOrderDetailData(orderId);
        this.onBackAction = onBackAction;
        this.layoutMode = layoutMode;
        this.compact = layoutMode == LayoutMode.COMPACT;
        this.view = buildView();
    }

    public static OrderDetailData getOrderDetailData(String orderId) {
        return SAMPLE_DATA.getOrDefault(orderId, SAMPLE_DATA.get("DH-2026-004"));
    }

    public Node getView() {
        return view;
    }

    private VBox buildView() {
        VBox content = new VBox(compact ? 16 : 24);
        content.setPadding(new Insets(compact ? 18 : 28));
        content.setStyle("-fx-background-color: white;");

        content.getChildren().addAll(
            buildHeader(),
            buildInfoCard(),
            buildProgressCard(),
            buildItemsCard()
        );

        return content;
    }

    private HBox buildHeader() {
        HBox header = new HBox(compact ? 12 : 16);
        header.setAlignment(Pos.TOP_LEFT);

        if (onBackAction != null) {
            Button backButton = new Button("←");
            backButton.setOnAction(event -> onBackAction.run());
            backButton.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #D8E2EF;" +
                "-fx-border-radius: " + (compact ? 10 : 12) + ";" +
                "-fx-background-radius: " + (compact ? 10 : 12) + ";" +
                "-fx-cursor: hand;" +
                "-fx-font-size: " + (compact ? 16 : 20) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #64748B;" +
                "-fx-padding: " + (compact ? "7 11" : "10 16") + ";"
            );
            header.getChildren().add(backButton);
        }

        VBox textGroup = new VBox(compact ? 6 : 10);
        HBox.setHgrow(textGroup, Priority.ALWAYS);

        Label title = new Label("Chi tiết đơn hàng");
        title.setStyle(
            "-fx-font-size: " + (compact ? 22 : 32) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1E293B;"
        );

        Label subtitle = new Label("Mã đơn hàng: " + detail.orderId());
        subtitle.setStyle(
            "-fx-font-size: " + (compact ? 12 : 13) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #64748B;"
        );

        textGroup.getChildren().addAll(title, subtitle, buildStatusBadge(detail.status()));
        header.getChildren().add(textGroup);
        return header;
    }

    private VBox buildInfoCard() {
        VBox card = buildCardShell("Thông tin tổng quan");

        GridPane grid = new GridPane();
        grid.setHgap(compact ? 18 : 28);
        grid.setVgap(compact ? 14 : 20);
        grid.setPadding(new Insets(6, 0, 0, 0));

        int columnCount = compact ? 2 : 3;
        for (int i = 0; i < columnCount; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / columnCount);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
        }

        if (compact) {
            grid.add(buildInfoItem("Mã đơn hàng", detail.orderId()), 0, 0);
            grid.add(buildInfoItem("Mã Site", detail.siteCode()), 1, 0);
            grid.add(buildInfoItem("Tên Site", detail.siteName()), 0, 1);
            grid.add(buildInfoItem("Ngày tạo", detail.createdAt()), 1, 1);
            grid.add(buildInfoStatus("Trạng thái", detail.status()), 0, 2);
            grid.add(buildInfoItem("Tổng số mặt hàng", detail.items().size() + " mặt hàng"), 1, 2);
        } else {
            grid.add(buildInfoItem("Mã đơn hàng", detail.orderId()), 0, 0);
            grid.add(buildInfoItem("Mã Site", detail.siteCode()), 1, 0);
            grid.add(buildInfoItem("Tên Site", detail.siteName()), 2, 0);
            grid.add(buildInfoItem("Ngày tạo", detail.createdAt()), 0, 1);
            grid.add(buildInfoStatus("Trạng thái", detail.status()), 1, 1);
            grid.add(buildInfoItem("Tổng số mặt hàng", detail.items().size() + " mặt hàng"), 2, 1);
        }

        card.getChildren().add(grid);
        return card;
    }

    private VBox buildInfoItem(String labelText, String value) {
        VBox item = new VBox(compact ? 4 : 6);

        Label label = new Label(labelText);
        label.setStyle(
            "-fx-font-size: " + (compact ? 11 : 12) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #64748B;"
        );

        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        valueLabel.setStyle(
            "-fx-font-size: " + (compact ? 15 : 18) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1E293B;"
        );

        item.getChildren().addAll(label, valueLabel);
        return item;
    }

    private VBox buildInfoStatus(String labelText, String status) {
        VBox item = new VBox(compact ? 4 : 6);

        Label label = new Label(labelText);
        label.setStyle(
            "-fx-font-size: " + (compact ? 11 : 12) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #64748B;"
        );

        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(compact ? 4 : 5);
        dot.setFill(Color.web(statusColor(status)));

        Label value = new Label(status);
        value.setStyle(
            "-fx-font-size: " + (compact ? 14 : 16) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + statusTextColor(status) + ";"
        );

        statusBox.getChildren().addAll(dot, value);
        item.getChildren().addAll(label, statusBox);
        return item;
    }

    private VBox buildProgressCard() {
        VBox card = buildCardShell("Tiến trình đơn hàng");
        HBox progress = new HBox();
        progress.setAlignment(Pos.CENTER);
        progress.setPadding(new Insets(compact ? 0 : 8, compact ? 2 : 6, 0, compact ? 2 : 6));

        int activeStep = resolveActiveStep(detail.status());
        String[] labels = {"Chờ xác nhận", "Đang giao", "Hoàn thành"};
        String[] symbols = {"◔", "🚚", "✓"};

        for (int i = 0; i < labels.length; i++) {
            progress.getChildren().add(buildStepNode(labels[i], symbols[i], i, activeStep));

            if (i < labels.length - 1) {
                Region connector = new Region();
                connector.setPrefHeight(3);
                connector.setMinHeight(3);
                connector.setMaxHeight(3);
                connector.setPrefWidth(compact ? 54 : 120);
                connector.setStyle(
                    "-fx-background-color: " + (i < activeStep ? "#3B82F6" : "#DCE3EE") + ";" +
                    "-fx-background-radius: 999;"
                );

                VBox connectorWrap = new VBox(connector);
                connectorWrap.setAlignment(Pos.CENTER);
                connectorWrap.setPadding(new Insets(0, 0, compact ? 20 : 24, 0));
                HBox.setHgrow(connectorWrap, Priority.ALWAYS);
                progress.getChildren().add(connectorWrap);
            }
        }

        card.getChildren().add(progress);
        return card;
    }

    private VBox buildStepNode(String labelText, String symbol, int index, int activeStep) {
        VBox step = new VBox(compact ? 8 : 10);
        step.setAlignment(Pos.CENTER);
        step.setMinWidth(compact ? 72 : 110);

        boolean passed = index < activeStep;
        boolean current = index == activeStep;

        String background = passed ? "#F59E0B" : current ? "#3B82F6" : "#FFFFFF";
        String textColor = passed || current ? "#FFFFFF" : "#94A3B8";
        String border = passed || current ? "transparent" : "#D9E2EE";
        int badgeSize = compact ? 38 : 48;

        Label badge = new Label(symbol);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-border-color: " + border + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 999;" +
            "-fx-background-radius: 999;" +
            "-fx-min-width: " + badgeSize + ";" +
            "-fx-min-height: " + badgeSize + ";" +
            "-fx-pref-width: " + badgeSize + ";" +
            "-fx-pref-height: " + badgeSize + ";" +
            "-fx-font-size: " + (compact ? 16 : 20) + "px;" +
            "-fx-font-weight: bold;"
        );

        Label label = new Label(labelText);
        label.setWrapText(true);
        label.setMaxWidth(compact ? 82 : 110);
        label.setAlignment(Pos.CENTER);
        label.setStyle(
            "-fx-font-size: " + (compact ? 11 : 13) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + (current ? "#2563EB" : passed ? "#B45309" : "#64748B") + ";"
        );

        step.getChildren().addAll(badge, label);
        return step;
    }

    private VBox buildItemsCard() {
        return compact ? buildCompactItemsCard() : buildStandardItemsCard();
    }

    private VBox buildStandardItemsCard() {
        VBox card = buildCardShell("Danh sách mặt hàng");

        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #E2E8F0;" +
            "-fx-border-radius: 18;" +
            "-fx-background-radius: 18;"
        );

        table.getChildren().add(buildItemHeaderRow());
        for (OrderItem item : detail.items()) {
            table.getChildren().add(buildItemRow(item));
        }

        card.getChildren().add(table);
        return card;
    }

    private VBox buildCompactItemsCard() {
        VBox card = buildCardShell("Danh sách mặt hàng");
        VBox list = new VBox(10);

        for (OrderItem item : detail.items()) {
            list.getChildren().add(buildCompactItemCard(item));
        }

        card.getChildren().add(list);
        return card;
    }

    private VBox buildCompactItemCard(OrderItem item) {
        VBox itemCard = new VBox(10);
        itemCard.setStyle(
            "-fx-background-color: #F8FAFC;" +
            "-fx-border-color: #E2E8F0;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 14;"
        );

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label codeBadge = new Label(item.code());
        codeBadge.setStyle(
            "-fx-background-color: #EAF1FF;" +
            "-fx-text-fill: #2563EB;" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 5 10;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(codeBadge, spacer, buildTransportBadge(item.transport()));

        Label itemName = new Label(item.name());
        itemName.setWrapText(true);
        itemName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        HBox metaRow = new HBox(12);
        metaRow.getChildren().addAll(
            buildCompactMeta("SL", item.quantity()),
            buildCompactMeta("ĐVT", item.unit()),
            buildCompactMeta("STT", item.index())
        );

        itemCard.getChildren().addAll(topRow, itemName, metaRow);
        return itemCard;
    }

    private VBox buildCompactMeta(String labelText, String valueText) {
        VBox meta = new VBox(3);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        Label value = new Label(valueText);
        value.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        meta.getChildren().addAll(label, value);
        return meta;
    }

    private HBox buildItemHeaderRow() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setStyle(
            "-fx-background-color: #F8FAFC;" +
            "-fx-background-radius: 18 18 0 0;" +
            "-fx-border-color: transparent transparent #E2E8F0 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        row.getChildren().addAll(
            itemHeader("STT", 48, Pos.CENTER),
            itemHeader("MÃ HÀNG", 84, Pos.CENTER_LEFT),
            itemHeader("TÊN MẶT HÀNG", 154, Pos.CENTER_LEFT),
            itemHeader("SỐ LƯỢNG ĐẶT", 86, Pos.CENTER),
            itemHeader("ĐƠN VỊ TÍNH", 82, Pos.CENTER),
            itemHeader("PHƯƠNG THỨC VẬN CHUYỂN", 130, Pos.CENTER)
        );

        return row;
    }

    private HBox buildItemRow(OrderItem item) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 18, 16, 18));
        row.setStyle("-fx-border-color: transparent transparent #EDF2F7 transparent; -fx-border-width: 0 0 1 0;");

        row.getChildren().addAll(
            itemValue(item.index(), 48, Pos.CENTER, false),
            itemValue(item.code(), 84, Pos.CENTER_LEFT, true),
            itemValue(item.name(), 154, Pos.CENTER_LEFT, false),
            itemValue(item.quantity(), 86, Pos.CENTER, true),
            itemValue(item.unit(), 82, Pos.CENTER, false),
            buildTransportCell(item.transport(), 130)
        );

        return row;
    }

    private Label itemHeader(String text, double width, Pos alignment) {
        Label label = new Label(text);
        label.setAlignment(alignment);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;");
        return label;
    }

    private Label itemValue(String text, double width, Pos alignment, boolean highlight) {
        Label label = new Label(text);
        label.setAlignment(alignment);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: " + (highlight ? "bold" : "normal") + ";" +
            "-fx-text-fill: #1E293B;"
        );
        return label;
    }

    private HBox buildTransportCell(String transport, double width) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(width);
        box.setPrefWidth(width);
        box.getChildren().add(buildTransportBadge(transport));
        return box;
    }

    private Label buildTransportBadge(String transport) {
        String icon = "Đường biển".equals(transport) ? "🚢 " : "✈ ";
        String background = "Đường biển".equals(transport) ? "#E8F1FF" : "#FFF5E6";
        String foreground = "Đường biển".equals(transport) ? "#2563EB" : "#D97706";

        Label badge = new Label(icon + transport);
        badge.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + foreground + ";" +
            "-fx-background-radius: 999;" +
            "-fx-padding: " + (compact ? "5 10" : "6 12") + ";" +
            "-fx-font-size: " + (compact ? 11 : 12) + "px;" +
            "-fx-font-weight: bold;"
        );
        return badge;
    }

    private Label buildStatusBadge(String status) {
        String icon;
        String background;
        String foreground;

        switch (status) {
            case "Chờ xác nhận":
                icon = "◔ ";
                background = "#FFF3E8";
                foreground = "#D97706";
                break;
            case "Đang giao":
                icon = "🚚 ";
                background = "#E8F1FF";
                foreground = "#2563EB";
                break;
            case "Hoàn thành":
                icon = "✓ ";
                background = "#EAF8EF";
                foreground = "#15803D";
                break;
            default:
                icon = "• ";
                background = "#F3F4F6";
                foreground = "#6B7280";
                break;
        }

        Label badge = new Label(icon + status);
        badge.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + foreground + ";" +
            "-fx-border-color: " + foreground + "22;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;" +
            "-fx-padding: " + (compact ? "7 12" : "9 16") + ";" +
            "-fx-font-size: " + (compact ? 12 : 13) + "px;" +
            "-fx-font-weight: bold;"
        );
        return badge;
    }

    private VBox buildCardShell(String titleText) {
        VBox card = new VBox(compact ? 14 : 18);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #D7E1EE;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: " + (compact ? 18 : 22) + ";" +
            "-fx-background-radius: " + (compact ? 18 : 22) + ";" +
            "-fx-padding: " + (compact ? 18 : 28) + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 16, 0, 0, 4);"
        );

        Label title = new Label(titleText);
        title.setStyle(
            "-fx-font-size: " + (compact ? 16 : 18) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1E293B;"
        );

        card.getChildren().add(title);
        return card;
    }

    private int resolveActiveStep(String status) {
        switch (status) {
            case "Chờ xác nhận":
                return 0;
            case "Đang giao":
                return 1;
            case "Hoàn thành":
                return 2;
            default:
                return -1;
        }
    }

    private String statusColor(String status) {
        switch (status) {
            case "Chờ xác nhận":
                return "#F59E0B";
            case "Đang giao":
                return "#3B82F6";
            case "Hoàn thành":
                return "#22C55E";
            default:
                return "#9CA3AF";
        }
    }

    private String statusTextColor(String status) {
        switch (status) {
            case "Chờ xác nhận":
                return "#B45309";
            case "Đang giao":
                return "#1D4ED8";
            case "Hoàn thành":
                return "#15803D";
            default:
                return "#6B7280";
        }
    }

    private static Map<String, OrderDetailData> buildSampleData() {
        Map<String, OrderDetailData> data = new LinkedHashMap<>();

        data.put("DH-2026-001", new OrderDetailData(
            "DH-2026-001",
            "SITE004",
            "Singapore Trade Center",
            "09:15 03/04/2026",
            "Hoàn thành",
            List.of(
                new OrderItem("1", "MH003", "MacBook Pro M4", "50", "Chiếc", "Đường biển")
            )
        ));

        data.put("DH-2026-002", new OrderDetailData(
            "DH-2026-002",
            "SITE001",
            "Tokyo Electronics Hub",
            "10:30 02/04/2026",
            "Đang giao",
            List.of(
                new OrderItem("1", "MH004", "iPad Air M3", "100", "Chiếc", "Hàng không"),
                new OrderItem("2", "MH006", "Apple Watch Ultra 3", "80", "Chiếc", "Hàng không")
            )
        ));

        data.put("DH-2026-003", new OrderDetailData(
            "DH-2026-003",
            "SITE004",
            "Singapore Trade Center",
            "10:30 02/04/2026",
            "Chờ xác nhận",
            List.of(
                new OrderItem("1", "MH006", "Apple Watch Ultra 3", "30", "Chiếc", "Đường biển")
            )
        ));

        data.put("DH-2026-004", new OrderDetailData(
            "DH-2026-004",
            "SITE003",
            "Shenzhen Import Co.",
            "08:45 01/04/2026",
            "Đang giao",
            List.of(
                new OrderItem("1", "MH002", "Samsung Galaxy S25 Ultra", "300", "Chiếc", "Đường biển"),
                new OrderItem("2", "MH005", "Sony WH-1000XM5", "150", "Chiếc", "Đường biển"),
                new OrderItem("3", "MH004", "iPad Air M3", "80", "Chiếc", "Đường biển")
            )
        ));

        return data;
    }
}
