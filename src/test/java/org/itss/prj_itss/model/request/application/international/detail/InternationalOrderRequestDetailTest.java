package org.itss.prj_itss.model.request.application.international.detail;

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

    // ==========================================
    // 3. Input Validation (Kiểm tra đầu vào)
    // ==========================================
    @Test
    @DisplayName("BB_03: Mã request null → trả ViewModel rỗng (không load nhầm)")
    void testLoad_NullRequestCode() {
        // Không cần setup stubPort — load() phải dừng sớm mà không gọi queryPort
        ReceivedRequestDetailViewModel result = service.load(null);

        assertNotNull(result, "Phải trả về ViewModel rỗng, không được null");
        assertEquals(0, result.requestId(), "requestId phải = 0 vì input không hợp lệ");
        assertEquals("N/A", result.statusText());
        assertTrue(result.requestItems().isEmpty());
        assertTrue(result.allocatedOrders().isEmpty());
    }

    @Test
    @DisplayName("BB_04: Mã request rỗng → trả ViewModel rỗng")
    void testLoad_EmptyRequestCode() {
        ReceivedRequestDetailViewModel result = service.load("");

        assertNotNull(result);
        assertEquals(0, result.requestId());
        assertEquals("N/A", result.statusText());
        assertTrue(result.requestItems().isEmpty());
    }

    @Test
    @DisplayName("BB_05: Mã request không chứa số → trả ViewModel rỗng")
    void testLoad_NoNumberInRequestCode() {
        ReceivedRequestDetailViewModel result = service.load("ABC-XYZ");

        assertNotNull(result);
        assertEquals(0, result.requestId());
        assertEquals("N/A", result.statusText());
        assertTrue(result.requestItems().isEmpty());
    }

    // ==========================================
    // 4. Status Mapping (Kiểm tra ánh xạ trạng thái)
    // ==========================================
    @Test
    @DisplayName("BB_10: Status unknown hiển thị nguyên giá trị (graceful degradation)")
    void testLoad_UnknownStatus() {
        stubPort.summary = new ReceivedRequestDetailQueryPort.RequestSummary(
            5, LocalDateTime.now(), "xyz_custom_status", null, null
        );
        stubPort.items = List.of();
        stubPort.orders = List.of();

        ReceivedRequestDetailViewModel result = service.load("YC-2026-005");

        // Status unknown → hiển thị nguyên giá trị thay vì crash
        assertEquals("xyz_custom_status", result.statusText());
    }

    @Test
    @DisplayName("BB_11: Request status 'shipping' → 'Đang giao'")
    void testLoad_ShippingStatus() {
        stubPort.summary = new ReceivedRequestDetailQueryPort.RequestSummary(
            6, LocalDateTime.now(), "shipping", null, null
        );
        stubPort.items = List.of();
        stubPort.orders = List.of();

        ReceivedRequestDetailViewModel result = service.load("YC-2026-006");

        assertEquals("Đang giao", result.statusText());
    }

    @Test
    @DisplayName("BB_07: Request có items nhưng không có orders (chưa phân bổ)")
    void testLoad_ItemsButNoOrders() {
        stubPort.summary = new ReceivedRequestDetailQueryPort.RequestSummary(
            3, LocalDateTime.now(), "pending", "Note", LocalDate.now().plusDays(7)
        );
        stubPort.items = List.of(
            new ReceivedRequestDetailQueryPort.RequestItemProjection(
                "MBA13", "MacBook Air", BigDecimal.valueOf(10), "chiếc", LocalDate.now().plusDays(7)),
            new ReceivedRequestDetailQueryPort.RequestItemProjection(
                "IPH15", "iPhone 15", BigDecimal.valueOf(20), "chiếc", LocalDate.now().plusDays(14))
        );
        stubPort.orders = List.of();

        ReceivedRequestDetailViewModel result = service.load("YC-2026-003");

        assertEquals(2, result.requestItems().size());
        assertTrue(result.allocatedOrders().isEmpty());
    }

    // ==========================================
    // 5. Order Status & Cancellable (Trạng thái đơn hàng)
    // ==========================================
    @Test
    @DisplayName("BB_12: Order status 'removed' → 'Đã loại bỏ' (chỉ Order mới có)")
    void testFindOrderRow_RemovedStatus() {
        stubPort.singleOrder = new ReceivedRequestDetailQueryPort.AllocatedOrderProjection(
            103, null, "Hà Nội", "Đường biển", LocalDateTime.now(), "removed"
        );

        AllocatedOrderRow row = service.findOrderRow(103);

        assertNotNull(row);
        assertEquals("Đã loại bỏ", row.statusText());
        assertFalse(row.cancellable(), "Đơn 'removed' không thể hủy");
    }

    @Test
    @DisplayName("UC_09: Đơn hàng completed không có nút hủy")
    void testFindOrderRow_CompletedNotCancellable() {
        stubPort.singleOrder = new ReceivedRequestDetailQueryPort.AllocatedOrderProjection(
            102, null, "Tokyo", "Đường hàng không", LocalDateTime.now(), "completed"
        );

        AllocatedOrderRow row = service.findOrderRow(102);

        assertNotNull(row);
        assertEquals("Đã hoàn thành", row.statusText());
        assertFalse(row.cancellable(), "Đơn completed không thể hủy");
    }

    @Test
    @DisplayName("Order pending → cancellable = true, statusText = 'Chờ xác nhận'")
    void testFindOrderRow_PendingIsCancellable() {
        stubPort.singleOrder = new ReceivedRequestDetailQueryPort.AllocatedOrderProjection(
            104, null, "Singapore", "Đường biển", LocalDateTime.now(), "pending"
        );

        AllocatedOrderRow row = service.findOrderRow(104);

        assertNotNull(row);
        assertEquals("Chờ xác nhận", row.statusText(), "Order pending = 'Chờ xác nhận' (khác Request 'Chờ xử lý')");
        assertTrue(row.cancellable(), "Chỉ đơn pending mới được hủy");
    }

    @Test
    @DisplayName("findOrderRow trả null khi order không tồn tại")
    void testFindOrderRow_NotFound() {
        stubPort.singleOrder = null;

        AllocatedOrderRow row = service.findOrderRow(999);

        assertNull(row);
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
