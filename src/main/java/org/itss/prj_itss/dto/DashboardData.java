package org.itss.prj_itss.dto;

import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.Request;

import java.util.List;

public record DashboardData(List<Request> requests, List<Order> orders, int siteCount) {
}
