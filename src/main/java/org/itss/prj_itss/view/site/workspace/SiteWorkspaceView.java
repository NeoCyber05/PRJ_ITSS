package org.itss.prj_itss.view.site.workspace;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private TableColumn<SiteOrderRow, String> orderStatusColumn;

    @FXML
    private TableColumn<SiteOrderRow, SiteOrderRow> orderActionColumn;

    @FXML
    private VBox orderDetailBox;

    @FXML
    private TableView<SiteOrderItemRow> orderItemTable;

    @FXML
    private TableColumn<SiteOrderItemRow, String> orderItemCodeColumn;

    @FXML
    private TableColumn<SiteOrderItemRow, String> orderItemNameColumn;

    @FXML
    private TableColumn<SiteOrderItemRow, String> orderItemQuantityColumn;

    @FXML
    private TableColumn<SiteOrderItemRow, String> orderItemUnitColumn;

    @FXML
    private TableColumn<SiteOrderItemRow, String> orderItemDeliveryColumn;

    @FXML
    private void initialize() {
        TableViewSupport.useConstrainedResize(inventoryTable);
        TableViewSupport.bindStringColumn(inventoryCodeColumn, SiteInventoryRow::merchandiseCode);
        TableViewSupport.bindStringColumn(inventoryNameColumn, SiteInventoryRow::merchandiseName);
        TableViewSupport.bindStringColumn(inventoryUnitColumn, SiteInventoryRow::unit);
        TableViewSupport.bindStringColumn(inventoryStockColumn, row -> String.valueOf(row.stockQuantity()));
        inventoryTable.setItems(inventoryRows);

        TableViewSupport.useConstrainedResize(orderTable);
        TableViewSupport.bindStringColumn(orderCodeColumn, SiteOrderRow::orderCode);
        TableViewSupport.bindStringColumn(requestCodeColumn, SiteOrderRow::requestCode);
        TableViewSupport.bindStringColumn(createdAtColumn, SiteOrderRow::createdAt);
        TableViewSupport.bindStringColumn(orderStatusColumn, SiteOrderRow::statusText);
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
                detailButton.setOnAction(event -> showOrderDetail(row));

                Button confirmButton = new Button("Xác nhận");
                confirmButton.setDisable(!row.confirmable());
                confirmButton.setOnAction(event -> confirmSupply(row.orderId()));

                HBox box = new HBox(6, detailButton, confirmButton);
                setGraphic(box);
                setText(null);
            }
        });
        orderTable.setItems(orderRows);

        TableViewSupport.useConstrainedResize(orderItemTable);
        TableViewSupport.bindStringColumn(orderItemCodeColumn, SiteOrderItemRow::merchandiseCode);
        TableViewSupport.bindStringColumn(orderItemNameColumn, SiteOrderItemRow::merchandiseName);
        TableViewSupport.bindStringColumn(orderItemQuantityColumn, SiteOrderItemRow::quantity);
        TableViewSupport.bindStringColumn(orderItemUnitColumn, SiteOrderItemRow::unit);
        TableViewSupport.bindStringColumn(orderItemDeliveryColumn, SiteOrderItemRow::deliveryMethod);
        orderItemTable.setItems(orderItemRows);

        saveProfileButton.setOnAction(event -> saveProfile());
        saveInventoryButton.setOnAction(event -> saveInventory());
        removeInventoryButton.setOnAction(event -> removeInventory());
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, row) -> populateInventorySelection(row));
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, row) -> showOrderDetail(row));
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
        inventoryRows.clear();
        orderRows.clear();
        orderItemRows.clear();
        merchandiseOptions.clear();
    }

    private void setWorkspaceEnabled(SiteWorkspaceSnapshot snapshot) {
        workspaceTabs.setDisable(false);
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

    private void showOrderDetail(SiteOrderRow row) {
        orderDetailBox.getChildren().clear();
        if (row == null) {
            orderDetailBox.getChildren().add(new Label("Chọn một đơn hàng để xem chi tiết."));
            orderItemRows.clear();
            return;
        }
        orderDetailBox.getChildren().addAll(
            new Label("Mã đơn: " + row.orderCode()),
            new Label("Yêu cầu gốc: " + row.requestCode()),
            new Label("Trạng thái: " + row.statusText())
        );
        orderItemRows.setAll(controller.loadOrderItems(row.orderId()));
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
