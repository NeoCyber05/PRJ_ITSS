package org.itss.prj_itss.model.request.application.sales.detail;

import org.itss.prj_itss.model.request.application.sales.view.RequestDetailItemRow;

import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.common.application.OrderingFormatters;
import org.itss.prj_itss.model.order.application.OrderUseCase;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.request.application.RequestManagementUseCase;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.domain.Site;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public final class RequestDetailApplicationService {

    private final RequestManagementUseCase requestService;
    private final OrderUseCase orderService;
    private final SiteUseCase siteService;
    private final CatalogUseCase catalogUseCase;

    public RequestDetailApplicationService(
        RequestManagementUseCase requestService,
        OrderUseCase orderService,
        SiteUseCase siteService,
        CatalogUseCase catalogUseCase
    ) {
        this.requestService = requestService;
        this.orderService = orderService;
        this.siteService = siteService;
        this.catalogUseCase = catalogUseCase;
    }

    public RequestDetailViewModel load(String requestCode) {
        int requestId = OrderingFormatters.parseEntityId(requestCode, 1);
        Request request = requestService.findById(requestId);
        if (request == null) {
            return new RequestDetailViewModel(
                requestId,
                requestCode,
                "N/A",
                "N/A",
                "N/A",
                "",
                "N/A",
                List.of(),
                List.of()
            );
        }

        List<RequestDetailItemRow> itemRows = requestService.findItemsByRequestId(requestId).stream()
            .map(this::toItemRow)
            .toList();

        List<Order> allocatedOrders = orderService.findAll().stream()
            .filter(order -> order.getRequestId() == requestId)
            .sorted(Comparator.comparingInt(Order::getId))
            .toList();

        List<AllocatedOrderRow> orderRows = allocatedOrders.stream()
            .map(this::toOrderRow)
            .toList();

        LocalDate earliestDeadline = requestService.getEarliestDeliveryDate(requestId);

        return new RequestDetailViewModel(
            request.getId(),
            OrderingFormatters.formatRequestCode(request.getId()),
            OrderingFormatters.formatDateOrEmpty(request.getCreatedAt()),
            request.getStatus(),
            OrderingFormatters.requestStatusText(request.getStatus()),
            request.getNote(),
            OrderingFormatters.formatDate(earliestDeadline),
            itemRows,
            orderRows
        );
    }

    private RequestDetailItemRow toItemRow(RequestMerchandise item) {
        Merchandise m = catalogUseCase.findById(item.getMerchandiseId());
        return new RequestDetailItemRow(
            m != null ? m.getCode() : "N/A",
            m != null ? m.getName() : "N/A",
            item.getQuantityOrdered() != null ? OrderingFormatters.formatQuantity(item.getQuantityOrdered()) : "0",
            m != null ? m.getUnit() : "N/A",
            OrderingFormatters.formatDate(item.getDesiredDeliveryDate())
        );
    }

    private AllocatedOrderRow toOrderRow(Order order) {
        Site site = siteService.findById(order.getSiteId());
        String deliveryMethod = resolvePrimaryDeliveryMethod(orderService.findItemsByOrderId(order.getId()));
        String statusKey = OrderingFormatters.normalizeStatusKey(order.getStatus());
        return new AllocatedOrderRow(
            order.getId(),
            OrderingFormatters.formatOrderCode(order.getId()),
            site != null ? site.getName() : "N/A",
            OrderingFormatters.deliveryMethodText(deliveryMethod),
            OrderingFormatters.formatDateOrEmpty(order.getCreatedAt()),
            order.getStatus(),
            OrderingFormatters.orderStatusText(order.getStatus()),
            "pending".equals(statusKey)
        );
    }

    public AllocatedOrderRow findOrderRow(int orderId) {
        Order order = orderService.findById(orderId);
        if (order == null) return null;
        return toOrderRow(order);
    }

    private String resolvePrimaryDeliveryMethod(List<OrderMerchandise> items) {
        if (items == null || items.isEmpty()) {
            return "N/A";
        }
        return items.get(0).getDeliveryMethod() != null ? items.get(0).getDeliveryMethod() : "N/A";
    }
}
