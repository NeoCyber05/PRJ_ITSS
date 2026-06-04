package org.itss.prj_itss.model.dashboard.application;

import org.itss.prj_itss.model.request.domain.request.Request;
import java.time.LocalDate;

public record DashboardRequestInfo(Request request, LocalDate earliestDeliveryDate, int itemCount) {
}
