package org.itss.prj_itss.model.request.application;

import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailApplicationService;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailQueryPort;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailViewModel;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailItemRow;
import org.itss.prj_itss.model.request.application.international.detail.AllocatedOrderRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class cho Use Case "Xem chi tiết yêu cầu đặt hàng"
 * Dành cho: Bộ phận đặt hàng quốc tế
 * Bám sát theo 4 phần: Basic flow, Alternative flows, Combinations, Infinite loops
 */
class InternationalOrderRequestDetailTest {

    private ReceivedRequestDetailApplicationService service;
    private StubRequestDetailQueryPort stubPort;

    @BeforeEach
    void setUp() {
        stubPort = new StubRequestDetailQueryPort();
        service = new ReceivedRequestDetailApplicationService(stubPort);
    }

    // ==========================================
    // 1. Basic Flow (Luồng thành công chính)
    // ==========================================
    @Test
    @DisplayName("TC_BF_01: Xem chi tiết yêu cầu đặt hàng thành công")
    void testViewRequestDetail_Success() {
        // Prepare valid data
        int validId = 1;
        
        stubPort.summary = new ReceivedRequestDetailQueryPort.RequestSummary(
            validId, LocalDateTime.of(2026, 4, 20, 10, 0), "pending", "Note", LocalDate.of(2026, 5, 23)
        );
        stubPort.items = List.of(
            new ReceivedRequestDetailQueryPort.RequestItemProjection("MBA13M4", "MacBook Air", BigDecimal.valueOf(35), "chiếc", LocalDate.of(2026, 5, 23))
        );
        stubPort.orders = List.of(
            new ReceivedRequestDetailQueryPort.AllocatedOrderProjection(101, null, "Singapore", "Đường biển", LocalDateTime.of(2026, 4, 20, 11, 0), "pending")
        );

        // Action
        ReceivedRequestDetailViewModel result = service.load("YC-2026-001");

        // Assert
        assertNotNull(result, "Hệ thống phải trả về ViewModel");
        assertEquals(validId, result.requestId());
        assertEquals("Chờ xử lý", result.statusText());
        assertEquals(1, result.requestItems().size());
        assertEquals("MacBook Air", result.requestItems().get(0).name());
        assertEquals(1, result.allocatedOrders().size());
        assertEquals("Singapore", result.allocatedOrders().get(0).siteName());
    }

    @Test
    @DisplayName("TC_BF_02: Lấy thông tin một đơn hàng thành công")
    void testFindOrderRow_Success() {
        stubPort.singleOrder = new ReceivedRequestDetailQueryPort.AllocatedOrderProjection(101, null, "Singapore", "Đường biển", LocalDateTime.of(2026, 4, 20, 11, 0), "cancelled");
        
        AllocatedOrderRow row = service.findOrderRow(101);
        
        assertNotNull(row);
        assertEquals("Đã hủy", row.statusText());
        assertFalse(row.cancellable(), "Đơn đã hủy không thể hủy tiếp");
    }

    // ==========================================
    // 2. Alternative Flows (Các kịch bản ngoại lệ/lỗi)
    // ==========================================
    @Test
    @DisplayName("TC_AF_01: Lỗi truy xuất cơ sở dữ liệu (Database Error)")
    void testViewRequestDetail_DatabaseError() {
        stubPort.shouldThrowException = new RuntimeException("Database connection error");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.load("YC-2026-001");
        });

        assertTrue(exception.getMessage().contains("Database"));
    }

    @Test
    @DisplayName("TC_AF_02: Yêu cầu đặt hàng không tồn tại (Not Found)")
    void testViewRequestDetail_NotFound() {
        stubPort.summary = null; // Query returns null

        ReceivedRequestDetailViewModel result = service.load("YC-2026-999");

        // Theo thiết kế, Application Service trả về ViewModel rỗng ("N/A") để tránh crash UI
        assertNotNull(result);
        assertEquals("N/A", result.statusText());
        assertTrue(result.requestItems().isEmpty());
        assertTrue(result.allocatedOrders().isEmpty());
    }

    @Test
    @DisplayName("TC_AF_04: Dữ liệu bị thiếu (Data Corruption/Missing)")
    void testViewRequestDetail_MissingData() {
        // Dữ liệu bị thiếu các trường (VD: status null, note null do lỗi database)
        stubPort.summary = new ReceivedRequestDetailQueryPort.RequestSummary(2, null, null, null, null);
        
        ReceivedRequestDetailViewModel result = service.load("YC-2026-002");

        // Không bị NullPointerException khi map dữ liệu
        assertEquals("N/A", result.statusText());
        assertEquals("N/A", result.earliestDeadline());
        assertEquals("", result.createdAt());
    }

    /**
     * Stub class đóng vai trò làm Test Double thay thế cho Query Port
     */
    private static class StubRequestDetailQueryPort implements ReceivedRequestDetailQueryPort {

        public RequestSummary summary;
        public List<RequestItemProjection> items = List.of();
        public List<AllocatedOrderProjection> orders = List.of();
        public AllocatedOrderProjection singleOrder;
        public RuntimeException shouldThrowException;

        @Override
        public RequestSummary findRequestSummary(int requestId) {
            if (shouldThrowException != null) throw shouldThrowException;
            return summary;
        }

        @Override
        public List<RequestItemProjection> findRequestItems(int requestId) {
            if (shouldThrowException != null) throw shouldThrowException;
            return items;
        }

        @Override
        public List<AllocatedOrderProjection> findAllocatedOrders(int requestId) {
            if (shouldThrowException != null) throw shouldThrowException;
            return orders;
        }
        
        @Override
        public AllocatedOrderProjection findAllocatedOrderById(int orderId) {
            if (shouldThrowException != null) throw shouldThrowException;
            return singleOrder;
        }
    }
}
