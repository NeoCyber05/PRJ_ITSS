package org.itss.prj_itss.request.processing;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.request.processing.AllocationPlanner.AllocationDraft;
import org.itss.prj_itss.request.processing.AllocationPlanner.SuggestedPlan;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AllocationPlanMutator {

    private final List<ItemRequirement> items;
    private final Map<Integer, Map<Integer, Allocation>> allocations;

    AllocationPlanMutator(List<ItemRequirement> items, Map<Integer, Map<Integer, Allocation>> allocations) {
        this.items = items;
        this.allocations = allocations;
    }

    void applyOptimalAllocation(AllocationPlanner planner) {
        clearCurrentAllocations();

        for (ItemRequirement item : items) {
            int remaining = item.required;

            for (var site : planner.buildCandidateSites(item)) {
                if (remaining <= 0) {
                    break;
                }

                String transport = planner.pickSuggestedTransport(site);
                if (transport == null) {
                    continue;
                }

                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                int quantity = Math.min(remaining, stock);
                if (quantity <= 0) {
                    continue;
                }

                allocations.computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>())
                    .put(site.id, new Allocation(site.id, item.merchandiseId, quantity, transport));
                remaining -= quantity;
            }
        }
    }

    void applySuggestedPlan(SuggestedPlan plan) {
        clearCurrentAllocations();

        for (Map.Entry<Integer, Map<Integer, AllocationDraft>> itemEntry : plan.allocationsByItem().entrySet()) {
            Map<Integer, Allocation> targetAllocations = allocations.computeIfAbsent(itemEntry.getKey(), key -> new LinkedHashMap<>());
            for (AllocationDraft draft : itemEntry.getValue().values()) {
                targetAllocations.put(
                    draft.siteId(),
                    new Allocation(draft.siteId(), draft.merchandiseId(), draft.quantity(), draft.transport())
                );
            }
        }
    }

    void clearCurrentAllocations() {
        for (ItemRequirement item : items) {
            allocations.computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>()).clear();
        }
    }
}
