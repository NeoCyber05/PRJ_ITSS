package org.itss.prj_itss.model.request.application.international.detail;

import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailQueryPort;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailQueryPort.RequestItemProjection;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailQueryPort.AllocatedOrderProjection;

import java.util.List;
import java.util.Objects;

public final class ReceivedRequestDetailApplicationService {

    private final ReceivedRequestDetailQueryPort queryPort;

    public ReceivedRequestDetailApplicationService(ReceivedRequestDetailQueryPort queryPort) {
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort");
    }

    public ReceivedRequestDetailViewModel load(String requestCode) {
        int requestId = OrderingFormatters.parseEntityId(requestCode, 1);
        
        ReceivedRequestDetailQueryPort.RequestSummary summary = queryPort.findRequestSummary(requestId);
        if (summary == null) {
            return emptyViewModel(requestId, requestCode);
        }

        List<ReceivedRequestDetailItemRow> itemRows = queryPort.findRequestItems(requestId).stream()
            .map(this::toItemRow)
            .toList();

        List<AllocatedOrderRow> orderRows = queryPort.findAllocatedOrders(requestId).stream()
            .map(this::toOrderRow)
            .toList();

        return new ReceivedRequestDetailViewModel(
            summary.id(),
            OrderingFormatters.formatRequestCode(summary.id()),
            OrderingFormatters.formatDateOrEmpty(summary.createdAt()),
            summary.status() != null ? OrderingFormatters.normalizeStatusKey(summary.status()) : "N/A",
            summary.status() != null ? OrderingFormatters.requestStatusText(summary.status()) : "N/A",
            summary.note(),
            summary.earliestDeliveryDate() != null ? OrderingFormatters.formatDate(summary.earliestDeliveryDate()) : "N/A",
            itemRows,
            orderRows
        );
    }

    public AllocatedOrderRow findOrderRow(int orderId) {
        AllocatedOrderProjection order = queryPort.findAllocatedOrderById(orderId);
        if (order == null) {
            return null;
        }
        return toOrderRow(order);
    }

    private ReceivedRequestDetailViewModel emptyViewModel(int requestId, String requestCode) {
        return new ReceivedRequestDetailViewModel(
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

    private ReceivedRequestDetailItemRow toItemRow(RequestItemProjection item) {
        return new ReceivedRequestDetailItemRow(
            item.merchandiseCode() != null ? item.merchandiseCode() : "N/A",
            item.merchandiseName() != null ? item.merchandiseName() : "N/A",
            item.quantity() != null ? OrderingFormatters.formatQuantity(item.quantity()) : "0",
            item.unit() != null ? item.unit() : "N/A",
            item.desiredDeliveryDate() != null ? OrderingFormatters.formatDate(item.desiredDeliveryDate()) : "N/A"
        );
    }

    private AllocatedOrderRow toOrderRow(AllocatedOrderProjection order) {
        String statusKey = order.status() != null ? OrderingFormatters.normalizeStatusKey(order.status()) : "N/A";
        return new AllocatedOrderRow(
            order.orderId(),
            OrderingFormatters.formatOrderCode(order.orderId()),
            order.siteName() != null ? order.siteName() : "N/A",
            order.deliveryMethod() != null ? OrderingFormatters.deliveryMethodText(order.deliveryMethod()) : "N/A",
            order.createdAt() != null ? OrderingFormatters.formatDateOrEmpty(order.createdAt()) : "N/A",
            order.status(),
            order.status() != null ? OrderingFormatters.orderStatusText(order.status()) : "N/A",
            "pending".equals(statusKey)
        );
    }
}
