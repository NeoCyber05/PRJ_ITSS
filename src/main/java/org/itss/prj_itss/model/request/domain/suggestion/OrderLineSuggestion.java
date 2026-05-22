package org.itss.prj_itss.model.request.domain.suggestion;

import org.itss.prj_itss.model.request.domain.processing.ItemRequirement;

public record OrderLineSuggestion(ItemRequirement item, int quantity, String transport, int deliveryDays) {
}
