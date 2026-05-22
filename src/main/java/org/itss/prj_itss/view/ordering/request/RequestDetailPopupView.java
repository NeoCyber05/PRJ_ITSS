package org.itss.prj_itss.view.ordering.request;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderDetailController;
import org.itss.prj_itss.controller.ordering.order.OrderManagementController;
import org.itss.prj_itss.controller.ordering.request.RequestDetailPopupController;
import org.itss.prj_itss.model.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.model.request.application.sales.AllocatedOrderRow;
import org.itss.prj_itss.model.request.application.sales.RequestDetailItemRow;
import org.itss.prj_itss.model.request.application.sales.RequestDetailViewModel;
import org.itss.prj_itss.view.ordering.order.OrderDetailView;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class RequestDetailPopupView implements ViewLifecycle {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private StackPane scrollContent;

    @FXML
    private StackPane dialogShell;

    @FXML
    private VBox requestCard;

    @FXML
    private StackPane orderDetailContainer;

    @FXML
    private Label titleLabel;

    @FXML
    private Button closeButton;

    @FXML
    private Label requestCodeValueLabel;

    @FXML
    private Label createdDateValueLabel;

    @FXML
    private Label earliestDeadlineValueLabel;

    @FXML
    private HBox statusContainer;

    @FXML
    private VBox requestItemsTable;

    @FXML
    private VBox allocatedOrdersTable;

    private RequestDetailPopupController controller;
    private OrderDetailController orderDetailController;
    private OrderManagementController orderManagementController;
    private Navigator navigator;

    private double requestCollapsedWidth;
    private double requestExpandedWidth;
    private double orderPanelWidth;

    private HBox currentSelectedRow;
    private AllocatedOrderRow currentSelectedOrder;
    private RequestDetailViewModel currentViewModel;

    public void init(
        Stage dialog,
        String requestCode,
        RequestDetailPopupController controller,
        OrderDetailController orderDetailController,
        OrderManagementController orderManagementController,
        Navigator navigator,
        RequestDetailViewModel viewModel,
        double sceneWidth,
        double requestCollapsedWidth,
        double requestExpandedWidth,
        double orderPanelWidth
    ) {
        this.controller = controller;
        this.orderDetailController = orderDetailController;
        this.orderManagementController = orderManagementController;
        this.navigator = navigator;
        this.requestCollapsedWidth = requestCollapsedWidth;
        this.requestExpandedWidth = requestExpandedWidth;
        this.orderPanelWidth = orderPanelWidth;
        this.currentViewModel = viewModel;

        closeButton.setOnAction(event -> dialog.close());
        StackPane.setAlignment(dialogShell, Pos.TOP_CENTER);
        scrollContent.setMinWidth(Math.max(0, sceneWidth - 1));
        setPanelWidth(requestCard, requestExpandedWidth);
        setPanelWidth(orderDetailContainer, requestExpandedWidth);
        hideOrderDetail();

        titleLabel.setText("Chi tiết " + requestCode);
        requestCodeValueLabel.setText(requestCode);
        createdDateValueLabel.setText(
            viewModel.createdAt() != null && !viewModel.createdAt().isBlank()
                ? viewModel.createdAt()
                : "N/A"
        );
        earliestDeadlineValueLabel.setText(viewModel.earliestDeadline() != null && !viewModel.earliestDeadline().isBlank()
            ? viewModel.earliestDeadline()
            : "N/A"
        );
        statusContainer.getChildren().setAll(buildStatusBadge(viewModel.status(), true));

        renderRequestItems(viewModel.requestItems());
        renderAllocatedOrders(viewModel.allocatedOrders());
    }

    private void renderRequestItems(List<RequestDetailItemRow> items) {
        List<Integer> widths = List.of(120, 200, 100, 90, 130);
        requestItemsTable.getChildren().clear();
        requestItemsTable.getChildren().add(buildTableHeader(
            List.of("MÃ HÀNG", "TÊN", "SỐ LƯỢNG", "ĐƠN VỊ", "NGÀY NHẬN RIÊNG"),
            widths
        ));

        if (items.isEmpty()) {
            requestItemsTable.getChildren().add(emptyLabel("Không có mặt hàng."));
            return;
        }

        for (RequestDetailItemRow item : items) {
            requestItemsTable.getChildren().add(buildTableRow(
                List.of(
                    item.code(),
                    item.name(),
                    item.quantity(),
                    item.unit(),
                    item.desiredDate()
                ),
                widths,
                false
            ));
        }
    }

    private void renderAllocatedOrders(List<AllocatedOrderRow> orders) {
        List<Integer> widths = List.of(115, 140, 110, 110, 120, 90);
        allocatedOrdersTable.getChildren().clear();
        allocatedOrdersTable.getChildren().add(buildTableHeader(
            List.of("MÃ ĐƠN", "SITE", "VẬN CHUYỂN", "NGÀY TẠO", "TRẠNG THÁI", ""),
            widths
        ));

        if (orders.isEmpty()) {
            allocatedOrdersTable.getChildren().add(emptyLabel("Chưa có đơn hàng nào được phân bổ."));
            return;
        }

        for (AllocatedOrderRow order : orders) {
            allocatedOrdersTable.getChildren().add(buildAllocatedOrderRow(
                order,
                widths,
                (selectedOrder, selectedRow) -> {
                    this.currentSelectedOrder = selectedOrder;
                    this.currentSelectedRow = selectedRow;
                    showOrderDetail(selectedOrder.orderId());
                }
            ));
        }
    }

    private HBox buildAllocatedOrderRow(
        AllocatedOrderRow order,
        List<Integer> widths,
        java.util.function.BiConsumer<AllocatedOrderRow, HBox> onOrderSelected
    ) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        addStyleClass(row, "request-detail-table-row", "request-detail-clickable-row");
        row.setOnMouseClicked(event -> onOrderSelected.accept(order, row));

        row.getChildren().add(fixedCell(order.orderCode(), widths.get(0), true));
        row.getChildren().add(fixedCell(order.siteName(), widths.get(1), false));

        HBox transportBox = new HBox(buildTransportBadgeCompact(displayTransportMethod(order.deliveryMethod())));
        transportBox.setAlignment(Pos.CENTER_LEFT);
        transportBox.setMinWidth(widths.get(2));
        transportBox.setPrefWidth(widths.get(2));
        row.getChildren().add(transportBox);

        row.getChildren().add(fixedCell(order.createdAt() != null && !order.createdAt().isBlank() ? order.createdAt() : "N/A", widths.get(3), false));

        HBox statusBox = new HBox(buildStatusBadge(order.status(), false));
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setMinWidth(widths.get(4));
        statusBox.setPrefWidth(widths.get(4));
        row.getChildren().add(statusBox);

        Button openButton = new Button("\u2192");
        openButton.setOnMouseClicked(event -> event.consume());
        openButton.setOnAction(event -> onOrderSelected.accept(order, row));
        addStyleClass(openButton, "request-detail-open-button");
        
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setMinWidth(widths.get(5));
        actionBox.setPrefWidth(widths.get(5));

        if (order.cancellable()) {
            Button cancelBtn = new Button("Hủy");
            cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 4;");
            cancelBtn.setOnAction(e -> {
                e.consume();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Xác nhận hủy");
                alert.setHeaderText("Bạn có chắc chắn muốn hủy đơn hàng này không?");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        OrderCancellationApplicationService.CancellationResult result = controller.cancel(order.orderId());
                        if (result.success()) {
                            statusBox.getChildren().setAll(buildStatusBadge("cancelled", false));
                            actionBox.getChildren().remove(cancelBtn);
                        }
                    }
                });
            });
            actionBox.getChildren().add(cancelBtn);
        }
        actionBox.getChildren().add(openButton);
        
        row.getChildren().add(actionBox);

        return row;
    }

    private void showOrderDetail(int orderId) {
        OrderDetailView orderDetailView = new OrderDetailView();
        orderDetailView.init(navigator, orderDetailController, orderManagementController, String.valueOf(orderId));
        orderDetailContainer.getChildren().setAll(orderDetailView.getView());

        requestCard.setVisible(false);
        requestCard.setManaged(false);

        orderDetailContainer.setManaged(true);
        orderDetailContainer.setVisible(true);
    }

    private void hideOrderDetail() {
        orderDetailContainer.getChildren().clear();
        orderDetailContainer.setManaged(false);
        orderDetailContainer.setVisible(false);

        requestCard.setVisible(true);
        requestCard.setManaged(true);

        if (currentSelectedOrder != null && currentSelectedRow != null && controller != null) {
            AllocatedOrderRow refreshed = controller.findOrderRow(currentSelectedOrder.orderId());
            if (refreshed != null && "cancelled".equalsIgnoreCase(refreshed.status())) {
                HBox statusBox = (HBox) currentSelectedRow.getChildren().get(4);
                statusBox.getChildren().setAll(buildStatusBadge("cancelled", false));
                
                HBox actionBox = (HBox) currentSelectedRow.getChildren().get(5);
                if (actionBox.getChildren().size() > 1) {
                    actionBox.getChildren().remove(0);
                }
            }
        }
    }

    private HBox buildTableHeader(List<String> labels, List<Integer> widths) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 18, 12, 18));
        addStyleClass(header, "request-detail-table-header");

        for (int index = 0; index < labels.size(); index++) {
            Label label = new Label(labels.get(index));
            label.setWrapText(true);
            label.setMinWidth(widths.get(index));
            label.setPrefWidth(widths.get(index));
            addStyleClass(label, "request-detail-table-header-cell");
            header.getChildren().add(label);
        }

        return header;
    }

    private HBox buildTableRow(List<String> values, List<Integer> widths, boolean subtle) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        addStyleClass(row, "request-detail-table-row");

        for (int index = 0; index < values.size(); index++) {
            row.getChildren().add(fixedCell(values.get(index), widths.get(index), index == 0 || !subtle));
        }

        return row;
    }

    private Label fixedCell(String value, double width, boolean bold) {
        Label label = new Label(value);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        addStyleClass(label, "request-detail-table-cell");
        if (bold) {
            addStyleClass(label, "request-detail-table-cell-strong");
        }
        return label;
    }

    private Label emptyLabel(String text) {
        Label label = new Label(text);
        addStyleClass(label, "request-detail-empty-label");
        return label;
    }

    private void setPanelWidth(Region panel, double width) {
        panel.setMinWidth(width);
        panel.setPrefWidth(width);
        panel.setMaxWidth(width);
    }

    private String requestStatusText(String status) {
        return switch (normalizeStatusKey(status)) {
            case "pending" -> "Chờ xử lý";
            case "processing" -> "Đang xử lý";
            case "shipping" -> "Đang giao";
            case "completed" -> "Đã hoàn thành";
            case "cancelled" -> "Đã hủy";
            default -> status == null || status.isBlank() ? "N/A" : status.trim();
        };
    }

    private String orderStatusText(String status) {
        return switch (normalizeStatusKey(status)) {
            case "pending" -> "Chờ xác nhận";
            case "shipping" -> "Đang giao";
            case "completed" -> "Đã hoàn thành";
            case "cancelled" -> "Đã hủy";
            default -> status == null || status.isBlank() ? "N/A" : status.trim();
        };
    }

    private String normalizeStatusKey(String status) {
        if (status == null || status.isBlank()) {
            return "other";
        }
        String normalized = status.trim().toLowerCase();
        return normalized.isBlank() ? "other" : normalized;
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

    private Label buildStatusBadge(String status, boolean requestStatus) {
        String[] colors = resolveStatusBadgeColors(status);
        String displayText = requestStatus ? requestStatusText(status) : orderStatusText(status);

        Label badge = new Label("\u25cf " + displayText);
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

    private String[] resolveStatusBadgeColors(String status) {
        if (status == null || status.isBlank()) {
            return new String[]{"#F3F4F6", "#6B7280"};
        }
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
            case "Duong bien", "Tau", "\u0110\u01b0\u1eddng bi\u1ec3n", "T\u00e0u" -> true;
            default -> false;
        };
    }

    private void addStyleClass(Node node, String... styleClasses) {
        for (String styleClass : styleClasses) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        }
    }
}
