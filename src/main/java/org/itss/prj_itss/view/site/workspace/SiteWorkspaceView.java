package org.itss.prj_itss.view.site.workspace;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.site.SiteWorkspaceController;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.site.application.self.SiteInventoryDraft;
import org.itss.prj_itss.model.site.application.self.SiteInventoryRow;
import org.itss.prj_itss.model.site.application.self.SiteOrderItemRow;
import org.itss.prj_itss.model.site.application.self.SiteOrderRow;
import org.itss.prj_itss.model.site.application.self.SiteProfileDraft;
import org.itss.prj_itss.model.site.application.self.SiteWorkspaceResult;
import org.itss.prj_itss.model.site.application.self.SiteWorkspaceSnapshot;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.view.shared.ViewLifecycle;
import org.itss.prj_itss.view.shared.ui.TableViewSupport;

public class SiteWorkspaceView implements ViewLifecycle {

    private final ObservableList<SiteInventoryRow> inventoryRows = FXCollections.observableArrayList();
    private final ObservableList<SiteOrderRow> orderRows = FXCollections.observableArrayList();
    private final ObservableList<SiteOrderItemRow> orderItemRows = FXCollections.observableArrayList();
    private final ObservableList<Merchandise> merchandiseOptions = FXCollections.observableArrayList();

    private Navigator navigator;
    private SiteWorkspaceController controller;
    private SiteWorkspaceSnapshot currentSnapshot;

    @FXML
    private TabPane workspaceTabs;

    @FXML
    private Label siteSubtitleLabel;

    @FXML
    private Label profileMessageLabel;

    @FXML
    private TextField siteCodeField;

    @FXML
    private TextField siteNameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField shipDaysField;

    @FXML
    private TextField airDaysField;

    @FXML
    private Button saveProfileButton;

    @FXML
    private ComboBox<Merchandise> merchandiseComboBox;

    @FXML
    private TextField stockQuantityField;

    @FXML
    private Button saveInventoryButton;

    @FXML
    private Button removeInventoryButton;

    @FXML
    private TableView<SiteInventoryRow> inventoryTable;

    @FXML
    private TableColumn<SiteInventoryRow, String> inventoryCodeColumn;

    @FXML
    private TableColumn<SiteInventoryRow, String> inventoryNameColumn;

    @FXML
    private TableColumn<SiteInventoryRow, String> inventoryUnitColumn;

    @FXML
    private TableColumn<SiteInventoryRow, String> inventoryStockColumn;

    @FXML
    private TableView<SiteOrderRow> orderTable;

    @FXML
    private TableColumn<SiteOrderRow, String> orderCodeColumn;

    @FXML
    private TableColumn<SiteOrderRow, String> requestCodeColumn;

    @FXML
    private TableColumn<SiteOrderRow, String> createdAtColumn;

    @FXML
    private TableColumn<SiteOrderRow, SiteOrderRow> orderStatusColumn;

    @FXML
    private TableColumn<SiteOrderRow, SiteOrderRow> orderActionColumn;

    @FXML
    private TextField inventorySearchField;

    @FXML
    private void initialize() {
        TableViewSupport.useConstrainedResize(inventoryTable);
        TableViewSupport.bindStringColumn(inventoryCodeColumn, SiteInventoryRow::merchandiseCode);
        TableViewSupport.bindStringColumn(inventoryNameColumn, SiteInventoryRow::merchandiseName);
        TableViewSupport.bindStringColumn(inventoryUnitColumn, SiteInventoryRow::unit);
        TableViewSupport.bindStringColumn(inventoryStockColumn, row -> String.valueOf(row.stockQuantity()));

        FilteredList<SiteInventoryRow> filteredInventory = new FilteredList<>(inventoryRows, p -> true);
        inventorySearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredInventory.setPredicate(row -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerFilter = newValue.toLowerCase().trim();
                if (row.merchandiseCode() != null && row.merchandiseCode().toLowerCase().contains(lowerFilter)) {
                    return true;
                }
                if (row.merchandiseName() != null && row.merchandiseName().toLowerCase().contains(lowerFilter)) {
                    return true;
                }
                return false;
            });
        });
        SortedList<SiteInventoryRow> sortedInventory = new SortedList<>(filteredInventory);
        sortedInventory.comparatorProperty().bind(inventoryTable.comparatorProperty());
        inventoryTable.setItems(sortedInventory);

        TableViewSupport.useConstrainedResize(orderTable);
        TableViewSupport.bindStringColumn(orderCodeColumn, SiteOrderRow::orderCode);
        TableViewSupport.bindStringColumn(requestCodeColumn, SiteOrderRow::requestCode);
        TableViewSupport.bindStringColumn(createdAtColumn, SiteOrderRow::createdAt);

        TableViewSupport.bindRowColumn(orderStatusColumn);

        // Status column: colored badge
        orderStatusColumn.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.getStyleClass().add("badge");
            }
            @Override
            protected void updateItem(SiteOrderRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                String statusKey = OrderingFormatters.normalizeStatusKey(row.status());
                badge.setText(row.statusText());
                badge.getStyleClass().removeIf(c -> c.startsWith("badge-"));
                badge.getStyleClass().add(statusBadgeClass(statusKey));
                setGraphic(badge);
                setText(null);
            }
        });

        TableViewSupport.bindRowColumn(orderActionColumn);
        orderActionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SiteOrderRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                Button detailButton = new Button("Chi tiết");
                detailButton.getStyleClass().add("order-btn-detail");
                detailButton.setMinWidth(Region.USE_PREF_SIZE);
                detailButton.setOnAction(event -> showOrderDetailPopup(row));

                Button confirmButton = new Button("✓ Xác nhận");
                confirmButton.getStyleClass().add("order-btn-confirm");
                confirmButton.setDisable(!row.confirmable());
                confirmButton.setMinWidth(Region.USE_PREF_SIZE);
                confirmButton.setOnAction(event -> confirmSupply(row.orderId()));

                Button rejectButton = new Button("✕ Từ chối");
                rejectButton.getStyleClass().add("order-btn-reject");
                rejectButton.setDisable(!row.confirmable());
                rejectButton.setMinWidth(Region.USE_PREF_SIZE);
                rejectButton.setOnAction(event -> rejectOrder(row.orderId()));

                HBox box = new HBox(6, detailButton, confirmButton, rejectButton);
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
            }
        });
        orderTable.setItems(orderRows);

        saveProfileButton.setOnAction(event -> saveProfile());
        saveInventoryButton.setOnAction(event -> saveInventory());
        removeInventoryButton.setOnAction(event -> removeInventory());
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, row) -> populateInventorySelection(row));
    }

    public void init(Navigator navigator, SiteWorkspaceController controller) {
        this.navigator = navigator;
        this.controller = controller;
        reload();
    }

    @Override
    public void onViewShown() {
        reload();
    }

    private void reload() {
        if (controller == null) {
            return;
        }
        SiteWorkspaceSnapshot snapshot = controller.load();
        currentSnapshot = snapshot;
        if (!snapshot.available()) {
            setWorkspaceDisabled(snapshot.message());
            return;
        }
        setWorkspaceEnabled(snapshot);
    }

    private void setWorkspaceDisabled(String message) {
        workspaceTabs.setDisable(true);
        siteSubtitleLabel.setText(message);
        profileMessageLabel.setText(message);
        profileMessageLabel.setVisible(true);
        profileMessageLabel.setManaged(true);
        inventoryRows.clear();
        orderRows.clear();
        orderItemRows.clear();
        merchandiseOptions.clear();
    }

    private void setWorkspaceEnabled(SiteWorkspaceSnapshot snapshot) {
        workspaceTabs.setDisable(false);
        profileMessageLabel.setText("");
        profileMessageLabel.setVisible(false);
        profileMessageLabel.setManaged(false);
        Site site = snapshot.site();
        siteSubtitleLabel.setText(site.getSiteCode() + " - " + site.getName());
        siteCodeField.setText(site.getSiteCode());
        siteNameField.setText(site.getName());
        descriptionArea.setText(site.getDescription() == null ? "" : site.getDescription());
        shipDaysField.setText(site.getShipDeliveryDays() == null ? "" : String.valueOf(site.getShipDeliveryDays()));
        airDaysField.setText(site.getAirDeliveryDays() == null ? "" : String.valueOf(site.getAirDeliveryDays()));
        merchandiseOptions.setAll(snapshot.merchandiseOptions());
        merchandiseComboBox.setItems(merchandiseOptions);
        merchandiseComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Merchandise merchandise) {
                return merchandise == null ? "" : merchandise.getCode() + " - " + merchandise.getName();
            }

            @Override
            public Merchandise fromString(String value) {
                return null;
            }
        });
        inventoryRows.setAll(snapshot.inventoryRows());
        orderRows.setAll(snapshot.orders());
    }

    private void saveProfile() {
        SiteWorkspaceResult result = controller.updateProfile(new SiteProfileDraft(
            siteNameField.getText(),
            descriptionArea.getText(),
            parseOptionalInt(shipDaysField.getText()),
            parseOptionalInt(airDaysField.getText())
        ));
        showResult(result);
        if (result.success()) {
            reload();
        }
    }

    private void saveInventory() {
        Merchandise selected = merchandiseComboBox.getValue();
        if (selected == null) {
            showWarning("Vui lòng chọn mặt hàng.");
            return;
        }
        Integer stock = parseRequiredInt(stockQuantityField.getText());
        if (stock == null) {
            showWarning("Tồn kho phải là số nguyên không âm.");
            return;
        }
        SiteWorkspaceResult result = controller.updateInventoryItem(new SiteInventoryDraft(selected.getId(), stock));
        showResult(result);
        if (result.success()) {
            reload();
        }
    }

    private void removeInventory() {
        SiteInventoryRow selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn mặt hàng cần bỏ.");
            return;
        }
        SiteWorkspaceResult result = controller.removeInventoryItem(selected.merchandiseId());
        showResult(result);
        if (result.success()) {
            reload();
        }
    }

    private void confirmSupply(int orderId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận cung ứng");
        confirm.setHeaderText("Xác nhận cung ứng đơn hàng này?");
        confirm.setContentText("Sau khi xác nhận, trạng thái đơn hàng sẽ chuyển sang Đang giao.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SiteWorkspaceResult result = controller.confirmSupply(orderId);
                showResult(result);
                if (result.success()) {
                    reload();
                }
            }
        });
    }

    public void selectTab(int index) {
        workspaceTabs.getSelectionModel().select(index);
    }

    private static String statusBadgeClass(String statusKey) {
        return switch (statusKey) {
            case OrderingFormatters.STATUS_PENDING    -> "badge-pending";
            case OrderingFormatters.STATUS_PROCESSING -> "badge-processing";
            case OrderingFormatters.STATUS_SHIPPING   -> "badge-shipping";
            case OrderingFormatters.STATUS_COMPLETED  -> "badge-completed";
            case OrderingFormatters.STATUS_CANCELLED  -> "badge-cancelled";
            case OrderingFormatters.STATUS_REMOVED    -> "badge-removed";
            default                                   -> "badge-info";
        };
    }

    private void showOrderDetailPopup(SiteOrderRow row) {
        if (row == null) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn hàng");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        if (workspaceTabs.getScene() != null) {
            dialogPane.getStylesheets().addAll(workspaceTabs.getScene().getStylesheets());
        }
        dialogPane.getStyleClass().add("sw-dialog");

        VBox content = new VBox(12);
        content.setPrefWidth(650);
        content.setPrefHeight(400);

        VBox infoCard = new VBox(8);
        infoCard.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 14; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        Label codeLbl = new Label("Mã đơn hàng: " + row.orderCode());
        codeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
        
        Label reqLbl = new Label("Yêu cầu gốc: " + row.requestCode());
        reqLbl.setStyle("-fx-text-fill: #475569;");
        
        Label statusLbl = new Label("Trạng thái: " + row.statusText());
        statusLbl.setStyle("-fx-text-fill: #475569;");
        
        infoCard.getChildren().addAll(codeLbl, reqLbl, statusLbl);

        TableView<SiteOrderItemRow> popupTable = new TableView<>();
        VBox.setVgrow(popupTable, Priority.ALWAYS);

        TableColumn<SiteOrderItemRow, String> codeCol = new TableColumn<>("Mã hàng");
        codeCol.setPrefWidth(120);
        TableViewSupport.bindStringColumn(codeCol, SiteOrderItemRow::merchandiseCode);

        TableColumn<SiteOrderItemRow, String> nameCol = new TableColumn<>("Tên mặt hàng");
        nameCol.setPrefWidth(220);
        TableViewSupport.bindStringColumn(nameCol, SiteOrderItemRow::merchandiseName);

        TableColumn<SiteOrderItemRow, String> qtyCol = new TableColumn<>("Số lượng");
        qtyCol.setPrefWidth(80);
        TableViewSupport.bindStringColumn(qtyCol, SiteOrderItemRow::quantity);

        TableColumn<SiteOrderItemRow, String> unitCol = new TableColumn<>("Đơn vị");
        unitCol.setPrefWidth(80);
        TableViewSupport.bindStringColumn(unitCol, SiteOrderItemRow::unit);

        TableColumn<SiteOrderItemRow, String> delCol = new TableColumn<>("Vận chuyển");
        delCol.setPrefWidth(130);
        TableViewSupport.bindStringColumn(delCol, SiteOrderItemRow::deliveryMethod);
        delCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("biển") || item.toLowerCase().contains("ship")) {
                        setStyle("-fx-text-fill: #0284C7; -fx-font-weight: bold;");
                    } else if (item.contains("không") || item.toLowerCase().contains("air")) {
                        setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        popupTable.getColumns().addAll(codeCol, nameCol, qtyCol, unitCol, delCol);
        TableViewSupport.useConstrainedResize(popupTable);

        List<SiteOrderItemRow> items = controller.loadOrderItems(row.orderId());
        popupTable.setItems(FXCollections.observableArrayList(items));

        content.getChildren().addAll(infoCard, popupTable);
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void rejectOrder(int orderId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Từ chối đơn hàng");
        confirm.setHeaderText("Từ chối đơn hàng này?");
        confirm.setContentText("Sau khi từ chối, đơn hàng sẽ có trạng thái Đã hủy.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SiteWorkspaceResult result = controller.rejectOrder(orderId);
                showResult(result);
                if (result.success()) {
                    reload();
                }
            }
        });
    }

    private void populateInventorySelection(SiteInventoryRow row) {
        if (row == null) {
            merchandiseComboBox.setValue(null);
            stockQuantityField.clear();
            return;
        }
        merchandiseOptions.stream()
            .filter(item -> item.getId() == row.merchandiseId())
            .findFirst()
            .ifPresent(merchandiseComboBox::setValue);
        stockQuantityField.setText(String.valueOf(row.stockQuantity()));
    }

    private Integer parseOptionalInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private Integer parseRequiredInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showResult(SiteWorkspaceResult result) {
        Alert alert = new Alert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(result.success() ? "Thành công" : "Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(result.message());
        alert.showAndWait();
    }
}
