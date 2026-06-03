package org.itss.prj_itss.model.order.application;

import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.order.application.OrderRow;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.site.application.SiteUseCase;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class OrderManagementApplicationService {

    private final OrderUseCase orderService;
    private final SiteUseCase siteService;
    private final MerchandiseUseCase merchandiseService;

    public OrderManagementApplicationService(
        OrderUseCase orderService,
        SiteUseCase siteService,
        MerchandiseUseCase merchandiseService
    ) {
        this.orderService = Objects.requireNonNull(orderService, "orderService");
        this.siteService = Objects.requireNonNull(siteService, "siteService");
        this.merchandiseService = Objects.requireNonNull(merchandiseService, "merchandiseService");
    }

    public List<OrderRow> loadRows() {
        return orderService.findAll().stream().map(this::toRow).toList();
    }

    public List<OrderRow> findRows() {
        return loadRows();
    }

    public List<OrderRow> loadRowsByStatus(String status) {
        return orderService.findByStatus(status).stream().map(this::toRow).toList();
    }

    public List<OrderRow> filterRows(List<OrderRow> rows, String keyword, String selectedStatus) {
        String selectedStatusKey = OrderingFormatters.toStatusKey(selectedStatus);
        return rows.stream()
            .filter(row -> row.matchesKeyword(keyword))
            .filter(row -> row.matchesStatusKey(selectedStatusKey))
            .toList();
    }

    public OrderRow toRow(Order order) {
        Site site = siteService.findById(order.getSiteId());
        String itemSummary = itemSummary(order.getId());
        String status = order.getStatus() == null ? "N/A" : order.getStatus();
        String statusKey = OrderingFormatters.normalizeStatusKey(order.getStatus());
        return new OrderRow(
            order,
            order.getId(),
            order.getRequestId(),
            order.getSiteId(),
            OrderingFormatters.formatOrderCode(order.getId()),
            OrderingFormatters.formatRequestCode(order.getRequestId()),
            site == null ? "Site #" + order.getSiteId() : site.getName(),
            itemSummary,
            OrderingFormatters.formatDateOrEmpty(order.getCreatedAt()),
            status,
            statusKey,
            OrderingFormatters.orderStatusText(order.getStatus())
        );
    }

    private String itemSummary(int orderId) {
        List<OrderMerchandise> items = orderService.findItemsByOrderId(orderId);
        String summary = items.stream()
            .map(item -> {
                Merchandise merchandise = merchandiseService.findById(item.getMerchandiseId());
                return merchandise == null ? "?" : merchandise.getCode();
            })
            .collect(Collectors.joining(", "));
        return summary.isBlank() ? "-" : summary;
    }
}
