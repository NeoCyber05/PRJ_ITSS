package org.itss.prj_itss.view.warehouse;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.warehouse.application.IncomingOrderDetail;
import org.itss.prj_itss.model.warehouse.application.IncomingOrderItemRow;
import org.itss.prj_itss.model.warehouse.application.IncomingOrderRow;
import org.itss.prj_itss.model.warehouse.domain.InspectionResult;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase.ConfirmationResult;
import org.itss.prj_itss.controller.warehouse.ConfirmOrderArrivalController;
import org.itss.prj_itss.controller.warehouse.ConfirmOrderArrivalController.InspectionItemDto;
import org.itss.prj_itss.controller.shared.ValidationResult;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ConfirmOrderArrivalView implements ViewLifecycle {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObservableList<OrderRow> orderRows = FXCollections.observableArrayList();
    private final ObservableList<InspectionItemRow> itemRows = FXCollections.observableArrayList();

    private ConfirmOrderArrivalController controller;
    private Order selectedOrder;

    @FXML
    private VBox listPane;

    @FXML
    private BorderPane confirmPane;

    @FXML
    private TableView<OrderRow> orderTable;

    @FXML
    private TableColumn<OrderRow, String> orderCodeColumn;

    @FXML
    private TableColumn<OrderRow, String> requestCodeColumn;

    @FXML
    private TableColumn<OrderRow, String> siteCodeColumn;

    @FXML
    private TableColumn<OrderRow, String> siteNameColumn;

    @FXML
    private TableColumn<OrderRow, String> createdAtColumn;

    @FXML
    private TableColumn<OrderRow, String> statusColumn;

    @FXML
    private TableColumn<OrderRow, OrderRow> actionColumn;

    @FXML
    private Label orderListInfoLabel;

    @FXML
    private Label listMessageLabel;

    @FXML
    private Label orderCodeValueLabel;

    @FXML
    private Label requestCodeValueLabel;

    @FXML
    private Label siteCodeValueLabel;

    @FXML
    private Label siteNameValueLabel;

    @FXML
    private Label createdAtValueLabel;

    @FXML
    private Label statusValueLabel;

    @FXML
    private TableView<InspectionItemRow> itemTable;

    @FXML
    private TableColumn<InspectionItemRow, Number> itemIndexColumn;

    @FXML
    private TableColumn<InspectionItemRow, String> itemCodeColumn;

    @FXML
    private TableColumn<InspectionItemRow, String> itemNameColumn;

    @FXML
    private TableColumn<InspectionItemRow, Number> itemOrderedQuantityColumn;

    @FXML
    private TableColumn<InspectionItemRow, String> itemUnitColumn;

    @FXML
    private TableColumn<InspectionItemRow, String> itemDeliveryColumn;

    @FXML
    private TableColumn<InspectionItemRow, String> itemReceivedQuantityColumn;

    @FXML
    private TableColumn<InspectionItemRow, InspectionResult> itemInspectionResultColumn;

    @FXML
    private TextArea overallNoteArea;

    @FXML
    private Label messageLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private void initialize() {
        configureOrderTable();
        configureItemTable();
        showListPane();
    }

    public void setController(ConfirmOrderArrivalController controller) {
        this.controller = controller;
    }

    @Override
    public void onViewShown() {
        loadInboundOrders();
    }

    public void showOrderById(int orderId) {
        loadInboundOrders();
        for (OrderRow row : orderRows) {
            if (row.order().getId() == orderId) {
                showConfirmPane(row.order());
                return;
            }
        }
        showListPane();
        showListError("Không tìm thấy đơn hàng đang giao cần xác nhận.");
    }

    private void configureOrderTable() {
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        orderTable.getStyleClass().add("no-horizontal-scroll-table");
        orderTable.setItems(orderRows);
        actionColumn.setMinWidth(220);
        actionColumn.setPrefWidth(220);
        actionColumn.setMaxWidth(220);
        actionColumn.setResizable(false);

        orderCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().orderCode()));
        requestCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().requestCode()));
        siteCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteCode()));
        siteNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteName()));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().createdAt()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        actionColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionColumn.setSortable(false);
        actionColumn.setReorderable(false);
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(OrderRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                setGraphic(buildOrderActionButtons(row));
                setText(null);
            }
        });
    }

    private HBox buildOrderActionButtons(OrderRow row) {
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setMinWidth(180);
        actions.setPrefWidth(180);

        Button detailButton = new Button("Chi tiết");
        detailButton.setMinWidth(78);
        detailButton.setPrefWidth(78);
        detailButton.setMaxWidth(78);
        detailButton.setTextOverrun(OverrunStyle.CLIP);
        detailButton.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        detailButton.setOnAction(event -> showDetailDialog(row));
        actions.getChildren().add(detailButton);

        Button confirmActionButton = new Button("Xác nhận");
        confirmActionButton.setMinWidth(94);
        confirmActionButton.setPrefWidth(94);
        confirmActionButton.setMaxWidth(94);
        confirmActionButton.setTextOverrun(OverrunStyle.CLIP);
        confirmActionButton.getStyleClass().add("forest-dark-button");
        confirmActionButton.setOnAction(event -> showConfirmPane(row.order()));
        actions.getChildren().add(confirmActionButton);

        return actions;
    }

    private void configureItemTable() {
        itemTable.setEditable(false);
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        itemTable.getStyleClass().add("no-horizontal-scroll-table");
        itemTable.setItems(itemRows);
        itemTable.setFixedCellSize(64);

        itemIndexColumn.setCellValueFactory(data -> data.getValue().indexProperty());
        itemCodeColumn.setCellValueFactory(data -> data.getValue().codeProperty());
        itemNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        itemOrderedQuantityColumn.setCellValueFactory(data -> data.getValue().orderedQuantityProperty());
        itemUnitColumn.setCellValueFactory(data -> data.getValue().unitProperty());
        itemDeliveryColumn.setCellValueFactory(data -> data.getValue().deliveryMethodProperty());
        itemReceivedQuantityColumn.setCellValueFactory(data -> data.getValue().receivedQuantityInputProperty());
        itemInspectionResultColumn.setCellValueFactory(data -> data.getValue().inspectionResultProperty());

        itemNameColumn.setCellFactory(column -> new TableCell<>() {
            private final Label valueLabel = new Label();

            {
                valueLabel.setWrapText(true);
                valueLabel.setMaxWidth(Double.MAX_VALUE);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                valueLabel.setText(item);
                setGraphic(valueLabel);
            }
        });

        itemReceivedQuantityColumn.setCellFactory(column -> new TableCell<>() {
            private final TextField quantityField = new TextField();
            private boolean syncing;

            {
                quantityField.getStyleClass().add("inspection-input");
                quantityField.setPromptText("Nhập số lượng");
                quantityField.setMaxWidth(Double.MAX_VALUE);
                quantityField.setTextFormatter(new TextFormatter<>(change ->
                    change.getControlNewText().matches("\\d*") ? change : null));
                quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (syncing) {
                        return;
                    }
                    InspectionItemRow row = currentRow();
                    if (row == null) {
                        return;
                    }
                    row.receivedQuantityInputProperty().set(newValue);
                    clearConfirmMessage();
                });
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                InspectionItemRow row = currentRow();
                if (row == null) {
                    setGraphic(null);
                    return;
                }

                String value = row.receivedQuantityInputProperty().get();
                if (!quantityField.isFocused() && !value.equals(quantityField.getText())) {
                    syncing = true;
                    quantityField.setText(value);
                    syncing = false;
                }
                setGraphic(quantityField);
            }

            private InspectionItemRow currentRow() {
                int rowIndex = getIndex();
                if (rowIndex < 0 || rowIndex >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(rowIndex);
            }
        });

        itemInspectionResultColumn.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<InspectionResult> comboBox =
                new ComboBox<>(FXCollections.observableArrayList(InspectionResult.values()));

            {
                comboBox.getStyleClass().add("inspection-combo");
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.setMinWidth(120);
                comboBox.setPrefWidth(130);
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    InspectionItemRow row = currentRow();
                    if (row == null || newValue == null) {
                        return;
                    }
                    row.inspectionResultProperty().set(newValue);
                    clearConfirmMessage();
                });
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(InspectionResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                InspectionItemRow row = currentRow();
                if (row == null) {
                    setGraphic(null);
                    return;
                }

                if (comboBox.getValue() != row.inspectionResultProperty().get()) {
                    comboBox.setValue(row.inspectionResultProperty().get());
                }
                setGraphic(comboBox);
            }

            private InspectionItemRow currentRow() {
                int rowIndex = getIndex();
                if (rowIndex < 0 || rowIndex >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(rowIndex);
            }
        });
    }

    private void loadInboundOrders() {
        if (controller == null) {
            return;
        }
        List<Order> inboundOrders = controller.findInboundOrders();
        java.util.Map<Integer, Site> siteMap = controller.findAllSites().stream()
            .collect(java.util.stream.Collectors.toMap(Site::getId, s -> s, (a, b) -> a));
        orderRows.setAll(inboundOrders.stream().map(o -> toOrderRow(o, siteMap)).toList());

        orderListInfoLabel.setText(orderRows.isEmpty()
            ? "Không có đơn hàng nào đang giao tới."
            : "Hiển thị " + orderRows.size() + " đơn hàng có trạng thái Đang giao.");
        orderTable.refresh();
    }

    private void showListPane() {
        selectedOrder = null;
        listPane.setVisible(true);
        listPane.setManaged(true);
        confirmPane.setVisible(false);
        confirmPane.setManaged(false);
        clearConfirmForm();
        clearConfirmMessage();
    }

    private void showConfirmPane(Order order) {
        selectedOrder = order;
        clearListMessage();
        listPane.setVisible(false);
        listPane.setManaged(false);
        confirmPane.setVisible(true);
        confirmPane.setManaged(true);

        renderSelectedOrder(order);
        loadOrderItems(order);
        overallNoteArea.clear();
        confirmButton.setDisable(false);
        clearConfirmMessage();
    }

    private void renderSelectedOrder(Order order) {
        if (controller == null) {
            return;
        }
        Site site = controller.findSiteById(order.getSiteId());
        orderCodeValueLabel.setText(formatOrderCode(order.getId()));
        requestCodeValueLabel.setText(String.format("YC-2026-%03d", order.getRequestId()));
        siteCodeValueLabel.setText(site == null ? "N/A" : safeText(site.getSiteCode()));
        siteNameValueLabel.setText(site == null ? "N/A" : safeText(site.getName()));
        createdAtValueLabel.setText(order.getCreatedAt() == null ? "N/A" : order.getCreatedAt().toLocalDate().format(DATE_FORMAT));
        statusValueLabel.setText(OrderingFormatters.orderStatusText(order.getStatus()));
    }

    private void loadOrderItems(Order order) {
        if (controller == null) {
            return;
        }
        List<OrderMerchandise> items = controller.findItemsByOrderId(order.getId());
        itemRows.setAll(buildInspectionRows(items));
        itemTable.refresh();
    }

    @FXML
    private void handleCancel() {
        clearListMessage();
        loadInboundOrders();
        showListPane();
    }

    @FXML
    private void handleConfirmArrival() {
        if (selectedOrder == null) {
            showConfirmError("Vui lòng chọn đơn hàng cần xác nhận.");
            return;
        }
        if (controller == null) {
            return;
        }

        List<InspectionItemDto> dtos = buildInspectionDtos();
        ValidationResult validation = controller.validateInspection(dtos, overallNoteArea.getText());
        if (!validation.isValid()) {
            showConfirmError(validation.errors().get(0));
            return;
        }

        ConfirmationResult result = controller.confirmArrival(
            selectedOrder.getId(),
            dtos,
            overallNoteArea.getText()
        );
        if (!result.success()) {
            showConfirmError(result.message());
            return;
        }

        String message = "Xác nhận đơn hàng thành công\nTrạng thái: Hoàn thành";
        if (!result.discrepancyNote().isBlank()) {
            message += "\nGhi chú chênh lệch: " + result.discrepancyNote();
        }

        loadInboundOrders();
        showListPane();
        showListSuccess(message);
    }

    private List<InspectionItemDto> buildInspectionDtos() {
        List<InspectionItemDto> dtos = new ArrayList<>();
        for (InspectionItemRow row : itemRows) {
            dtos.add(new InspectionItemDto(
                row.merchandiseId(),
                row.orderedQuantityProperty().get(),
                row.receivedQuantityInputProperty().get(),
                row.inspectionResultProperty().get()
            ));
        }
        return dtos;
    }

    private List<InspectionItemRow> buildInspectionRows(List<OrderMerchandise> items) {
        List<InspectionItemRow> rows = new ArrayList<>();
        int index = 1;
        for (OrderMerchandise item : items) {
            Merchandise merchandise = controller != null ? controller.findMerchandiseById(item.getMerchandiseId()) : null;
            int orderedQuantity = item.getQuantity() == null ? 0 : item.getQuantity().intValue();
            rows.add(new InspectionItemRow(
                index++,
                item.getMerchandiseId(),
                merchandise == null ? "N/A" : safeText(merchandise.getCode()),
                merchandise == null ? "N/A" : safeText(merchandise.getName()),
                orderedQuantity,
                merchandise == null ? "N/A" : safeText(merchandise.getUnit()),
                OrderingFormatters.deliveryMethodText(item.getDeliveryMethod()),
                String.valueOf(orderedQuantity),
                InspectionResult.ENOUGH
            ));
        }
        return rows;
    }

    private void showDetailDialog(OrderRow row) {
        if (controller == null) {
            return;
        }
        List<OrderMerchandise> items = controller.findItemsByOrderId(row.order().getId());
        IncomingOrderDetail detail = new IncomingOrderDetail(
            toIncomingOrderRow(row, items.size()),
            buildIncomingOrderItemRows(items)
        );
        IncomingOrderDetailDialog.show(orderTable.getScene().getWindow(), detail);
    }

    private IncomingOrderRow toIncomingOrderRow(OrderRow row, int itemCount) {
        Order order = row.order();
        return new IncomingOrderRow(
            order.getId(),
            order.getRequestId(),
            order.getSiteId(),
            row.orderCode(),
            row.requestCode(),
            row.siteCode(),
            row.siteName(),
            row.createdAt(),
            order.getStatus(),
            row.status(),
            itemCount
        );
    }

    private List<IncomingOrderItemRow> buildIncomingOrderItemRows(List<OrderMerchandise> items) {
        if (controller == null) {
            return List.of();
        }
        return items.stream()
            .map(item -> {
                Merchandise merchandise = controller.findMerchandiseById(item.getMerchandiseId());
                return new IncomingOrderItemRow(
                    item.getMerchandiseId(),
                    merchandise == null ? "N/A" : safeText(merchandise.getCode()),
                    merchandise == null ? "N/A" : safeText(merchandise.getName()),
                    merchandise == null ? "N/A" : safeText(merchandise.getUnit()),
                    OrderingFormatters.formatQuantity(item.getQuantity()),
                    OrderingFormatters.deliveryMethodText(item.getDeliveryMethod())
                );
            })
            .toList();
    }

    private OrderRow toOrderRow(Order order, java.util.Map<Integer, Site> siteMap) {
        Site site = siteMap.get(order.getSiteId());
        return new OrderRow(
            order,
            formatOrderCode(order.getId()),
            String.format("YC-2026-%03d", order.getRequestId()),
            site == null ? "N/A" : safeText(site.getSiteCode()),
            site == null ? "Site #" + order.getSiteId() : safeText(site.getName()),
            order.getCreatedAt() == null ? "" : order.getCreatedAt().toLocalDate().format(DATE_FORMAT),
            OrderingFormatters.orderStatusText(order.getStatus())
        );
    }

    private void clearConfirmForm() {
        itemRows.clear();
        orderCodeValueLabel.setText("Chưa chọn");
        requestCodeValueLabel.setText("Chưa chọn");
        siteCodeValueLabel.setText("Chưa chọn");
        siteNameValueLabel.setText("Chưa chọn");
        createdAtValueLabel.setText("Chưa chọn");
        statusValueLabel.setText("Chưa chọn");
        overallNoteArea.clear();
        confirmButton.setDisable(true);
    }



    private String formatOrderCode(int orderId) {
        return String.format("ĐH-2026-%03d", orderId);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }

    private void showListSuccess(String message) {
        listMessageLabel.setText(message);
        listMessageLabel.setManaged(true);
        listMessageLabel.setVisible(true);
        listMessageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #15803D;");
    }

    private void showListError(String message) {
        listMessageLabel.setText(message);
        listMessageLabel.setManaged(true);
        listMessageLabel.setVisible(true);
        listMessageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #B91C1C;");
    }

    private void clearListMessage() {
        listMessageLabel.setText("");
        listMessageLabel.setManaged(false);
        listMessageLabel.setVisible(false);
        listMessageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
    }

    private void showConfirmError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #B91C1C;");
    }

    private void clearConfirmMessage() {
        messageLabel.setText("");
        messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
    }

    private record OrderRow(
        Order order,
        String orderCode,
        String requestCode,
        String siteCode,
        String siteName,
        String createdAt,
        String status
    ) {
    }

    private static final class InspectionItemRow {
        private final IntegerProperty index;
        private final int merchandiseId;
        private final StringProperty code;
        private final StringProperty name;
        private final IntegerProperty orderedQuantity;
        private final StringProperty unit;
        private final StringProperty deliveryMethod;
        private final StringProperty receivedQuantityInput;
        private final ObjectProperty<InspectionResult> inspectionResult;

        private InspectionItemRow(
            int index,
            int merchandiseId,
            String code,
            String name,
            int orderedQuantity,
            String unit,
            String deliveryMethod,
            String receivedQuantityInput,
            InspectionResult inspectionResult
        ) {
            this.index = new SimpleIntegerProperty(index);
            this.merchandiseId = merchandiseId;
            this.code = new SimpleStringProperty(code);
            this.name = new SimpleStringProperty(name);
            this.orderedQuantity = new SimpleIntegerProperty(orderedQuantity);
            this.unit = new SimpleStringProperty(unit);
            this.deliveryMethod = new SimpleStringProperty(deliveryMethod);
            this.receivedQuantityInput = new SimpleStringProperty(receivedQuantityInput);
            this.inspectionResult = new SimpleObjectProperty<>(inspectionResult);
        }

        public IntegerProperty indexProperty() {
            return index;
        }

        public int merchandiseId() {
            return merchandiseId;
        }

        public StringProperty codeProperty() {
            return code;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public IntegerProperty orderedQuantityProperty() {
            return orderedQuantity;
        }

        public StringProperty unitProperty() {
            return unit;
        }

        public StringProperty deliveryMethodProperty() {
            return deliveryMethod;
        }

        public StringProperty receivedQuantityInputProperty() {
            return receivedQuantityInput;
        }

        public ObjectProperty<InspectionResult> inspectionResultProperty() {
            return inspectionResult;
        }
    }
}
