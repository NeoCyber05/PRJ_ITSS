package org.itss.prj_itss.request.domain.allocation.algo;

import org.itss.prj_itss.request.domain.allocation.model.Allocation;
import org.itss.prj_itss.request.domain.allocation.model.AllocationDraft;
import org.itss.prj_itss.request.domain.processing.ItemRequirement;

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

    public void apply(Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem) {
        clearCurrentAllocations();

        for (Map.Entry<Integer, Map<Integer, AllocationDraft>> itemEntry : allocationsByItem.entrySet()) {
            Map<Integer, Allocation> targetAllocations = allocations.computeIfAbsent(itemEntry.getKey(), key -> new LinkedHashMap<>());
            for (AllocationDraft draft : itemEntry.getValue().values()) {
                targetAllocations.put(
                    draft.siteId(),
                    new Allocation(draft.siteId(), draft.merchandiseId(), draft.quantity(), draft.transport())
                );
            }
        }
    }

    private void clearCurrentAllocations() {
        for (ItemRequirement item : items) {
            allocations.computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>()).clear();
        }
    }
}

