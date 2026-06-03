package org.itss.prj_itss.view.ordering.order;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.itss.prj_itss.App;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderDetailController;
import org.itss.prj_itss.controller.ordering.order.OrderManagementController;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class OrderDetailView implements ViewLifecycle {

    private static final String VIEW_RESOURCE = "/org/itss/prj_itss/view/ordering/order/order-detail-view.fxml";
    private static final String ORDERS_VIEW_ID = "orders";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Node view;
    private Navigator navigator;
    private OrderDetailController controller;
    private OrderManagementController managementController;
    private String orderIdRaw;
    private Order currentOrder;

    @FXML
    private StackPane backgroundContainer;

    @FXML
    private Region backdrop;

    @FXML
    private BorderPane panelRoot;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Region cancelSpacer;

    @FXML
    private Button cancelButton;

    @FXML
    private HBox topStatusContainer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox contentBox;

    public OrderDetailView() {
        loadView();
    }

    public void init(
        Navigator navigator,
        OrderDetailController controller,
        OrderManagementController managementController,
        String orderIdRaw
    ) {
        this.navigator = navigator;
        this.controller = controller;
        this.managementController = managementController;
        this.orderIdRaw = orderIdRaw;

        backdrop.setOnMouseClicked(event -> navigateToOrders());
        configureBackground();
        renderOrderDetail();
    }

    @Override
    public void onViewShown() {
        renderOrderDetail();
    }

    @FXML
    private void initialize() {
        scrollPane.addEventFilter(ScrollEvent.SCROLL, this::handlePanelScroll);
    }

    @FXML
    private void handleBackAction() {
        navigateToOrders();
    }

    @FXML
    private void handleCancelOrderAction() {
        if (currentOrder == null || controller == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy");
        alert.setHeaderText("Bạn có chắc chắn muốn hủy đơn hàng này không?");
        alert.setContentText("Hành động này không thể hoàn tác.");

        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }

            OrderCancellationApplicationService.CancellationResult result = controller.cancel(currentOrder.getId());
            if (result.success()) {
                navigateToOrders();
            }
        });
    }

    public Node getView() {
        return view;
    }

    private void loadView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(VIEW_RESOURCE));
            loader.setController(this);
            view = loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load order detail view.", exception);
        }
    }

    private void configureBackground() {
        Node background = loadOrdersBackground();
        background.setEffect(new GaussianBlur(14));
        background.setOpacity(0.96);
        backgroundContainer.getChildren().setAll(background);
    }

    private Node loadOrdersBackground() {
        try {
            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/org/itss/prj_itss/view/ordering/order/order-management-view.fxml")
            );
            Node background = loader.load();
            Object controllerObj = loader.getController();
            if (controllerObj instanceof OrderManagementView viewObj) {
                viewObj.init(navigator, managementController);
            }
            return background;
        } catch (Exception exception) {
            Label errorLabel = new Label("Không thể tải danh sách đơn hàng.");
            StackPane fallback = new StackPane(errorLabel);
            fallback.getStyleClass().add("content-area");
            return fallback;
        }
    }

    private void renderOrderDetail() {
        if (controller == null) {
            return;
        }

        int orderId = parseOrderId(orderIdRaw);
        Order order = controller.findById(orderId);
        currentOrder = order;
        if (order == null) {
            Label errorLabel = new Label("Không tìm thấy đơn hàng.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #DC2626; -fx-padding: 40;");
            panelRoot.setCenter(new StackPane(errorLabel));
            return;
        }

        Site site = controller.findSiteById(order.getSiteId());
        List<OrderMerchandise> items = controller.findItemsByOrderId(orderId);

        subtitleLabel.setText("Mã đơn hàng: " + formatOrderCode(order.getId()));
        setCancellationVisible("pending".equalsIgnoreCase(order.getStatus()));
        topStatusContainer.getChildren().setAll(buildTopStatusBadge(order.getStatus()));
        contentBox.getChildren().setAll(
            buildOverviewCard(order, site, items),
            buildProgressCard(order.getStatus()),
            buildItemsCard(items)
        );
    }

    private void setCancellationVisible(boolean visible) {
        cancelSpacer.setManaged(visible);
        cancelSpacer.setVisible(visible);
        cancelButton.setManaged(visible);
        cancelButton.setVisible(visible);
    }

    private VBox buildOverviewCard(Order order, Site site, List<OrderMerchandise> items) {
        VBox card = buildCard("Thông tin tổng quan");

        VBox grid = new VBox(24);
        grid.getChildren().addAll(
            buildOverviewRow(
                buildInfoCell("Mã đơn hàng", formatOrderCode(order.getId())),
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
        boolean confirmed = shipping || "pending".equals(normalizedStatus);

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

        double indexWidth = 42;
        double codeWidth = 90;
        double nameWidth = 170;
        double quantityWidth = 92;
        double unitWidth = 88;
        double transportWidth = 120;

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
                table.getChildren().add(buildItemRow(item, index++, indexWidth, codeWidth, nameWidth, quantityWidth, unitWidth, transportWidth));
            }
        }

        card.getChildren().add(table);
        return card;
    }

    private HBox buildItemRow(
        OrderMerchandise item,
        int index,
        double indexWidth,
        double codeWidth,
        double nameWidth,
        double quantityWidth,
        double unitWidth,
        double transportWidth
    ) {
        Merchandise merchandise = controller.findMerchandiseById(item.getMerchandiseId());
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 18, 16, 18));
        row.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");
        row.getChildren().addAll(
            tableCell(String.valueOf(index), indexWidth, false),
            tableCell(merchandise != null ? merchandise.getCode() : "N/A", codeWidth, true),
            tableCell(merchandise != null ? merchandise.getName() : "N/A", nameWidth, false),
            tableCell(item.getQuantity() != null ? item.getQuantity().toPlainString() : "0", quantityWidth, true),
            tableCell(merchandise != null && merchandise.getUnit() != null ? merchandise.getUnit() : "N/A", unitWidth, false),
            buildTransportCell(displayTransportMethod(item.getDeliveryMethod()), transportWidth)
        );
        return row;
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

        cell.getChildren().addAll(labelNode, buildStatusBadge(status));
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
        double lineWidth = 74;
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
        HBox box = new HBox(buildTransportBadgeCompact(deliveryMethod));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(width);
        box.setPrefWidth(width);
        return box;
    }

    private Label buildStatusBadge(String status) {
        String[] colors = resolveStatusBadgeColors(status);
        Label badge = new Label("● " + statusText(status));
        badge.setStyle(
            "-fx-background-color: " + colors[0] + ";" +
                "-fx-text-fill: " + colors[1] + ";" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 7 12;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );
        return badge;
    }

    private Label buildTransportBadgeCompact(String transport) {
        boolean seaTransport = isSeaTransport(transport);
        String icon = seaTransport ? "🚢 " : "✈ ";
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

    private String[] resolveStatusBadgeColors(String status) {
        return switch (normalizeStatusKey(status)) {
            case "pending" -> new String[]{"#FFF4E5", "#D97706"};
            case "processing" -> new String[]{"#E8F1FF", "#2563EB"};
            case "shipping" -> new String[]{"#F2EAFF", "#7C3AED"};
            case "completed" -> new String[]{"#EAF8EF", "#15803D"};
            case "cancelled" -> new String[]{"#FEE2E2", "#B91C1C"};
            default -> new String[]{"#F3F4F6", "#6B7280"};
        };
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

    private Label buildTopStatusBadge(String status) {
        String normalizedStatus = normalizeStatusKey(status);
        String background = "#E8F1FF";
        String foreground = "#2563EB";

        if ("pending".equals(normalizedStatus)) {
            background = "#FFF4E5";
            foreground = "#D97706";
        } else if ("completed".equals(normalizedStatus)) {
            background = "#EAF8EF";
            foreground = "#15803D";
        } else if ("cancelled".equals(normalizedStatus)) {
            background = "#FEE2E2";
            foreground = "#B91C1C";
        }

        Label label = new Label(statusText(status));
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

    private void handlePanelScroll(ScrollEvent event) {
        if (event.getDeltaY() == 0) {
            return;
        }

        double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollRange = contentHeight - viewportHeight;
        if (scrollRange > 0) {
            double deltaY = event.getDeltaY() * 3;
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY / scrollRange);
        }
        event.consume();
    }

    private void navigateToOrders() {
        if (navigator != null) {
            navigator.showView(ORDERS_VIEW_ID);
        }
    }

    private String formatOrderCode(int orderId) {
        return String.format("DH-2026-%03d", orderId);
    }

    private String formatDateTime(Order order) {
        if (order.getCreatedAt() == null) {
            return "N/A";
        }
        return order.getCreatedAt().toLocalTime().withSecond(0).withNano(0)
            + "\n"
            + order.getCreatedAt().toLocalDate().format(DATE_FORMAT);
    }

    private String statusText(String status) {
        return switch (normalizeStatusKey(status)) {
            case "pending" -> "Chờ xác nhận";
            case "shipping" -> "Đang giao";
            case "completed" -> "Đã hoàn thành";
            case "cancelled" -> "Đã hủy";
            default -> status == null || status.isBlank() ? "N/A" : status.trim();
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
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "other";
        }
        return normalized;
    }

    private int parseOrderId(String rawOrderId) {
        if (rawOrderId == null || rawOrderId.isBlank()) {
            return 1;
        }
        try {
            return Integer.parseInt(rawOrderId.replaceAll("\\D+", ""));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }
}
