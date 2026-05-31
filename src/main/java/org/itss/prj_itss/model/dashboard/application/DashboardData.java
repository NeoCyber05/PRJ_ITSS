package org.itss.prj_itss.model.dashboard.application;

import org.itss.prj_itss.model.order.domain.Order;

import java.util.List;

public record DashboardData(List<DashboardRequestInfo> requests, List<Order> orders, int siteCount) {
}
