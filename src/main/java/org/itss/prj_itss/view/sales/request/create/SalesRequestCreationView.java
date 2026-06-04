package org.itss.prj_itss.view.sales.request.create;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.itss.prj_itss.controller.sales.request.create.SalesRequestCreationController;
import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class SalesRequestCreationView implements ViewLifecycle {

    private final List<SalesRequestCreationItemRow> itemRows = new ArrayList<>();

    private Stage dialog;
    private SalesRequestCreationController controller;
    private Runnable onSaveCallback;
    private List<String> availableCodes;

    @FXML private Button closeButton;
    @FXML private Button cancelButton;
    @FXML private javafx.scene.control.ScrollPane itemsScroll;
    @FXML private VBox itemsContainer;
    @FXML private Button addItemButton;
    @FXML private Button submitButton;

    public void init(Stage dialog, SalesRequestCreationController controller) {
        this.dialog = dialog;
        this.controller = controller;

        if (itemsScroll != null && itemsContainer != null) {
            itemsScroll.prefHeightProperty().bind(itemsContainer.heightProperty());
            itemsScroll.setMaxHeight(400);
        }

        closeButton.setOnAction(event -> closePopup());
        cancelButton.setOnAction(event -> closePopup());
        addItemButton.setOnAction(event -> addNewRow());
        submitButton.setOnAction(event -> submitRequest());

        if (controller != null) {
            availableCodes = controller.getAllMerchandiseCodes();
        } else {
            availableCodes = new ArrayList<>();
        }

        itemRows.clear();
        itemsContainer.getChildren().clear();
        addNewRow();
    }

    public void setOnSave(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void submitRequest() {
        if (controller == null) {
            return;
        }

        List<SalesRequestItemSubmission> items = new ArrayList<>();
        for (SalesRequestCreationItemRow row : itemRows) {
            SalesRequestCreationItemCandidate candidate = row.inputCandidate().orElseThrow();
            if (!candidate.complete()) {
                showError("Vui lòng điền đầy đủ thông tin hợp lệ cho tất cả các mặt hàng.");
                return;
            }

            MerchandiseOption merchandise = controller.getMerchandiseOptionByCode(candidate.merchandiseCode());
            if (merchandise == null) {
                showError("Mã hàng \"" + candidate.merchandiseCode() + "\" không tồn tại trong hệ thống.");
                return;
            }
            
            if (candidate.quantity().compareTo(new BigDecimal(merchandise.totalStock())) > 0) {
                showError("Mặt hàng \"" + merchandise.name() + "\" chỉ còn tồn kho " + merchandise.totalStock() + " " + merchandise.unit() + ". Vui lòng nhập số lượng nhỏ hơn hoặc bằng tồn kho.");
                return;
            }

            items.add(new SalesRequestItemSubmission(
                merchandise.id(),
                candidate.quantity(),
                candidate.desiredDate()
            ));
        }

        if (items.isEmpty()) {
            showError("Cần ít nhất một mặt hàng để tạo yêu cầu.");
            return;
        }

        ActionResult result = controller.createRequest(items);
        if (!result.success()) {
            showError("Lỗi khi lưu yêu cầu: " + result.message());
            return;
        }

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
    }

    private void addNewRow() {
        SalesRequestCreationItemRow row = new SalesRequestCreationItemRow(
            itemRows.size() + 1,
            this::findMerchandiseByCode,
            availableCodes,
            this::removeRow
        );
        itemRows.add(row);
        itemsContainer.getChildren().add(row);
    }

    private void removeRow(SalesRequestCreationItemRow row) {
        if (itemRows.size() <= 1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(dialog);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Không thể xóa sản phẩm cuối cùng. Yêu cầu phải có ít nhất một sản phẩm.");
            alert.showAndWait();
            return;
        }

        itemRows.remove(row);
        itemsContainer.getChildren().remove(row);
        renumberRows();
    }

    private void renumberRows() {
        for (int index = 0; index < itemRows.size(); index++) {
            itemRows.get(index).updateIndex(index + 1);
        }
    }

    private MerchandiseOption findMerchandiseByCode(String code) {
        if (controller == null || code == null || code.isBlank()) {
            return null;
        }
        return controller.getMerchandiseOptionByCode(code.trim());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closePopup() {
        if (dialog != null) {
            dialog.close();
        }
    }
}
