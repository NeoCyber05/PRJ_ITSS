package org.itss.prj_itss.request.processing.allocation;

import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AllocationPlanner {

    private static final String TRANSPORT_SHIP = "ship";
    private static final String TRANSPORT_AIR = "air";
    private static final int MAX_COMBINATION_ATTEMPTS = 240;

    private final List<ItemRequirement> items;
    private final List<SiteStockOption> allSites;
    private final Set<Integer> excludedSiteIds;
    private final Set<Integer> prioritySiteIds;
    private final int deadlineDays;

    AllocationPlanner(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        int deadlineDays
    ) {
        this.items = items;
        this.allSites = allSites;
        this.excludedSiteIds = excludedSiteIds;
        this.prioritySiteIds = prioritySiteIds;
        this.deadlineDays = deadlineDays;
    }

    List<SuggestedPlan> buildSuggestedPlans(int limit, int maxItemVariants) {
        if (items.isEmpty()) {
            return List.of();
        }

        List<List<ItemVariant>> variantsByItem = new ArrayList<>();
        for (ItemRequirement item : items) {
            List<ItemVariant> variants = buildItemVariants(item, maxItemVariants);
            if (variants.isEmpty()) {
                return List.of();
            }
            variantsByItem.add(variants);
        }

        long maxAttempts = 1;
        for (List<ItemVariant> variants : variantsByItem) {
            maxAttempts = Math.min((long) MAX_COMBINATION_ATTEMPTS, maxAttempts * variants.size());
        }
        maxAttempts = Math.min((long) MAX_COMBINATION_ATTEMPTS, Math.max(limit * 12L, maxAttempts));

        List<SuggestedPlan> plans = new ArrayList<>();
        Set<String> seenPlanSignatures = new LinkedHashSet<>();
        for (long variantIndex = 0; variantIndex < maxAttempts; variantIndex++) {
            SuggestedPlan plan = buildPlanFromVariantIndex(variantsByItem, variantIndex);
            if (plan != null && seenPlanSignatures.add(plan.signature())) {
                plans.add(plan);
            }
        }

        plans.sort(Comparator
            .comparingInt(SuggestedPlan::siteCount)
            .thenComparingInt(SuggestedPlan::totalDeliveryDays)
            .thenComparingInt(SuggestedPlan::totalLineCount)
            .thenComparing(SuggestedPlan::signature));

        return plans.size() > limit ? plans.subList(0, limit) : plans;
    }

    List<SiteStockOption> buildCandidateSites(ItemRequirement item) {
        return allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
            .filter(site -> pickSuggestedTransport(site) != null)
            .sorted(buildSiteComparator(item))
            .toList();
    }

    String pickSuggestedTransport(SiteStockOption site) {
        if (site.shipDays <= deadlineDays && site.shipDays < 999) {
            return TRANSPORT_SHIP;
        }
        if (site.airDays <= deadlineDays && site.airDays < 999) {
            return TRANSPORT_AIR;
        }
        return null;
    }

    private List<ItemVariant> buildItemVariants(ItemRequirement item, int limit) {
        List<SiteStockOption> candidateSites = buildCandidateSites(item);
        if (candidateSites.isEmpty()) {
            return List.of();
        }

        int totalStock = candidateSites.stream()
            .mapToInt(site -> site.stock.getOrDefault(item.merchandiseId, 0))
            .sum();
        if (totalStock < item.required) {
            return List.of();
        }

        List<List<SiteStockOption>> orderings = buildSiteOrderings(item, candidateSites);
        List<ItemVariant> variants = new ArrayList<>();
        Set<String> seenSignatures = new LinkedHashSet<>();
        for (List<SiteStockOption> ordering : orderings) {
            collectItemVariants(
                item,
                ordering,
                0,
                item.required,
                new LinkedHashMap<>(),
                variants,
                seenSignatures,
                limit
            );
            if (variants.size() >= limit) {
                break;
            }
        }

        variants.sort(Comparator
            .comparingInt(ItemVariant::siteCount)
            .thenComparingInt(ItemVariant::totalDeliveryDays)
            .thenComparing(ItemVariant::signature));
        return variants;
    }

    private Comparator<SiteStockOption> buildSiteComparator(ItemRequirement item) {
        return Comparator
            .comparingInt(this::bestFeasibleDeliveryDays)
            .thenComparing(Comparator.comparingInt((SiteStockOption site) -> site.stock.getOrDefault(item.merchandiseId, 0)).reversed())
            .thenComparingInt(site -> site.id);
    }

    private List<List<SiteStockOption>> buildSiteOrderings(ItemRequirement item, List<SiteStockOption> candidateSites) {
        List<List<SiteStockOption>> orderings = new ArrayList<>();
        Set<String> seenOrderings = new LinkedHashSet<>();

        addOrdering(candidateSites, orderings, seenOrderings);
        for (int rotation = 1; rotation < candidateSites.size(); rotation++) {
            addOrdering(rotate(candidateSites, rotation), orderings, seenOrderings);
        }

        List<SiteStockOption> reversed = new ArrayList<>(candidateSites);
        Collections.reverse(reversed);
        addOrdering(reversed, orderings, seenOrderings);
        for (int rotation = 1; rotation < reversed.size(); rotation++) {
            addOrdering(rotate(reversed, rotation), orderings, seenOrderings);
        }

        List<SiteStockOption> stockAscending = new ArrayList<>(candidateSites);
        stockAscending.sort(Comparator
            .comparingInt((SiteStockOption site) -> site.stock.getOrDefault(item.merchandiseId, 0))
            .thenComparingInt(this::bestFeasibleDeliveryDays)
            .thenComparingInt(site -> site.id));
        addOrdering(stockAscending, orderings, seenOrderings);
        for (int rotation = 1; rotation < stockAscending.size(); rotation++) {
            addOrdering(rotate(stockAscending, rotation), orderings, seenOrderings);
        }

        return orderings;
    }

    private void addOrdering(List<SiteStockOption> ordering, List<List<SiteStockOption>> target, Set<String> seenOrderings) {
        String signature = ordering.stream()
            .map(site -> String.valueOf(site.id))
            .reduce((left, right) -> left + "|" + right)
            .orElse("");
        if (seenOrderings.add(signature)) {
            target.add(List.copyOf(ordering));
        }
    }

    private List<SiteStockOption> rotate(List<SiteStockOption> sites, int offset) {
        if (sites.isEmpty()) {
            return List.of();
        }
        int rotation = offset % sites.size();
        List<SiteStockOption> rotated = new ArrayList<>(sites.size());
        rotated.addAll(sites.subList(rotation, sites.size()));
        rotated.addAll(sites.subList(0, rotation));
        return rotated;
    }

    private void collectItemVariants(
        ItemRequirement item,
        List<SiteStockOption> orderedSites,
        int siteIndex,
        int remaining,
        LinkedHashMap<Integer, AllocationDraft> current,
        List<ItemVariant> variants,
        Set<String> seenSignatures,
        int limit
    ) {
        if (variants.size() >= limit) {
            return;
        }
        if (remaining == 0) {
            ItemVariant variant = buildItemVariant(current);
            if (variant != null && seenSignatures.add(variant.signature())) {
                variants.add(variant);
            }
            return;
        }
        if (siteIndex >= orderedSites.size()) {
            return;
        }

        int remainingCapacity = calculateRemainingCapacity(item, orderedSites, siteIndex);
        if (remainingCapacity < remaining) {
            return;
        }

        SiteStockOption site = orderedSites.get(siteIndex);
        int stock = site.stock.getOrDefault(item.merchandiseId, 0);
        int restCapacity = calculateRemainingCapacity(item, orderedSites, siteIndex + 1);
        int minTake = Math.max(0, remaining - restCapacity);
        int maxTake = Math.min(stock, remaining);
        if (maxTake < minTake) {
            return;
        }

        String transport = pickSuggestedTransport(site);
        if (transport == null) {
            collectItemVariants(item, orderedSites, siteIndex + 1, remaining, current, variants, seenSignatures, limit);
            return;
        }

        for (int quantity : buildQuantityChoices(minTake, maxTake, remaining, orderedSites.size() - siteIndex)) {
            if (quantity > 0) {
                current.put(site.id, new AllocationDraft(site.id, item.merchandiseId, quantity, transport));
            }
            collectItemVariants(
                item,
                orderedSites,
                siteIndex + 1,
                remaining - quantity,
                current,
                variants,
                seenSignatures,
                limit
            );
            if (quantity > 0) {
                current.remove(site.id);
            }
            if (variants.size() >= limit) {
                return;
            }
        }
    }

    private int calculateRemainingCapacity(ItemRequirement item, List<SiteStockOption> orderedSites, int startIndex) {
        int total = 0;
        for (int index = startIndex; index < orderedSites.size(); index++) {
            total += orderedSites.get(index).stock.getOrDefault(item.merchandiseId, 0);
        }
        return total;
    }

    private List<Integer> buildQuantityChoices(int minTake, int maxTake, int remaining, int sitesLeft) {
        LinkedHashSet<Integer> choices = new LinkedHashSet<>();
        choices.add(maxTake);
        choices.add(clamp((remaining * 2 + 2) / 3, minTake, maxTake));
        choices.add(clamp((int) Math.ceil((double) remaining / Math.max(1, sitesLeft)), minTake, maxTake));
        choices.add(clamp((minTake + maxTake) / 2, minTake, maxTake));
        choices.add(minTake);
        return new ArrayList<>(choices);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private ItemVariant buildItemVariant(Map<Integer, AllocationDraft> current) {
        if (current.isEmpty()) {
            return null;
        }

        Map<Integer, AllocationDraft> allocationsBySite = new LinkedHashMap<>();
        List<AllocationDraft> drafts = current.values().stream()
            .sorted(Comparator.comparingInt(AllocationDraft::siteId))
            .toList();

        int totalDeliveryDays = 0;
        StringBuilder signature = new StringBuilder();
        for (AllocationDraft draft : drafts) {
            allocationsBySite.put(draft.siteId(), draft);
            SiteStockOption site = findSiteById(draft.siteId());
            if (site != null) {
                totalDeliveryDays += getDeliveryDays(site, draft.transport());
            }
            if (!signature.isEmpty()) {
                signature.append('|');
            }
            signature.append(draft.siteId())
                .append(':')
                .append(draft.quantity())
                .append(':')
                .append(draft.transport());
        }

        return new ItemVariant(allocationsBySite, allocationsBySite.size(), totalDeliveryDays, signature.toString());
    }

    private SuggestedPlan buildPlanFromVariantIndex(List<List<ItemVariant>> variantsByItem, long variantIndex) {
        Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem = new LinkedHashMap<>();
        long workingIndex = variantIndex;

        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            List<ItemVariant> variants = variantsByItem.get(itemIndex);
            if (variants.isEmpty()) {
                return null;
            }

            int selectedIndex = (int) (workingIndex % variants.size());
            workingIndex /= variants.size();

            ItemVariant variant = variants.get(selectedIndex);
            Map<Integer, AllocationDraft> itemAllocations = new LinkedHashMap<>();
            for (AllocationDraft draft : variant.allocationsBySite().values()) {
                itemAllocations.put(draft.siteId(), draft);
            }
            allocationsByItem.put(items.get(itemIndex).merchandiseId, itemAllocations);
        }

        return buildSuggestedPlan(allocationsByItem);
    }

    private SuggestedPlan buildSuggestedPlan(Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem) {
        Map<Integer, MutableSiteOrder> siteOrdersById = new LinkedHashMap<>();
        int totalQuantity = 0;
        int totalLines = 0;
        int totalDeliveryDays = 0;
        int prioritySiteCount = 0;
        Set<Integer> countedPrioritySites = new LinkedHashSet<>();
        StringBuilder signature = new StringBuilder();

        for (ItemRequirement item : items) {
            Map<Integer, AllocationDraft> itemAllocations = allocationsByItem.getOrDefault(item.merchandiseId, Collections.emptyMap());
            List<AllocationDraft> drafts = itemAllocations.values().stream()
                .sorted(Comparator.comparingInt(AllocationDraft::siteId))
                .toList();
            for (AllocationDraft draft : drafts) {
                SiteStockOption site = findSiteById(draft.siteId());
                if (site == null) {
                    continue;
                }

                int deliveryDays = getDeliveryDays(site, draft.transport());
                OrderLineSuggestion line = new OrderLineSuggestion(item, draft.quantity(), draft.transport(), deliveryDays);
                MutableSiteOrder siteOrder = siteOrdersById.computeIfAbsent(site.id, key -> new MutableSiteOrder(site));
                siteOrder.lines.add(line);
                siteOrder.totalQuantity += draft.quantity();
                siteOrder.deliveryDays = Math.max(siteOrder.deliveryDays, deliveryDays);
                siteOrder.transports.add(draft.transport());

                totalQuantity += draft.quantity();
                totalLines++;
                totalDeliveryDays += deliveryDays;

                if (prioritySiteIds.contains(site.id) && countedPrioritySites.add(site.id)) {
                    prioritySiteCount++;
                }

                if (!signature.isEmpty()) {
                    signature.append('|');
                }
                signature.append(item.merchandiseId)
                    .append('@')
                    .append(draft.siteId())
                    .append(':')
                    .append(draft.quantity())
                    .append(':')
                    .append(draft.transport());
            }
        }

        List<SiteOrderSuggestion> siteOrders = siteOrdersById.values().stream()
            .map(this::toSiteOrderSuggestion)
            .sorted(Comparator
                .comparingInt(SiteOrderSuggestion::totalQuantity).reversed()
                .thenComparing(siteOrder -> siteOrder.site().name))
            .toList();

        return new SuggestedPlan(
            allocationsByItem,
            siteOrders,
            totalQuantity,
            totalLines,
            siteOrders.size(),
            prioritySiteCount,
            totalDeliveryDays,
            signature.toString()
        );
    }

    private SiteOrderSuggestion toSiteOrderSuggestion(MutableSiteOrder siteOrder) {
        List<OrderLineSuggestion> lines = siteOrder.lines.stream()
            .sorted(Comparator.comparing(line -> line.item().code))
            .toList();
        String transportSummary;
        if (siteOrder.transports.size() == 1) {
            transportSummary = transportLabel(siteOrder.transports.iterator().next());
        } else {
            transportSummary = "Nhiều cách";
        }
        return new SiteOrderSuggestion(siteOrder.site, lines, siteOrder.totalQuantity, siteOrder.deliveryDays, transportSummary);
    }

    private int bestFeasibleDeliveryDays(SiteStockOption site) {
        int best = 999;
        if (site.shipDays <= deadlineDays && site.shipDays < best) {
            best = site.shipDays;
        }
        if (site.airDays <= deadlineDays && site.airDays < best) {
            best = site.airDays;
        }
        return best;
    }

    private int getDeliveryDays(SiteStockOption site, String transport) {
        return TRANSPORT_AIR.equals(transport) ? site.airDays : site.shipDays;
    }

    private String transportLabel(String transport) {
        return TRANSPORT_AIR.equals(transport) ? "Hàng không" : "Đường biển";
    }

    private SiteStockOption findSiteById(int siteId) {
        for (SiteStockOption site : allSites) {
            if (site.id == siteId) {
                return site;
            }
        }
        return null;
    }

    record AllocationDraft(int siteId, int merchandiseId, int quantity, String transport) {
    }

    private record ItemVariant(
        Map<Integer, AllocationDraft> allocationsBySite,
        int siteCount,
        int totalDeliveryDays,
        String signature
    ) {
    }

    record OrderLineSuggestion(ItemRequirement item, int quantity, String transport, int deliveryDays) {
    }

    record SiteOrderSuggestion(
        SiteStockOption site,
        List<OrderLineSuggestion> lines,
        int totalQuantity,
        int deliveryDays,
        String transportSummary
    ) {
    }

    record SuggestedPlan(
        Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem,
        List<SiteOrderSuggestion> siteOrders,
        int totalQuantity,
        int totalLineCount,
        int siteCount,
        int prioritySiteCount,
        int totalDeliveryDays,
        String signature
    ) {
    }

    private static final class MutableSiteOrder {
        private final SiteStockOption site;
        private final List<OrderLineSuggestion> lines = new ArrayList<>();
        private final Set<String> transports = new LinkedHashSet<>();
        private int totalQuantity;
        private int deliveryDays;

        private MutableSiteOrder(SiteStockOption site) {
            this.site = site;
        }
    }
}

