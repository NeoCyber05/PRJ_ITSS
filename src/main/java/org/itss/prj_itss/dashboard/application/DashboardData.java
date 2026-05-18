package org.itss.prj_itss.dashboard.application;

import org.itss.prj_itss.order.domain.Order;
import org.itss.prj_itss.request.domain.request.Request;

import java.util.List;

public record DashboardData(List<Request> requests, List<Order> orders, int siteCount) {
}
