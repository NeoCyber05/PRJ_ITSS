package org.itss.prj_itss.request.presentation.ordering.process.items;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.request.business.model.ItemRequirement;
import org.itss.prj_itss.request.business.model.SiteStockOption;
import org.itss.prj_itss.request.business.allocation.AllocationControl;
import org.itss.prj_itss.request.business.allocation.AllocationControl.AllocationChangeRequest;
import org.itss.prj_itss.request.business.allocation.AllocationControl.AllocationChangeResult;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    private ItemRequirement item;
    private int itemIndex;
    private List<SiteStockOption> allSites = List.of();
    private Set<Integer> excludedSiteIds = Set.of();
    private AllocationControl allocationControl;
    private Function<AllocationChangeRequest, AllocationChangeResult> onAllocationInputChanged;
    private IntConsumer onItemAllocationChanged = index -> {};

    public AllocationItemEditorView() {
    }

    public static VBox load(
        ItemRequirement item,
        int itemIndex,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        AllocationControl allocationControl,
        Function<AllocationChangeRequest, AllocationChangeResult> onAllocationInputChanged,
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
                allSites,
                excludedSiteIds,
                allocationControl,
                onAllocationInputChanged,
                onItemAllocationChanged
            );
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
        AllocationControl allocationControl,
        Function<AllocationChangeRequest, AllocationChangeResult> onAllocationInputChanged,
        IntConsumer onItemAllocationChanged
    ) {
        this.item = item;
        this.itemIndex = itemIndex;
        this.allSites = allSites == null ? List.of() : allSites;
        this.excludedSiteIds = excludedSiteIds == null ? Set.of() : excludedSiteIds;
        this.allocationControl = Objects.requireNonNull(allocationControl, "allocationControl");
        this.onAllocationInputChanged = Objects.requireNonNull(onAllocationInputChanged, "onAllocationInputChanged");
        this.onItemAllocationChanged = onItemAllocationChanged == null ? index -> {} : onItemAllocationChanged;

        titleLabel.setText(item.code + " - " + item.name);
        subtitleLabel.setText("CÃ³ máº·t hÃ ng táº¡i " + countAvailableSites() + " site");
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
        AllocationSiteRowView rowView = new AllocationSiteRowView(allocationControl, onAllocationInputChanged);

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
            Label emptyLabel = new Label("KhÃ´ng cÃ³ site kháº£ dá»¥ng cho máº·t hÃ ng nÃ y.");
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
            buildColumnHeader("Tá»’N KHO", 100),
            buildColumnHeader("SL PHÃ‚N Bá»”", 170),
            buildColumnHeader("Váº¬N CHUYá»‚N", 180),
            buildColumnHeader("TRáº NG THÃI", 120)
        );
        return header;
    }

    private void refreshSummaryBadges() {
        int allocated = allocationControl.getAllocated(item.merchandiseId);
        int remaining = item.required - allocated;

        allocatedBadgeLabel.setText("ÄÃ£ phÃ¢n bá»• " + allocated + "/" + item.required);
        addStyleClass(allocatedBadgeLabel, "allocation-summary-badge", "allocation-summary-allocated");

        addStyleClass(remainingBadgeLabel, "allocation-summary-badge");
        if (remaining > 0) {
            remainingBadgeLabel.setText("CÃ²n thiáº¿u " + remaining);
            setStateClass(remainingBadgeLabel, SUMMARY_STATE_CLASSES, "allocation-summary-short");
        } else if (remaining < 0) {
            remainingBadgeLabel.setText("VÆ°á»£t " + Math.abs(remaining));
            setStateClass(remainingBadgeLabel, SUMMARY_STATE_CLASSES, "allocation-summary-over");
        } else {
            remainingBadgeLabel.setText("ÄÃ£ Ä‘á»§");
            setStateClass(remainingBadgeLabel, SUMMARY_STATE_CLASSES, "allocation-summary-complete");
        }
    }

    private int countAvailableSites() {
        return (int) allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
            .count();
    }

}

