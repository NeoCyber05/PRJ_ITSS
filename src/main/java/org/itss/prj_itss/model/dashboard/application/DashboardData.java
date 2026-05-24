package org.itss.prj_itss.model.dashboard.application;

import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.request.domain.request.Request;

import java.util.List;

public record DashboardData(List<Request> requests, List<Order> orders, int siteCount) {
}
