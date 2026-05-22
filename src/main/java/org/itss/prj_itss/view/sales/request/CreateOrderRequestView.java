package org.itss.prj_itss.view.sales.request;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.itss.prj_itss.controller.sales.request.CreateOrderRequestController;
import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.model.request.application.sales.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.RequestItemInput;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class CreateOrderRequestView implements ViewLifecycle {
    private Stage dialog;
    private CreateOrderRequestController controller;

    @FXML
    private Button closeButton;

    @FXML
    private Button cancelButton;

    @FXML
    private VBox itemsContainer;

    @FXML
    private Button addItemButton;

    @FXML
    private Button submitButton;

    private Runnable onSaveCallback;

    public void init(Stage dialog, CreateOrderRequestController controller) {
        this.dialog = dialog;
        this.controller = controller;
        
        if (closeButton != null) {
            closeButton.setOnAction(e -> closePopup());
        }
        
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> closePopup());
        }

        if (addItemButton != null) {
            addItemButton.setOnAction(e -> addNewRow());
        }

        if (submitButton != null) {
            submitButton.setOnAction(e -> submitRequest());
        }

        if (itemsContainer != null && !itemsContainer.getChildren().isEmpty()) {
            HBox firstRow = (HBox) itemsContainer.getChildren().get(0);
            setupRowListeners(firstRow);
        }
    }

    public void setOnSave(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void submitRequest() {
        if (controller == null) return;
        try {
            List<RequestItemInput> items = new ArrayList<>();
            
            for (javafx.scene.Node node : itemsContainer.getChildren()) {
                HBox row = (HBox) node;
                TextField codeField = (TextField) row.getChildren().get(1);
                TextField quantityField = (TextField) row.getChildren().get(2);
                DatePicker datePicker = (DatePicker) row.getChildren().get(4);

                String code = codeField.getText();
                String qtyStr = quantityField.getText();
                LocalDate date = datePicker.getValue();

                if (code == null || code.isBlank() || qtyStr == null || qtyStr.isBlank() || date == null) {
                    showError("Vui lòng điền đầy đủ thông tin cho tất cả các mặt hàng.");
                    return;
                }

                MerchandiseOption m = controller.getMerchandiseOptionByCode(code.trim());
                if (m == null) {
                    showError("Mã hàng \"" + code + "\" không tồn tại trong hệ thống.");
                    return;
                }

                BigDecimal quantity;
                try {
                    quantity = new BigDecimal(qtyStr.trim());
                    if (quantity.compareTo(BigDecimal.ZERO) <= 0) throw new Exception();
                } catch (Exception e) {
                    showError("Số lượng phải là một số dương hợp lệ.");
                    return;
                }

                items.add(new RequestItemInput(m.id(), quantity, date));
            }

            if (items.isEmpty()) {
                showError("Cần ít nhất một mặt hàng để tạo yêu cầu.");
                return;
            }

            ActionResult result = controller.createRequest(items);
            if (result.success()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initOwner(dialog);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText(result.message());
                alert.showAndWait();

                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                closePopup();
            } else {
                showError("Lỗi khi lưu yêu cầu: " + result.message());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi lưu yêu cầu: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addNewRow() {
        int nextId = itemsContainer.getChildren().size() + 1;
        
        HBox newRow = new HBox(12);
        newRow.setAlignment(Pos.CENTER_LEFT);
        newRow.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-padding: 8;");

        javafx.scene.control.Label idLabel = new javafx.scene.control.Label(String.valueOf(nextId));
        idLabel.setMinWidth(30);
        idLabel.setAlignment(Pos.CENTER);
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        TextField codeField = new TextField();
        codeField.setPromptText("VD: MH-001");
        codeField.setMinWidth(150);
        codeField.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 6 10;");

        TextField quantityField = new TextField();
        quantityField.setPromptText("0");
        quantityField.setMinWidth(100);
        quantityField.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 6 10;");

        TextField unitField = new TextField();
        unitField.setPromptText("VD: Thùng");
        unitField.setMinWidth(120);
        unitField.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 6 10;");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("dd/mm/yyyy");
        datePicker.setMinWidth(180);
        datePicker.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4;");

        Button deleteBtn = new Button();
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
        SVGPath trashIcon = new SVGPath();
        trashIcon.setContent("M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19V4M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z");
        trashIcon.setFill(Color.web("#EF4444"));
        trashIcon.setScaleX(0.8);
        trashIcon.setScaleY(0.8);
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setOnAction(e -> removeRow(newRow));

        newRow.getChildren().addAll(idLabel, codeField, quantityField, unitField, datePicker, deleteBtn);
        setupRowListeners(newRow);
        
        itemsContainer.getChildren().add(newRow);
    }

    private void setupRowListeners(HBox row) {
        if (controller == null) return;
        TextField codeField = (TextField) row.getChildren().get(1);
        TextField unitField = (TextField) row.getChildren().get(3);

        String defaultStyle = "-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 6 10;";
        String errorStyle = "-fx-background-color: #FEF2F2; -fx-border-color: #EF4444; -fx-border-radius: 4; -fx-padding: 6 10;";

        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                codeField.setStyle(defaultStyle);
                unitField.setText("");
                return;
            }

            MerchandiseOption m = controller.getMerchandiseOptionByCode(newValue.trim());
            if (m != null) {
                codeField.setStyle(defaultStyle);
                unitField.setText(m.unit());
            } else {
                codeField.setStyle(errorStyle);
                unitField.setText("");
            }
        });
    }

    @FXML
    private void handleDeleteRow(javafx.event.ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        HBox row = (HBox) source.getParent();
        removeRow(row);
    }

    private void removeRow(HBox row) {
        if (itemsContainer.getChildren().size() <= 1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(dialog);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Không thể xóa sản phẩm cuối cùng. Yêu cầu phải có ít nhất một sản phẩm.");
            alert.showAndWait();
            return;
        }

        itemsContainer.getChildren().remove(row);
        renumberRows();
    }

    private void renumberRows() {
        int id = 1;
        for (javafx.scene.Node node : itemsContainer.getChildren()) {
            HBox row = (HBox) node;
            javafx.scene.control.Label idLabel = (javafx.scene.control.Label) row.getChildren().get(0);
            idLabel.setText(String.valueOf(id++));
        }
    }

    private void closePopup() {
        if (dialog != null) {
            dialog.close();
        }
    }
}
