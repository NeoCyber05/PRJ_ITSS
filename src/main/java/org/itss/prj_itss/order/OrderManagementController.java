package org.itss.prj_itss.order;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import org.itss.prj_itss.common.StatusBadgeFactory;
import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.dao.IMerchandiseDAO;
import org.itss.prj_itss.dao.IOrderDAO;
import org.itss.prj_itss.dao.ISiteDAO;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.layout.Navigator;
import org.itss.prj_itss.layout.ViewController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OrderManagementController implements ViewController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObservableList<OrderRow> rows = FXCollections.observableArrayList();
    private final FilteredList<OrderRow> filteredRows = new FilteredList<>(rows, row -> true);

    private Navigator navigator;
    private IOrderDAO orderDAO;
    private ISiteDAO siteDAO;
    private IMerchandiseDAO merchandiseDAO;

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
                    HBox badge = StatusBadgeFactory.buildStatusDot(status);
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
    public void init(Navigator navigator, DAOFactory daoFactory) {
        this.navigator = navigator;
        this.orderDAO = daoFactory.getOrderDAO();
        this.siteDAO = daoFactory.getSiteDAO();
        this.merchandiseDAO = daoFactory.getMerchandiseDAO();
        reload();
    }

    private void reload() {
        rows.setAll(orderDAO.findAll().stream().map(this::toRow).toList());
        applyFilters();
    }

    private OrderRow toRow(Order order) {
        Site site = siteDAO.findById(order.getSiteId());
        List<OrderMerchandise> items = orderDAO.findItemsByOrderId(order.getId());
        String itemSummary = items.stream()
            .map(item -> {
                Merchandise merchandise = merchandiseDAO.findById(item.getMerchandiseId());
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
            boolean matchesStatus = selectedStatus == null
                || "Mọi trạng thái".equals(selectedStatus)
                || selectedStatus.equals(row.status());
            return matchesKeyword && matchesStatus;
        });

        int size = filteredRows.size();
        paginationInfoLabel.setText(size == 0
            ? "Không có đơn hàng phù hợp"
            : "Hiển thị 1 - " + size + " của " + size + " đơn hàng");
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
