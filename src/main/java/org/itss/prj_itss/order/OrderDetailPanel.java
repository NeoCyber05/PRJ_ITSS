package org.itss.prj_itss.order;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.dao.IMerchandiseDAO;
import org.itss.prj_itss.dao.IOrderDAO;
import org.itss.prj_itss.dao.ISiteDAO;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.ui.StatusNodes;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderDetailPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int orderId;
    private final Runnable onBack;
    private final IOrderDAO orderDAO;
    private final ISiteDAO siteDAO;
    private final IMerchandiseDAO merchandiseDAO;
    private final double panelWidth;
    private final boolean wideLayout;

    private final BorderPane root;

    public OrderDetailPanel(String orderIdRaw, Runnable onBack, DAOFactory daoFactory) {
        this(orderIdRaw, onBack, daoFactory, 540);
    }

    public OrderDetailPanel(String orderIdRaw, Runnable onBack, DAOFactory daoFactory, double preferredWidth) {
        this.onBack = onBack;
        this.orderDAO = daoFactory.getOrderDAO();
        this.siteDAO = daoFactory.getSiteDAO();
        this.merchandiseDAO = daoFactory.getMerchandiseDAO();
        this.orderId = parseOrderId(orderIdRaw);
        this.panelWidth = Math.max(540, preferredWidth);
        this.wideLayout = this.panelWidth >= 720;

        root = new BorderPane();
        root.setMinWidth(this.panelWidth);
        root.setPrefWidth(this.panelWidth);
        root.setMaxWidth(this.panelWidth);
        root.setStyle("-fx-background-color: #F5F7FB;");
        buildContent();
    }

    private void buildContent() {
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            Label errorLabel = new Label("Không tìm thấy đơn hàng.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #DC2626; -fx-padding: 40;");
            root.setCenter(new StackPane(errorLabel));
            return;
        }

        Site site = siteDAO.findById(order.getSiteId());
        List<OrderMerchandise> items = orderDAO.findItemsByOrderId(orderId);

        VBox header = new VBox(18);
        header.setPadding(new Insets(28, 28, 22, 28));
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #E7EDF5 transparent; -fx-border-width: 0 0 1 0;");

        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("‹");
        backButton.setOnAction(event -> {
            if (onBack != null) {
                onBack.run();
            }
        });
        backButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #D9E2EE;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-text-fill: #475569;" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-min-width: 38;" +
            "-fx-min-height: 38;" +
            "-fx-cursor: hand;"
        );

        VBox titleBox = new VBox(6);
        Label titleLabel = new Label("Chi tiết đơn hàng");
        titleLabel.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label subtitleLabel = new Label("Mã đơn hàng: " + String.format("DH-2026-%03d", order.getId()));
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #7B8DA6;");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        topRow.getChildren().addAll(backButton, titleBox);

        Label topStatusBadge = buildTopStatusBadge(order.getStatus());
        header.getChildren().addAll(topRow, topStatusBadge);

        VBox content = new VBox(22);
        content.setPadding(new Insets(22, 22, 28, 22));
        content.getChildren().addAll(
            buildOverviewCard(order, site, items),
            buildProgressCard(order.getStatus()),
            buildItemsCard(items)
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setTop(header);
        root.setCenter(scrollPane);
    }

    private VBox buildOverviewCard(Order order, Site site, List<OrderMerchandise> items) {
        VBox card = buildCard("Thông tin tổng quan");

        VBox grid = new VBox(24);
        grid.getChildren().addAll(
            buildOverviewRow(
                buildInfoCell("Mã đơn hàng", String.format("DH-2026-%03d", order.getId())),
                buildInfoCell("Mã Site", site != null ? site.getSiteCode() : "N/A"),
                buildInfoCell("Tên Site", site != null ? site.getName() : "N/A")
            ),
            buildOverviewRow(
                buildInfoCell("Ngày tạo", formatDateTime(order)),
                buildBadgeInfoCell("Trạng thái", order.getStatus()),
                buildInfoCell("Tổng số mặt hàng", items.size() + " mặt hàng")
            )
        );

        card.getChildren().add(grid);
        return card;
    }

    private VBox buildProgressCard(String status) {
        VBox card = buildCard("Tiến trình đơn hàng");

        String normalizedStatus = normalizeStatusKey(status);
        boolean delivered = "completed".equals(normalizedStatus);
        boolean shipping = delivered || "shipping".equals(normalizedStatus);
        boolean confirmed = shipping || "confirmed".equals(normalizedStatus);

        HBox progress = new HBox(0);
        progress.setAlignment(Pos.CENTER);

        progress.getChildren().add(buildProgressStep("1", "Chờ xác nhận", confirmed ? "#F59E0B" : "#CBD5E1", confirmed));
        progress.getChildren().add(buildProgressLine(shipping ? "#60A5FA" : "#D6DFEA"));
        progress.getChildren().add(buildProgressStep("2", "Đang giao", shipping ? "#3B82F6" : "#CBD5E1", shipping));
        progress.getChildren().add(buildProgressLine(delivered ? "#22C55E" : "#D6DFEA"));
        progress.getChildren().add(buildProgressStep("3", "Hoàn thành", delivered ? "#22C55E" : "#CBD5E1", delivered));

        card.getChildren().add(progress);
        return card;
    }

    private VBox buildItemsCard(List<OrderMerchandise> items) {
        VBox card = buildCard("Danh sách mặt hàng");

        double indexWidth = wideLayout ? 50 : 42;
        double codeWidth = wideLayout ? 110 : 90;
        double nameWidth = wideLayout ? 240 : 170;
        double quantityWidth = wideLayout ? 120 : 92;
        double unitWidth = wideLayout ? 100 : 88;
        double transportWidth = wideLayout ? 170 : 120;

        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-radius: 16;" +
            "-fx-border-color: #E7EDF5;" +
            "-fx-border-width: 1;"
        );

        HBox header = new HBox();
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #FBFCFE; -fx-background-radius: 16 16 0 0;");
        header.getChildren().addAll(
            headerCell("STT", indexWidth),
            headerCell("MÃ HÀNG", codeWidth),
            headerCell("TÊN MẶT HÀNG", nameWidth),
            headerCell("SỐ LƯỢNG ĐẶT", quantityWidth),
            headerCell("ĐƠN VỊ TÍNH", unitWidth),
            headerCell("PHƯƠNG THỨC VẬN CHUYỂN", transportWidth)
        );
        table.getChildren().add(header);

        if (items.isEmpty()) {
            Label emptyLabel = new Label("Không có mặt hàng.");
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-padding: 18 20;");
            table.getChildren().add(emptyLabel);
        } else {
            int index = 1;
            for (OrderMerchandise item : items) {
                Merchandise merchandise = merchandiseDAO.findById(item.getMerchandiseId());
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(16, 18, 16, 18));
                row.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");
                row.getChildren().addAll(
                    tableCell(String.valueOf(index++), indexWidth, false),
                    tableCell(merchandise != null ? merchandise.getCode() : "N/A", codeWidth, true),
                    tableCell(merchandise != null ? merchandise.getName() : "N/A", nameWidth, false),
                    tableCell(item.getQuantity() != null ? item.getQuantity().toPlainString() : "0", quantityWidth, true),
                    tableCell(merchandise != null && merchandise.getUnit() != null ? merchandise.getUnit() : "N/A", unitWidth, false),
                    buildTransportCell(displayTransportMethod(item.getDeliveryMethod()), transportWidth)
                );
                table.getChildren().add(row);
            }
        }

        card.getChildren().add(table);
        return card;
    }

    private VBox buildCard(String title) {
        VBox card = new VBox(18);
        card.setPadding(new Insets(22));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-border-radius: 18;" +
            "-fx-border-color: #E5ECF4;" +
            "-fx-border-width: 1;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        card.getChildren().add(titleLabel);
        return card;
    }

    private HBox buildOverviewRow(VBox first, VBox second, VBox third) {
        HBox row = new HBox(18, first, second, third);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private VBox buildInfoCell(String label, String value) {
        VBox cell = new VBox(10);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setPrefWidth(140);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7B8DA6;");

        Label valueNode = new Label(value);
        valueNode.setWrapText(true);
        valueNode.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        cell.getChildren().addAll(labelNode, valueNode);
        return cell;
    }

    private VBox buildBadgeInfoCell(String label, String status) {
        VBox cell = new VBox(10);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setPrefWidth(140);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7B8DA6;");

        cell.getChildren().addAll(labelNode, StatusNodes.buildStatusBadge(displayStatus(status)));
        return cell;
    }

    private VBox buildProgressStep(String iconText, String label, String color, boolean active) {
        VBox step = new VBox(10);
        step.setAlignment(Pos.CENTER);

        Circle circle = new Circle(18);
        circle.setStyle("-fx-fill: " + (active ? color : "#FFFFFF") + "; -fx-stroke: " + color + "; -fx-stroke-width: 3;");

        Label iconLabel = new Label(iconText);
        iconLabel.setStyle("-fx-text-fill: " + (active ? "white" : color) + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        StackPane iconWrapper = new StackPane(circle, iconLabel);
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        step.getChildren().addAll(iconWrapper, labelNode);
        return step;
    }

    private Region buildProgressLine(String color) {
        Region line = new Region();
        double lineWidth = wideLayout ? 112 : 74;
        line.setPrefWidth(lineWidth);
        line.setMinWidth(lineWidth);
        line.setMaxWidth(lineWidth);
        line.setMinHeight(4);
        line.setPrefHeight(4);
        line.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 999;");
        VBox wrapper = new VBox(line);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private Label headerCell(String text, double width) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7B8DA6;");
        return label;
    }

    private Label tableCell(String text, double width, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #334155;" +
            (bold ? "-fx-font-weight: bold;" : "")
        );
        return label;
    }

    private HBox buildTransportCell(String deliveryMethod, double width) {
        HBox box = new HBox(StatusNodes.buildTransportBadgeCompact(deliveryMethod));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(width);
        box.setPrefWidth(width);
        return box;
    }

    private Label buildTopStatusBadge(String status) {
        String effectiveStatus = displayStatus(status);
        String normalizedStatus = normalizeStatusKey(status);
        String background = "#E8F1FF";
        String foreground = "#2563EB";

        if ("confirmed".equals(normalizedStatus)) {
            background = "#FFF4E5";
            foreground = "#D97706";
        } else if ("completed".equals(normalizedStatus)) {
            background = "#EAF8EF";
            foreground = "#15803D";
        }

        Label label = new Label(effectiveStatus);
        label.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + foreground + ";" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 10 16;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );
        return label;
    }

    private String formatDateTime(Order order) {
        if (order.getCreatedAt() == null) {
            return "N/A";
        }
        return order.getCreatedAt().toLocalTime().withSecond(0).withNano(0)
            + "\n"
            + order.getCreatedAt().toLocalDate().format(DATE_FORMAT);
    }

    private String displayStatus(String status) {
        if (status == null || status.isBlank()) {
            return "N/A";
        }
        return switch (status.trim()) {
            case "Cho xu ly", "Chờ xử lý" -> "Chờ xử lý";
            case "Dang xu ly", "Đang xử lý" -> "Đang xử lý";
            case "Cho xac nhan", "Chờ xác nhận" -> "Chờ xác nhận";
            case "Dang giao", "Đang giao" -> "Đang giao";
            case "Da hoan thanh", "Hoan thanh", "Đã hoàn thành", "Hoàn thành" -> "Đã hoàn thành";
            case "Da huy", "Đã hủy" -> "Đã hủy";
            default -> status;
        };
    }

    private String displayTransportMethod(String deliveryMethod) {
        if (deliveryMethod == null || deliveryMethod.isBlank()) {
            return "N/A";
        }
        return switch (deliveryMethod.trim()) {
            case "May bay", "Máy bay" -> "Máy bay";
            case "Tau", "Tàu" -> "Tàu";
            case "Duong bien", "Đường biển" -> "Đường biển";
            default -> deliveryMethod;
        };
    }

    private String normalizeStatusKey(String status) {
        if (status == null) {
            return "other";
        }
        return switch (status.trim()) {
            case "Cho xac nhan", "Chờ xác nhận" -> "confirmed";
            case "Dang giao", "Đang giao" -> "shipping";
            case "Da hoan thanh", "Hoan thanh", "Đã hoàn thành", "Hoàn thành" -> "completed";
            default -> "other";
        };
    }

    private int parseOrderId(String orderIdRaw) {
        try {
            return Integer.parseInt(orderIdRaw.replaceAll("\\D+", ""));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    public Node getView() {
        return root;
    }
}
