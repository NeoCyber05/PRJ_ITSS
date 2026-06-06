package org.itss.prj_itss.model.warehouse.application;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.order.domain.OrderStatus;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase.ConfirmationResult;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase.InspectionItemInput;
import org.itss.prj_itss.model.warehouse.application.port.WarehouseReceiptRepository;
import org.itss.prj_itss.model.warehouse.domain.InspectionResult;
import org.itss.prj_itss.model.warehouse.domain.WarehouseReceipt;
import org.itss.prj_itss.model.warehouse.domain.WarehouseReceiptItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarehouseReceivingUseCaseTest {

    @Test
    void confirmArrivalLoadsMerchandiseSnapshotsInOneBulkLookup() {
        FakeOrderRepository orderRepository = new FakeOrderRepository();
        orderRepository.orders.put(10, new Order(
            10,
            4,
            3,
            LocalDateTime.of(2026, 6, 1, 8, 0),
            OrderStatus.SHIPPING.displayValue()
        ));
        orderRepository.itemsByOrderId.put(10, List.of(
            new OrderMerchandise(10, 100, BigDecimal.valueOf(5), "ship"),
            new OrderMerchandise(10, 101, BigDecimal.valueOf(8), "air")
        ));

        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(3, new Site(3, "TOKYO", "Tokyo", "", 10, 2));

        FakeMerchandiseRepository merchandiseRepository = new FakeMerchandiseRepository();
        merchandiseRepository.merchandise.put(100, new Merchandise(100, "M100", "Rice", "kg"));
        merchandiseRepository.merchandise.put(101, new Merchandise(101, "M101", "Tea", "box"));

        RecordingWarehouseReceiptRepository receiptRepository = new RecordingWarehouseReceiptRepository();
        WarehouseReceivingUseCase useCase = new WarehouseReceivingUseCase(
            orderRepository,
            new SiteUseCase(siteRepository, siteRepository),
            new MerchandiseUseCase(merchandiseRepository),
            receiptRepository,
            callback -> callback.execute(),
            () -> null
        );

        ConfirmationResult result = useCase.confirmArrival(
            10,
            List.of(
                new InspectionItemInput(100, 5, InspectionResult.ENOUGH, ""),
                new InspectionItemInput(101, 8, InspectionResult.ENOUGH, "")
            ),
            ""
        );

        assertTrue(result.success());
        assertEquals(List.of(100, 101), merchandiseRepository.requestedBulkIds);
        assertEquals(1, merchandiseRepository.bulkFindByIdsCalls);
        assertEquals(0, merchandiseRepository.findByIdCalls);
        assertEquals(2, receiptRepository.items.size());
        assertEquals(OrderStatus.DELIVERED.displayValue(), orderRepository.updatedStatus);
    }

    private static final class FakeOrderRepository implements OrderRepository {
        private final Map<Integer, Order> orders = new LinkedHashMap<>();
        private final Map<Integer, List<OrderMerchandise>> itemsByOrderId = new LinkedHashMap<>();
        private String updatedStatus;

        @Override
        public List<Order> findAll() {
            return List.copyOf(orders.values());
        }

        @Override
        public List<Order> findByStatus(String status) {
            return orders.values().stream()
                .filter(order -> status.equalsIgnoreCase(order.getStatus()))
                .toList();
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
            updatedStatus = newStatus;
            return true;
        }

        @Override
        public LocalDate findDesiredDeliveryDate(int orderId, int merchandiseId) {
            return null;
        }
    }

    private static final class FakeSiteRepository implements SiteRepository, InventoryRepository {
        private final Map<Integer, Site> sites = new LinkedHashMap<>();

        @Override
        public List<Site> findAll() {
            return List.copyOf(sites.values());
        }

        @Override
        public List<Site> findAvailableForMerchandiseIds(List<Integer> merchandiseIds) {
            return List.of();
        }

        @Override
        public Site findById(int id) {
            return sites.get(id);
        }

        @Override
        public Site findBySiteCode(String siteCode) {
            return null;
        }

        @Override
        public int countAll() {
            return sites.size();
        }

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

    private static final class FakeMerchandiseRepository implements MerchandiseRepository {
        private final Map<Integer, Merchandise> merchandise = new LinkedHashMap<>();
        private int findByIdCalls;
        private int bulkFindByIdsCalls;
        private List<Integer> requestedBulkIds = List.of();

        @Override
        public List<Merchandise> findAll() {
            return List.copyOf(merchandise.values());
        }

        @Override
        public List<Merchandise> findActive() {
            return List.copyOf(merchandise.values());
        }

        @Override
        public Merchandise findById(int id) {
            findByIdCalls++;
            return merchandise.get(id);
        }

        @Override
        public Map<Integer, Merchandise> findByIds(Collection<Integer> ids) {
            bulkFindByIdsCalls++;
            requestedBulkIds = List.copyOf(ids);

            Map<Integer, Merchandise> result = new LinkedHashMap<>();
            for (Integer id : ids) {
                Merchandise value = merchandise.get(id);
                if (value != null) {
                    result.put(id, value);
                }
            }
            return result;
        }

        @Override
        public Merchandise findByCode(String code) {
            return null;
        }

        @Override
        public int countAll() {
            return merchandise.size();
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

    private static final class RecordingWarehouseReceiptRepository implements WarehouseReceiptRepository {
        private final List<WarehouseReceiptItem> items = new java.util.ArrayList<>();

        @Override
        public int createReceipt(WarehouseReceipt receipt) {
            return 1;
        }

        @Override
        public boolean addReceiptItem(WarehouseReceiptItem item) {
            items.add(item);
            return true;
        }
    }
}
