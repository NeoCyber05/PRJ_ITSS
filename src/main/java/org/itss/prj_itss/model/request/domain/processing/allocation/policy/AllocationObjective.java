package org.itss.prj_itss.model.request.domain.processing.allocation.policy;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.model.request.domain.processing.suggestion.ItemVariant;
import org.itss.prj_itss.model.request.domain.processing.suggestion.SuggestedPlan;

import java.util.Comparator;

public interface AllocationObjective {
    String pickTransport(SiteStockOption site, int deadlineDays);

    Comparator<SiteStockOption> siteComparator(ItemRequirement item, int deadlineDays);

    Comparator<ItemVariant> itemVariantComparator();

    Comparator<SuggestedPlan> planComparator();
}
