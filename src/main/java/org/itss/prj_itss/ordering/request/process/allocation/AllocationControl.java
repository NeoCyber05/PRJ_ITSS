package org.itss.prj_itss.ordering.request.process.allocation;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.ordering.request.process.allocation.algo.AllSuggestAlgo;
import org.itss.prj_itss.ordering.request.process.allocation.algo.AllSuggestAlgo.AllocationDraft;
import org.itss.prj_itss.ordering.request.process.allocation.algo.AllSuggestAlgo.SuggestedPlan;
import org.itss.prj_itss.ordering.request.process.allocation.algo.ApplyPlan;
import org.itss.prj_itss.ordering.request.process.allocation.algo.OptimalSuggestAlgo;
import org.itss.prj_itss.ordering.request.process.ui.AllSuggestPopupView;
import org.itss.prj_itss.ordering.request.process.ui.AllocationItemEditorView;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.FRACTION_STATE_CLASSES;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.addStyleClass;
import static org.itss.prj_itss.ordering.request.process.ui.AllocationViewSupport.setStateClass;

public class AllocationControl {

    private static final int MAX_SUGGESTED_PLANS = 10;
    private static final int MAX_ITEM_VARIANTS = 12;

    private final List<ItemRequirement> items;
    private final List<SiteStockOption> allSites;
    private final Set<Integer> excludedSiteIds;
    private final Set<Integer> prioritySiteIds;
    private final Map<Integer, Map<Integer, Allocation>> allocations;
    private final int deadlineDays;
    private final ApplyPlan applyPlan;

    private Label[] allocFractionLabels;
    private Runnable onAllocationChanged;
    private Runnable onPlanApplied;

    public AllocationControl(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        Map<Integer, Map<Integer, Allocation>> allocations,
        int deadlineDays
    ) {
        this.items = items;
        this.allSites = allSites;
        this.excludedSiteIds = excludedSiteIds;
        this.prioritySiteIds = prioritySiteIds;
        this.allocations = allocations;
        this.deadlineDays = deadlineDays;
        this.applyPlan = new ApplyPlan(items, allocations);
    }

    public void setAllocFractionLabels(Label[] labels) {
        this.allocFractionLabels = labels;
    }

    public void setOnAllocationChanged(Runnable callback) {
        this.onAllocationChanged = callback;
    }

    public void setOnPlanApplied(Runnable callback) {
        this.onPlanApplied = callback;
    }

    public int getAllocated(int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Collections.emptyMap())
            .values()
            .stream()
            .mapToInt(Allocation::getQuantity)
            .sum();
    }

    public VBox buildInlineEditor(ItemRequirement item, int itemIndex) {
        return AllocationItemEditorView.load(
            item,
            itemIndex,
            allSites,
            excludedSiteIds,
            allocations,
            deadlineDays,
            this::onItemFractionChanged,
            this::notifyAllocationChanged
        );
    }

    public void applyOptimalAllocation() {
        OptimalSuggestAlgo optimalAlgo = new OptimalSuggestAlgo(allSites, excludedSiteIds, deadlineDays);
        applyPlan.apply(buildOptimalDrafts(optimalAlgo));
        refreshAfterPlanChange(false);
    }

    public void showAllAllocationsDialog() {
        List<SuggestedPlan> suggestedPlans = new AllSuggestAlgo(
            items,
            allSites,
            excludedSiteIds,
            prioritySiteIds,
            deadlineDays
        ).buildSuggestedPlans(MAX_SUGGESTED_PLANS, MAX_ITEM_VARIANTS);

        AllSuggestPopupView.show(suggestedPlans, plan -> {
            applyPlan.apply(plan.allocationsByItem());
            refreshAfterPlanChange(true);
        });
    }

    public void updateItemFractionLabel(ItemRequirement item, int index) {
        if (allocFractionLabels == null || index >= allocFractionLabels.length || allocFractionLabels[index] == null) {
            return;
        }

        int allocated = getAllocated(item.merchandiseId);
        String stateClass = "allocation-fraction-muted";
        if (allocated > item.required) {
            stateClass = "allocation-fraction-over";
        } else if (allocated == item.required) {
            stateClass = "allocation-fraction-complete";
        } else if (allocated > 0) {
            stateClass = "allocation-fraction-partial";
        }

        Label label = allocFractionLabels[index];
        label.setText(allocated + "/" + item.required);
        addStyleClass(label, "allocation-fraction-label");
        setStateClass(label, FRACTION_STATE_CLASSES, stateClass);
    }

    private void onItemFractionChanged(int index) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        updateItemFractionLabel(items.get(index), index);
    }

    private void refreshAfterPlanChange(boolean notifyPlanApplied) {
        for (int index = 0; index < items.size(); index++) {
            updateItemFractionLabel(items.get(index), index);
        }
        notifyAllocationChanged();
        if (notifyPlanApplied && onPlanApplied != null) {
            onPlanApplied.run();
        }
    }

    private Map<Integer, Map<Integer, AllocationDraft>> buildOptimalDrafts(OptimalSuggestAlgo optimalAlgo) {
        Map<Integer, Map<Integer, AllocationDraft>> draftsByItem = new LinkedHashMap<>();

        for (ItemRequirement item : items) {
            int remaining = item.required;
            Map<Integer, AllocationDraft> draftsBySite = new LinkedHashMap<>();

            for (SiteStockOption site : optimalAlgo.buildCandidateSites(item)) {
                if (remaining <= 0) {
                    break;
                }

                String transport = optimalAlgo.pickSuggestedTransport(site);
                if (transport == null) {
                    continue;
                }

                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                int quantity = Math.min(remaining, stock);
                if (quantity <= 0) {
                    continue;
                }

                draftsBySite.put(site.id, new AllocationDraft(site.id, item.merchandiseId, quantity, transport));
                remaining -= quantity;
            }

            draftsByItem.put(item.merchandiseId, draftsBySite);
        }

        return draftsByItem;
    }

    private void notifyAllocationChanged() {
        if (onAllocationChanged != null) {
            onAllocationChanged.run();
        }
    }
}
