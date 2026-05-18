package org.itss.prj_itss.request.presentation.sales.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.request.application.sales.RequestDetailItemRow;
import org.itss.prj_itss.request.application.sales.RequestReadOnlyView;
import org.itss.prj_itss.request.application.sales.RequestSalesApplicationService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class ViewOrderRequestController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private RequestSalesApplicationService salesService;
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

    void init(Stage stage, int requestId, ApplicationContext context) {
        this.stage = stage;
        this.currentRequestId = requestId;
        this.salesService = context.requestSalesApplicationService();
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
        if (currentRequestId <= 0) return;
        RequestReadOnlyView view = salesService.findReadOnlyView(currentRequestId);
        if (view == null) return;

        requestCodeLabel.setText(view.requestCode());
        createdAtLabel.setText(view.createdAt() != null && !view.createdAt().isBlank() ? view.createdAt() : "N/A");
        buildStatusBadge(view.status());

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

    private void buildStatusBadge(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        String[] colors = resolveStatusColors(normalized);
        String display = resolveStatusDisplay(normalized, status);

        Label badge = new Label("● " + display);
        badge.setStyle(
            "-fx-background-color: " + colors[0] + ";" +
            "-fx-text-fill: " + colors[1] + ";" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 4 12;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );
        statusBadge.getChildren().setAll(badge);
    }

    private static String[] resolveStatusColors(String normalized) {
        return switch (normalized) {
            case "pending"    -> new String[]{"#FFF4E5", "#D97706"};
            case "processing" -> new String[]{"#E8F1FF", "#2563EB"};
            case "shipping"   -> new String[]{"#F2EAFF", "#7C3ED"};
            case "completed"  -> new String[]{"#EAF8EF", "#15803D"};
            case "cancelled"  -> new String[]{"#FEE2E2", "#B91C1C"};
            default           -> new String[]{"#F3F4F6", "#6B7280"};
        };
    }

    private static String resolveStatusDisplay(String normalized, String fallback) {
        return switch (normalized) {
            case "pending"    -> "Chờ xử lý";
            case "processing" -> "Đang xử lý";
            case "shipping"   -> "Đang giao";
            case "completed"  -> "Đã hoàn thành";
            case "cancelled"  -> "Đã hủy";
            default           -> fallback != null ? fallback : "N/A";
        };
    }

    private void goBack() {
        if (stage != null) {
            stage.close();
        }
    }
}
