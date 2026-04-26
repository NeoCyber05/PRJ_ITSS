package org.itss.prj_itss.ordering.order;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;
import org.itss.prj_itss.service.MerchandiseService;
import org.itss.prj_itss.service.OrderService;
import org.itss.prj_itss.service.SiteService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OrderManagementController implements IViewController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObservableList<OrderRow> rows = FXCollections.observableArrayList();
    private final FilteredList<OrderRow> filteredRows = new FilteredList<>(rows, row -> true);

    private INavigator navigator;
    private OrderService orderService;
    private SiteService siteService;
    private MerchandiseService merchandiseService;

    @FXML
    private TableView<OrderRow> orderTable;

    @FXML
    private TableColumn<OrderRow, String> orderCodeColumn;

    @FXML
    private TableColumn<OrderRow, String> requestCodeColumn;

    @FXML
    private TableColumn<OrderRow, String> siteColumn;

    @FXML
    private TableColumn<OrderRow, String> itemsColumn;

    @FXML
    private TableColumn<OrderRow, String> createdAtColumn;

    @FXML
    private TableColumn<OrderRow, String> statusColumn;

    @FXML
    private TableColumn<OrderRow, OrderRow> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private Label paginationInfoLabel;

    @FXML
    private void initialize() {
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        orderCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().orderCode()));
        requestCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().requestCode()));
        siteColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteName()));
        itemsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().itemsSummary()));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().createdAt()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    HBox badge = buildStatusDot(status);
                    badge.setMinWidth(150);
                    setGraphic(badge);
                }
                setText(null);
            }
        });

        actionsColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(OrderRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                Button detailButton = new Button("Chi tiết");
                detailButton.setOnAction(event -> navigator.showView("order-detail:" + row.order().getId()));

                setGraphic(detailButton);
                setText(null);
            }
        });

        orderTable.setItems(filteredRows);

        statusFilter.getItems().addAll(
            "Mọi trạng thái",
            "Chờ xác nhận",
            "Đang giao",
            "Đã hoàn thành",
            "Đã hủy"
        );
        statusFilter.setValue("Mọi trạng thái");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    @Override
    public void init(INavigator navigator, ApplicationContext context) {
        this.navigator = navigator;
        this.orderService = context.orderService();
        this.siteService = context.siteService();
        this.merchandiseService = context.merchandiseService();
        reload();
    }

    private void reload() {
        rows.setAll(orderService.findAll().stream().map(this::toRow).toList());
        applyFilters();
    }

    private OrderRow toRow(Order order) {
        Site site = siteService.findById(order.getSiteId());
        List<OrderMerchandise> items = orderService.findItemsByOrderId(order.getId());
        String itemSummary = items.stream()
            .map(item -> {
                Merchandise merchandise = merchandiseService.findById(item.getMerchandiseId());
                return merchandise == null ? "?" : merchandise.getCode();
            })
            .collect(Collectors.joining(", "));

        if (itemSummary.isBlank()) {
            itemSummary = "-";
        }

        return new OrderRow(
            order,
            String.format("DH-2026-%03d", order.getId()),
            String.format("YC-2026-%03d", order.getRequestId()),
            site == null ? "Site #" + order.getSiteId() : site.getName(),
            itemSummary,
            order.getCreatedAt() == null ? "" : order.getCreatedAt().toLocalDate().format(DATE_FORMAT),
            order.getStatus() == null ? "N/A" : order.getStatus()
        );
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilter.getValue();

        filteredRows.setPredicate(row -> {
            boolean matchesKeyword = keyword.isBlank()
                || row.orderCode().toLowerCase(Locale.ROOT).contains(keyword)
                || row.requestCode().toLowerCase(Locale.ROOT).contains(keyword)
                || row.siteName().toLowerCase(Locale.ROOT).contains(keyword);
            String selectedStatusKey = toStatusKey(selectedStatus);
            boolean matchesStatus = selectedStatus == null
                || "all".equalsIgnoreCase(selectedStatusKey)
                || selectedStatusKey.equalsIgnoreCase(normalizeStatusKey(row.status()));
            return matchesKeyword && matchesStatus;
        });

        int size = filteredRows.size();
        paginationInfoLabel.setText(size == 0
            ? "Không có đơn hàng phù hợp"
            : "Hiển thị 1 - " + size + " của " + size + " đơn hàng");
    }

    private HBox buildStatusDot(String status) {
        HBox box = new HBox(7);
        box.setAlignment(Pos.CENTER_LEFT);

        String[] colors = resolveStatusDotColors(status);
        Circle dot = new Circle(5);
        dot.setFill(Color.web(colors[0]));

        Label label = new Label(statusText(status));
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + colors[1] + ";");

        box.getChildren().addAll(dot, label);
        return box;
    }

    private String[] resolveStatusDotColors(String status) {
        return switch (normalizeStatusKey(status)) {
            case "pending" -> new String[]{"#F59E0B", "#B45309"};
            case "processing" -> new String[]{"#3B82F6", "#1D4ED8"};
            case "shipping" -> new String[]{"#A855F7", "#7E22CE"};
            case "completed" -> new String[]{"#22C55E", "#15803D"};
            case "cancelled" -> new String[]{"#EF4444", "#B91C1C"};
            default -> new String[]{"#9CA3AF", "#6B7280"};
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

    private String toStatusKey(String selectedStatus) {
        if (selectedStatus == null) {
            return "all";
        }
        return switch (selectedStatus.trim()) {
            case "Mọi trạng thái" -> "all";
            case "Chờ xác nhận" -> "pending";
            case "Đang giao" -> "shipping";
            case "Đã hoàn thành" -> "completed";
            case "Đã hủy" -> "cancelled";
            default -> selectedStatus.trim().toLowerCase(Locale.ROOT);
        };
    }

    private String statusText(String status) {
        return switch (normalizeStatusKey(status)) {
            case "pending" -> "Chờ xác nhận";
            case "shipping" -> "Đang giao";
            case "completed" -> "Đã hoàn thành";
            case "cancelled" -> "Đã hủy";
            default -> status == null || status.isBlank() ? "N/A" : status;
        };
    }

    private record OrderRow(
        Order order,
        String orderCode,
        String requestCode,
        String siteName,
        String itemsSummary,
        String createdAt,
        String status
    ) { }
}
