package org.itss.prj_itss.model.order.domain.cancellation;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.model.request.domain.processing.SiteStockOption;

import java.time.LocalDate;
import java.util.List;

public record CancelledOrderProcessingData(
    int cancelledOrderId,
    int requestId,
    int cancelledSiteId,
    String requestCode,
    LocalDate desiredDeliveryDate,
    int deadlineDays,
    List<ItemRequirement> items,
    List<SiteStockOption> sites
) {}
