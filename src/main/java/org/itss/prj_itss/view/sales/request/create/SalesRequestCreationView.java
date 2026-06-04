package org.itss.prj_itss.view.sales.request.create;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.controller.sales.request.create.ISalesRequestCreationActions;
import org.itss.prj_itss.controller.sales.request.create.ISalesRequestCreationViewPort;
import org.itss.prj_itss.controller.sales.request.create.SalesRequestCreationItemInput;
import org.itss.prj_itss.controller.sales.request.create.SalesRequestCreationViewState;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SalesRequestCreationView implements ViewLifecycle, ISalesRequestCreationViewPort {

    private final List<SalesRequestCreationItemRow> itemRows = new ArrayList<>();

    private ISalesRequestCreationActions actions;
    private Runnable closeHandler;
    private Map<String, MerchandiseOption> merchandiseByCode = Map.of();

    @FXML private Button closeButton;
    @FXML private Button cancelButton;
    @FXML private VBox itemsContainer;
    @FXML private Button addItemButton;
    @FXML private Button submitButton;

    @FXML
    private void initialize() {
        closeButton.setOnAction(event -> closePopup());
        cancelButton.setOnAction(event -> closePopup());
        addItemButton.setOnAction(event -> addNewRow());
        submitButton.setOnAction(event -> submitRequest());
    }

    @Override
    public void bindEvents(ISalesRequestCreationActions actions) {
        this.actions = actions;
    }

    @Override
    public void render(SalesRequestCreationViewState viewModel) {
        merchandiseByCode = viewModel.merchandiseOptions().stream()
            .collect(Collectors.toUnmodifiableMap(
                option -> normalizeCode(option.code()),
                Function.identity(),
                (first, second) -> first
            ));
        itemRows.clear();
        itemsContainer.getChildren().clear();
        addNewRow();
    }

    private void submitRequest() {
        if (actions == null) {
            return;
        }

        List<SalesRequestCreationItemInput> items = new ArrayList<>();
        for (SalesRequestCreationItemRow row : itemRows) {
            SalesRequestCreationItemCandidate candidate = row.inputCandidate().orElseThrow();
            if (!candidate.complete()) {
                showError("Vui lòng điền đầy đủ thông tin hợp lệ cho tất cả các mặt hàng.");
                return;
            }

            items.add(new SalesRequestCreationItemInput(
                candidate.merchandiseCode(),
                candidate.quantityText(),
                candidate.desiredDate()
            ));
        }

        actions.submitRequested(items);
    }

    private void addNewRow() {
        SalesRequestCreationItemRow row = new SalesRequestCreationItemRow(
            itemRows.size() + 1,
            this::findMerchandiseByCode,
            this::removeRow
        );
        itemRows.add(row);
        itemsContainer.getChildren().add(row);
    }

    private void removeRow(SalesRequestCreationItemRow row) {
        if (itemRows.size() <= 1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
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
        if (code == null || code.isBlank()) {
            return null;
        }
        return merchandiseByCode.get(normalizeCode(code));
    }

    @Override
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    @Override
    public void close() {
        closePopup();
    }

    private void closePopup() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toLowerCase(Locale.ROOT);
    }
}
