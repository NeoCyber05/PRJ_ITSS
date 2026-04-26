package org.itss.prj_itss.ordering.request.process.allocation.algo;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.ordering.request.process.allocation.algo.AllSuggestAlgo.AllocationDraft;
import org.itss.prj_itss.ordering.request.process.allocation.algo.AllSuggestAlgo.SuggestedPlan;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ApplyPlan {

    private final List<ItemRequirement> items;
    private final Map<Integer, Map<Integer, Allocation>> allocations;

    public ApplyPlan(List<ItemRequirement> items, Map<Integer, Map<Integer, Allocation>> allocations) {
        this.items = items;
        this.allocations = allocations;
    }

    public void applyOptimal(OptimalSuggestAlgo optimalAlgo) {
        clearCurrentAllocations();

        for (ItemRequirement item : items) {
            int remaining = item.required;
            for (var site : optimalAlgo.buildCandidateSites(item)) {
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

                allocations.computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>())
                    .put(site.id, new Allocation(site.id, item.merchandiseId, quantity, transport));
                remaining -= quantity;
            }
        }
    }

    public void applyAllSuggest(SuggestedPlan plan) {
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

    public void clearCurrentAllocations() {
        for (ItemRequirement item : items) {
            allocations.computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>()).clear();
        }
    }
}
