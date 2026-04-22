package org.itss.prj_itss.request;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

import org.itss.prj_itss.common.StatusBadgeFactory;
import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.dao.IMerchandiseDAO;
import org.itss.prj_itss.dao.IOrderDAO;
import org.itss.prj_itss.dao.IRequestDAO;
import org.itss.prj_itss.dao.ISiteDAO;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.entity.RequestMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.layout.Navigator;
import org.itss.prj_itss.order.OrderDetailPanel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class RequestDetailPopup {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private RequestDetailPopup() {
    }

    public static void show(Window owner, String requestCode, DAOFactory daoFactory) {
        show(owner, requestCode, daoFactory, null);
    }

    public static void show(Window owner, String requestCode, DAOFactory daoFactory, Navigator navigator) {
        int requestId = parseRequestId(requestCode);

        IRequestDAO requestDAO = daoFactory.getRequestDAO();
        IMerchandiseDAO merchandiseDAO = daoFactory.getMerchandiseDAO();
        IOrderDAO orderDAO = daoFactory.getOrderDAO();
        ISiteDAO siteDAO = daoFactory.getSiteDAO();

        Request request = requestDAO.findById(requestId);
        List<RequestMerchandise> requestItems = requestDAO.findItemsByRequestId(requestId);
        List<Order> allocatedOrders = orderDAO.findAll().stream()
            .filter(order -> order.getRequestId() == requestId)
            .sorted(Comparator.comparingInt(Order::getId))
            .toList();
        LocalDate earliestDeadline = requestDAO.getEarliestDeliveryDate(requestId);

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) {
            dialog.initOwner(owner);
        }

        double sceneWidth = owner != null ? owner.getWidth() : 1440;
        double sceneHeight = owner != null ? owner.getHeight() : 900;
        double dialogMaxWidth = Math.min(sceneWidth - 64, 1660);
        double requestExpandedWidth = Math.min(980, dialogMaxWidth);
        double requestCollapsedWidth = Math.max(720, Math.min(780, dialogMaxWidth * 0.46));
        double orderPanelWidth = Math.max(740, Math.min(880, dialogMaxWidth - requestCollapsedWidth - 20));

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(15,23,42,0.35);");

        HBox dialogShell = new HBox(20);
        dialogShell.setAlignment(Pos.TOP_CENTER);

        VBox requestCard = new VBox(0);
        setPanelWidth(requestCard, requestExpandedWidth);
        requestCard.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 24;" +
            "-fx-border-radius: 24;" +
            "-fx-border-color: #E5ECF4;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.18), 28, 0, 0, 10);"
        );

        StackPane orderDetailContainer = new StackPane();
        orderDetailContainer.setManaged(false);
        orderDetailContainer.setVisible(false);
        setPanelWidth(orderDetailContainer, orderPanelWidth);
        orderDetailContainer.setStyle("-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.18), 28, 0, 0, 10);");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(28, 28, 20, 28));
        header.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label("Chi tiết " + requestCode);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.setOnAction(event -> dialog.close());
        closeButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #64748B;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 0 6;" +
            "-fx-cursor: hand;"
        );

        header.getChildren().addAll(titleLabel, headerSpacer, closeButton);

        HBox summaryRow = new HBox(28);
        summaryRow.setPadding(new Insets(24, 28, 28, 28));
        summaryRow.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");
        summaryRow.getChildren().addAll(
            buildSummaryBlock("MÃ YÊU CẦU", requestCode, "#1F2937"),
            buildSummaryBlock(
                "NGÀY TẠO",
                request != null && request.getCreatedAt() != null ? request.getCreatedAt().toLocalDate().format(DATE_FORMAT) : "N/A",
                "#1F2937"
            ),
            buildSummaryBlock(
                "NGÀY ĐẾN HẠN SỚM NHẤT",
                earliestDeadline != null ? earliestDeadline.format(DATE_FORMAT) : "N/A",
                "#DC2626"
            ),
            buildStatusBlock("TRẠNG THÁI", request != null ? request.getStatus() : null)
        );

        VBox content = new VBox(28);
        content.setPadding(new Insets(28));
        content.getChildren().addAll(
            buildRequestItemsSection(requestItems, merchandiseDAO),
            buildAllocatedOrdersSection(
                allocatedOrders,
                orderDAO,
                siteDAO,
                order -> showOrderDetail(
                    requestCard,
                    orderDetailContainer,
                    daoFactory,
                    order.getId(),
                    requestCollapsedWidth,
                    requestExpandedWidth,
                    orderPanelWidth
                )
            )
        );

        requestCard.getChildren().addAll(header, summaryRow, content);
        dialogShell.getChildren().addAll(requestCard, orderDetailContainer);

        StackPane scrollContent = new StackPane(dialogShell);
        scrollContent.setMinWidth(Math.max(0, sceneWidth - 1));
        scrollContent.setPadding(new Insets(32));
        StackPane.setAlignment(dialogShell, Pos.TOP_CENTER);

        ScrollPane dialogScroll = new ScrollPane(scrollContent);
        dialogScroll.setFitToHeight(true);
        dialogScroll.setPannable(true);
        dialogScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dialogScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dialogScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        overlay.getChildren().add(dialogScroll);

        Scene scene = new Scene(overlay, sceneWidth, sceneHeight);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        if (owner != null) {
            dialog.setX(owner.getX());
            dialog.setY(owner.getY());
        }

        dialog.showAndWait();
    }

    private static VBox buildSummaryBlock(String label, String value, String valueColor) {
        VBox box = new VBox(8);
        HBox.setHgrow(box, Priority.ALWAYS);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8B9AAF;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + valueColor + ";");

        box.getChildren().addAll(labelNode, valueNode);
        return box;
    }

    private static VBox buildStatusBlock(String label, String status) {
        VBox box = new VBox(8);
        HBox.setHgrow(box, Priority.ALWAYS);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8B9AAF;");

        box.getChildren().addAll(labelNode, StatusBadgeFactory.buildStatusBadge(displayStatus(status)));
        return box;
    }

    private static VBox buildRequestItemsSection(List<RequestMerchandise> items, IMerchandiseDAO merchandiseDAO) {
        VBox section = new VBox(16);

        Label title = new Label("Danh sách mặt hàng");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        List<Integer> widths = List.of(120, 200, 100, 90, 130);

        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-border-radius: 18;" +
            "-fx-border-color: #EEF3F8;" +
            "-fx-border-width: 1;"
        );

        table.getChildren().add(buildTableHeader(
            List.of("MÃ HÀNG", "TÊN", "SỐ LƯỢNG", "ĐƠN VỊ", "NGÀY NHẬN RIÊNG"),
            widths
        ));

        if (items.isEmpty()) {
            Label emptyLabel = new Label("Không có mặt hàng.");
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-padding: 18 20;");
            table.getChildren().add(emptyLabel);
        } else {
            for (RequestMerchandise item : items) {
                Merchandise merchandise = merchandiseDAO.findById(item.getMerchandiseId());
                table.getChildren().add(buildTableRow(
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

        section.getChildren().addAll(title, table);
        return section;
    }

    private static VBox buildAllocatedOrdersSection(
        List<Order> orders,
        IOrderDAO orderDAO,
        ISiteDAO siteDAO,
        Consumer<Order> onOrderSelected
    ) {
        VBox section = new VBox(16);

        Label title = new Label("Danh sách đơn hàng đã phân bổ");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        List<Integer> widths = List.of(115, 170, 120, 110, 130, 36);

        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-border-radius: 18;" +
            "-fx-border-color: #EEF3F8;" +
            "-fx-border-width: 1;"
        );

        table.getChildren().add(buildTableHeader(
            List.of("MÃ ĐƠN", "SITE", "VẬN CHUYỂN", "NGÀY TẠO", "TRẠNG THÁI", ""),
            widths
        ));

        if (orders.isEmpty()) {
            Label emptyLabel = new Label("Chưa có đơn hàng nào được phân bổ.");
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-padding: 18 20;");
            table.getChildren().add(emptyLabel);
        } else {
            for (Order order : orders) {
                Site site = siteDAO.findById(order.getSiteId());
                String deliveryMethod = resolvePrimaryDeliveryMethod(orderDAO.findItemsByOrderId(order.getId()));
                table.getChildren().add(buildAllocatedOrderRow(order, site, deliveryMethod, onOrderSelected, widths));
            }
        }

        section.getChildren().addAll(title, table);
        return section;
    }

    private static HBox buildAllocatedOrderRow(
        Order order,
        Site site,
        String deliveryMethod,
        Consumer<Order> onOrderSelected,
        List<Integer> widths
    ) {
        String baseRowStyle = "-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;";
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setStyle(baseRowStyle + (onOrderSelected != null ? "-fx-cursor: hand;" : ""));
        if (onOrderSelected != null) {
            row.setOnMouseEntered(event -> row.setStyle(baseRowStyle + "-fx-background-color: #F8FBFF; -fx-cursor: hand;"));
            row.setOnMouseExited(event -> row.setStyle(baseRowStyle + "-fx-cursor: hand;"));
            row.setOnMouseClicked(event -> onOrderSelected.accept(order));
        }

        row.getChildren().add(fixedCell(String.format("DH-2026-%03d", order.getId()), widths.get(0), true));
        row.getChildren().add(fixedCell(site != null ? site.getName() : "N/A", widths.get(1), false));

        HBox transportBox = new HBox(StatusBadgeFactory.buildTransportBadgeCompact(displayTransportMethod(deliveryMethod)));
        transportBox.setAlignment(Pos.CENTER_LEFT);
        transportBox.setMinWidth(widths.get(2));
        transportBox.setPrefWidth(widths.get(2));
        row.getChildren().add(transportBox);

        row.getChildren().add(fixedCell(
            order.getCreatedAt() != null ? order.getCreatedAt().toLocalDate().format(DATE_FORMAT) : "N/A",
            widths.get(3),
            false
        ));

        HBox statusBox = new HBox(StatusBadgeFactory.buildStatusBadge(displayStatus(order.getStatus())));
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setMinWidth(widths.get(4));
        statusBox.setPrefWidth(widths.get(4));
        row.getChildren().add(statusBox);

        Button openButton = new Button("→");
        openButton.setOnMouseClicked(event -> event.consume());
        openButton.setOnAction(event -> {
            if (onOrderSelected != null) {
                onOrderSelected.accept(order);
            }
        });
        openButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #64748B;" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 0;" +
            "-fx-cursor: hand;"
        );
        HBox actionBox = new HBox(openButton);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setMinWidth(widths.get(5));
        actionBox.setPrefWidth(widths.get(5));
        row.getChildren().add(actionBox);

        return row;
    }

    private static void showOrderDetail(
        VBox requestCard,
        StackPane orderDetailContainer,
        DAOFactory daoFactory,
        int orderId,
        double requestCollapsedWidth,
        double requestExpandedWidth,
        double orderPanelWidth
    ) {
        orderDetailContainer.getChildren().setAll(
            new OrderDetailPanel(
                String.valueOf(orderId),
                () -> hideOrderDetail(requestCard, orderDetailContainer, requestExpandedWidth),
                daoFactory,
                orderPanelWidth
            ).getView()
        );
        setPanelWidth(requestCard, requestCollapsedWidth);
        setPanelWidth(orderDetailContainer, orderPanelWidth);
        orderDetailContainer.setManaged(true);
        orderDetailContainer.setVisible(true);
    }

    private static void hideOrderDetail(VBox requestCard, StackPane orderDetailContainer, double requestExpandedWidth) {
        orderDetailContainer.getChildren().clear();
        orderDetailContainer.setManaged(false);
        orderDetailContainer.setVisible(false);
        setPanelWidth(requestCard, requestExpandedWidth);
    }

    private static HBox buildTableHeader(List<String> labels, List<Integer> widths) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 18, 12, 18));
        header.setStyle("-fx-background-color: #F8FBF5; -fx-background-radius: 18 18 0 0;");

        for (int index = 0; index < labels.size(); index++) {
            Label label = new Label(labels.get(index));
            label.setWrapText(true);
            label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #8191A7;");
            label.setMinWidth(widths.get(index));
            label.setPrefWidth(widths.get(index));
            header.getChildren().add(label);
        }

        return header;
    }

    private static HBox buildTableRow(List<String> values, List<Integer> widths, boolean subtle) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");

        for (int index = 0; index < values.size(); index++) {
            row.getChildren().add(fixedCell(values.get(index), widths.get(index), index == 0 || !subtle));
        }

        return row;
    }

    private static Label fixedCell(String value, double width, boolean bold) {
        Label label = new Label(value);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #475569;" +
            (bold ? "-fx-font-weight: bold;" : "")
        );
        return label;
    }

    private static String resolvePrimaryDeliveryMethod(List<OrderMerchandise> items) {
        if (items.isEmpty()) {
            return "N/A";
        }
        return items.get(0).getDeliveryMethod() != null ? items.get(0).getDeliveryMethod() : "N/A";
    }

    private static void setPanelWidth(Region panel, double width) {
        panel.setMinWidth(width);
        panel.setPrefWidth(width);
        panel.setMaxWidth(width);
    }

    private static String displayStatus(String status) {
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

    private static String displayTransportMethod(String deliveryMethod) {
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

    private static int parseRequestId(String requestCode) {
        try {
            int parsed = Integer.parseInt(requestCode.replaceAll("\\D+", "").replaceFirst("^2026", ""));
            return parsed > 0 ? parsed : 1;
        } catch (Exception exception) {
            return 1;
        }
    }
}
