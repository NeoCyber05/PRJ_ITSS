package org.itss.prj_itss.dto;

import org.itss.prj_itss.entity.Order;

public record OrderSummary(Order order, String siteName, String itemsSummary) {
}
