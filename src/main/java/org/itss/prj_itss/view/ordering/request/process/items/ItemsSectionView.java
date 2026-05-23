package org.itss.prj_itss.view.ordering.request.process.items;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.model.request.application.processing.AllocationChangeCommand;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeResultView;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;

public final class ItemsSectionView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/items/items-section-view.fxml";

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

    private final List<ItemsSectionItemRowView> itemRowViews = new ArrayList<>();

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
        for (int index = 0; index < itemRowViews.size() && index < viewModel.allocationItems().size(); index++) {
            itemRowViews.get(index).refresh(viewModel.allocationItems().get(index));
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
        itemsContainer.getChildren().clear();
        itemRowViews.clear();

        List<RequestProcessingViewModel.AllocationItemViewModel> items = viewModel.allocationItems();
        for (int index = 0; index < items.size(); index++) {
            RequestProcessingViewModel.AllocationItemViewModel item = items.get(index);

            VBox block = new VBox(0);
            ItemsSectionItemRowView rowView = ItemsSectionItemRowView.load(
                item,
                index,
                viewModel.earliestDeliveryDate(),
                onToggleExpandedItem
            );
            itemRowViews.add(rowView);
            block.getChildren().add(rowView.root());

            if (item.expanded()) {
                block.getChildren().add(AllocationItemEditorView.load(
                    item,
                    index,
                    item.siteRows(),
                    onAllocationInputChanged,
                    this::handleItemAllocationChanged
                ));
            }
            itemsContainer.getChildren().add(block);
        }
    }

    private void handleItemAllocationChanged(int index) {
        if (index >= 0 && index < itemRowViews.size() && index < viewModel.allocationItems().size()) {
            itemRowViews.get(index).refresh(viewModel.allocationItems().get(index));
        }
    }
}
