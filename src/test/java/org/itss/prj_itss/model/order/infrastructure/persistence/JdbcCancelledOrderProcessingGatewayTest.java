package org.itss.prj_itss.model.order.infrastructure.persistence;

import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.order.domain.cancellation.CancelledOrderProcessingData;
import org.itss.prj_itss.model.request.application.processing.ProcessingRequestPort;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;
import org.itss.prj_itss.model.shared.database.TransactionException;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.itss.prj_itss.model.site.domain.Site;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcCancelledOrderProcessingGatewayTest {

    @Test
    void loadProcessingDataLoadsSiteInventoriesInOneBulkLookup() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        orderRepository.orders.put(20, new Order(
            20,
            7,
            99,
            LocalDateTime.of(2026, 6, 1, 8, 0),
            "cancelled"
        ));
        orderRepository.itemsByOrderId.put(20, List.of(
            new OrderMerchandise(20, 100, BigDecimal.valueOf(5), "ship")
        ));

        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.availableSites = List.of(
            new Site(1, "S1", "Site 1", "", 2, 1),
            new Site(2, "S2", "Site 2", "", 3, 2)
        );

        RecordingInventoryRepository inventoryRepository = new RecordingInventoryRepository();
        inventoryRepository.inventoriesBySiteId.put(1, Map.of(100, 7));
        inventoryRepository.inventoriesBySiteId.put(2, Map.of(100, 4));

        JdbcCancelledOrderProcessingGateway gateway = new JdbcCancelledOrderProcessingGateway(
            orderRepository,
            siteRepository,
            inventoryRepository,
            new FakeProcessingRequestPort(),
            new FakeMerchandiseRepository(),
            callback -> callback.execute()
        );

        CancelledOrderProcessingData data = gateway.loadProcessingData(20);

        assertEquals(List.of(1, 2), inventoryRepository.requestedBulkSiteIds);
        assertEquals(1, inventoryRepository.bulkInventoryCallCount);
        assertEquals(0, inventoryRepository.singleInventoryCallCount);
        assertEquals(Map.of(100, 7), data.sites().get(0).stock);
        assertEquals(Map.of(100, 4), data.sites().get(1).stock);
    }

    private static final class FakeOrderRepository implements OrderRepository {
        private final Map<Integer, Order> orders = new LinkedHashMap<>();
        private final Map<Integer, List<OrderMerchandise>> itemsByOrderId = new LinkedHashMap<>();

        @Override
        public List<Order> findAll() {
            return List.copyOf(orders.values());
        }

        @Override
        public List<Order> findByStatus(String status) {
            return List.of();
        }

        @Override
        public Order findById(int id) {
            return orders.get(id);
        }

        @Override
        public List<OrderMerchandise> findItemsByOrderId(int orderId) {
            return itemsByOrderId.getOrDefault(orderId, List.of());
        }

        @Override
        public int create(Order order) {
            return -1;
        }

        @Override
        public boolean addItem(OrderMerchandise item) {
            return false;
        }

        @Override
        public boolean updateStatus(int orderId, String newStatus) {
            return true;
        }

        @Override
        public LocalDate findDesiredDeliveryDate(int orderId, int merchandiseId) {
            return null;
        }
    }

    private static final class FakeSiteRepository implements SiteRepository {
        private List<Site> availableSites = List.of();

        @Override
        public List<Site> findAll() {
            return List.of();
        }

        @Override
        public List<Site> findAvailableForMerchandiseIds(List<Integer> merchandiseIds) {
            return availableSites;
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

    private static final class RecordingInventoryRepository implements InventoryRepository {
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

    private static final class FakeProcessingRequestPort implements ProcessingRequestPort {
        @Override
        public List<RequestMerchandise> findItemsByRequestId(int requestId) {
            return List.of(
                new RequestMerchandise(requestId, 100, BigDecimal.valueOf(5), LocalDate.now().plusDays(7))
            );
        }

        @Override
        public LocalDate getEarliestDeliveryDate(int requestId) {
            return LocalDate.now().plusDays(7);
        }

        @Override
        public boolean updateStatus(int requestId, RequestStatus newStatus) {
            return true;
        }
    }

    private static final class FakeMerchandiseRepository implements MerchandiseRepository {
        @Override
        public List<Merchandise> findAll() {
            return List.of(new Merchandise(100, "M100", "Item 100", "pcs"));
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
