package org.itss.prj_itss.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record RequestProcessingData(
    int requestId,
    LocalDate earliestDeliveryDate,
    int deadlineDays,
    List<ItemRequirement> items,
    List<SiteStockOption> sites,
    Map<Integer, LocalDate> desiredDeliveryDates
) {
}
