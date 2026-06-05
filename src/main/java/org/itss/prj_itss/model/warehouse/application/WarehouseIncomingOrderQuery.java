package org.itss.prj_itss.model.warehouse.application;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.order.domain.OrderStatus;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.domain.Site;

import java.util.Comparator;
import java.util.List;

public final class WarehouseIncomingOrderQuery {

    private final OrderRepository orderRepository;
    private final SiteUseCase siteUseCase;
    private final MerchandiseUseCase merchandiseUseCase;

    public WarehouseIncomingOrderQuery(
        OrderRepository orderRepository,
        SiteUseCase siteUseCase,
        MerchandiseUseCase merchandiseUseCase
    ) {
        this.orderRepository = orderRepository;
        this.siteUseCase = siteUseCase;
        this.merchandiseUseCase = merchandiseUseCase;
    }

    public List<IncomingOrderRow> findIncomingRows() {
        List<Order> orders = orderRepository.findByStatus(OrderStatus.SHIPPING.displayValue());
        java.util.Map<Integer, Site> siteMap = siteUseCase.findAll().stream()
            .collect(java.util.stream.Collectors.toMap(Site::getId, java.util.function.Function.identity(), (a, b) -> a));
        java.util.Map<Integer, Integer> itemCounts = orderRepository.countItemsGroupedByOrderId();

        return orders.stream()
            .sorted(Comparator
                .comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Comparator.comparingInt(Order::getId).reversed()))
            .map(order -> toRow(order, siteMap, itemCounts))
            .toList();
    }

    public IncomingOrderDetail findIncomingDetail(int orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            return null;
        }
        IncomingOrderRow summary = toRow(order);
        List<IncomingOrderItemRow> items = orderRepository.findItemsByOrderId(orderId).stream()
            .map(this::toItemRow)
            .toList();
        return new IncomingOrderDetail(summary, items);
    }

    private IncomingOrderRow toRow(Order order, java.util.Map<Integer, Site> siteMap, java.util.Map<Integer, Integer> itemCounts) {
        Site site = siteMap.get(order.getSiteId());
        int itemCount = itemCounts.getOrDefault(order.getId(), 0);
        return new IncomingOrderRow(
            order.getId(),
            order.getRequestId(),
            order.getSiteId(),
            OrderingFormatters.formatOrderCode(order.getId()),
            OrderingFormatters.formatRequestCode(order.getRequestId()),
            site == null ? "N/A" : safeText(site.getSiteCode()),
            site == null ? "Site #" + order.getSiteId() : safeText(site.getName()),
            OrderingFormatters.formatDateOrEmpty(order.getCreatedAt()),
            order.getStatus(),
            OrderingFormatters.orderStatusText(order.getStatus()),
            itemCount
        );
    }

    private IncomingOrderRow toRow(Order order) {
        Site site = siteUseCase.findById(order.getSiteId());
        int itemCount = orderRepository.findItemsByOrderId(order.getId()).size();
        return new IncomingOrderRow(
            order.getId(),
            order.getRequestId(),
            order.getSiteId(),
            OrderingFormatters.formatOrderCode(order.getId()),
            OrderingFormatters.formatRequestCode(order.getRequestId()),
            site == null ? "N/A" : safeText(site.getSiteCode()),
            site == null ? "Site #" + order.getSiteId() : safeText(site.getName()),
            OrderingFormatters.formatDateOrEmpty(order.getCreatedAt()),
            order.getStatus(),
            OrderingFormatters.orderStatusText(order.getStatus()),
            itemCount
        );
    }

    private IncomingOrderItemRow toItemRow(OrderMerchandise item) {
        Merchandise merchandise = merchandiseUseCase.findById(item.getMerchandiseId());
        return new IncomingOrderItemRow(
            item.getMerchandiseId(),
            merchandise == null ? "N/A" : safeText(merchandise.getCode()),
            merchandise == null ? "N/A" : safeText(merchandise.getName()),
            merchandise == null ? "N/A" : safeText(merchandise.getUnit()),
            OrderingFormatters.formatQuantity(item.getQuantity()),
            OrderingFormatters.deliveryMethodText(item.getDeliveryMethod())
        );
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }
}
