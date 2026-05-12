package org.itss.prj_itss.ordering.request.process.model;

import org.itss.prj_itss.dto.Allocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AllocationPlan {
    private final Map<Integer, Map<Integer, Allocation>> allocations;

    private AllocationPlan(Map<Integer, Map<Integer, Allocation>> allocations) {
        this.allocations = allocations;
    }

    public static AllocationPlan using(Map<Integer, Map<Integer, Allocation>> allocations) {
        return new AllocationPlan(allocations);
    }

    public int allocatedQuantity(int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Map.of())
            .values()
            .stream()
            .mapToInt(Allocation::getQuantity)
            .sum();
    }

    public void removeSites(Set<Integer> excludedSiteIds) {
        for (Map<Integer, Allocation> itemAllocations : allocations.values()) {
            itemAllocations.keySet().removeIf(excludedSiteIds::contains);
        }
    }

    public Map<Integer, List<Allocation>> groupBySite() {
        Map<Integer, List<Allocation>> groupedAllocations = new LinkedHashMap<>();
        for (Map<Integer, Allocation> itemAllocations : allocations.values()) {
            for (Allocation allocation : itemAllocations.values()) {
                if (allocation.getQuantity() <= 0) {
                    continue;
                }
                groupedAllocations
                    .computeIfAbsent(allocation.siteId, key -> new ArrayList<>())
                    .add(allocation);
            }
        }
        return groupedAllocations;
    }
}
