package org.itss.prj_itss.ordering.request.detail;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.entity.RequestMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.ordering.order.OrderDetailPanel;
import org.itss.prj_itss.service.MerchandiseService;
import org.itss.prj_itss.service.OrderService;
import org.itss.prj_itss.service.SiteService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public final class RequestDetailPopupController {

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

    private ApplicationContext context;
    private double requestCollapsedWidth;
    private double requestExpandedWidth;
    private double orderPanelWidth;

    void init(
        Stage dialog,
        String requestCode,
        ApplicationContext context,
        Request request,
        List<RequestMerchandise> requestItems,
        List<Order> allocatedOrders,
        LocalDate earliestDeadline,
        double sceneWidth,
        double requestCollapsedWidth,
        double requestExpandedWidth,
        double orderPanelWidth
    ) {
        this.context = context;
        this.requestCollapsedWidth = requestCollapsedWidth;
        this.requestExpandedWidth = requestExpandedWidth;
        this.orderPanelWidth = orderPanelWidth;

        closeButton.setOnAction(event -> dialog.close());
        StackPane.setAlignment(dialogShell, Pos.TOP_CENTER);
        scrollContent.setMinWidth(Math.max(0, sceneWidth - 1));
        setPanelWidth(requestCard, requestExpandedWidth);
        setPanelWidth(orderDetailContainer, requestExpandedWidth);
        hideOrderDetail();

        titleLabel.setText("Chi tiết " + requestCode);
        requestCodeValueLabel.setText(requestCode);
        createdDateValueLabel.setText(
            request != null && request.getCreatedAt() != null
                ? request.getCreatedAt().toLocalDate().format(DATE_FORMAT)
                : "N/A"
        );
        earliestDeadlineValueLabel.setText(earliestDeadline != null ? earliestDeadline.format(DATE_FORMAT) : "N/A");
        statusContainer.getChildren().setAll(buildStatusBadge(statusText(request != null ? request.getStatus() : null)));

        renderRequestItems(requestItems, context.merchandiseService());
        renderAllocatedOrders(allocatedOrders, context.orderService(), context.siteService());
    }

    private void renderRequestItems(List<RequestMerchandise> items, MerchandiseService merchandiseService) {
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

        for (RequestMerchandise item : items) {
            Merchandise merchandise = merchandiseService.findById(item.getMerchandiseId());
            requestItemsTable.getChildren().add(buildTableRow(
                List.of(
                    merchandise != null ? merchandise.getCode() : "N/A",
                    merchandise != null ? merchandise.getName() : "N/A",
                    item.getQuantityOrdered() != null ? item.getQuantityOrdered().toPlainString() : "0",
                    merchandise != null && merchandise.getUnit() != null ? merchandise.getUnit() : "N/A",
                    item.getDesiredDeliveryDate() != null ? item.getDesiredDeliveryDate().format(DATE_FORMAT) : "N/A"
                ),
                widths,
                false
            ));
        }
    }

    private void renderAllocatedOrders(List<Order> orders, OrderService orderService, SiteService siteService) {
        List<Integer> widths = List.of(115, 170, 120, 110, 130, 36);
        allocatedOrdersTable.getChildren().clear();
        allocatedOrdersTable.getChildren().add(buildTableHeader(
            List.of("MÃ ĐƠN", "SITE", "VẬN CHUYỂN", "NGÀY TẠO", "TRẠNG THÁI", ""),
            widths
        ));

        if (orders.isEmpty()) {
            allocatedOrdersTable.getChildren().add(emptyLabel("Chưa có đơn hàng nào được phân bổ."));
            return;
        }

        for (Order order : orders) {
            Site site = siteService.findById(order.getSiteId());
            String deliveryMethod = resolvePrimaryDeliveryMethod(orderService.findItemsByOrderId(order.getId()));
            allocatedOrdersTable.getChildren().add(buildAllocatedOrderRow(
                order,
                site,
                deliveryMethod,
                selectedOrder -> showOrderDetail(selectedOrder.getId()),
                widths
            ));
        }
    }

    private HBox buildAllocatedOrderRow(
        Order order,
        Site site,
        String deliveryMethod,
        Consumer<Order> onOrderSelected,
        List<Integer> widths
    ) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        addStyleClass(row, "request-detail-table-row", "request-detail-clickable-row");
        row.setOnMouseClicked(event -> onOrderSelected.accept(order));

        row.getChildren().add(fixedCell(String.format("DH-2026-%03d", order.getId()), widths.get(0), true));
        row.getChildren().add(fixedCell(site != null ? site.getName() : "N/A", widths.get(1), false));

        HBox transportBox = new HBox(buildTransportBadgeCompact(displayTransportMethod(deliveryMethod)));
        transportBox.setAlignment(Pos.CENTER_LEFT);
        transportBox.setMinWidth(widths.get(2));
        transportBox.setPrefWidth(widths.get(2));
        row.getChildren().add(transportBox);

        row.getChildren().add(fixedCell(
            order.getCreatedAt() != null ? order.getCreatedAt().toLocalDate().format(DATE_FORMAT) : "N/A",
            widths.get(3),
            false
        ));

        HBox statusBox = new HBox(buildStatusBadge(statusText(order.getStatus())));
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setMinWidth(widths.get(4));
        statusBox.setPrefWidth(widths.get(4));
        row.getChildren().add(statusBox);

        Button openButton = new Button("→");
        openButton.setOnMouseClicked(event -> event.consume());
        openButton.setOnAction(event -> onOrderSelected.accept(order));
        addStyleClass(openButton, "request-detail-open-button");
        HBox actionBox = new HBox(openButton);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setMinWidth(widths.get(5));
        actionBox.setPrefWidth(widths.get(5));
        row.getChildren().add(actionBox);

        return row;
    }

    private void showOrderDetail(int orderId) {
        orderDetailContainer.getChildren().setAll(
            new OrderDetailPanel(
                String.valueOf(orderId),
                this::hideOrderDetail,
                context,
                requestExpandedWidth
            ).getView()
        );

        // Ẩn Popup yêu cầu
        requestCard.setVisible(false);
        requestCard.setManaged(false);

        orderDetailContainer.setManaged(true);
        orderDetailContainer.setVisible(true);
    }

    private void hideOrderDetail() {
        orderDetailContainer.getChildren().clear();
        orderDetailContainer.setManaged(false);
        orderDetailContainer.setVisible(false);

        // Hiện lại Popup yêu cầu
        requestCard.setVisible(true);
        requestCard.setManaged(true);
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

    private String resolvePrimaryDeliveryMethod(List<OrderMerchandise> items) {
        if (items.isEmpty()) {
            return "N/A";
        }
        return items.get(0).getDeliveryMethod() != null ? items.get(0).getDeliveryMethod() : "N/A";
    }

    private void setPanelWidth(Region panel, double width) {
        panel.setMinWidth(width);
        panel.setPrefWidth(width);
        panel.setMaxWidth(width);
    }

    private String statusText(String status) {
        if (status == null || status.isBlank()) {
            return "N/A";
        }
        return status.trim();
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

    private Label buildStatusBadge(String status) {
        String[] colors = resolveStatusBadgeColors(status);

        Label badge = new Label("\u25cf " + status);
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
        return switch (status.trim().toLowerCase()) {
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
