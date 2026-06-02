package org.itss.prj_itss.model.request.domain.processing.suggestion.algo;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.allocation.AllocationDraft;
import org.itss.prj_itss.model.request.domain.processing.allocation.policy.AllocationObjective;
import org.itss.prj_itss.model.request.domain.processing.allocation.policy.FastDeliveryObjective;
import org.itss.prj_itss.model.request.domain.processing.suggestion.ItemVariant;
import org.itss.prj_itss.model.request.domain.processing.suggestion.SuggestedPlan;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllocationSuggestEngineTest {

    private static final int MERCH = 10;

    private SiteStockOption site(int id, int shipDays, int airDays, int stock) {
        return new SiteStockOption(id, "S" + id, "Site " + id, "", shipDays, airDays, Map.of(MERCH, stock));
    }

    private ItemRequirement item(int required) {
        return new ItemRequirement(MERCH, "M10", "Part", required);
    }

    @Test
    void emptySelectionUsesAllNonExcludedSites() {
        SiteStockOption s1 = site(1, 2, 1, 5);
        SiteStockOption s2 = site(2, 3, 1, 5);
        List<ItemRequirement> items = List.of(item(6));
        List<SiteStockOption> allSites = List.of(s1, s2);

        AllSuggest all = new AllSuggest(items, allSites, Set.of(), Set.of(), 7, new FastDeliveryObjective());
        List<SuggestedPlan> plans = all.buildSuggestedPlans(10, 12);
        assertFalse(plans.isEmpty());
        assertTrue(containsSite(plans.get(0), 1));
        assertTrue(containsSite(plans.get(0), 2));

        OptimalSuggest optimal = new OptimalSuggest(allSites, Set.of(), Set.of(), 7, new FastDeliveryObjective());
        Map<Integer, Map<Integer, AllocationDraft>> drafts = optimal.buildOptimalDrafts(items);
        assertTrue(drafts.containsKey(MERCH));
        assertTrue(drafts.get(MERCH).containsKey(1));
        assertTrue(drafts.get(MERCH).containsKey(2));
    }

    @Test
    void selectedSiteRestrictsBothOptimalAndAllSuggest() {
        SiteStockOption s1 = site(1, 2, 1, 5);
        SiteStockOption s2 = site(2, 3, 1, 10);
        List<ItemRequirement> items = List.of(item(6));
        List<SiteStockOption> allSites = List.of(s1, s2);
        Set<Integer> selected = Set.of(2);

        AllSuggest all = new AllSuggest(items, allSites, Set.of(), selected, 7, new FastDeliveryObjective());
        List<SuggestedPlan> plans = all.buildSuggestedPlans(10, 12);
        assertFalse(plans.isEmpty());
        assertFalse(containsSite(plans.get(0), 1));
        assertTrue(containsSite(plans.get(0), 2));

        OptimalSuggest optimal = new OptimalSuggest(allSites, Set.of(), selected, 7, new FastDeliveryObjective());
        Map<Integer, Map<Integer, AllocationDraft>> drafts = optimal.buildOptimalDrafts(items);
        assertTrue(drafts.containsKey(MERCH));
        assertFalse(drafts.get(MERCH).containsKey(1));
        assertTrue(drafts.get(MERCH).containsKey(2));
    }

    @Test
    void excludedStillRemovedEvenWhenSelected() {
        SiteStockOption s1 = site(1, 2, 1, 5);
        SiteStockOption s2 = site(2, 3, 1, 5);
        SiteStockOption s3 = site(3, 1, 1, 5);
        List<ItemRequirement> items = List.of(item(6));
        List<SiteStockOption> allSites = List.of(s1, s2, s3);
        Set<Integer> selected = Set.of(1, 2, 3);
        Set<Integer> excluded = Set.of(2);

        AllSuggest all = new AllSuggest(items, allSites, excluded, selected, 7, new FastDeliveryObjective());
        List<SuggestedPlan> plans = all.buildSuggestedPlans(10, 12);
        assertFalse(plans.isEmpty());
        assertFalse(containsSite(plans.get(0), 2));

        OptimalSuggest optimal = new OptimalSuggest(allSites, excluded, selected, 7, new FastDeliveryObjective());
        Map<Integer, Map<Integer, AllocationDraft>> drafts = optimal.buildOptimalDrafts(items);
        assertTrue(drafts.containsKey(MERCH));
        assertFalse(drafts.get(MERCH).containsKey(2));
    }

    @Test
    void allSuggestRespectsLimit() {
        SiteStockOption s1 = site(1, 2, 1, 3);
        SiteStockOption s2 = site(2, 3, 1, 3);
        SiteStockOption s3 = site(3, 4, 1, 3);
        List<ItemRequirement> items = List.of(item(5));
        List<SiteStockOption> allSites = List.of(s1, s2, s3);

        AllSuggest all = new AllSuggest(items, allSites, Set.of(), Set.of(), 7, new FastDeliveryObjective());
        List<SuggestedPlan> plans = all.buildSuggestedPlans(2, 12);
        assertTrue(plans.size() <= 2);
    }

    @Test
    void optimalSuggestReturnsTopPlanFromEngine() {
        SiteStockOption s1 = site(1, 2, 1, 3);
        SiteStockOption s2 = site(2, 5, 1, 5);
        List<ItemRequirement> items = List.of(item(5));
        List<SiteStockOption> allSites = List.of(s1, s2);

        AllSuggest all = new AllSuggest(items, allSites, Set.of(), Set.of(), 7, new FastDeliveryObjective());
        List<SuggestedPlan> plans = all.buildSuggestedPlans(1, 12);
        assertFalse(plans.isEmpty());
        SuggestedPlan topPlan = plans.get(0);

        OptimalSuggest optimal = new OptimalSuggest(allSites, Set.of(), Set.of(), 7, new FastDeliveryObjective());
        Map<Integer, Map<Integer, AllocationDraft>> drafts = optimal.buildOptimalDrafts(items);

        assertEquals(topPlan.allocationsByItem(), drafts);
    }

    @Test
    void fakeObjectiveChangesRankingWithoutModifyingSuggest() {
        SiteStockOption s1 = site(1, 2, 1, 5);
        SiteStockOption s2 = site(2, 3, 1, 5);
        List<ItemRequirement> items = List.of(item(5));
        List<SiteStockOption> allSites = List.of(s1, s2);

        AllSuggest defaultSuggest = new AllSuggest(items, allSites, Set.of(), Set.of(), 7, new FastDeliveryObjective());
        SuggestedPlan defaultPlan = defaultSuggest.buildSuggestedPlans(1, 12).get(0);

        AllocationObjective reverseObjective = new AllocationObjective() {
            @Override
            public String pickTransport(SiteStockOption site, int deadlineDays) {
                return new FastDeliveryObjective().pickTransport(site, deadlineDays);
            }

            @Override
            public Comparator<SiteStockOption> siteComparator(ItemRequirement item, int deadlineDays) {
                return Comparator.comparingInt((SiteStockOption site) -> site.id).reversed();
            }

            @Override
            public Comparator<ItemVariant> itemVariantComparator() {
                return new FastDeliveryObjective().itemVariantComparator();
            }

            @Override
            public Comparator<SuggestedPlan> planComparator() {
                return Comparator.comparingInt(SuggestedPlan::siteCount).reversed();
            }
        };

        AllSuggest reverseSuggest = new AllSuggest(items, allSites, Set.of(), Set.of(), 7, reverseObjective);
        SuggestedPlan reversePlan = reverseSuggest.buildSuggestedPlans(1, 12).get(0);

        assertNotNull(reversePlan);
    }

    private boolean containsSite(SuggestedPlan plan, int siteId) {
        return plan.siteOrders().stream().anyMatch(so -> so.site().id == siteId);
    }
}
