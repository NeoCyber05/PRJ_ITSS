package org.itss.prj_itss.request.domain.allocation.validator;

import org.itss.prj_itss.request.domain.allocation.model.Allocation;
import org.itss.prj_itss.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.request.domain.processing.SiteStockOption;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AllocationValidator {
    List<String> validateAllocations(
        List<ItemRequirement> items,
        Map<Integer, Map<Integer, Allocation>> allocations
    );

    String validateSubmission(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Map<Integer, Map<Integer, Allocation>> allocations,
        Map<Integer, LocalDate> desiredDeliveryDates,
        int deadlineDays
    );
}
