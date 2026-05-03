package org.itss.prj_itss.sales.request.create;

import org.itss.prj_itss.layout.IViewController;

public class CreateOrderRequestController implements IViewController {
    private javafx.stage.Stage dialog;
    private org.itss.prj_itss.service.MerchandiseService merchandiseService;
    private org.itss.prj_itss.service.RequestService requestService;

    @javafx.fxml.FXML
    private javafx.scene.control.Button closeButton;

    @javafx.fxml.FXML
    private javafx.scene.control.Button cancelButton;

    @javafx.fxml.FXML
    private javafx.scene.layout.VBox itemsContainer;

    @javafx.fxml.FXML
    private javafx.scene.control.Button addItemButton;

    @javafx.fxml.FXML
    private javafx.scene.control.Button submitButton;

    private Runnable onSaveCallback;

    @Override
    public void init(org.itss.prj_itss.layout.INavigator navigator, org.itss.prj_itss.common.config.ApplicationContext context) {
        this.merchandiseService = org.itss.prj_itss.common.config.ApplicationContext.getInstance().merchandiseService();
        this.requestService = org.itss.prj_itss.common.config.ApplicationContext.getInstance().requestService();
        
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

        // Initialize with the first row's listener if it already exists in FXML
        if (itemsContainer != null && !itemsContainer.getChildren().isEmpty()) {
            javafx.scene.layout.HBox firstRow = (javafx.scene.layout.HBox) itemsContainer.getChildren().get(0);
            setupRowListeners(firstRow);
        }
    }

    public void setOnSave(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void submitRequest() {
        try {
            java.util.List<org.itss.prj_itss.entity.RequestMerchandise> items = new java.util.ArrayList<>();
            
            for (javafx.scene.Node node : itemsContainer.getChildren()) {
                javafx.scene.layout.HBox row = (javafx.scene.layout.HBox) node;
                javafx.scene.control.TextField codeField = (javafx.scene.control.TextField) row.getChildren().get(1);
                javafx.scene.control.TextField quantityField = (javafx.scene.control.TextField) row.getChildren().get(2);
                javafx.scene.control.DatePicker datePicker = (javafx.scene.control.DatePicker) row.getChildren().get(4);

                String code = codeField.getText();
                String qtyStr = quantityField.getText();
                java.time.LocalDate date = datePicker.getValue();

                if (code == null || code.isBlank() || qtyStr == null || qtyStr.isBlank() || date == null) {
                    showError("Vui lòng điền đầy đủ thông tin cho tất cả các mặt hàng.");
                    return;
                }

                org.itss.prj_itss.entity.Merchandise m = merchandiseService.findByCode(code.trim());
                if (m == null) {
                    showError("Mã hàng \"" + code + "\" không tồn tại trong hệ thống.");
                    return;
                }

                java.math.BigDecimal quantity;
                try {
                    quantity = new java.math.BigDecimal(qtyStr.trim());
                    if (quantity.compareTo(java.math.BigDecimal.ZERO) <= 0) throw new Exception();
                } catch (Exception e) {
                    showError("Số lượng phải là một số dương hợp lệ.");
                    return;
                }

                org.itss.prj_itss.entity.RequestMerchandise item = new org.itss.prj_itss.entity.RequestMerchandise();
                item.setMerchandiseId(m.getId());
                item.setQuantityOrdered(quantity);
                item.setDesiredDeliveryDate(date);
                items.add(item);
            }

            if (items.isEmpty()) {
                showError("Cần ít nhất một mặt hàng để tạo yêu cầu.");
                return;
            }

            requestService.createRequest(items, ""); // Note is empty for now
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.initOwner(dialog);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Yêu cầu nhập hàng đã được gửi thành công.");
            alert.showAndWait();

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            closePopup();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi lưu yêu cầu: " + e.getMessage());
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addNewRow() {
        int nextId = itemsContainer.getChildren().size() + 1;
        
        javafx.scene.layout.HBox newRow = new javafx.scene.layout.HBox(12);
        newRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        newRow.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-padding: 8;");

        javafx.scene.control.Label idLabel = new javafx.scene.control.Label(String.valueOf(nextId));
        idLabel.setMinWidth(30);
        idLabel.setAlignment(javafx.geometry.Pos.CENTER);
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        javafx.scene.control.TextField codeField = new javafx.scene.control.TextField();
        codeField.setPromptText("VD: MH-001");
        codeField.setMinWidth(150);
        codeField.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 6 10;");

        javafx.scene.control.TextField quantityField = new javafx.scene.control.TextField();
        quantityField.setPromptText("0");
        quantityField.setMinWidth(100);
        quantityField.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 6 10;");

        javafx.scene.control.TextField unitField = new javafx.scene.control.TextField();
        unitField.setPromptText("VD: Thùng");
        unitField.setMinWidth(120);
        unitField.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 6 10;");

        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
        datePicker.setPromptText("dd/mm/yyyy");
        datePicker.setMinWidth(180);
        datePicker.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4;");

        javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button();
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
        javafx.scene.shape.SVGPath trashIcon = new javafx.scene.shape.SVGPath();
        trashIcon.setContent("M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19V4M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z");
        trashIcon.setFill(javafx.scene.paint.Color.web("#EF4444"));
        trashIcon.setScaleX(0.8);
        trashIcon.setScaleY(0.8);
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setOnAction(e -> removeRow(newRow));

        newRow.getChildren().addAll(idLabel, codeField, quantityField, unitField, datePicker, deleteBtn);
        setupRowListeners(newRow);
        
        itemsContainer.getChildren().add(newRow);
    }

    private void setupRowListeners(javafx.scene.layout.HBox row) {
        javafx.scene.control.TextField codeField = (javafx.scene.control.TextField) row.getChildren().get(1);
        javafx.scene.control.TextField unitField = (javafx.scene.control.TextField) row.getChildren().get(3);

        String defaultStyle = "-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 6 10;";
        String errorStyle = "-fx-background-color: #FEF2F2; -fx-border-color: #EF4444; -fx-border-radius: 4; -fx-padding: 6 10;";

        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                codeField.setStyle(defaultStyle);
                unitField.setText("");
                return;
            }

            org.itss.prj_itss.entity.Merchandise m = merchandiseService.findByCode(newValue.trim());
            if (m != null) {
                codeField.setStyle(defaultStyle);
                unitField.setText(m.getUnit());
            } else {
                codeField.setStyle(errorStyle);
                unitField.setText("");
            }
        });
    }

    @javafx.fxml.FXML
    private void handleDeleteRow(javafx.event.ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.scene.layout.HBox row = (javafx.scene.layout.HBox) source.getParent();
        removeRow(row);
    }

    private void removeRow(javafx.scene.layout.HBox row) {
        if (itemsContainer.getChildren().size() <= 1) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
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
            javafx.scene.layout.HBox row = (javafx.scene.layout.HBox) node;
            javafx.scene.control.Label idLabel = (javafx.scene.control.Label) row.getChildren().get(0);
            idLabel.setText(String.valueOf(id++));
        }
    }

    public void setDialog(javafx.stage.Stage dialog) {
        this.dialog = dialog;
    }

    private void closePopup() {
        if (dialog != null) {
            dialog.close();
        }
    }
}
