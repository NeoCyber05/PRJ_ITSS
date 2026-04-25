package org.itss.prj_itss.service;

import org.itss.prj_itss.common.config.ITransactionRunner;
import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.entity.RequestMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.repository.IInventoryRepository;
import org.itss.prj_itss.repository.IMerchandiseRepository;
import org.itss.prj_itss.repository.IOrderRepository;
import org.itss.prj_itss.repository.IRequestRepository;
import org.itss.prj_itss.repository.ISiteRepository;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestProcessingServiceTest {

    @Test
    void validatesEnoughMissingAndExcessQuantity() {
        RequestProcessingService service = serviceWith(new FakeOrderRepository(), new RecordingTransactionRunner());
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 5);

        Map<Integer, Map<Integer, Allocation>> missing = allocations(item.merchandiseId, allocation(1, item.merchandiseId, 3));
        Map<Integer, Map<Integer, Allocation>> exact = allocations(item.merchandiseId, allocation(1, item.merchandiseId, 5));
        Map<Integer, Map<Integer, Allocation>> excess = allocations(item.merchandiseId, allocation(1, item.merchandiseId, 7));

        assertEquals(1, service.validateAllocations(List.of(item), missing).size());
        assertTrue(service.validateAllocations(List.of(item), exact).isEmpty());
        assertEquals(1, service.validateAllocations(List.of(item), excess).size());
    }

    @Test
    void createsOneOrderPerAllocatedSite() throws SQLException {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        RecordingTransactionRunner transactionRunner = new RecordingTransactionRunner();
        RequestProcessingService service = serviceWith(orderRepository, transactionRunner);

        Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();
        allocations.put(10, Map.of(
            1, allocation(1, 10, 2),
            2, allocation(2, 10, 3)
        ));

        service.createAllocatedOrders(99, allocations);

        assertEquals(1, transactionRunner.commits);
        assertEquals(2, orderRepository.createdOrders.size());
        assertEquals(2, orderRepository.createdItems.size());
    }

    @Test
    void rollsBackWhenOrderLineCannotBeCreated() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        orderRepository.failAddItem = true;
        RecordingTransactionRunner transactionRunner = new RecordingTransactionRunner();
        RequestProcessingService service = serviceWith(orderRepository, transactionRunner);

        Map<Integer, Map<Integer, Allocation>> allocations = allocations(10, allocation(1, 10, 2));

        assertThrows(SQLException.class, () -> service.createAllocatedOrders(99, allocations));
        assertEquals(1, transactionRunner.rollbacks);
    }

    @Test
    void rejectsSubmissionWhenDesiredDeliveryCannotBeMet() {
        RequestProcessingService service = serviceWith(new FakeOrderRepository(), new RecordingTransactionRunner());
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 5);
        SiteStockOption site = new SiteStockOption(1, "S1", "Site 1", "", 6, 2, Map.of(item.merchandiseId, 5));

        String validationMessage = service.validateSubmission(
            List.of(item),
            List.of(site),
            allocations(item.merchandiseId, allocation(1, item.merchandiseId, 5)),
            Map.of(item.merchandiseId, LocalDate.now().plusDays(3)),
            14
        );

        assertEquals("Không đáp ứng ngày nhận mong muốn", validationMessage);
    }

    @Test
    void acceptsSubmissionWhenDesiredDeliveryCanBeMet() {
        RequestProcessingService service = serviceWith(new FakeOrderRepository(), new RecordingTransactionRunner());
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 5);
        SiteStockOption site = new SiteStockOption(1, "S1", "Site 1", "", 2, 5, Map.of(item.merchandiseId, 5));

        String validationMessage = service.validateSubmission(
            List.of(item),
            List.of(site),
            allocations(item.merchandiseId, allocation(1, item.merchandiseId, 5)),
            Map.of(item.merchandiseId, LocalDate.now().plusDays(4)),
            14
        );

        assertNull(validationMessage);
    }

    private RequestProcessingService serviceWith(
        FakeOrderRepository orderRepository,
        RecordingTransactionRunner transactionRunner
    ) {
        return new RequestProcessingService(
            new EmptyRequestRepository(),
            orderRepository,
            new EmptySiteRepository(),
            new EmptyInventoryRepository(),
            new EmptyMerchandiseRepository(),
            transactionRunner
        );
    }

    private Map<Integer, Map<Integer, Allocation>> allocations(int merchandiseId, Allocation allocation) {
        Map<Integer, Map<Integer, Allocation>> result = new LinkedHashMap<>();
        result.put(merchandiseId, Map.of(allocation.siteId, allocation));
        return result;
    }

    private Allocation allocation(int siteId, int merchandiseId, int quantity) {
        return new Allocation(siteId, merchandiseId, quantity, AllocationPlanningService.TRANSPORT_SHIP);
    }

    private static final class RecordingTransactionRunner implements ITransactionRunner {
        private int commits;
        private int rollbacks;

        @Override
        public void execute(ITransactionCallback callback) throws SQLException {
            try {
                callback.execute();
                commits++;
            } catch (SQLException | RuntimeException exception) {
                rollbacks++;
                throw exception;
            }
        }
    }

    private static final class FakeOrderRepository implements IOrderRepository {
        private final List<Order> createdOrders = new ArrayList<>();
        private final List<OrderMerchandise> createdItems = new ArrayList<>();
        private boolean failAddItem;
        private int nextOrderId = 1;

        @Override
        public List<Order> findAll() {
            return List.of();
        }

        @Override
        public Order findById(int id) {
            return null;
        }

        @Override
        public List<OrderMerchandise> findItemsByOrderId(int orderId) {
            return List.of();
        }

        @Override
        public int create(Order order) {
            order.setId(nextOrderId++);
            createdOrders.add(order);
            return order.getId();
        }

        @Override
        public boolean addItem(OrderMerchandise item) {
            if (failAddItem) {
                return false;
            }
            createdItems.add(item);
            return true;
        }

        @Override
        public boolean updateStatus(int orderId, String newStatus) {
            return true;
        }
    }

    private static final class EmptyRequestRepository implements IRequestRepository {
        @Override
        public List<Request> findAll() {
            return List.of();
        }

        @Override
        public Request findById(int id) {
            return null;
        }

        @Override
        public List<RequestMerchandise> findItemsByRequestId(int requestId) {
            return List.of();
        }

        @Override
        public int countItemTypes(int requestId) {
            return 0;
        }

        @Override
        public LocalDate getEarliestDeliveryDate(int requestId) {
            return null;
        }

        @Override
        public boolean updateStatus(int requestId, String newStatus) {
            return true;
        }
    }

    private static final class EmptySiteRepository implements ISiteRepository {
        @Override
        public List<Site> findAll() {
            return List.of();
        }

        @Override
        public Site findById(int id) {
            return null;
        }

        @Override
        public Site findBySiteCode(String siteCode) {
            return null;
        }

        @Override
        public int countAll() {
            return 0;
        }
    }

    private static final class EmptyInventoryRepository implements IInventoryRepository {
        @Override
        public Map<Integer, Integer> getInventoryBySiteId(int siteId) {
            return Map.of();
        }

        @Override
        public int getStockQuantity(int siteId, int merchandiseId) {
            return 0;
        }

        @Override
        public int getTotalStock(int merchandiseId) {
            return 0;
        }

        @Override
        public int countMerchandiseAtSite(int siteId) {
            return 0;
        }
    }

    private static final class EmptyMerchandiseRepository implements IMerchandiseRepository {
        @Override
        public List<Merchandise> findAll() {
            return List.of();
        }

        @Override
        public Merchandise findById(int id) {
            return null;
        }

        @Override
        public Merchandise findByCode(String code) {
            return null;
        }

        @Override
        public int countAll() {
            return 0;
        }
    }
}
