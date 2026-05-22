package org.itss.prj_itss.model.request.infrastructure.persistence;

import org.itss.prj_itss.common.config.ITransactionRunner;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.request.domain.allocation.model.Allocation;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.processing.RequestProcessingData;
import org.itss.prj_itss.model.request.application.port.RequestProcessingGatewayException;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.catalog.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.request.application.port.RequestRepository;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
    void loadProcessingDataOnlyQueriesSitesAvailableForRequestedMerchandise() {
        RecordingSiteRepository siteRepository = new RecordingSiteRepository(List.of(
            new Site(1, "S1", "Site 1", "", 2, 1)
        ));
        JdbcRequestProcessingGateway gateway = new JdbcRequestProcessingGateway(
            new RecordingRequestRepository() {
                @Override
                public List<RequestMerchandise> findItemsByRequestId(int requestId) {
                    return List.of(
                        new RequestMerchandise(requestId, 10, BigDecimal.valueOf(2), LocalDate.now().plusDays(7)),
                        new RequestMerchandise(requestId, 11, BigDecimal.valueOf(3), LocalDate.now().plusDays(8))
                    );
                }
            },
            new FakeOrderRepository(),
            siteRepository,
            new EmptyInventoryRepository() {
                @Override
                public Map<Integer, Integer> getInventoryBySiteId(int siteId) {
                    return Map.of(10, 5);
                }
            },
            new EmptyMerchandiseRepository() {
                @Override
                public Merchandise findById(int id) {
                    return new Merchandise(id, "M" + id, "Item " + id, "pcs");
                }
            },
            new RecordingTransactionRunner()
        );

        RequestProcessingData data = gateway.loadProcessingData(99);

        assertEquals(List.of(10, 11), siteRepository.requestedMerchandiseIds);
        assertEquals(1, data.sites().size());
        assertEquals(1, data.sites().get(0).id);
    }

    @Test
    void createsOneOrderPerAllocatedSiteAndUpdatesRequest() throws RequestProcessingGatewayException {
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

        assertThrows(RequestProcessingGatewayException.class, () -> gateway.createAllocatedOrders(
            99,
            Map.of(10, Map.of(1, allocation(1, 10, 2)))
        ));
        assertEquals(1, transactionRunner.rollbacks);
    }

    private JdbcRequestProcessingGateway gateway(
        RequestRepository requestRepository,
        OrderRepository orderRepository,
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

    private static final class FakeOrderRepository implements OrderRepository {
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

    private static final class RecordingSiteRepository extends EmptySiteRepository {
        private final List<Site> result;
        private List<Integer> requestedMerchandiseIds = List.of();

        private RecordingSiteRepository(List<Site> result) {
            this.result = result;
        }

        @Override
        public List<Site> findAll() {
            throw new AssertionError("Request processing should not query all sites");
        }

        @Override
        public List<Site> findAvailableForMerchandiseIds(List<Integer> merchandiseIds) {
            requestedMerchandiseIds = merchandiseIds;
            return result;
        }
    }

    private static class RecordingRequestRepository implements RequestRepository {
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

    private static class EmptySiteRepository implements SiteRepository {
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

    private static class EmptyInventoryRepository implements InventoryRepository {
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

    private static class EmptyMerchandiseRepository implements MerchandiseRepository {
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
