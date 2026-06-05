package org.itss.prj_itss.view.warehouse;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.warehouse.WarehouseIncomingOrderController;
import org.itss.prj_itss.model.warehouse.application.IncomingOrderDetail;
import org.itss.prj_itss.model.warehouse.application.IncomingOrderRow;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.List;
import java.util.Locale;

public final class WarehouseIncomingOrdersView implements ViewLifecycle {

    private Navigator navigator;
    private WarehouseIncomingOrderController controller;
    private List<IncomingOrderRow> allRows = List.of();
    private final ObservableList<IncomingOrderRow> displayedRows = FXCollections.observableArrayList();

    @FXML private Label listInfoLabel;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private TableView<IncomingOrderRow> orderTable;
    @FXML private TableColumn<IncomingOrderRow, String> orderCodeColumn;
    @FXML private TableColumn<IncomingOrderRow, String> requestCodeColumn;
    @FXML private TableColumn<IncomingOrderRow, String> siteCodeColumn;
    @FXML private TableColumn<IncomingOrderRow, String> siteNameColumn;
    @FXML private TableColumn<IncomingOrderRow, String> createdAtColumn;
    @FXML private TableColumn<IncomingOrderRow, String> statusColumn;
    @FXML private TableColumn<IncomingOrderRow, IncomingOrderRow> actionColumn;

    @FXML
    private void initialize() {
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        actionColumn.setMinWidth(220);
        actionColumn.setPrefWidth(220);
        actionColumn.setMaxWidth(220);
        actionColumn.setResizable(false);

        orderCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().orderCode()));
        requestCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().requestCode()));
        siteCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteCode()));
        siteNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteName()));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().createdAt()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().statusText()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                HBox box = new HBox(7);
                box.setAlignment(Pos.CENTER_LEFT);
                Circle dot = new Circle(5);
                dot.setFill(Color.web("#A855F7"));
                Label label = new Label(status);
                label.setStyle("-fx-font-size: 13px; -fx-text-fill: #7E22CE;");
                box.getChildren().addAll(dot, label);
                setGraphic(box);
                setText(null);
            }
        });

        actionColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionColumn.setSortable(false);
        actionColumn.setReorderable(false);
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(IncomingOrderRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(buildActionButtons(row));
                setText(null);
            }
        });

        orderTable.setItems(displayedRows);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    public void init(Navigator navigator, WarehouseIncomingOrderController controller) {
        this.navigator = navigator;
        this.controller = controller;
    }

    @Override
    public void onViewShown() {
        reload();
    }

    private void reload() {
        if (controller == null) return;
        allRows = controller.loadIncomingOrders();
        listInfoLabel.setText(allRows.isEmpty()
            ? "Không có đơn hàng nào đang giao tới."
            : "Hiển thị " + allRows.size() + " đơn hàng có trạng thái Đang giao.");
        applyFilter();
        clearMessage();
    }

    private void applyFilter() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.isBlank()) {
            displayedRows.setAll(allRows);
        } else {
            String normalized = keyword.trim().toLowerCase(Locale.ROOT);
            displayedRows.setAll(allRows.stream()
                .filter(row ->
                    row.orderCode().toLowerCase(Locale.ROOT).contains(normalized) ||
                    row.requestCode().toLowerCase(Locale.ROOT).contains(normalized) ||
                    row.siteCode().toLowerCase(Locale.ROOT).contains(normalized) ||
                    row.siteName().toLowerCase(Locale.ROOT).contains(normalized))
                .toList());
        }
    }

    private HBox buildActionButtons(IncomingOrderRow row) {
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setMinWidth(180);
        actions.setPrefWidth(180);

        Button detailBtn = new Button("Chi tiết");
        detailBtn.setMinWidth(78);
        detailBtn.setPrefWidth(78);
        detailBtn.setMaxWidth(78);
        detailBtn.setTextOverrun(OverrunStyle.CLIP);
        detailBtn.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        detailBtn.setOnAction(e -> showDetailDialog(row));
        actions.getChildren().add(detailBtn);

        Button confirmBtn = new Button("Xác nhận");
        confirmBtn.setMinWidth(94);
        confirmBtn.setPrefWidth(94);
        confirmBtn.setMaxWidth(94);
        confirmBtn.setTextOverrun(OverrunStyle.CLIP);
        confirmBtn.setStyle("-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> navigator.showView("warehouse-order-confirm-arrival:" + row.orderId()));
        actions.getChildren().add(confirmBtn);

        return actions;
    }

    private void showDetailDialog(IncomingOrderRow row) {
        if (controller == null) return;
        IncomingOrderDetail detail = controller.findIncomingDetail(row.orderId());
        if (detail == null) {
            showError("Không thể tải chi tiết đơn hàng.");
            return;
        }
        IncomingOrderDetailDialog.show(orderTable.getScene().getWindow(), detail);
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #B91C1C;");
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
    }
}
