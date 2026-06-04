package org.itss.prj_itss.model.order.application;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.shared.formatting.DeliveryStatusFormatter;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.domain.Site;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public final class OrderDetailApplicationService {

    private final OrderUseCase orderService;
    private final SiteUseCase siteService;
    private final MerchandiseUseCase merchandiseService;

    public OrderDetailApplicationService(
            OrderUseCase orderService,
            SiteUseCase siteService,
            MerchandiseUseCase merchandiseService) {
        this.orderService = orderService;
        this.siteService = siteService;
        this.merchandiseService = merchandiseService;
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
            mapItems(items, order, site)
        );
    }

    private List<OrderDetailViewModel.OrderItemRow> mapItems(List<OrderMerchandise> items, Order order, Site site) {
        List<OrderDetailViewModel.OrderItemRow> rows = new ArrayList<>();
        for (OrderMerchandise item : items) {
            Merchandise merchandise = merchandiseService.findById(item.getMerchandiseId());
            
            java.time.LocalDate desiredDeliveryDate = orderService.findDesiredDeliveryDate(order.getId(), item.getMerchandiseId());
            String desiredDateText = desiredDeliveryDate != null ? OrderingFormatters.formatDate(desiredDeliveryDate) : "N/A";
            
            String statusTextVal = "N/A";
            String statusStyleClass = "allocation-eta-unavailable";
            
            if (desiredDeliveryDate != null && order.getCreatedAt() != null) {
                int deadlineDays = (int) ChronoUnit.DAYS.between(order.getCreatedAt().toLocalDate(), desiredDeliveryDate);
                int deliveryDays = 999;
                if (site != null) {
                    boolean isSea = "ship".equalsIgnoreCase(item.getDeliveryMethod());
                    deliveryDays = isSea 
                        ? (site.getShipDeliveryDays() == null ? 999 : site.getShipDeliveryDays())
                        : (site.getAirDeliveryDays() == null ? 999 : site.getAirDeliveryDays());
                }
                int dayDelta = deadlineDays - deliveryDays;
                
                DeliveryStatusFormatter.DeliveryStatusView statusView = DeliveryStatusFormatter.format(dayDelta, deliveryDays < 999);
                statusTextVal = statusView.text();
                statusStyleClass = statusView.styleClass();
            }

            rows.add(new OrderDetailViewModel.OrderItemRow(
                merchandise != null ? blankToFallback(merchandise.getCode()) : "N/A",
                merchandise != null ? blankToFallback(merchandise.getName()) : "N/A",
                item.getQuantity() != null ? item.getQuantity().toPlainString() : "0",
                merchandise != null && merchandise.getUnit() != null ? merchandise.getUnit() : "N/A",
                item.getDeliveryMethod(),
                desiredDateText,
                statusTextVal,
                statusStyleClass
            ));
        }
        return rows;
    }

    private static String blankToFallback(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }
}
