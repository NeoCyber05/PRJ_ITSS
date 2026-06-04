package org.itss.prj_itss.model.order.application;

import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.domain.Site;

import java.util.ArrayList;
import java.util.List;

public final class OrderDetailApplicationService {

    private final OrderUseCase orderService;
    private final SiteUseCase siteService;
    private final CatalogUseCase catalogService;

    public OrderDetailApplicationService(
            OrderUseCase orderService,
            SiteUseCase siteService,
            CatalogUseCase catalogService) {
        this.orderService = orderService;
        this.siteService = siteService;
        this.catalogService = catalogService;
    }

    public OrderDetailViewModel load(int orderId) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            return null;
        }

        Site site = siteService.findById(order.getSiteId());
        List<OrderMerchandise> items = orderService.findItemsByOrderId(orderId);

        return new OrderDetailViewModel(
            order.getId(),
            OrderingFormatters.formatOrderCode(order.getId()),
            OrderingFormatters.normalizeStatusKey(order.getStatus()),
            OrderingFormatters.formatDateTimeMultiline(order.getCreatedAt()),
            site != null ? blankToFallback(site.getSiteCode()) : "N/A",
            site != null ? blankToFallback(site.getName()) : "N/A",
            items.size(),
            OrderingFormatters.STATUS_PENDING.equalsIgnoreCase(order.getStatus()),
            mapItems(items)
        );
    }

    private List<OrderDetailViewModel.OrderItemRow> mapItems(List<OrderMerchandise> items) {
        List<OrderDetailViewModel.OrderItemRow> rows = new ArrayList<>();
        for (OrderMerchandise item : items) {
            Merchandise merchandise = catalogService.findById(item.getMerchandiseId());
            rows.add(new OrderDetailViewModel.OrderItemRow(
                merchandise != null ? blankToFallback(merchandise.getCode()) : "N/A",
                merchandise != null ? blankToFallback(merchandise.getName()) : "N/A",
                item.getQuantity() != null ? item.getQuantity().toPlainString() : "0",
                merchandise != null && merchandise.getUnit() != null ? merchandise.getUnit() : "N/A",
                item.getDeliveryMethod()
            ));
        }
        return rows;
    }

    private static String blankToFallback(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }
}
