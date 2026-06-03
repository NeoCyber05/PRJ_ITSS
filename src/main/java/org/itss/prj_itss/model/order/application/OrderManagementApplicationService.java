package org.itss.prj_itss.model.order.application;

import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.order.application.OrderRow;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.site.application.SiteUseCase;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class OrderManagementApplicationService {

    private final OrderUseCase orderService;
    private final SiteUseCase siteService;
    private final CatalogUseCase merchandiseService;

    public OrderManagementApplicationService(
        OrderUseCase orderService,
        SiteUseCase siteService,
        CatalogUseCase merchandiseService
    ) {
        this.orderService = Objects.requireNonNull(orderService, "orderService");
        this.siteService = Objects.requireNonNull(siteService, "siteService");
        this.merchandiseService = Objects.requireNonNull(merchandiseService, "merchandiseService");
    }

    public List<OrderRow> loadRows() {
        List<Order> orders = orderService.findAll();
        
        java.util.Map<Integer, Site> siteMap = siteService.findAll().stream()
            .collect(Collectors.toMap(Site::getId, java.util.function.Function.identity(), (a, b) -> a));
            
        java.util.Map<Integer, Merchandise> merchandiseMap = merchandiseService.findAll().stream()
            .collect(Collectors.toMap(Merchandise::getId, java.util.function.Function.identity(), (a, b) -> a));
            
        return orders.stream()
            .map(order -> toRow(order, siteMap, merchandiseMap))
            .toList();
    }

    public List<OrderRow> findRows() {
        return loadRows();
    }

    public List<OrderRow> loadRowsByStatus(String status) {
        List<Order> orders = orderService.findByStatus(status);
        
        java.util.Map<Integer, Site> siteMap = siteService.findAll().stream()
            .collect(Collectors.toMap(Site::getId, java.util.function.Function.identity(), (a, b) -> a));
            
        java.util.Map<Integer, Merchandise> merchandiseMap = merchandiseService.findAll().stream()
            .collect(Collectors.toMap(Merchandise::getId, java.util.function.Function.identity(), (a, b) -> a));
            
        return orders.stream()
            .map(order -> toRow(order, siteMap, merchandiseMap))
            .toList();
    }

    public List<OrderRow> filterRows(List<OrderRow> rows, String keyword, String selectedStatus) {
        String selectedStatusKey = OrderingFormatters.toStatusKey(selectedStatus);
        return rows.stream()
            .filter(row -> row.matchesKeyword(keyword))
            .filter(row -> row.matchesStatusKey(selectedStatusKey))
            .toList();
    }

    public OrderRow toRow(Order order) {
        return toRow(order, null, null);
    }

    public OrderRow toRow(Order order, java.util.Map<Integer, Site> siteMap, java.util.Map<Integer, Merchandise> merchandiseMap) {
        Site site = null;
        if (siteMap != null) {
            site = siteMap.get(order.getSiteId());
        } else {
            site = siteService.findById(order.getSiteId());
        }
        
        String itemSummary = itemSummary(order.getId(), merchandiseMap);
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
        return itemSummary(orderId, null);
    }

    private String itemSummary(int orderId, java.util.Map<Integer, Merchandise> merchandiseMap) {
        List<OrderMerchandise> items = orderService.findItemsByOrderId(orderId);
        String summary = items.stream()
            .map(item -> {
                Merchandise merchandise = null;
                if (merchandiseMap != null) {
                    merchandise = merchandiseMap.get(item.getMerchandiseId());
                } else {
                    merchandise = merchandiseService.findById(item.getMerchandiseId());
                }
                return merchandise == null ? "?" : merchandise.getCode();
            })
            .collect(Collectors.joining(", "));
        return summary.isBlank() ? "-" : summary;
    }
}

