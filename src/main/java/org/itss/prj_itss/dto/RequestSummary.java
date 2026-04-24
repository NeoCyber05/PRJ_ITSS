package org.itss.prj_itss.dto;

import org.itss.prj_itss.entity.Request;

import java.time.LocalDate;

public record RequestSummary(Request request, int itemCount, LocalDate earliestDeliveryDate) {
}
