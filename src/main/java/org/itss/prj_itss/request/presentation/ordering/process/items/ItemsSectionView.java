package org.itss.prj_itss.request.presentation.ordering.process.items;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.request.business.allocation.AllocationControl;
import org.itss.prj_itss.request.business.allocation.AllocationControl.AllocationChangeRequest;
import org.itss.prj_itss.request.business.allocation.AllocationControl.AllocationChangeResult;
import org.itss.prj_itss.request.business.model.ItemRequirement;
import org.itss.prj_itss.request.business.model.SiteStockOption;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntConsumer;

import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.FRACTION_STATE_CLASSES;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.applyItemAllocationState;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.setStateClass;

public final class ItemsSectionView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/request/presentation/ordering/process/items/items-section-view.fxml";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private Button optimizeButton;

    @FXML
    private Button showAllButton;

    @FXML
    private VBox itemsContainer;

    private VBox root;

    private List<ItemRequirement> items = List.of();
    private List<SiteStockOption> allSites = List.of();
    private Set<Integer> excludedSiteIds = Set.of();
    private AllocationControl allocationControl;
    private LocalDate earliestDeliveryDate;
    private int expandedItemIndex = -1;
    private Runnable onOptimizeRequested = () -> {};
    private Runnable onShowAllPlansRequested = () -> {};
    private IntConsumer onToggleExpandedItem = index -> {};
    private Function<AllocationChangeRequest, AllocationChangeResult> onAllocationInputChanged = request -> null;

    private Label[] allocationStatusLabels = new Label[0];
    private Label[] allocationFractionLabels = new Label[0];

    public static ItemsSectionView load(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        AllocationControl allocationControl,
        LocalDate earliestDeliveryDate,
        int expandedItemIndex,
        Runnable onOptimizeRequested,
        Runnable onShowAllPlansRequested,
        IntConsumer onToggleExpandedItem,
        Function<AllocationChangeRequest, AllocationChangeResult> onAllocationInputChanged
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                ItemsSectionView.class.getResource(VIEW_RESOURCE),
                "Missing items section FXML"
            ));
            VBox root = loader.load();
            ItemsSectionView view = loader.getController();
            view.root = root;
            view.init(
                items,
                allSites,
                excludedSiteIds,
                allocationControl,
                earliestDeliveryDate,
                expandedItemIndex,
                onOptimizeRequested,
                onShowAllPlansRequested,
                onToggleExpandedItem,
                onAllocationInputChanged
            );
            return view;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load items section view", exception);
        }
    }

    public VBox root() {
        return root;
    }

    public void refreshAllocationLabels() {
        for (int index = 0; index < items.size(); index++) {
            updateAllocationLabels(items.get(index), index);
        }
    }

    @FXML
    private void handleOptimize() {
        onOptimizeRequested.run();
    }

    @FXML
    private void handleShowAllPlans() {
        onShowAllPlansRequested.run();
    }

    private void init(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        AllocationControl allocationControl,
        LocalDate earliestDeliveryDate,
        int expandedItemIndex,
        Runnable onOptimizeRequested,
        Runnable onShowAllPlansRequested,
        IntConsumer onToggleExpandedItem,
        Function<AllocationChangeRequest, AllocationChangeResult> onAllocationInputChanged
    ) {
        this.items = items == null ? List.of() : items;
        this.allSites = allSites == null ? List.of() : allSites;
        this.excludedSiteIds = excludedSiteIds == null ? Set.of() : excludedSiteIds;
        this.allocationControl = Objects.requireNonNull(allocationControl, "allocationControl");
        this.earliestDeliveryDate = earliestDeliveryDate;
        this.expandedItemIndex = expandedItemIndex;
        this.onOptimizeRequested = onOptimizeRequested == null ? () -> {} : onOptimizeRequested;
        this.onShowAllPlansRequested = onShowAllPlansRequested == null ? () -> {} : onShowAllPlansRequested;
        this.onToggleExpandedItem = onToggleExpandedItem == null ? index -> {} : onToggleExpandedItem;
        this.onAllocationInputChanged = Objects.requireNonNull(onAllocationInputChanged, "onAllocationInputChanged");

        renderItems();
    }

    private void renderItems() {
        allocationStatusLabels = new Label[items.size()];
        allocationFractionLabels = new Label[items.size()];

        itemsContainer.getChildren().clear();
        for (int index = 0; index < items.size(); index++) {
            itemsContainer.getChildren().add(buildItemBlock(items.get(index), index));
        }
        refreshAllocationLabels();
    }

    private VBox buildItemBlock(ItemRequirement item, int index) {
        VBox block = new VBox(0);
        block.getChildren().add(buildItemRow(item, index));
        if (expandedItemIndex == index) {
            block.getChildren().add(AllocationItemEditorView.load(
                item,
                index,
                allSites,
                excludedSiteIds,
                allocationControl,
                onAllocationInputChanged,
                this::handleItemAllocationChanged
            ));
        }
        return block;
    }

    private HBox buildItemRow(ItemRequirement item, int index) {
        boolean expanded = expandedItemIndex == index;

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle(
            "-fx-border-color:transparent transparent #F0F4F2 transparent;"
                + "-fx-border-width:0 0 1 0;"
        );

        VBox codeColumn = new VBox(4);
        codeColumn.setMinWidth(200);
        Label codeLabel = new Label(item.code);
        codeLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        Label nameLabel = new Label(item.name);
        nameLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#6B7C72;");
        codeColumn.getChildren().addAll(codeLabel, nameLabel);

        Label requiredLabel = new Label(item.required + " chiáº¿c");
        requiredLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;");
        requiredLabel.setMinWidth(170);

        Label deadlineLabel = new Label(earliestDeliveryDate != null ? earliestDeliveryDate.format(DATE_FORMAT) : "N/A");
        deadlineLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;");
        deadlineLabel.setMinWidth(150);

        VBox allocationColumn = new VBox(4);
        allocationColumn.setMinWidth(180);
        Label allocationStatusLabel = new Label();
        Label allocationFractionLabel = new Label();
        allocationStatusLabels[index] = allocationStatusLabel;
        allocationFractionLabels[index] = allocationFractionLabel;
        allocationColumn.getChildren().addAll(allocationStatusLabel, allocationFractionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int totalStock = allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .mapToInt(site -> site.stock.getOrDefault(item.merchandiseId, 0))
            .sum();

        VBox stockColumn = new VBox(10);
        stockColumn.setAlignment(Pos.CENTER_RIGHT);
        stockColumn.setMinWidth(160);
        Label stockValueLabel = new Label(String.valueOf(totalStock));
        stockValueLabel.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");

        Button toggleButton = new Button(expanded ? "áº¨n tá»“n kho" : "Hiá»‡n tá»“n kho");
        toggleButton.getStyleClass().add(expanded ? "forest-dark-button" : "forest-outline-button");
        toggleButton.setOnAction(event -> onToggleExpandedItem.accept(index));
        stockColumn.getChildren().addAll(stockValueLabel, toggleButton);

        row.getChildren().addAll(codeColumn, requiredLabel, deadlineLabel, allocationColumn, spacer, stockColumn);
        updateAllocationLabels(item, index);
        return row;
    }

    private void updateAllocationLabels(ItemRequirement item, int index) {
        if (index >= allocationStatusLabels.length || index >= allocationFractionLabels.length) {
            return;
        }

        Label stateLabel = allocationStatusLabels[index];
        Label fractionLabel = allocationFractionLabels[index];
        if (stateLabel == null || fractionLabel == null) {
            return;
        }

        AllocationControl.ItemAllocationSummary summary = allocationControl.allocationSummary(item);
        applyItemAllocationState(stateLabel, summary.state());
        updateFractionLabel(fractionLabel, summary);
    }

    private void handleItemAllocationChanged(int index) {
        if (index >= 0 && index < items.size()) {
            updateAllocationLabels(items.get(index), index);
        }
    }

    private void updateFractionLabel(Label label, AllocationControl.ItemAllocationSummary summary) {
        String stateClass = switch (summary.state()) {
            case OVER -> "allocation-fraction-over";
            case COMPLETE -> "allocation-fraction-complete";
            case PARTIAL -> "allocation-fraction-partial";
            case NONE -> "allocation-fraction-muted";
        };
        label.setText(summary.allocated() + "/" + summary.required());
        addStyleClass(label, "allocation-fraction-label");
        setStateClass(label, FRACTION_STATE_CLASSES, stateClass);
    }
}

