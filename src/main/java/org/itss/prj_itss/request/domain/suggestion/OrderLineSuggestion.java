package org.itss.prj_itss.request.domain.suggestion;

import org.itss.prj_itss.request.domain.processing.ItemRequirement;

public record OrderLineSuggestion(ItemRequirement item, int quantity, String transport, int deliveryDays) {
}
