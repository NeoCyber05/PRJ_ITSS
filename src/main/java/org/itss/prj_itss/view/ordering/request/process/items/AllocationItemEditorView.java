package org.itss.prj_itss.view.ordering.request.process.items;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.controller.ordering.request.process.state.AllocationChangeCommand;
import org.itss.prj_itss.controller.ordering.request.process.state.AllocationChangeResult;
import org.itss.prj_itss.controller.ordering.request.process.state.RequestProcessingState;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;

import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.SUMMARY_STATE_CLASSES;
import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.setStateClass;

public final class AllocationItemEditorView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/process/items/allocation-item-editor-view.fxml";

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label allocatedBadgeLabel;

    @FXML
    private Label remainingBadgeLabel;

    @FXML
    private VBox siteRowsBox;

    @FXML
    private Label emptyLabel;

    private RequestProcessingState.AllocationItemState item;
    private int itemIndex;
    private List<RequestProcessingState.AllocationSiteRowState> siteRows;
    private Function<AllocationChangeCommand, AllocationChangeResult> onAllocationInputChanged;
    private IntConsumer onItemAllocationChanged = index -> {};

    public AllocationItemEditorView() {
    }

    public static VBox load(
        RequestProcessingState.AllocationItemState item,
        int itemIndex,
        List<RequestProcessingState.AllocationSiteRowState> siteRows,
        Function<AllocationChangeCommand, AllocationChangeResult> onAllocationInputChanged,
        IntConsumer onItemAllocationChanged
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                AllocationItemEditorView.class.getResource(VIEW_RESOURCE),
                "Missing allocation item editor FXML"
            ));
            Parent root = loader.load();
            AllocationItemEditorView controller = loader.getController();
            controller.init(
                item,
                itemIndex,
                siteRows,
                onAllocationInputChanged,
                onItemAllocationChanged
            );
            return (VBox) root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load allocation item editor view", exception);
        }
    }

    private void init(
        RequestProcessingState.AllocationItemState item,
        int itemIndex,
        List<RequestProcessingState.AllocationSiteRowState> siteRows,
        Function<AllocationChangeCommand, AllocationChangeResult> onAllocationInputChanged,
        IntConsumer onItemAllocationChanged
    ) {
        this.item = item;
        this.itemIndex = itemIndex;
        this.siteRows = siteRows == null ? List.of() : siteRows;
        this.onAllocationInputChanged = Objects.requireNonNull(onAllocationInputChanged, "onAllocationInputChanged");
        this.onItemAllocationChanged = onItemAllocationChanged == null ? index -> {} : onItemAllocationChanged;

        titleLabel.setText(item.code() + " - " + item.name());
        subtitleLabel.setText("Có mặt hàng tại " + countAvailableSites() + " site");
        refreshSummaryBadges();
        renderSiteRows();
    }

    private void renderSiteRows() {
        siteRowsBox.getChildren().clear();

        Runnable onRowChanged = () -> {
            refreshSummaryBadges();
            onItemAllocationChanged.accept(itemIndex);
        };

        boolean hasRows = false;
        for (RequestProcessingState.AllocationSiteRowState siteRow : siteRows) {
            if (siteRow.stock() <= 0) {
                continue;
            }
            siteRowsBox.getChildren().add(AllocationSiteRowView.load(siteRow, onAllocationInputChanged, onRowChanged));
            hasRows = true;
        }

        emptyLabel.setVisible(!hasRows);
        emptyLabel.setManaged(!hasRows);
    }

    private void refreshSummaryBadges() {
        int allocated = item.allocated();
        int remaining = item.required() - allocated;

        allocatedBadgeLabel.setText("Đã phân bổ " + allocated + "/" + item.required());
        addStyleClass(allocatedBadgeLabel, "allocation-summary-badge", "allocation-summary-allocated");

        addStyleClass(remainingBadgeLabel, "allocation-summary-badge");
        if (remaining > 0) {
            remainingBadgeLabel.setText("Còn thiếu " + remaining);
            setStateClass(remainingBadgeLabel, SUMMARY_STATE_CLASSES, "allocation-summary-short");
        } else if (remaining < 0) {
            remainingBadgeLabel.setText("Vượt " + Math.abs(remaining));
            setStateClass(remainingBadgeLabel, SUMMARY_STATE_CLASSES, "allocation-summary-over");
        } else {
            remainingBadgeLabel.setText("Đã đủ");
            setStateClass(remainingBadgeLabel, SUMMARY_STATE_CLASSES, "allocation-summary-complete");
        }
    }

    private int countAvailableSites() {
        return (int) siteRows.stream()
            .filter(siteRow -> siteRow.stock() > 0)
            .count();
    }
}
