package org.itss.prj_itss.request.presentation.ordering.process.items;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.request.business.model.ItemRequirement;
import org.itss.prj_itss.request.business.model.SiteStockOption;
import org.itss.prj_itss.request.business.allocation.AllocationControl;
import org.itss.prj_itss.request.business.allocation.AllocationControl.AllocationChangeRequest;
import org.itss.prj_itss.request.business.allocation.AllocationControl.AllocationChangeResult;
import org.itss.prj_itss.request.business.allocation.AllocationControl.AllocationInputError;
import org.itss.prj_itss.request.business.allocation.AllocationControl.AllocationSiteRowState;
import org.itss.prj_itss.request.business.allocation.AllocationControl.DeliveryStatus;

import java.util.function.Function;

import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.ETA_STATE_CLASSES;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.setStateClass;

final class AllocationSiteRowView {

    private final AllocationControl allocationControl;
    private final Function<AllocationChangeRequest, AllocationChangeResult> onAllocationInputChanged;

    AllocationSiteRowView(
        AllocationControl allocationControl,
        Function<AllocationChangeRequest, AllocationChangeResult> onAllocationInputChanged
    ) {
        this.allocationControl = allocationControl;
        this.onAllocationInputChanged = onAllocationInputChanged;
    }

    VBox build(ItemRequirement item, SiteStockOption site, Runnable onChanged) {
        AllocationSiteRowState state = allocationControl.siteRowState(item, site);
        VBox rowBox = new VBox(4);
        rowBox.setPadding(new Insets(0, 0, 0, 0));
        addStyleClass(rowBox, "allocation-table-row");

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));

        VBox siteBox = buildSiteInfoBox(state);
        Label stockLabel = buildStockLabel(state.stock());
        TextField quantityField = buildQuantityField(state.quantity());
        HBox quantityBox = wrapQuantityField(quantityField);
        ComboBox<String> transportBox = buildTransportBox(state);
        Label etaBadge = buildEtaBadge(state.deliveryStatus());
        Label warningLabel = buildWarningLabel();

        wireListeners(item, site, quantityField, transportBox, etaBadge, warningLabel, onChanged);

        row.getChildren().addAll(siteBox, stockLabel, quantityBox, transportBox, etaBadge);
        rowBox.getChildren().addAll(row, warningLabel);
        return rowBox;
    }

    private void wireListeners(
        ItemRequirement item,
        SiteStockOption site,
        TextField quantityField,
        ComboBox<String> transportBox,
        Label etaBadge,
        Label warningLabel,
        Runnable onChanged
    ) {
        quantityField.textProperty().addListener((observable, oldValue, newValue) ->
            applyAllocationChange(item, site, quantityField, transportBox, etaBadge, warningLabel, onChanged)
        );
        transportBox.valueProperty().addListener((observable, oldValue, newValue) ->
            applyAllocationChange(item, site, quantityField, transportBox, etaBadge, warningLabel, onChanged)
        );
    }

    private VBox buildSiteInfoBox(AllocationSiteRowState state) {
        VBox siteBox = new VBox(4);
        siteBox.setMinWidth(380);
        siteBox.setPrefWidth(380);

        Label siteNameLabel = new Label(state.siteName());
        addStyleClass(siteNameLabel, "allocation-site-name");

        Label siteDetailLabel = new Label(state.siteDetail());
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
        Label unitLabel = new Label("chiáº¿c");
        addStyleClass(unitLabel, "allocation-unit-label");
        HBox quantityBox = new HBox(8, quantityField, unitLabel);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        quantityBox.setMinWidth(170);
        quantityBox.setPrefWidth(170);
        return quantityBox;
    }

    private ComboBox<String> buildTransportBox(AllocationSiteRowState state) {
        ComboBox<String> transportBox = new ComboBox<>();
        transportBox.getItems().addAll(state.transportLabels());
        transportBox.setDisable(state.transportDisabled());
        transportBox.setValue(state.transportDisabled() ? state.transportLabels().get(0) : state.selectedTransportLabel());
        transportBox.setPrefWidth(180);
        transportBox.setMinWidth(180);
        addStyleClass(transportBox, "allocation-transport-box");
        return transportBox;
    }

    private Label buildEtaBadge(DeliveryStatus deliveryStatus) {
        Label etaBadge = new Label();
        etaBadge.setMinWidth(120);
        addStyleClass(etaBadge, "allocation-eta-badge");
        updateEtaBadge(etaBadge, deliveryStatus);
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
        ItemRequirement item,
        SiteStockOption site,
        TextField quantityField,
        ComboBox<String> transportBox,
        Label etaBadge,
        Label warningLabel,
        Runnable onUpdated
    ) {
        AllocationChangeResult result = onAllocationInputChanged.apply(new AllocationChangeRequest(
            item,
            site,
            quantityField.getText(),
            transportBox.getValue()
        ));
        updateEtaBadge(etaBadge, result.deliveryStatus());

        if (!result.applied()) {
            showWarning(warningLabel, warningMessage(result));
            return;
        }

        hideWarning(warningLabel);
        onUpdated.run();
    }

    private void updateEtaBadge(Label badge, DeliveryStatus deliveryStatus) {
        if (!deliveryStatus.available()) {
            badge.setText("KhÃ´ng kháº£ dá»¥ng");
            setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-unavailable");
            return;
        }

        if (deliveryStatus.dayDelta() > 0) {
            badge.setText("Sá»›m " + deliveryStatus.dayDelta() + " ngÃ y");
            setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-early");
        } else if (deliveryStatus.dayDelta() == 0) {
            badge.setText("Ká»‹p háº¡n");
            setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-on-time");
        } else {
            badge.setText("Trá»… " + Math.abs(deliveryStatus.dayDelta()) + " ngÃ y");
            setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-late");
        }
    }

    private String warningMessage(AllocationChangeResult result) {
        AllocationInputError error = result.error();
        return switch (error) {
            case EXCEEDS_STOCK -> "VÆ°á»£t tá»“n kho cá»§a site (" + result.stock() + ").";
            case NEGATIVE_QUANTITY -> "Sá»‘ lÆ°á»£ng khÃ´ng Ä‘Æ°á»£c Ã¢m.";
            case INVALID_INTEGER -> "Nháº­p sá»‘ nguyÃªn há»£p lá»‡.";
            case NONE -> "";
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

