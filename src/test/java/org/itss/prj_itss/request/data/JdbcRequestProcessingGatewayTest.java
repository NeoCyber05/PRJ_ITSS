package org.itss.prj_itss.request.data;

import org.itss.prj_itss.common.config.ITransactionRunner;
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
import org.itss.prj_itss.request.business.model.Allocation;
import org.itss.prj_itss.request.business.model.DeliveryMethod;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcRequestProcessingGatewayTest {
    @Test
    void createsOneOrderPerAllocatedSiteAndUpdatesRequest() throws SQLException {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        RecordingRequestRepository requestRepository = new RecordingRequestRepository();
        RecordingTransactionRunner transactionRunner = new RecordingTransactionRunner();
        JdbcRequestProcessingGateway gateway = gateway(requestRepository, orderRepository, transactionRunner);

        Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();
        allocations.put(10, Map.of(
            1, allocation(1, 10, 2),
            2, allocation(2, 10, 3)
        ));

        gateway.createAllocatedOrders(99, allocations);

        assertEquals(1, transactionRunner.commits);
        assertEquals(2, orderRepository.createdOrders.size());
        assertEquals(2, orderRepository.createdItems.size());
        assertEquals("processing", requestRepository.updatedStatus);
    }

    @Test
    void rollsBackWhenOrderLineCannotBeCreated() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        orderRepository.failAddItem = true;
        RecordingTransactionRunner transactionRunner = new RecordingTransactionRunner();
        JdbcRequestProcessingGateway gateway = gateway(
            new RecordingRequestRepository(),
            orderRepository,
            transactionRunner
        );

        assertThrows(SQLException.class, () -> gateway.createAllocatedOrders(
            99,
            Map.of(10, Map.of(1, allocation(1, 10, 2)))
        ));
        assertEquals(1, transactionRunner.rollbacks);
    }

    private JdbcRequestProcessingGateway gateway(
        IRequestRepository requestRepository,
        IOrderRepository orderRepository,
        ITransactionRunner transactionRunner
    ) {
        return new JdbcRequestProcessingGateway(
            requestRepository,
            orderRepository,
            new EmptySiteRepository(),
            new EmptyInventoryRepository(),
            new EmptyMerchandiseRepository(),
            transactionRunner
        );
    }

    private Allocation allocation(int siteId, int merchandiseId, int quantity) {
        return new Allocation(siteId, merchandiseId, quantity, DeliveryMethod.SHIP.storageValue());
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
        public List<Order> findByStatus(String status) {
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

    private static final class RecordingRequestRepository implements IRequestRepository {
        private String updatedStatus;

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
            updatedStatus = newStatus;
            return true;
        }

        @Override
        public void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) {
        }

        @Override
        public int createRequest(List<RequestMerchandise> items, String note) {
            return 1;
        }

        @Override
        public boolean deleteById(int requestId) {
            return true;
        }
    }

    private static final class EmptySiteRepository implements ISiteRepository {
        @Override
        public List<Site> findAll() {
            return List.of();
        }

        @Override
        public List<Site> findAvailableForMerchandiseIds(List<Integer> merchandiseIds) {
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
