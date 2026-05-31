package org.itss.prj_itss.model.request.application.international.detail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReceivedRequestDetailQueryPort {

    // Lấy thông tin chung của Request + Earliest deadline (1 câu SQL)
    RequestSummary findRequestSummary(int requestId);

    // Lấy danh sách mặt hàng đã JOIN tên merchandise (1 câu SQL)
    List<RequestItemProjection> findRequestItems(int requestId);

    // Lấy danh sách đơn hàng đã JOIN tên site + delivery method (1 câu SQL)
    List<AllocatedOrderProjection> findAllocatedOrders(int requestId);
    
    // Lấy một đơn hàng cụ thể theo ID
    AllocatedOrderProjection findAllocatedOrderById(int orderId);

    // Projection records (data objects returned by queries)
    record RequestSummary(
        int id,
        LocalDateTime createdAt,
        String status,
        String note,
        LocalDate earliestDeliveryDate
    ) {}

    record RequestItemProjection(
        String merchandiseCode,
        String merchandiseName,
        java.math.BigDecimal quantity,
        String unit,
        LocalDate desiredDeliveryDate
    ) {}

    record AllocatedOrderProjection(
        int orderId,
        String orderCode,
        String siteName,
        String deliveryMethod,
        LocalDateTime createdAt,
        String status
    ) {}
}
