package org.itss.prj_itss.view.ordering.order.management;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.management.OrderManagementController;
import org.itss.prj_itss.model.order.application.management.OrderRow;
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
    private Button prevPageButton;

    @FXML
    private Button nextPageButton;

    private final ObservableList<OrderRow> pagedRows = FXCollections.observableArrayList();
    private final int itemsPerPage = 10;
    private int currentPage = 0;

    @FXML
    private void initialize() {
        TableViewSupport.useConstrainedResize(orderTable);
        orderTable.getStyleClass().add("no-vertical-scrollbar");
        orderTable.setFixedCellSize(46);
        orderTable.prefHeightProperty().bind(
            Bindings.max(1, Bindings.size(pagedRows)).multiply(orderTable.getFixedCellSize()).add(38)
        );
        orderTable.minHeightProperty().bind(orderTable.prefHeightProperty());
        orderTable.maxHeightProperty().bind(orderTable.prefHeightProperty());

        orderCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().orderCode()));
        requestCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().requestCode()));
        siteColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteCode()));
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
                detailButton.getStyleClass().add("forest-secondary-button");
                detailButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
                detailButton.setOnAction(event -> {
                    if (navigator != null) {
                        navigator.showView("order-detail:" + row.order().getId());
                    }
                });

                HBox actions = new HBox(8);
                actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                actions.getChildren().add(detailButton);

                if (OrderingFormatters.STATUS_CANCELLED.equalsIgnoreCase(OrderingFormatters.normalizeStatusKey(row.status()))) {
                    Button processButton = new Button("Xử lý");
                    processButton.getStyleClass().add("forest-dark-button");
                    processButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
                    processButton.setOnAction(event -> {
                        if (navigator != null) {
                            navigator.showView("ordering-order-handle-cancellation:" + row.order().getId());
                        }
                    });
                    actions.getChildren().add(processButton);
                }

                setGraphic(actions);
                setText(null);
            }
        });

        orderTable.setItems(pagedRows);

        filteredRows.addListener((javafx.collections.ListChangeListener.Change<? extends OrderRow> c) -> {
            updatePage();
        });

        statusFilter.getItems().addAll(
            "Mọi trạng thái",
            "Chờ xác nhận",
            "Đang giao",
            "Đã hoàn thành",
            "Đã hủy",
            "Đã loại bỏ"
        );
        statusFilter.setValue("Mọi trạng thái");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    public void init(Navigator navigator, OrderManagementController controller) {
        this.navigator = navigator;
        this.controller = controller;
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

        currentPage = 0;
        updatePage();
    }

    private void updatePage() {
        int totalItems = filteredRows.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalItems);

        pagedRows.setAll(filteredRows.subList(start, end));

        if (prevPageButton != null) prevPageButton.setDisable(currentPage == 0);
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages - 1);

        if (paginationInfoLabel != null) {
            paginationInfoLabel.setText(totalItems == 0
                ? "Không có đơn hàng phù hợp"
                : "Hiển thị " + (start + 1) + " - " + end + " của " + totalItems + " đơn hàng");
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    @FXML
    private void handleNextPage() {
        int totalItems = filteredRows.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePage();
        }
    }
}
