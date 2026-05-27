package org.itss.prj_itss.view.ordering.order;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderManagementController;
import org.itss.prj_itss.model.order.application.OrderRow;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.view.shared.ui.StatusBadgeFactory;
import org.itss.prj_itss.view.shared.ui.TableViewSupport;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.Locale;

public final class OrderManagementView implements ViewLifecycle {

    private final ObservableList<OrderRow> rows = FXCollections.observableArrayList();
    private final FilteredList<OrderRow> filteredRows = new FilteredList<>(rows, row -> true);

    private Navigator navigator;
    private OrderManagementController controller;

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
        TableViewSupport.useConstrainedResize(orderTable);
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
                    HBox badge = StatusBadgeFactory.statusDot(status, false);
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
                detailButton.setOnAction(event -> {
                    if (navigator != null) {
                        navigator.showView("order-detail:" + row.order().getId());
                    }
                });

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

    public void init(Navigator navigator, OrderManagementController controller) {
        this.navigator = navigator;
        this.controller = controller;
        reload();
    }

    @Override
    public void onViewShown() {
        reload();
    }

    private void reload() {
        if (controller == null) return;
        rows.setAll(controller.findRows());
        applyFilters();
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilter.getValue();

        filteredRows.setPredicate(row -> {
            boolean matchesKeyword = keyword.isBlank()
                || row.orderCode().toLowerCase(Locale.ROOT).contains(keyword)
                || row.requestCode().toLowerCase(Locale.ROOT).contains(keyword)
                || row.siteName().toLowerCase(Locale.ROOT).contains(keyword);
            String selectedStatusKey = OrderingFormatters.toOrderStatusKey(selectedStatus);
            boolean matchesStatus = selectedStatus == null
                || "all".equalsIgnoreCase(selectedStatusKey)
                || selectedStatusKey.equalsIgnoreCase(OrderingFormatters.normalizeStatusKey(row.status()));
            return matchesKeyword && matchesStatus;
        });

        int size = filteredRows.size();
        paginationInfoLabel.setText(size == 0
            ? "Không có đơn hàng phù hợp"
            : "Hiển thị 1 - " + size + " của " + size + " đơn hàng");
    }
}
