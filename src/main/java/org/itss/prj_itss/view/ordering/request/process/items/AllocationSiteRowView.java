package org.itss.prj_itss.view.ordering.request.process.items;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.model.request.application.processing.AllocationChangeCommand;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeResultView;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingViewModel;

import java.util.function.Function;

import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.ETA_STATE_CLASSES;
import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.setStateClass;

final class AllocationSiteRowView {

    private final Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged;
    private final Runnable onRowChanged;

    AllocationSiteRowView(
        Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged,
        Runnable onRowChanged
    ) {
        this.onAllocationInputChanged = onAllocationInputChanged;
        this.onRowChanged = onRowChanged;
    }

    VBox build(RequestProcessingViewModel.AllocationSiteRowViewModel siteRow) {
        VBox rowBox = new VBox(4);
        rowBox.setPadding(new Insets(0, 0, 0, 0));
        addStyleClass(rowBox, "allocation-table-row");

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));

        VBox siteBox = buildSiteInfoBox(siteRow);
        Label stockLabel = buildStockLabel(siteRow.stock());
        TextField quantityField = buildQuantityField(siteRow.quantity());
        HBox quantityBox = wrapQuantityField(quantityField);
        ComboBox<String> transportBox = buildTransportBox(siteRow);
        Label etaBadge = buildEtaBadge(siteRow.deliveryStatusText(), siteRow.deliveryStatusClass());
        Label warningLabel = buildWarningLabel();

        wireListeners(siteRow, quantityField, transportBox, etaBadge, warningLabel);

        row.getChildren().addAll(siteBox, stockLabel, quantityBox, transportBox, etaBadge);
        rowBox.getChildren().addAll(row, warningLabel);
        return rowBox;
    }

    private void wireListeners(
        RequestProcessingViewModel.AllocationSiteRowViewModel siteRow,
        TextField quantityField,
        ComboBox<String> transportBox,
        Label etaBadge,
        Label warningLabel
    ) {
        quantityField.textProperty().addListener((observable, oldValue, newValue) ->
            applyAllocationChange(siteRow, quantityField, transportBox, etaBadge, warningLabel)
        );
        transportBox.valueProperty().addListener((observable, oldValue, newValue) ->
            applyAllocationChange(siteRow, quantityField, transportBox, etaBadge, warningLabel)
        );
    }

    private VBox buildSiteInfoBox(RequestProcessingViewModel.AllocationSiteRowViewModel siteRow) {
        VBox siteBox = new VBox(4);
        siteBox.setMinWidth(380);
        siteBox.setPrefWidth(380);

        Label siteNameLabel = new Label(siteRow.siteName());
        addStyleClass(siteNameLabel, "allocation-site-name");

        Label siteDetailLabel = new Label(siteRow.siteDetail());
        addStyleClass(siteDetailLabel, "allocation-site-detail");
        siteBox.getChildren().addAll(siteNameLabel, siteDetailLabel);
        return siteBox;
    }

    private Label buildStockLabel(int stock) {
        Label stockLabel = new Label(String.valueOf(stock));
        stockLabel.setMinWidth(100);
        stockLabel.setPrefWidth(100);
        addStyleClass(stockLabel, "allocation-stock-label");
        return stockLabel;
    }

    private TextField buildQuantityField(int quantity) {
        TextField quantityField = new TextField();
        quantityField.setPrefWidth(110);
        quantityField.setText(quantity == 0 ? "0" : String.valueOf(quantity));
        addStyleClass(quantityField, "allocation-quantity-field");
        return quantityField;
    }

    private HBox wrapQuantityField(TextField quantityField) {
        Label unitLabel = new Label("chiếc");
        addStyleClass(unitLabel, "allocation-unit-label");
        HBox quantityBox = new HBox(8, quantityField, unitLabel);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        quantityBox.setMinWidth(170);
        quantityBox.setPrefWidth(170);
        return quantityBox;
    }

    private ComboBox<String> buildTransportBox(RequestProcessingViewModel.AllocationSiteRowViewModel siteRow) {
        ComboBox<String> transportBox = new ComboBox<>();
        transportBox.getItems().addAll(siteRow.transportLabels());
        transportBox.setDisable(siteRow.transportDisabled());
        transportBox.setValue(siteRow.transportDisabled() ? siteRow.transportLabels().get(0) : siteRow.selectedTransportLabel());
        transportBox.setPrefWidth(180);
        transportBox.setMinWidth(180);
        addStyleClass(transportBox, "allocation-transport-box");
        return transportBox;
    }

    private Label buildEtaBadge(String deliveryStatusText, String deliveryStatusClass) {
        Label etaBadge = new Label(deliveryStatusText);
        etaBadge.setMinWidth(120);
        addStyleClass(etaBadge, "allocation-eta-badge");
        setStateClass(etaBadge, ETA_STATE_CLASSES, deliveryStatusClass);
        return etaBadge;
    }

    private Label buildWarningLabel() {
        Label warningLabel = new Label();
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
        addStyleClass(warningLabel, "allocation-warning-label");
        return warningLabel;
    }

    private void applyAllocationChange(
        RequestProcessingViewModel.AllocationSiteRowViewModel siteRow,
        TextField quantityField,
        ComboBox<String> transportBox,
        Label etaBadge,
        Label warningLabel
    ) {
        AllocationChangeResultView result = onAllocationInputChanged.apply(new AllocationChangeCommand(
            siteRow.itemMerchandiseId(),
            siteRow.siteId(),
            quantityField.getText(),
            transportBox.getValue()
        ));

        etaBadge.setText(buildDeliveryStatusText(result));
        setStateClass(etaBadge, ETA_STATE_CLASSES, buildDeliveryStatusClass(result));

        if (!result.applied()) {
            showWarning(warningLabel, warningMessage(result));
            return;
        }

        hideWarning(warningLabel);
        onRowChanged.run();
    }

    private String buildDeliveryStatusText(AllocationChangeResultView result) {
        if (!result.deliveryAvailable()) {
            return "Không khả dụng";
        }
        if (result.dayDelta() > 0) {
            return "Sớm " + result.dayDelta() + " ngày";
        } else if (result.dayDelta() == 0) {
            return "Kịp hạn";
        } else {
            return "Trễ " + Math.abs(result.dayDelta()) + " ngày";
        }
    }

    private String buildDeliveryStatusClass(AllocationChangeResultView result) {
        if (!result.deliveryAvailable()) {
            return "allocation-eta-unavailable";
        }
        if (result.dayDelta() > 0) {
            return "allocation-eta-early";
        } else if (result.dayDelta() == 0) {
            return "allocation-eta-on-time";
        } else {
            return "allocation-eta-late";
        }
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

    private void showWarning(Label warningLabel, String message) {
        warningLabel.setText(message);
        warningLabel.setVisible(true);
        warningLabel.setManaged(true);
    }

    private void hideWarning(Label warningLabel) {
        warningLabel.setText("");
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
    }
}
