package org.itss.prj_itss.view.ordering.request.process.items;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import org.itss.prj_itss.controller.ordering.request.process.state.RequestProcessingState;

import java.io.IOException;
import java.util.Objects;
import java.util.function.IntConsumer;

import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.FRACTION_STATE_CLASSES;
import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.setStateClass;

public final class ItemsSectionItemRowView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/process/items/items-section-item-row-view.fxml";

    @FXML
    private Label codeLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label requiredLabel;
    @FXML
    private Label deadlineLabel;
    @FXML
    private Label allocationStatusLabel;
    @FXML
    private Label allocationFractionLabel;
    @FXML
    private Label stockValueLabel;
    @FXML
    private Button toggleButton;

    private HBox root;
    private int itemIndex;
    private IntConsumer onToggle = index -> {};

    public static ItemsSectionItemRowView load(
        RequestProcessingState.AllocationItemState item,
        int itemIndex,
        String earliestDeliveryDate,
        IntConsumer onToggle
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                ItemsSectionItemRowView.class.getResource(VIEW_RESOURCE),
                "Missing items section item row FXML"
            ));
            HBox root = loader.load();
            ItemsSectionItemRowView view = loader.getController();
            view.root = root;
            view.init(item, itemIndex, earliestDeliveryDate, onToggle);
            return view;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load items section item row view", exception);
        }
    }

    public HBox root() {
        return root;
    }

    public void refresh(RequestProcessingState.AllocationItemState item) {
        updateAllocationLabels(item);
    }

    @FXML
    private void handleToggle() {
        onToggle.accept(itemIndex);
    }

    private void init(
        RequestProcessingState.AllocationItemState item,
        int itemIndex,
        String earliestDeliveryDate,
        IntConsumer onToggle
    ) {
        this.itemIndex = itemIndex;
        this.onToggle = onToggle == null ? index -> {} : onToggle;

        boolean expanded = item.expanded();

        codeLabel.setText(item.code());
        nameLabel.setText(item.name());
        requiredLabel.setText(item.required() + " chiếc");
        deadlineLabel.setText(
            earliestDeliveryDate != null && !earliestDeliveryDate.isBlank() ? earliestDeliveryDate : "N/A"
        );
        stockValueLabel.setText(String.valueOf(item.totalStock()));
        toggleButton.setText(expanded ? "Ẩn tồn kho" : "Hiện tồn kho");
        toggleButton.getStyleClass().removeAll("forest-dark-button", "forest-outline-button");
        toggleButton.getStyleClass().add(expanded ? "forest-dark-button" : "forest-outline-button");

        updateAllocationLabels(item);
    }

    private void updateAllocationLabels(RequestProcessingState.AllocationItemState item) {
        allocationStatusLabel.setText(item.allocationStatusText());
        String stateClass = switch (item.allocationStatusText()) {
            case "Vượt mức" -> "allocation-fraction-over";
            case "Đủ" -> "allocation-fraction-complete";
            case "Chưa đủ" -> "allocation-fraction-partial";
            default -> "allocation-fraction-muted";
        };
        allocationFractionLabel.setText(item.allocationFractionText());
        addStyleClass(allocationFractionLabel, "allocation-fraction-label");
        setStateClass(allocationFractionLabel, FRACTION_STATE_CLASSES, stateClass);
    }
}
