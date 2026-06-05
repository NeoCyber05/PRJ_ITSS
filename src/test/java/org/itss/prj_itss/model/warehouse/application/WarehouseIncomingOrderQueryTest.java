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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseIncomingOrderQueryTest {

    @Test
    void findIncomingRowsReturnsOnlyShippingOrdersSortedByCreatedAtDesc() {
        FakeOrderRepository orderRepo = new FakeOrderRepository();
        FakeSiteRepository siteRepo = new FakeSiteRepository();
        FakeMerchandiseRepository merchandiseRepo = new FakeMerchandiseRepository();

        orderRepo.orders
                .add(new Order(1, 10, 5, LocalDateTime.of(2026, 6, 1, 10, 0), OrderStatus.SHIPPING.displayValue()));
        orderRepo.orders
                .add(new Order(2, 11, 6, LocalDateTime.of(2026, 6, 2, 10, 0), OrderStatus.SHIPPING.displayValue()));
        orderRepo.orders.add(new Order(3, 12, 5, LocalDateTime.of(2026, 6, 3, 10, 0),
                OrderStatus.PENDING_CONFIRMATION.displayValue()));

        siteRepo.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        siteRepo.sites.put(6, new Site(6, "OSAKA", "Osaka", "", 8, 3));

        WarehouseIncomingOrderQuery query = new WarehouseIncomingOrderQuery(
            orderRepo,
            new SiteUseCase(siteRepo, siteRepo),
            new MerchandiseUseCase(merchandiseRepo)
        );

        List<IncomingOrderRow> rows = query.findIncomingRows();

        assertEquals(2, rows.size());
        assertEquals(2, rows.get(0).orderId());
        assertEquals(1, rows.get(1).orderId());
        assertEquals("DH-2026-002", rows.get(0).orderCode());
        assertEquals("Tokyo", rows.get(1).siteName());
    }

    @Test
    void findIncomingDetailReturnsItemsWithMerchandiseNames() {
        FakeOrderRepository orderRepo = new FakeOrderRepository();
        FakeSiteRepository siteRepo = new FakeSiteRepository();
        FakeMerchandiseRepository merchandiseRepo = new FakeMerchandiseRepository();

        orderRepo.orders.add(new Order(1, 10, 5, LocalDateTime.now(), OrderStatus.SHIPPING.displayValue()));
        orderRepo.items.put(1, List.of(new OrderMerchandise(1, 7, BigDecimal.valueOf(12), "air")));

        siteRepo.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        merchandiseRepo.merchandise.put(7, new Merchandise(7, "M-01", "Tea", "box"));

        WarehouseIncomingOrderQuery query = new WarehouseIncomingOrderQuery(
            orderRepo,
            new SiteUseCase(siteRepo, siteRepo),
            new MerchandiseUseCase(merchandiseRepo)
        );

        IncomingOrderDetail detail = query.findIncomingDetail(1);

        assertNotNull(detail);
        assertEquals(1, detail.summary().orderId());
        assertEquals(1, detail.items().size());
        assertEquals("M-01", detail.items().get(0).merchandiseCode());
        assertEquals("Tea", detail.items().get(0).merchandiseName());
        assertEquals("box", detail.items().get(0).unit());
        assertEquals("12", detail.items().get(0).orderedQuantity());
        assertEquals("Hàng không", detail.items().get(0).deliveryMethod());
    }

    @Test
    void findIncomingDetailReturnsNullForMissingOrder() {
        FakeOrderRepository orderRepo = new FakeOrderRepository();
        FakeSiteRepository siteRepo = new FakeSiteRepository();
        FakeMerchandiseRepository merchandiseRepo = new FakeMerchandiseRepository();

        WarehouseIncomingOrderQuery query = new WarehouseIncomingOrderQuery(
            orderRepo,
            new SiteUseCase(siteRepo, siteRepo),
            new MerchandiseUseCase(merchandiseRepo)
        );

        assertNull(query.findIncomingDetail(99));
    }

    static final class FakeOrderRepository implements OrderRepository {
        final List<Order> orders = new ArrayList<>();
        final Map<Integer, List<OrderMerchandise>> items = new LinkedHashMap<>();

        @Override
        public List<Order> findAll() {
            return List.copyOf(orders);
        }

        @Override
        public List<Order> findByStatus(String status) {
            return orders.stream()
                    .filter(o -> status.equalsIgnoreCase(o.getStatus()))
                    .toList();
        }

        @Override
        public Order findById(int id) {
            return orders.stream().filter(o -> o.getId() == id).findFirst().orElse(null);
        }

        @Override
        public List<OrderMerchandise> findItemsByOrderId(int orderId) {
            return items.getOrDefault(orderId, List.of());
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
            return false;
        }

        @Override
        public java.time.LocalDate findDesiredDeliveryDate(int orderId, int merchandiseId) {
            return null;
        }
    }

    static final class FakeSiteRepository implements SiteRepository, InventoryRepository {
        final Map<Integer, Site> sites = new LinkedHashMap<>();

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
    }

    static final class FakeMerchandiseRepository implements MerchandiseRepository {
        final Map<Integer, Merchandise> merchandise = new LinkedHashMap<>();

        @Override
        public List<Merchandise> findAll() {
            return List.copyOf(merchandise.values());
        }

        @Override
        public List<Merchandise> findActive() {
            return merchandise.values().stream().filter(Merchandise::isActive).toList();
        }

        @Override
        public Merchandise findById(int id) {
            return merchandise.get(id);
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
        public int create(Merchandise m) {
            return -1;
        }

        @Override
        public boolean update(Merchandise m) {
            return false;
        }

        @Override
        public boolean setActive(int merchandiseId, boolean active) {
            return false;
        }
    }
}
