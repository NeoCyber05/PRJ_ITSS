package org.itss.prj_itss.request.business.port;

import org.itss.prj_itss.request.business.model.Allocation;
import org.itss.prj_itss.request.business.model.ItemRequirement;
import org.itss.prj_itss.request.business.model.SiteStockOption;

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
