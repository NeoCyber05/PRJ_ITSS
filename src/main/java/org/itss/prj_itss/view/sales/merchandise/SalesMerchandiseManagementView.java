package org.itss.prj_itss.view.sales.merchandise;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.sales.merchandise.SalesMerchandiseController;
import org.itss.prj_itss.model.merchandise.application.MerchandiseDraft;
import org.itss.prj_itss.model.merchandise.application.MerchandiseManagementResult;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class SalesMerchandiseManagementView implements ViewLifecycle {

    private Navigator navigator;
    private SalesMerchandiseController controller;
    private List<SalesMerchandiseController.MerchandiseRow> allRows = List.of();
    private final ObservableList<SalesMerchandiseController.MerchandiseRow> displayedRows = FXCollections.observableArrayList();

    @FXML private Label totalLabel;
    @FXML private Label activeLabel;
    @FXML private Label inactiveLabel;
    @FXML private TextField searchField;
    @FXML private Button createButton;
    @FXML private TableView<SalesMerchandiseController.MerchandiseRow> merchandiseTable;
    @FXML private TableColumn<SalesMerchandiseController.MerchandiseRow, String> codeColumn;
    @FXML private TableColumn<SalesMerchandiseController.MerchandiseRow, String> nameColumn;
    @FXML private TableColumn<SalesMerchandiseController.MerchandiseRow, String> unitColumn;
    @FXML private TableColumn<SalesMerchandiseController.MerchandiseRow, String> statusColumn;
    @FXML private TableColumn<SalesMerchandiseController.MerchandiseRow, SalesMerchandiseController.MerchandiseRow> actionColumn;

    @FXML
    private void initialize() {
        merchandiseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        codeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().code()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().unit()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().active() ? "Hoạt động" : "Vô hiệu"));
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
                boolean active = "Hoạt động".equals(status);
                dot.setFill(Color.web(active ? "#22C55E" : "#EF4444"));
                Label label = new Label(status);
                label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (active ? "#15803D" : "#B91C1C") + ";");
                box.getChildren().addAll(dot, label);
                setGraphic(box);
                setText(null);
            }
        });

        actionColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SalesMerchandiseController.MerchandiseRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(buildActionButtons(row));
                setText(null);
            }
        });

        merchandiseTable.setItems(displayedRows);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        createButton.setOnAction(e -> handleCreate());
    }

    public void init(Navigator navigator, SalesMerchandiseController controller) {
        this.navigator = navigator;
        this.controller = controller;
    }

    @Override
    public void onViewShown() {
        reload();
    }

    private void reload() {
        if (controller == null) return;
        allRows = controller.loadAll();
        totalLabel.setText(String.valueOf(allRows.size()));
        activeLabel.setText(String.valueOf(allRows.stream().filter(SalesMerchandiseController.MerchandiseRow::active).count()));
        inactiveLabel.setText(String.valueOf(allRows.stream().filter(r -> !r.active()).count()));
        applyFilter();
    }

    private void applyFilter() {
        String keyword = searchField.getText();
        List<SalesMerchandiseController.MerchandiseRow> filtered = controller != null
            ? controller.filterRows(allRows, keyword)
            : allRows;
        displayedRows.setAll(filtered);
    }

    private void handleCreate() {
        if (controller == null) return;
        Optional<MerchandiseDraft> draft = buildDialog("Thêm mặt hàng", null);
        draft.ifPresent(d -> {
            MerchandiseManagementResult result = controller.create(d);
            showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                result.success() ? "Thành công" : "Lỗi", result.message());
            if (result.success()) reload();
        });
    }

    private void handleEdit(SalesMerchandiseController.MerchandiseRow row) {
        if (controller == null) return;
        Optional<MerchandiseDraft> draft = buildDialog("Sửa mặt hàng", row);
        draft.ifPresent(d -> {
            MerchandiseManagementResult result = controller.update(row.id(), d);
            showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                result.success() ? "Thành công" : "Lỗi", result.message());
            if (result.success()) reload();
        });
    }

    private void handleDeactivate(SalesMerchandiseController.MerchandiseRow row) {
        if (controller == null) return;
        if (!confirm("Xác nhận vô hiệu hóa", "Vô hiệu hóa mặt hàng \"" + row.name() + "\"?")) return;
        MerchandiseManagementResult result = controller.deactivate(row.id());
        showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
            result.success() ? "Thành công" : "Lỗi", result.message());
        if (result.success()) reload();
    }

    private void handleRestore(SalesMerchandiseController.MerchandiseRow row) {
        if (controller == null) return;
        if (!confirm("Xác nhận khôi phục", "Khôi phục mặt hàng \"" + row.name() + "\"?")) return;
        MerchandiseManagementResult result = controller.restore(row.id());
        showAlert(result.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
            result.success() ? "Thành công" : "Lỗi", result.message());
        if (result.success()) reload();
    }

    private HBox buildActionButtons(SalesMerchandiseController.MerchandiseRow row) {
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("Sửa");
        editBtn.setStyle("-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEdit(row));
        actions.getChildren().add(editBtn);

        if (row.active()) {
            Button deactivateBtn = new Button("Vô hiệu hóa");
            deactivateBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
            deactivateBtn.setOnAction(e -> handleDeactivate(row));
            actions.getChildren().add(deactivateBtn);
        } else {
            Button restoreBtn = new Button("Khôi phục");
            restoreBtn.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
            restoreBtn.setOnAction(e -> handleRestore(row));
            actions.getChildren().add(restoreBtn);
        }

        return actions;
    }

    private Optional<MerchandiseDraft> buildDialog(String title, SalesMerchandiseController.MerchandiseRow prefill) {
        Dialog<MerchandiseDraft> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField codeField = new TextField();
        codeField.setPromptText("Mã hàng");
        TextField nameField = new TextField();
        nameField.setPromptText("Tên mặt hàng");
        TextField unitField = new TextField();
        unitField.setPromptText("Đơn vị");

        if (prefill != null) {
            codeField.setText(prefill.code());
            nameField.setText(prefill.name());
            unitField.setText(prefill.unit());
        }

        grid.add(new Label("Mã hàng:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Tên mặt hàng:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Đơn vị:"), 0, 2);
        grid.add(unitField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            return new MerchandiseDraft(
                codeField.getText(),
                nameField.getText(),
                unitField.getText()
            );
        });

        return dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}
