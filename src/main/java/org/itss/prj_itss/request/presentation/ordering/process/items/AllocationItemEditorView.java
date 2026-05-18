package org.itss.prj_itss.request.presentation.ordering.process.items;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.request.application.processing.AllocationChangeCommand;
import org.itss.prj_itss.request.application.processing.AllocationChangeResultView;
import org.itss.prj_itss.request.application.processing.RequestProcessingViewModel;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;

import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.SUMMARY_STATE_CLASSES;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.buildColumnHeader;
import static org.itss.prj_itss.request.presentation.ordering.process.shared.AllocationViewSupport.setStateClass;

public final class AllocationItemEditorView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/request/presentation/ordering/process/items/allocation-item-editor-view.fxml";

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label allocatedBadgeLabel;

    @FXML
    private Label remainingBadgeLabel;

    @FXML
    private VBox siteTableBox;

    private RequestProcessingViewModel.AllocationItemViewModel item;
    private int itemIndex;
    private List<RequestProcessingViewModel.AllocationSiteRowViewModel> siteRows;
    private Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged;
    private IntConsumer onItemAllocationChanged = index -> {};

    public AllocationItemEditorView() {
    }

    public static VBox load(
        RequestProcessingViewModel.AllocationItemViewModel item,
        int itemIndex,
        List<RequestProcessingViewModel.AllocationSiteRowViewModel> siteRows,
        Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged,
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
        RequestProcessingViewModel.AllocationItemViewModel item,
        int itemIndex,
        List<RequestProcessingViewModel.AllocationSiteRowViewModel> siteRows,
        Function<AllocationChangeCommand, AllocationChangeResultView> onAllocationInputChanged,
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
        renderSiteTable();
    }

    private void renderSiteTable() {
        siteTableBox.getChildren().clear();
        siteTableBox.getChildren().add(buildTableHeader());

        Runnable onRowChanged = () -> {
            refreshSummaryBadges();
            onItemAllocationChanged.accept(itemIndex);
        };
        AllocationSiteRowView rowView = new AllocationSiteRowView(onAllocationInputChanged, onRowChanged);

        boolean hasRows = false;
        for (RequestProcessingViewModel.AllocationSiteRowViewModel siteRow : siteRows) {
            if (siteRow.stock() <= 0) {
                continue;
            }
            siteTableBox.getChildren().add(rowView.build(siteRow));
            hasRows = true;
        }

        if (!hasRows) {
            Label emptyLabel = new Label("Không có site khả dụng cho mặt hàng này.");
            addStyleClass(emptyLabel, "allocation-empty-label");
            siteTableBox.getChildren().add(emptyLabel);
        }
    }

    private HBox buildTableHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(12, 16, 12, 16));
        addStyleClass(header, "allocation-table-header");
        header.getChildren().addAll(
            buildColumnHeader("SITE", 380),
            buildColumnHeader("TỒN KHO", 100),
            buildColumnHeader("SL PHÂN BỔ", 170),
            buildColumnHeader("VẬN CHUYỂN", 180),
            buildColumnHeader("TRẠNG THÁI", 120)
        );
        return header;
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
