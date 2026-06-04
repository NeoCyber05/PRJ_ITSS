package org.itss.prj_itss.view.sales.request.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.itss.prj_itss.controller.sales.request.view.ViewOrderRequestController;
import org.itss.prj_itss.model.request.application.sales.view.RequestDetailItemRow;
import org.itss.prj_itss.model.request.application.sales.view.RequestReadOnlyView;
import org.itss.prj_itss.view.shared.ViewLifecycle;
import org.itss.prj_itss.view.shared.ui.StatusBadgeFactory;

public final class ViewOrderRequestView implements ViewLifecycle {

    private ViewOrderRequestController controller;
    private Stage stage;
    private int currentRequestId = -1;

    private final ObservableList<RequestDetailItemRow> items = FXCollections.observableArrayList();

    @FXML
    private Label headerTitle;

    @FXML
    private Button closeButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label requestCodeLabel;

    @FXML
    private Label createdAtLabel;

    @FXML
    private HBox statusBadge;

    @FXML
    private TextArea noteArea;

    @FXML
    private TableView<RequestDetailItemRow> itemsTable;

    @FXML
    private TableColumn<RequestDetailItemRow, String> merchandiseCodeColumn;

    @FXML
    private TableColumn<RequestDetailItemRow, String> merchandiseNameColumn;

    @FXML
    private TableColumn<RequestDetailItemRow, RequestDetailItemRow> quantityColumn;

    @FXML
    private TableColumn<RequestDetailItemRow, String> unitColumn;

    @FXML
    private TableColumn<RequestDetailItemRow, RequestDetailItemRow> desiredDateColumn;

    public void init(Stage stage, int requestId, ViewOrderRequestController controller) {
        this.stage = stage;
        this.currentRequestId = requestId;
        this.controller = controller;
        setupUI();
        loadData();
    }

    private void setupUI() {
        closeButton.setOnAction(event -> goBack());
        cancelButton.setOnAction(event -> goBack());
        bindTableHeight();
        setupColumns();
        itemsTable.setItems(items);
    }

    private void bindTableHeight() {
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        itemsTable.setFixedCellSize(48);
        itemsTable.prefHeightProperty().bind(
            Bindings.size(items).multiply(itemsTable.getFixedCellSize()).add(35)
        );
        itemsTable.minHeightProperty().bind(itemsTable.prefHeightProperty());
        itemsTable.maxHeightProperty().bind(itemsTable.prefHeightProperty());
    }

    private void setupColumns() {
        merchandiseCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().code()));
        merchandiseCodeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Label label = new Label(item != null ? item : "");
                label.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                setGraphic(label);
            }
        });

        merchandiseNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        merchandiseNameColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Label label = new Label(item != null ? item : "");
                label.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                setGraphic(label);
            }
        });

        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().unit()));
        unitColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Label label = new Label(item != null ? item : "");
                label.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                setGraphic(label);
            }
        });

        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        quantityColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RequestDetailItemRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                Label label = new Label(row.quantity() != null ? row.quantity() : "");
                label.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                setGraphic(label);
            }
        });

        desiredDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        desiredDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RequestDetailItemRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                Label label = new Label(row.desiredDate() != null ? row.desiredDate() : "");
                label.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                setGraphic(label);
            }
        });
    }

    private void loadData() {
        if (currentRequestId <= 0 || controller == null) return;
        RequestReadOnlyView view = controller.loadRequest(currentRequestId);
        if (view == null) return;

        requestCodeLabel.setText(view.requestCode());
        createdAtLabel.setText(view.createdAt() != null && !view.createdAt().isBlank() ? view.createdAt() : "N/A");
        statusBadge.getChildren().setAll(StatusBadgeFactory.statusBadge(view.status(), StatusBadgeFactory.StatusKind.REQUEST));

        items.setAll(view.items());
        noteArea.setText(view.note() != null ? view.note() : "");
        noteArea.setEditable(false);
        noteArea.setStyle(
            "-fx-opacity: 1;" +
            "-fx-control-inner-background: transparent;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-text-fill: #333;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 0;"
        );
    }


    private void goBack() {
        if (stage != null) {
            stage.close();
        }
    }
}
