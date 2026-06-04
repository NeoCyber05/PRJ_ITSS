package org.itss.prj_itss.model.request.application.processing;

import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PreviewBuilder {

    void reset();

    PreviewBuilder items(List<ItemRequirement> items);

    PreviewBuilder sites(List<SiteStockOption> sites);

    PreviewBuilder allocations(Map<Integer, Map<Integer, Allocation>> allocations);

    PreviewBuilder desiredDeliveryDates(Map<Integer, LocalDate> desiredDeliveryDates);
}
