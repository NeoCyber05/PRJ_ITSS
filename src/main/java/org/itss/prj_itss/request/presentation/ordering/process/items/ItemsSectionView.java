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

import org.itss.prj_itss.request.application.processing.AllocationChangeCommand;
import org.itss.prj_itss.request.application.processing.AllocationChangeResultView;
import org.itss.prj_itss.request.application.processing.RequestProcessingViewModel;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;

import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.FRACTION_STATE_CLASSES;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.setStateClass;

public final class ItemsSectionView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/request/presentation/ordering/process/items/items-section-view.fxml";

    @FXML
    private Button optimizeButton;

    @FXML
    private Button showAllButton;

    @FXML
    private VBox itemsContainer;

    private VBox root;

    private RequestProcessingViewModel viewModel;
    private Runnable onOptimizeRequested = () -> {};
    private Runnable onShowAllPlansRequested = () -> {};
    private IntConsumer onToggleExpandedItem = index -> {};
    private Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged = request -> null;

    private Label[] allocationStatusLabels = new Label[0];
    private Label[] allocationFractionLabels = new Label[0];

    public static ItemsSectionView load(
        RequestProcessingViewModel viewModel,
        Runnable onOptimizeRequested,
        Runnable onShowAllPlansRequested,
        IntConsumer onToggleExpandedItem,
        Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged
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
                viewModel,
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
        for (int index = 0; index < viewModel.allocationItems().size(); index++) {
            updateAllocationLabels(viewModel.allocationItems().get(index), index);
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
        RequestProcessingViewModel viewModel,
        Runnable onOptimizeRequested,
        Runnable onShowAllPlansRequested,
        IntConsumer onToggleExpandedItem,
        Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged
    ) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
        this.onOptimizeRequested = onOptimizeRequested == null ? () -> {} : onOptimizeRequested;
        this.onShowAllPlansRequested = onShowAllPlansRequested == null ? () -> {} : onShowAllPlansRequested;
        this.onToggleExpandedItem = onToggleExpandedItem == null ? index -> {} : onToggleExpandedItem;
        this.onAllocationInputChanged = Objects.requireNonNull(onAllocationInputChanged, "onAllocationInputChanged");

        renderItems();
    }

    private void renderItems() {
        allocationStatusLabels = new Label[viewModel.allocationItems().size()];
        allocationFractionLabels = new Label[viewModel.allocationItems().size()];

        itemsContainer.getChildren().clear();
        for (int index = 0; index < viewModel.allocationItems().size(); index++) {
            itemsContainer.getChildren().add(buildItemBlock(viewModel.allocationItems().get(index), index));
        }
        refreshAllocationLabels();
    }

    private VBox buildItemBlock(RequestProcessingViewModel.AllocationItemViewModel item, int index) {
        VBox block = new VBox(0);
        block.getChildren().add(buildItemRow(item, index));
        if (item.expanded()) {
            block.getChildren().add(AllocationItemEditorView.load(
                item,
                index,
                item.siteRows(),
                onAllocationInputChanged,
                this::handleItemAllocationChanged
            ));
        }
        return block;
    }

    private HBox buildItemRow(RequestProcessingViewModel.AllocationItemViewModel item, int index) {
        boolean expanded = item.expanded();

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle(
            "-fx-border-color:transparent transparent #F0F4F2 transparent;"
                + "-fx-border-width:0 0 1 0;"
        );

        VBox codeColumn = new VBox(4);
        codeColumn.setMinWidth(200);
        Label codeLabel = new Label(item.code());
        codeLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        Label nameLabel = new Label(item.name());
        nameLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#6B7C72;");
        codeColumn.getChildren().addAll(codeLabel, nameLabel);

        Label requiredLabel = new Label(item.required() + " chiếc");
        requiredLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;");
        requiredLabel.setMinWidth(170);

        Label deadlineLabel = new Label(viewModel.earliestDeliveryDate() != null && !viewModel.earliestDeliveryDate().isBlank() ? viewModel.earliestDeliveryDate() : "N/A");
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

        VBox stockColumn = new VBox(10);
        stockColumn.setAlignment(Pos.CENTER_RIGHT);
        stockColumn.setMinWidth(160);
        Label stockValueLabel = new Label(String.valueOf(item.totalStock()));
        stockValueLabel.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");

        Button toggleButton = new Button(expanded ? "Ẩn tồn kho" : "Hiện tồn kho");
        toggleButton.getStyleClass().add(expanded ? "forest-dark-button" : "forest-outline-button");
        toggleButton.setOnAction(event -> onToggleExpandedItem.accept(index));
        stockColumn.getChildren().addAll(stockValueLabel, toggleButton);

        row.getChildren().addAll(codeColumn, requiredLabel, deadlineLabel, allocationColumn, spacer, stockColumn);
        updateAllocationLabels(item, index);
        return row;
    }

    private void updateAllocationLabels(RequestProcessingViewModel.AllocationItemViewModel item, int index) {
        if (index >= allocationStatusLabels.length || index >= allocationFractionLabels.length) {
            return;
        }

        Label stateLabel = allocationStatusLabels[index];
        Label fractionLabel = allocationFractionLabels[index];
        if (stateLabel == null || fractionLabel == null) {
            return;
        }

        stateLabel.setText(item.allocationStatusText());
        String stateClass = switch (item.allocationStatusText()) {
            case "Vượt mức" -> "allocation-fraction-over";
            case "Đủ" -> "allocation-fraction-complete";
            case "Chưa đủ" -> "allocation-fraction-partial";
            default -> "allocation-fraction-muted";
        };
        fractionLabel.setText(item.allocationFractionText());
        addStyleClass(fractionLabel, "allocation-fraction-label");
        setStateClass(fractionLabel, FRACTION_STATE_CLASSES, stateClass);
    }

    private void handleItemAllocationChanged(int index) {
        if (index >= 0 && index < viewModel.allocationItems().size()) {
            updateAllocationLabels(viewModel.allocationItems().get(index), index);
        }
    }
}
