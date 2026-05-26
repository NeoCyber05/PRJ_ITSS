package org.itss.prj_itss.view.ordering.request.process.items;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.model.request.application.processing.AllocationChangeCommand;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeResultView;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingViewModel;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.ETA_STATE_CLASSES;
import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.setStateClass;

public final class AllocationSiteRowView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/process/items/allocation-site-row-view.fxml";

    @FXML
    private Label siteNameLabel;
    @FXML
    private Label siteDetailLabel;
    @FXML
    private Label stockLabel;
    @FXML
    private TextField quantityField;
    @FXML
    private ComboBox<String> transportBox;
    @FXML
    private Label etaBadge;
    @FXML
    private Label warningLabel;

    private RequestProcessingViewModel.AllocationSiteRowViewModel siteRow;
    private Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged;
    private Runnable onRowChanged;

    public static VBox load(
        RequestProcessingViewModel.AllocationSiteRowViewModel siteRow,
        Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged,
        Runnable onRowChanged
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                AllocationSiteRowView.class.getResource(VIEW_RESOURCE),
                "Missing allocation site row FXML"
            ));
            Parent root = loader.load();
            AllocationSiteRowView controller = loader.getController();
            controller.init(siteRow, onAllocationInputChanged, onRowChanged);
            return (VBox) root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load allocation site row view", exception);
        }
    }

    private void init(
        RequestProcessingViewModel.AllocationSiteRowViewModel siteRow,
        Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged,
        Runnable onRowChanged
    ) {
        this.siteRow = Objects.requireNonNull(siteRow, "siteRow");
        this.onAllocationInputChanged = Objects.requireNonNull(onAllocationInputChanged, "onAllocationInputChanged");
        this.onRowChanged = onRowChanged == null ? () -> {} : onRowChanged;

        siteNameLabel.setText(siteRow.siteName());
        siteDetailLabel.setText(siteRow.siteDetail());
        stockLabel.setText(String.valueOf(siteRow.stock()));
        quantityField.setText(siteRow.quantity() == 0 ? "0" : String.valueOf(siteRow.quantity()));

        transportBox.getItems().addAll(siteRow.transportLabels());
        transportBox.setDisable(siteRow.transportDisabled());
        transportBox.setValue(siteRow.transportDisabled()
            ? siteRow.transportLabels().get(0)
            : siteRow.selectedTransportLabel());

        etaBadge.setText(siteRow.deliveryStatusText());
        setStateClass(etaBadge, ETA_STATE_CLASSES, siteRow.deliveryStatusClass());

        quantityField.textProperty().addListener((observable, oldValue, newValue) -> applyAllocationChange());
        transportBox.valueProperty().addListener((observable, oldValue, newValue) -> applyAllocationChange());
    }

    private void applyAllocationChange() {
        AllocationChangeResultView result = onAllocationInputChanged.apply(new AllocationChangeCommand(
            siteRow.itemMerchandiseId(),
            siteRow.siteId(),
            quantityField.getText(),
            transportBox.getValue()
        ));

        etaBadge.setText(result.deliveryStatusText());
        setStateClass(etaBadge, ETA_STATE_CLASSES, result.deliveryStatusClass());

        if (!result.applied()) {
            showWarning(warningMessage(result));
            return;
        }

        hideWarning();
        onRowChanged.run();
    }

    private String warningMessage(AllocationChangeResultView result) {
        String errorType = result.errorType();
        return switch (errorType) {
            case "EXCEEDS_STOCK" -> "Vượt tồn kho của site (" + result.stock() + ").";
            case "NEGATIVE_QUANTITY" -> "Số lượng không được âm.";
            case "INVALID_INTEGER" -> "Nhập số nguyên hợp lệ.";
            default -> "";
        };
    }

    private void showWarning(String message) {
        warningLabel.setText(message);
        warningLabel.setVisible(true);
        warningLabel.setManaged(true);
    }

    private void hideWarning() {
        warningLabel.setText("");
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
    }
}
