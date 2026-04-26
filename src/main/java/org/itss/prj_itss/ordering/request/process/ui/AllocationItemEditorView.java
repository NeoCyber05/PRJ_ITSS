package org.itss.prj_itss.ordering.request.process.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntConsumer;

import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.SUMMARY_STATE_CLASSES;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.buildColumnHeader;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.setStateClass;

public final class AllocationItemEditorView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/ui/allocation-item-editor-view.fxml";

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

    private ItemRequirement item;
    private int itemIndex;
    private List<SiteStockOption> allSites = List.of();
    private Set<Integer> excludedSiteIds = Set.of();
    private Map<Integer, Map<Integer, Allocation>> allocations = Map.of();
    private IntConsumer onItemFractionChanged = index -> {};
    private Runnable onAllocationChanged = () -> {};
    private AllocationSiteRowView rowView;

    public AllocationItemEditorView() {
    }

    public static VBox load(
        ItemRequirement item,
        int itemIndex,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Map<Integer, Map<Integer, Allocation>> allocations,
        int deadlineDays,
        IntConsumer onItemFractionChanged,
        Runnable onAllocationChanged
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                AllocationItemEditorView.class.getResource(VIEW_RESOURCE),
                "Missing allocation item editor FXML"
            ));
            Parent root = loader.load();
            AllocationItemEditorView controller = loader.getController();
            controller.init(item, itemIndex, allSites, excludedSiteIds, allocations, deadlineDays, onItemFractionChanged, onAllocationChanged);
            return (VBox) root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load allocation item editor view", exception);
        }
    }

    private void init(
        ItemRequirement item,
        int itemIndex,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Map<Integer, Map<Integer, Allocation>> allocations,
        int deadlineDays,
        IntConsumer onItemFractionChanged,
        Runnable onAllocationChanged
    ) {
        this.item = item;
        this.itemIndex = itemIndex;
        this.allSites = allSites == null ? List.of() : allSites;
        this.excludedSiteIds = excludedSiteIds == null ? Set.of() : excludedSiteIds;
        this.allocations = allocations == null ? Map.of() : allocations;
        this.onItemFractionChanged = onItemFractionChanged == null ? index -> {} : onItemFractionChanged;
        this.onAllocationChanged = onAllocationChanged == null ? () -> {} : onAllocationChanged;
        this.rowView = new AllocationSiteRowView(this.allocations, deadlineDays);

        titleLabel.setText(item.code + " - " + item.name);
        subtitleLabel.setText("Có mặt hàng tại " + countAvailableSites() + " site");
        refreshSummaryBadges();
        renderSiteTable();
    }

    private void renderSiteTable() {
        siteTableBox.getChildren().clear();
        siteTableBox.getChildren().add(buildTableHeader());

        Runnable onRowChanged = () -> {
            onItemFractionChanged.accept(itemIndex);
            refreshSummaryBadges();
            onAllocationChanged.run();
        };

        boolean hasRows = false;
        for (SiteStockOption site : allSites) {
            if (excludedSiteIds.contains(site.id)) {
                continue;
            }
            if (site.stock.getOrDefault(item.merchandiseId, 0) <= 0) {
                continue;
            }
            siteTableBox.getChildren().add(rowView.build(item, site, onRowChanged));
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
        int allocated = totalAllocated();
        int remaining = item.required - allocated;

        allocatedBadgeLabel.setText("Đã phân bổ " + allocated + "/" + item.required);
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
        return (int) allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
            .count();
    }

    private int totalAllocated() {
        return allocations.getOrDefault(item.merchandiseId, Collections.emptyMap())
            .values()
            .stream()
            .mapToInt(Allocation::getQuantity)
            .sum();
    }
}
