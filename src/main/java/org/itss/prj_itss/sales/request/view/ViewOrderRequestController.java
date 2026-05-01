package org.itss.prj_itss.sales.request.view;

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
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.entity.RequestMerchandise;
import org.itss.prj_itss.service.MerchandiseService;
import org.itss.prj_itss.service.RequestService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class ViewOrderRequestController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Services & state ─────────────────────────────────────────────────────

    private RequestService requestService;
    private MerchandiseService merchandiseService;
    private Stage stage;
    private int currentRequestId = -1;
    private Request currentRequest;

    private final ObservableList<ViewItemRow> items = FXCollections.observableArrayList();

    // ── FXML fields ───────────────────────────────────────────────────────────

    @FXML
    private Label headerTitle;

    @FXML
    private Button closeButton;

    @FXML
    private Label requestCodeLabel;

    @FXML
    private Label createdAtLabel;

    @FXML
    private HBox statusBadge;

    @FXML
    private TextArea noteArea;

    @FXML
    private TableView<ViewItemRow> itemsTable;

    @FXML
    private TableColumn<ViewItemRow, Merchandise> merchandiseCodeColumn;

    @FXML
    private TableColumn<ViewItemRow, Merchandise> merchandiseNameColumn;

    @FXML
    private TableColumn<ViewItemRow, ViewItemRow> quantityColumn;

    @FXML
    private TableColumn<ViewItemRow, String> unitColumn;

    @FXML
    private TableColumn<ViewItemRow, ViewItemRow> desiredDateColumn;

    @FXML
    private Button cancelButton;

    // ── Initialisation ────────────────────────────────────────────────────────

    void init(Stage stage, int requestId, ApplicationContext context) {
        this.stage = stage;
        this.currentRequestId = requestId;
        this.requestService = context.requestService();
        this.merchandiseService = context.merchandiseService();
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

    // ── Table setup ───────────────────────────────────────────────────────────

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
        setupMerchandiseCodeColumn();
        setupMerchandiseNameColumn();
        setupUnitColumn();
        setupQuantityColumn();
        setupDesiredDateColumn();
    }

    private void setupMerchandiseCodeColumn() {
        merchandiseCodeColumn.setCellValueFactory(data -> data.getValue().merchandise);
        merchandiseCodeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Merchandise item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                ViewItemRow row = getTableRow().getItem();
                Label label = new Label(row.merchandise.get() != null ? row.merchandise.get().getCode() : "");
                label.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                setGraphic(label);
            }
        });
    }

    private void setupMerchandiseNameColumn() {
        merchandiseNameColumn.setCellValueFactory(data -> data.getValue().merchandise);
        merchandiseNameColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Merchandise item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                ViewItemRow row = getTableRow().getItem();
                Label label = new Label(row.merchandise.get() != null ? row.merchandise.get().getName() : "");
                label.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                setGraphic(label);
            }
        });
    }

    private void setupUnitColumn() {
        unitColumn.setCellValueFactory(data -> {
            SimpleStringProperty prop = new SimpleStringProperty();
            Merchandise m = data.getValue().merchandise.get();
            prop.set(m != null ? m.getUnit() : "");
            data.getValue().merchandise.addListener((obs, oldValue, newValue) ->
                prop.set(newValue != null ? newValue.getUnit() : "")
            );
            return prop;
        });
    }

    private void setupQuantityColumn() {
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        quantityColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ViewItemRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                Label label = new Label(row.quantity != null ? row.quantity.toString() : "");
                label.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                setGraphic(label);
            }
        });
    }

    private void setupDesiredDateColumn() {
        desiredDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        desiredDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ViewItemRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                String text = row.desiredDate != null ? row.desiredDate.format(DATE_FORMAT) : "";
                Label label = new Label(text);
                label.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                setGraphic(label);
            }
        });
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadData() {
        if (currentRequestId <= 0) return;
        currentRequest = requestService.findById(currentRequestId);
        if (currentRequest == null) return;

        requestCodeLabel.setText(String.format("YC-2026-%03d", currentRequest.getId()));
        createdAtLabel.setText(
            currentRequest.getCreatedAt() != null
                ? currentRequest.getCreatedAt().toLocalDate().format(DATE_FORMAT)
                : "N/A"
        );
        buildStatusBadge(currentRequest.getStatus());

        List<RequestMerchandise> requestItems = requestService.findItemsByRequestId(currentRequestId);
        items.clear();
        for (RequestMerchandise rm : requestItems) {
            Merchandise merchandise = merchandiseService.findById(rm.getMerchandiseId());
            if (merchandise != null) {
                items.add(new ViewItemRow(merchandise, rm.getQuantityOrdered(), rm.getDesiredDeliveryDate()));
            }
        }

        noteArea.setText(currentRequest.getNote() != null ? currentRequest.getNote() : "");
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

    // ── Status badge ──────────────────────────────────────────────────────────

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
            case "shipping"   -> new String[]{"#F2EAFF", "#7C3AED"};
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

    // ── Navigation ────────────────────────────────────────────────────────────

    private void goBack() {
        if (stage != null) {
            stage.close();
        }
    }
}
