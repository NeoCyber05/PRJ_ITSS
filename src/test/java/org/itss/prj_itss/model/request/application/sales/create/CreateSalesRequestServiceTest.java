package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandPort;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bộ kiểm thử CreateSalesRequestService")
public class CreateSalesRequestServiceTest {

    // --- MANUAL STUBS ---
    private static class StubCommandPort implements SalesRequestCommandPort {
        public int callCount = 0;
        public Request capturedRequest = null;
        public int returnId = 1;

        @Override
        public int createRequest(Request request) {
            this.callCount++;
            this.capturedRequest = request;
            return returnId;
        }

        @Override
        public void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) { }

        @Override
        public boolean deleteById(int requestId) { return true; }
    }

    private static class StubInventoryRepository implements InventoryRepository {
        public int callCount = 0;
        public int singleStockCallCount = 0;
        public int bulkStockCallCount = 0;
        public int returnStock = 0;
        public List<Integer> requestedBulkIds = List.of();
        public Map<Integer, Integer> stockByMerchandiseId = new LinkedHashMap<>();

        @Override
        public int getTotalStock(int merchandiseId) {
            this.callCount++;
            this.singleStockCallCount++;
            return returnStock;
        }

        @Override
        public Map<Integer, Integer> getTotalStockByMerchandiseIds(Collection<Integer> merchandiseIds) {
            this.callCount++;
            this.bulkStockCallCount++;
            this.requestedBulkIds = List.copyOf(merchandiseIds);

            Map<Integer, Integer> result = new LinkedHashMap<>();
            for (Integer merchandiseId : merchandiseIds) {
                result.put(merchandiseId, stockByMerchandiseId.getOrDefault(merchandiseId, returnStock));
            }
            return result;
        }

        @Override
        public Map<Integer, Integer> getInventoryBySiteId(int siteId) { return null; }

        @Override
        public int getStockQuantity(int siteId, int merchandiseId) { return 0; }

        @Override
        public int countMerchandiseAtSite(int siteId) { return 0; }

        @Override
        public Map<Integer, Integer> countMerchandiseGroupedBySiteId() { return Map.of(); }
    }

    private StubCommandPort commandPort;
    private StubInventoryRepository inventoryRepository;
    private CreateSalesRequestService service;

    @BeforeEach
    void setUp() {
        commandPort = new StubCommandPort();
        inventoryRepository = new StubInventoryRepository();
        service = new CreateSalesRequestService(commandPort, inventoryRepository);
    }

    /**
     * Kỹ thuật: Hộp trắng (Độ đo C1 - Không vào vòng lặp)
     * Test Case: WB_01
     * Mô tả: Truyền vào một danh sách rỗng. Vòng lặp for không được thực thi.
     */
    @Test
    @DisplayName("TC_01: danh sách mặt hàng rỗng hợp lệ")
    public void testCreateRequest_EmptyList() throws Exception {
        // Arrange
        List<SalesRequestItemSubmission> emptyList = new ArrayList<>();
        commandPort.returnId = 1;

        // Act
        int result = service.createRequest(emptyList, "Ghi chú trống");

        // Assert
        assertEquals(1, result);
        assertEquals(0, inventoryRepository.callCount, "InventoryRepository không nên được gọi");
        assertEquals(1, commandPort.callCount, "CommandPort phải được gọi 1 lần");
    }

    /**
     * Kỹ thuật: Hộp đen (Biên hợp lệ) + Hộp trắng (C1 - Vào vòng lặp, Không vào if lỗi)
     * Test Case: BB_01 / WB_02
     * Mô tả: Truyền vào số lượng đặt bằng đúng tồn kho hiện tại.
     */
    @Test
    @DisplayName("TC_02: yêu cầu số lượng bằng đúng số lượng tồn kho hợp lệ")
    public void testCreateRequest_ValidQuantity_EqualsStock() throws Exception {
        // Arrange
        int merchandiseId = 100;
        int stock = 50;
        BigDecimal orderQuantity = new BigDecimal("50"); // Số lượng bằng đúng tồn kho
        LocalDate futureDate = LocalDate.now().plusDays(5);

        List<SalesRequestItemSubmission> items = Collections.singletonList(
                new SalesRequestItemSubmission(merchandiseId, orderQuantity, futureDate)
        );

        inventoryRepository.returnStock = stock;
        commandPort.returnId = 2;

        // Act
        int result = service.createRequest(items, "Đặt bằng tồn kho");

        // Assert
        assertEquals(2, result);
        assertEquals(1, inventoryRepository.callCount, "InventoryRepository phải được gọi 1 lần");
        assertEquals(1, commandPort.callCount, "CommandPort phải được gọi 1 lần");

        Request capturedRequest = commandPort.capturedRequest;
        assertNotNull(capturedRequest);
        assertEquals(1, capturedRequest.getItems().size());
        assertEquals("Đặt bằng tồn kho", capturedRequest.getNote());
    }

    /**
     * Kỹ thuật: Hộp đen (Biên lỗi) + Hộp trắng (C1 - Vào vòng lặp, Vào if lỗi)
     * Test Case: BB_02 / WB_03
     * Mô tả: Truyền vào số lượng đặt lớn hơn tồn kho 1 đơn vị.
     */
    @Test
    @DisplayName("TC_03: số lượng yêu cầu vượt quá tồn kho bị từ chối")
    public void testCreateRequest_QuantityExceedsStock() {
        // Arrange
        int merchandiseId = 101;
        int stock = 20;
        BigDecimal orderQuantity = new BigDecimal("21"); // Vượt tồn kho 1 đơn vị
        LocalDate futureDate = LocalDate.now().plusDays(2);

        List<SalesRequestItemSubmission> items = Collections.singletonList(
                new SalesRequestItemSubmission(merchandiseId, orderQuantity, futureDate)
        );

        inventoryRepository.returnStock = stock;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createRequest(items, "Đặt vượt tồn kho");
        });

        assertEquals("Số lượng đặt hàng vượt quá tồn kho hiện tại.", exception.getMessage());

        assertEquals(1, inventoryRepository.callCount, "InventoryRepository phải được gọi 1 lần");
        assertEquals(0, commandPort.callCount, "CommandPort không nên được gọi vì có lỗi");
    }

    /**
     * Kỹ thuật: Hộp đen (Biên 0) và Hộp trắng (Nhánh quantity <= 0)
     * Test Case: BB_03 / WB_04
     * Mô tả: Số lượng yêu cầu bằng 0 bị từ chối.
     */
    @Test
    @DisplayName("TC_04: số lượng yêu cầu bằng 0 bị từ chối")
    public void testCreateRequest_ZeroQuantity() {
        // Arrange
        int merchandiseId = 100;
        BigDecimal orderQuantity = BigDecimal.ZERO;
        LocalDate futureDate = LocalDate.now().plusDays(5);

        List<SalesRequestItemSubmission> items = Collections.singletonList(
                new SalesRequestItemSubmission(merchandiseId, orderQuantity, futureDate)
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createRequest(items, "Ghi chú");
        });

        assertEquals("Số lượng đặt hàng phải lớn hơn 0.", exception.getMessage());
        assertEquals(0, inventoryRepository.callCount);
        assertEquals(0, commandPort.callCount);
    }

    /**
     * Kỹ thuật: Hộp đen (Lớp tương đương không hợp lệ)
     * Test Case: BB_04
     * Mô tả: Số lượng yêu cầu là số âm bị từ chối.
     */
    @Test
    @DisplayName("TC_05: số lượng yêu cầu là số âm bị từ chối")
    public void testCreateRequest_NegativeQuantity() {
        // Arrange
        int merchandiseId = 100;
        BigDecimal orderQuantity = new BigDecimal("-5");
        LocalDate futureDate = LocalDate.now().plusDays(5);

        List<SalesRequestItemSubmission> items = Collections.singletonList(
                new SalesRequestItemSubmission(merchandiseId, orderQuantity, futureDate)
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createRequest(items, "Ghi chú");
        });

        assertEquals("Số lượng đặt hàng phải lớn hơn 0.", exception.getMessage());
        assertEquals(0, inventoryRepository.callCount);
        assertEquals(0, commandPort.callCount);
    }

    /**
     * Kỹ thuật: Hộp đen (Lớp tương đương hợp lệ)
     * Test Case: BB_05
     * Mô tả: Số lượng yêu cầu lớn hơn 0 và nhỏ hơn tồn kho (0 < Q < Stock).
     */
    @Test
    @DisplayName("TC_06: số lượng yêu cầu nhỏ hơn tồn kho hợp lệ")
    public void testCreateRequest_ValidQuantity_LessThanStock() throws Exception {
        // Arrange
        int merchandiseId = 100;
        int stock = 50;
        BigDecimal orderQuantity = new BigDecimal("25"); // 0 < 25 < 50
        LocalDate futureDate = LocalDate.now().plusDays(5);

        List<SalesRequestItemSubmission> items = Collections.singletonList(
                new SalesRequestItemSubmission(merchandiseId, orderQuantity, futureDate)
        );

        inventoryRepository.returnStock = stock;
        commandPort.returnId = 4;

        // Act
        int result = service.createRequest(items, "Ghi chú hợp lệ");

        // Assert
        assertEquals(4, result);
        assertEquals(1, inventoryRepository.callCount);
        assertEquals(1, commandPort.callCount);
    }

    @Test
    @DisplayName("TC_07: kiểm tra tồn kho nhiều mặt hàng bằng một bulk query")
    public void testCreateRequest_MultipleItems_UsesBulkStockLookup() throws Exception {
        List<SalesRequestItemSubmission> items = List.of(
            new SalesRequestItemSubmission(100, new BigDecimal("5"), LocalDate.now().plusDays(5)),
            new SalesRequestItemSubmission(101, new BigDecimal("8"), LocalDate.now().plusDays(6))
        );
        inventoryRepository.stockByMerchandiseId.put(100, 10);
        inventoryRepository.stockByMerchandiseId.put(101, 12);

        int result = service.createRequest(items, "Bulk stock lookup");

        assertEquals(1, result);
        assertEquals(List.of(100, 101), inventoryRepository.requestedBulkIds);
        assertEquals(1, inventoryRepository.bulkStockCallCount);
        assertEquals(0, inventoryRepository.singleStockCallCount);
        assertEquals(1, commandPort.callCount);
    }
}
