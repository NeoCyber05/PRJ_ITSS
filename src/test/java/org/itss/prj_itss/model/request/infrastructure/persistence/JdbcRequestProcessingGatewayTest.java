package org.itss.prj_itss.model.request.infrastructure.persistence;

import org.itss.prj_itss.model.shared.database.TransactionException;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.request.domain.processing.allocation.Allocation;
import org.itss.prj_itss.model.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.model.request.domain.processing.RequestProcessingData;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingGatewayException;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.request.application.processing.ProcessingRequestPort;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
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
            new RecordingProcessingRequestPort() {
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
                public List<Merchandise> findAll() {
                    return List.of(
                        new Merchandise(10, "M10", "Item 10", "pcs"),
                        new Merchandise(11, "M11", "Item 11", "pcs")
                    );
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
    void loadProcessingDataLoadsSiteInventoriesInOneBulkLookup() {
        RecordingSiteRepository siteRepository = new RecordingSiteRepository(List.of(
            new Site(1, "S1", "Site 1", "", 2, 1),
            new Site(2, "S2", "Site 2", "", 3, 2)
        ));
        RecordingInventoryRepository inventoryRepository = new RecordingInventoryRepository();
        inventoryRepository.inventoriesBySiteId.put(1, Map.of(10, 5));
        inventoryRepository.inventoriesBySiteId.put(2, Map.of(10, 4));

        JdbcRequestProcessingGateway gateway = new JdbcRequestProcessingGateway(
            new RecordingProcessingRequestPort() {
                @Override
                public List<RequestMerchandise> findItemsByRequestId(int requestId) {
                    return List.of(
                        new RequestMerchandise(requestId, 10, BigDecimal.valueOf(2), LocalDate.now().plusDays(7))
                    );
                }
            },
            new FakeOrderRepository(),
            siteRepository,
            inventoryRepository,
            new EmptyMerchandiseRepository() {
                @Override
                public List<Merchandise> findAll() {
                    return List.of(new Merchandise(10, "M10", "Item 10", "pcs"));
                }
            },
            new RecordingTransactionRunner()
        );

        RequestProcessingData data = gateway.loadProcessingData(99);

        assertEquals(List.of(1, 2), inventoryRepository.requestedBulkSiteIds);
        assertEquals(1, inventoryRepository.bulkInventoryCallCount);
        assertEquals(0, inventoryRepository.singleInventoryCallCount);
        assertEquals(Map.of(10, 5), data.sites().get(0).stock);
        assertEquals(Map.of(10, 4), data.sites().get(1).stock);
    }

    @Test
    void createsOneOrderPerAllocatedSiteAndUpdatesRequest() throws RequestProcessingGatewayException {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        RecordingProcessingRequestPort requestPort = new RecordingProcessingRequestPort();
        RecordingTransactionRunner transactionRunner = new RecordingTransactionRunner();
        JdbcRequestProcessingGateway gateway = gateway(requestPort, orderRepository, transactionRunner);

        Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();
        allocations.put(10, Map.of(
            1, allocation(1, 10, 2),
            2, allocation(2, 10, 3)
        ));

        gateway.createAllocatedOrders(99, allocations);

        assertEquals(1, transactionRunner.commits);
        assertEquals(2, orderRepository.createdOrders.size());
        assertEquals(2, orderRepository.createdItems.size());
        assertEquals(RequestStatus.PROCESSING, requestPort.updatedStatus);
    }

    @Test
    void rollsBackWhenOrderLineCannotBeCreated() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        orderRepository.failAddItem = true;
        RecordingTransactionRunner transactionRunner = new RecordingTransactionRunner();
        JdbcRequestProcessingGateway gateway = gateway(
            new RecordingProcessingRequestPort(),
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
        ProcessingRequestPort requestPort,
        OrderRepository orderRepository,
        TransactionRunner transactionRunner
    ) {
        return new JdbcRequestProcessingGateway(
            requestPort,
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

    private static final class RecordingTransactionRunner implements TransactionRunner {
        private int commits;
        private int rollbacks;

        @Override
        public void execute(TransactionCallback callback) throws TransactionException {
            try {
                callback.execute();
                commits++;
            } catch (TransactionException | RuntimeException exception) {
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

        @Override
        public java.time.LocalDate findDesiredDeliveryDate(int orderId, int merchandiseId) {
            return null;
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

    private static class RecordingProcessingRequestPort implements ProcessingRequestPort {
        private RequestStatus updatedStatus;



        @Override
        public List<RequestMerchandise> findItemsByRequestId(int requestId) {
            return List.of();
        }



        @Override
        public LocalDate getEarliestDeliveryDate(int requestId) {
            return null;
        }

        @Override
        public boolean updateStatus(int requestId, RequestStatus newStatus) {
            updatedStatus = newStatus;
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

        @Override
        public Map<Integer, Integer> countMerchandiseGroupedBySiteId() {
            return Map.of();
        }
    }

    private static final class RecordingInventoryRepository extends EmptyInventoryRepository {
        private final Map<Integer, Map<Integer, Integer>> inventoriesBySiteId = new LinkedHashMap<>();
        private int singleInventoryCallCount;
        private int bulkInventoryCallCount;
        private List<Integer> requestedBulkSiteIds = List.of();

        @Override
        public Map<Integer, Integer> getInventoryBySiteId(int siteId) {
            singleInventoryCallCount++;
            return inventoriesBySiteId.getOrDefault(siteId, Map.of());
        }

        @Override
        public Map<Integer, Map<Integer, Integer>> getInventoryBySiteIds(Collection<Integer> siteIds) {
            bulkInventoryCallCount++;
            requestedBulkSiteIds = List.copyOf(siteIds);

            Map<Integer, Map<Integer, Integer>> result = new LinkedHashMap<>();
            for (Integer siteId : siteIds) {
                result.put(siteId, inventoriesBySiteId.getOrDefault(siteId, Map.of()));
            }
            return result;
        }
    }

    private static class EmptyMerchandiseRepository implements MerchandiseRepository {
        @Override
        public List<Merchandise> findAll() {
            return List.of();
        }

        @Override
        public List<Merchandise> findActive() {
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

        @Override
        public int create(Merchandise merchandise) {
            return -1;
        }

        @Override
        public boolean update(Merchandise merchandise) {
            return false;
        }

        @Override
        public boolean setActive(int merchandiseId, boolean active) {
            return false;
        }
    }
}
