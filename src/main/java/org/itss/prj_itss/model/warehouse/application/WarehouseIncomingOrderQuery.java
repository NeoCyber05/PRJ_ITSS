package org.itss.prj_itss.model.warehouse.application;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.order.domain.OrderStatus;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.domain.Site;

import java.util.Comparator;
import java.util.List;

public final class WarehouseIncomingOrderQuery {

    private final OrderUseCase orderUseCase;
    private final SiteUseCase siteUseCase;
    private final MerchandiseUseCase merchandiseUseCase;

    public WarehouseIncomingOrderQuery(
        OrderUseCase orderUseCase,
        SiteUseCase siteUseCase,
        MerchandiseUseCase merchandiseUseCase
    ) {
        this.orderUseCase = orderUseCase;
        this.siteUseCase = siteUseCase;
        this.merchandiseUseCase = merchandiseUseCase;
    }

    public List<IncomingOrderRow> findIncomingRows() {
        List<Order> orders = orderUseCase.findByStatus(OrderStatus.SHIPPING.displayValue());
        return orders.stream()
            .sorted(Comparator
                .comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Comparator.comparingInt(Order::getId).reversed()))
            .map(this::toRow)
            .toList();
    }

    public IncomingOrderDetail findIncomingDetail(int orderId) {
        Order order = orderUseCase.findById(orderId);
        if (order == null) {
            return null;
        }
        IncomingOrderRow summary = toRow(order);
        List<IncomingOrderItemRow> items = orderUseCase.findItemsByOrderId(orderId).stream()
            .map(this::toItemRow)
            .toList();
        return new IncomingOrderDetail(summary, items);
    }

    private IncomingOrderRow toRow(Order order) {
        Site site = siteUseCase.findById(order.getSiteId());
        int itemCount = orderUseCase.findItemsByOrderId(order.getId()).size();
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
