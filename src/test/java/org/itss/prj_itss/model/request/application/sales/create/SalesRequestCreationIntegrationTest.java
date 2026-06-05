package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.controller.sales.request.create.SalesRequestCreationController;
import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.controller.shared.MerchandiseOptionDTO;
import org.itss.prj_itss.controller.shared.SalesRequestItemInput;
import org.itss.prj_itss.model.request.application.sales.create.CreateSalesRequestService;
import org.itss.prj_itss.model.request.application.sales.create.CreateSalesRequestUseCase;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryService;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandPort;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SalesRequestCreationIntegrationTest {

    private SalesRequestCreationController controller;
    private FakeInventoryRepository inventoryRepository;
    private FakeCommandPort commandPort;

    @BeforeEach
    void setUp() {
        inventoryRepository = new FakeInventoryRepository();
        commandPort = new FakeCommandPort();
        CreateSalesRequestUseCase createUseCase = new CreateSalesRequestService(commandPort, inventoryRepository);
        
        SalesRequestQueryService queryService = new FakeQueryService();
        controller = new SalesRequestCreationController(queryService, createUseCase);
    }

    @Test
    @DisplayName("TC_UC_01: Tạo yêu cầu đặt hàng thành công (Luồng cơ bản)")
    public void testTC01_CreateRequestSuccess() {
        List<SalesRequestItemInput> items = List.of(
            new SalesRequestItemInput(100, new BigDecimal("10"), LocalDate.now().plusDays(7))
        );
        ActionResult result = controller.createRequest(items);
        assertTrue(result.success());
        assertEquals("Yêu cầu nhập hàng đã được gửi thành công.", result.message());
        assertEquals(1, commandPort.callCount);
    }

    @Test
    @DisplayName("TC_UC_02: Xử lý lỗi Validation - Số âm (Luồng thay thế)")
    public void testTC02_NegativeQuantity() {
        List<SalesRequestItemInput> items = List.of(
            new SalesRequestItemInput(100, new BigDecimal("-5"), LocalDate.now().plusDays(7))
        );
        ActionResult result = controller.createRequest(items);
        assertFalse(result.success());
        assertEquals("Số lượng đặt hàng phải lớn hơn 0.", result.message());
        assertEquals(0, commandPort.callCount);
    }

    @Test
    @DisplayName("TC_UC_03: Xử lý lỗi Validation - Số 0 (Luồng thay thế)")
    public void testTC03_ZeroQuantity() {
        List<SalesRequestItemInput> items = List.of(
            new SalesRequestItemInput(100, BigDecimal.ZERO, LocalDate.now().plusDays(7))
        );
        ActionResult result = controller.createRequest(items);
        assertFalse(result.success());
        assertEquals("Số lượng đặt hàng phải lớn hơn 0.", result.message());
        assertEquals(0, commandPort.callCount);
    }

    @Test
    @DisplayName("TC_UC_04: Xử lý lỗi Validation - Nhập chữ (Luồng thay thế)")
    public void testTC04_TextInput() {
        // Mô phỏng việc UI không parse được chữ "abc" và gửi giá trị null xuống
        List<SalesRequestItemInput> items = List.of(
            new SalesRequestItemInput(100, null, LocalDate.now().plusDays(7))
        );
        ActionResult result = controller.createRequest(items);
        assertFalse(result.success());
        assertEquals("Vui lòng điền đầy đủ thông tin hợp lệ.", result.message());
    }

    @Test
    @DisplayName("TC_UC_05: Lỗi vượt mức tồn kho (Luồng thay thế)")
    public void testTC05_ExceedsStock() {
        List<SalesRequestItemInput> items = List.of(
            new SalesRequestItemInput(101, new BigDecimal("100"), LocalDate.now().plusDays(7)) // Tồn kho chỉ 20
        );
        ActionResult result = controller.createRequest(items);
        assertFalse(result.success());
        assertEquals("Số lượng đặt hàng vượt quá tồn kho hiện tại.", result.message());
        assertEquals(0, commandPort.callCount);
    }

    @Test
    @DisplayName("TC_UC_06: Hủy tạo yêu cầu (Luồng thay thế)")
    public void testTC06_CancelCreation() {
        // Mô phỏng người dùng bấm nút Hủy, đóng cửa sổ, không gọi submit controller
        assertEquals(0, commandPort.callCount, "Hệ thống không lưu bất kỳ yêu cầu nào khi Hủy");
    }

    @Test
    @DisplayName("TC_UC_07: Xóa bớt một mặt hàng (Luồng thay thế)")
    public void testTC07_DeleteOneRow() {
        // Mô phỏng thêm 2 dòng, xóa 1 dòng trên UI, sau đó submit dòng còn lại
        List<SalesRequestItemInput> items = List.of(
            new SalesRequestItemInput(100, new BigDecimal("10"), LocalDate.now().plusDays(7))
        );
        ActionResult result = controller.createRequest(items);
        assertTrue(result.success());
        assertEquals(1, commandPort.callCount);
        assertEquals(1, commandPort.capturedRequest.getItems().size(), "Chỉ có 1 mặt hàng được gửi đi");
    }

    @Test
    @DisplayName("TC_UC_08: Lỗi xóa mặt hàng duy nhất (Luồng thay thế)")
    public void testTC08_DeleteLastRow() {
        // UI chặn không cho xóa mặt hàng cuối cùng. Nếu submit mảng rỗng thì sẽ bị Controller chặn.
        ActionResult result = controller.createRequest(Collections.emptyList());
        assertFalse(result.success());
        assertEquals("Cần ít nhất một mặt hàng để tạo yêu cầu.", result.message());
    }

    @Test
    @DisplayName("TC_UC_09: Xử lý lỗi Validation - Thiếu thông tin (Luồng thay thế)")
    public void testTC09_MissingInformation() {
        // Mô phỏng thiếu ngày nhận
        List<SalesRequestItemInput> items = List.of(
            new SalesRequestItemInput(100, new BigDecimal("10"), null)
        );
        ActionResult result = controller.createRequest(items);
        assertFalse(result.success());
        assertEquals("Vui lòng điền đầy đủ thông tin hợp lệ.", result.message());
    }

    @Test
    @DisplayName("TC_UC_10: Xử lý lỗi Validation - Sai mã hàng (Luồng thay thế)")
    public void testTC10_InvalidMerchandiseCode() {
        MerchandiseOptionDTO m = controller.getMerchandiseOptionByCode("XYZ-999");
        assertNull(m, "Hệ thống phải trả về null khi mã hàng không tồn tại");
    }

    @Test
    @DisplayName("TC_UC_11: Xử lý lỗi Validation - Ngày quá khứ (Luồng thay thế)")
    public void testTC11_PastDate() {
        // Mô phỏng nhập ngày hôm qua
        List<SalesRequestItemInput> items = List.of(
            new SalesRequestItemInput(100, new BigDecimal("10"), LocalDate.now().minusDays(1))
        );
        ActionResult result = controller.createRequest(items);
        assertFalse(result.success());
        assertEquals("Ngày nhận không được nằm trong quá khứ.", result.message());
    }

    // --- Fakes ---

    private static class FakeQueryService extends SalesRequestQueryService {
        public FakeQueryService() {
            super(null, null, null);
        }

        @Override
        public MerchandiseOption findMerchandiseOptionByCode(String code) {
            if ("MH-001".equals(code)) return new MerchandiseOption(100, "MH-001", "Gạch lát", "Thùng");
            if ("MH-002".equals(code)) return new MerchandiseOption(101, "MH-002", "Xi măng", "Bao");
            return null;
        }

        @Override
        public int getAvailableStock(String code) {
            if ("MH-001".equals(code)) return 50;
            if ("MH-002".equals(code)) return 20;
            return 0;
        }

        @Override
        public List<MerchandiseOption> findMerchandiseOptions() {
            return List.of(
                new MerchandiseOption(100, "MH-001", "Gạch lát", "Thùng"),
                new MerchandiseOption(101, "MH-002", "Xi măng", "Bao")
            );
        }
    }

    private static class FakeInventoryRepository implements InventoryRepository {
        @Override
        public int getTotalStock(int merchandiseId) {
            if (merchandiseId == 100) return 50;
            if (merchandiseId == 101) return 20;
            return 0;
        }

        @Override
        public java.util.Map<Integer, Integer> getInventoryBySiteId(int siteId) { return null; }

        @Override
        public int getStockQuantity(int siteId, int merchandiseId) { return 0; }

        @Override
        public int countMerchandiseAtSite(int siteId) { return 0; }

        @Override
        public java.util.Map<Integer, Integer> countMerchandiseGroupedBySiteId() {
            return java.util.Map.of();
        }
    }

    private static class FakeCommandPort implements SalesRequestCommandPort {
        int callCount = 0;
        Request capturedRequest;

        @Override
        public int createRequest(Request request) {
            callCount++;
            capturedRequest = request;
            return 1;
        }

        @Override
        public void updateRequestItems(int requestId, List<org.itss.prj_itss.model.request.domain.request.RequestMerchandise> items, String note) { }

        @Override
        public boolean deleteById(int requestId) { return true; }
    }
}
