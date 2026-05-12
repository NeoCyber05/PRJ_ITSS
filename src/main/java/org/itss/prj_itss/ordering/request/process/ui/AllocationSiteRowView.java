package org.itss.prj_itss.ordering.request.process.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.model.DeliveryMethod;
import org.itss.prj_itss.ordering.request.process.model.DeliveryOptions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.ETA_STATE_CLASSES;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.setStateClass;

final class AllocationSiteRowView {

    private final Map<Integer, Map<Integer, Allocation>> allocations;
    private final int deadlineDays;

    AllocationSiteRowView(Map<Integer, Map<Integer, Allocation>> allocations, int deadlineDays) {
        this.allocations = allocations;
        this.deadlineDays = deadlineDays;
    }

    VBox build(ItemRequirement item, SiteStockOption site, Runnable onChanged) {
        VBox rowBox = new VBox(4);
        rowBox.setPadding(new Insets(0, 0, 0, 0));
        addStyleClass(rowBox, "allocation-table-row");

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));

        VBox siteBox = buildSiteInfoBox(site);
        Label stockLabel = buildStockLabel(site.stock.getOrDefault(item.merchandiseId, 0));

        Allocation existing = allocations.getOrDefault(item.merchandiseId, Collections.emptyMap()).get(site.id);
        String selectedTransport = DeliveryOptions.resolveStorageValue(
            site,
            existing == null ? null : existing.transport,
            deadlineDays
        );

        TextField quantityField = buildQuantityField(existing);
        HBox quantityBox = wrapQuantityField(quantityField);
        ComboBox<String> transportBox = buildTransportBox(site, selectedTransport);
        Label etaBadge = buildEtaBadge(site, selectedTransport);
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

    private VBox buildSiteInfoBox(SiteStockOption site) {
        VBox siteBox = new VBox(4);
        siteBox.setMinWidth(380);
        siteBox.setPrefWidth(380);

        Label siteNameLabel = new Label(site.name);
        addStyleClass(siteNameLabel, "allocation-site-name");

        String detailText = site.siteCode;
        if (site.description != null && !site.description.isBlank()) {
            detailText += " - " + site.description;
        }
        Label siteDetailLabel = new Label(detailText);
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

    private TextField buildQuantityField(Allocation existing) {
        TextField quantityField = new TextField();
        quantityField.setPrefWidth(110);
        quantityField.setText(existing == null || existing.getQuantity() == 0 ? "0" : String.valueOf(existing.getQuantity()));
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

    private ComboBox<String> buildTransportBox(SiteStockOption site, String selectedTransport) {
        ComboBox<String> transportBox = new ComboBox<>();
        if (site.shipDays < 999) {
            transportBox.getItems().add(DeliveryMethod.SHIP.displayLabel());
        }
        if (site.airDays < 999) {
            transportBox.getItems().add(DeliveryMethod.AIR.displayLabel());
        }
        if (transportBox.getItems().isEmpty()) {
            transportBox.getItems().add("Không khả dụng");
            transportBox.setDisable(true);
        } else {
            transportBox.setValue(DeliveryMethod.displayLabelOf(selectedTransport));
        }
        transportBox.setPrefWidth(180);
        transportBox.setMinWidth(180);
        addStyleClass(transportBox, "allocation-transport-box");
        return transportBox;
    }

    private Label buildEtaBadge(SiteStockOption site, String selectedTransport) {
        Label etaBadge = new Label();
        etaBadge.setMinWidth(120);
        addStyleClass(etaBadge, "allocation-eta-badge");
        updateEtaBadge(etaBadge, site, selectedTransport);
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
        Integer quantity = parseQuantity(quantityField.getText(), warningLabel);
        if (quantity == null) {
            return;
        }

        int stock = site.stock.getOrDefault(item.merchandiseId, 0);
        if (quantity > stock) {
            showWarning(warningLabel, "Vượt tồn kho của site (" + stock + ").");
            return;
        }

        hideWarning(warningLabel);

        String transport = DeliveryOptions.resolveStorageValue(site, transportBox.getValue(), deadlineDays);
        updateEtaBadge(etaBadge, site, transport);
        updateAllocationsState(item, site, quantity, transport);

        onUpdated.run();
    }

    private Integer parseQuantity(String rawText, Label warningLabel) {
        String rawValue = rawText == null ? "" : rawText.trim();
        try {
            int parsed = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);
            if (parsed < 0) {
                showWarning(warningLabel, "Số lượng không được âm.");
                return null;
            }
            return parsed;
        } catch (NumberFormatException exception) {
            showWarning(warningLabel, "Nhập số nguyên hợp lệ.");
            return null;
        }
    }

    private void updateAllocationsState(ItemRequirement item, SiteStockOption site, int quantity, String transport) {
        if (quantity > 0) {
            Allocation allocation = allocations
                .computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>())
                .computeIfAbsent(site.id, key -> new Allocation(site.id, item.merchandiseId, 0, transport));
            allocation.setQuantity(quantity);
            allocation.transport = transport;
        } else {
            Map<Integer, Allocation> itemAllocations = allocations.get(item.merchandiseId);
            if (itemAllocations != null) {
                itemAllocations.remove(site.id);
            }
        }
    }

    private void updateEtaBadge(Label badge, SiteStockOption site, String transport) {
        int deliveryDays = DeliveryOptions.deliveryDays(
            site,
            DeliveryOptions.resolve(site, transport, deadlineDays)
        );
        if (deliveryDays >= 999) {
            badge.setText("Không khả dụng");
            setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-unavailable");
            return;
        }

        int delta = deadlineDays - deliveryDays;
        if (delta > 0) {
            badge.setText("Sớm " + delta + " ngày");
            setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-early");
        } else if (delta == 0) {
            badge.setText("Kịp hạn");
            setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-on-time");
        } else {
            badge.setText("Trễ " + Math.abs(delta) + " ngày");
            setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-late");
        }
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
