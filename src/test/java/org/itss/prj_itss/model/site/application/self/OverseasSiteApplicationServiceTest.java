package org.itss.prj_itss.model.site.application.self;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.order.application.port.SiteOrderRepository;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.site.application.port.SiteInventoryCommandPort;
import org.itss.prj_itss.model.site.application.port.SiteProfileCommandPort;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.itss.prj_itss.model.site.domain.Site;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OverseasSiteApplicationServiceTest {

    @Test
    void updateProfileRejectsMissingSite() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        OverseasSiteApplicationService service = newService(siteRepository);

        SiteWorkspaceResult result = service.updateProfile(
            99,
            new SiteProfileDraft("Tokyo Site", "Updated description", 14, 3)
        );

        assertFalse(result.success());
        assertEquals("Site không tồn tại.", result.message());
    }

    @Test
    void updateProfileKeepsSiteCodeReadOnly() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "Old", 10, 2));
        OverseasSiteApplicationService service = newService(siteRepository);

        SiteWorkspaceResult result = service.updateProfile(
            5,
            new SiteProfileDraft("Tokyo Updated", "New", 12, 4)
        );

        assertTrue(result.success());
        assertEquals("TOKYO", siteRepository.sites.get(5).getSiteCode());
        assertEquals("Tokyo Updated", siteRepository.sites.get(5).getName());
        assertEquals(12, siteRepository.sites.get(5).getShipDeliveryDays());
    }

    @Test
    void updateInventoryRejectsNegativeStock() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        FakeCatalogRepository catalogRepository = new FakeCatalogRepository();
        catalogRepository.merchandise.put(7, new Merchandise(7, "M-01", "Tea", "box"));
        OverseasSiteApplicationService service = newService(siteRepository, catalogRepository);

        SiteWorkspaceResult result = service.updateInventoryItem(5, new SiteInventoryDraft(7, -1));

        assertFalse(result.success());
        assertEquals("Số lượng tồn kho không được âm.", result.message());
    }

    @Test
    void updateInventoryStoresSelectedMerchandiseStock() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        FakeCatalogRepository catalogRepository = new FakeCatalogRepository();
        catalogRepository.merchandise.put(7, new Merchandise(7, "M-01", "Tea", "box"));
        OverseasSiteApplicationService service = newService(siteRepository, catalogRepository);

        SiteWorkspaceResult result = service.updateInventoryItem(5, new SiteInventoryDraft(7, 25));

        assertTrue(result.success());
        assertEquals(25, siteRepository.inventory.get(5).get(7));
    }

    @Test
    void removeInventoryItemDeletesOnlyThatSiteMerchandise() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        siteRepository.inventory.put(5, new LinkedHashMap<>(Map.of(7, 25, 8, 10)));
        OverseasSiteApplicationService service = newService(siteRepository);

        SiteWorkspaceResult result = service.removeInventoryItem(5, 7);

        assertTrue(result.success());
        assertFalse(siteRepository.inventory.get(5).containsKey(7));
        assertEquals(10, siteRepository.inventory.get(5).get(8));
    }

    @Test
    void loadIncludesOnlyOrdersForTheSite() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
            new Order(10, 1, 5, LocalDateTime.now(), "pending"),
            new Order(11, 1, 9, LocalDateTime.now(), "pending")
        );
        OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

        SiteWorkspaceSnapshot snapshot = service.load(5);

        assertTrue(snapshot.available());
        assertEquals(1, snapshot.orders().size());
        assertEquals(10, snapshot.orders().get(0).orderId());
    }

    @Test
    void confirmSupplyRejectsOrderFromDifferentSite() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
            new Order(10, 1, 9, LocalDateTime.now(), "pending")
        );
        OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

        SiteWorkspaceResult result = service.confirmSupply(5, 10);

        assertFalse(result.success());
        assertEquals("Đơn hàng không thuộc site này.", result.message());
        assertEquals(0, orderRepository.updatedOrderId);
    }

    @Test
    void loadOrderItemsReturnsItemsOnlyAfterSiteOwnershipCheck() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        FakeCatalogRepository catalogRepository = new FakeCatalogRepository();
        catalogRepository.merchandise.put(7, new Merchandise(7, "M-01", "Tea", "box"));
        FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
            new Order(10, 1, 5, LocalDateTime.now(), "pending")
        );
        orderRepository.items.put(10, List.of(new OrderMerchandise(10, 7, BigDecimal.valueOf(12), "Tau")));
        OverseasSiteApplicationService service = newService(siteRepository, catalogRepository, orderRepository);

        List<SiteOrderItemRow> rows = service.loadOrderItems(5, 10);

        assertEquals(1, rows.size());
        assertEquals("M-01", rows.get(0).merchandiseCode());
        assertEquals("12", rows.get(0).quantity());
    }

    @Test
    void loadOrderItemsRejectsOrderFromDifferentSite() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
            new Order(10, 1, 9, LocalDateTime.now(), "pending")
        );
        OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

        List<SiteOrderItemRow> rows = service.loadOrderItems(5, 10);

        assertTrue(rows.isEmpty());
    }

    @Test
    void confirmSupplyRejectsNonPendingOrder() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
            new Order(10, 1, 5, LocalDateTime.now(), "shipping")
        );
        OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

        SiteWorkspaceResult result = service.confirmSupply(5, 10);

        assertFalse(result.success());
        assertEquals("Chỉ có thể xác nhận đơn hàng đang chờ xác nhận.", result.message());
    }

    @Test
    void confirmSupplyMovesPendingOrderToShipping() {
        FakeSiteRepository siteRepository = new FakeSiteRepository();
        siteRepository.sites.put(5, new Site(5, "TOKYO", "Tokyo", "", 10, 2));
        FakeSiteOrderRepository orderRepository = new FakeSiteOrderRepository(
            new Order(10, 1, 5, LocalDateTime.now(), "pending")
        );
        OverseasSiteApplicationService service = newService(siteRepository, new FakeCatalogRepository(), orderRepository);

        SiteWorkspaceResult result = service.confirmSupply(5, 10);

        assertTrue(result.success());
        assertEquals(10, orderRepository.updatedOrderId);
        assertEquals("shipping", orderRepository.orders.get(10).getStatus());
    }

    private OverseasSiteApplicationService newService(FakeSiteRepository siteRepository) {
        return newService(siteRepository, new FakeCatalogRepository(), new FakeSiteOrderRepository());
    }

    private OverseasSiteApplicationService newService(
        FakeSiteRepository siteRepository,
        FakeCatalogRepository catalogRepository
    ) {
        return newService(siteRepository, catalogRepository, new FakeSiteOrderRepository());
    }

    private OverseasSiteApplicationService newService(
        FakeSiteRepository siteRepository,
        FakeCatalogRepository catalogRepository,
        FakeSiteOrderRepository orderRepository
    ) {
        return new OverseasSiteApplicationService(
            new SiteUseCase(siteRepository, siteRepository),
            new MerchandiseUseCase(catalogRepository),
            siteRepository,
            siteRepository,
            orderRepository
        );
    }

    static final class FakeSiteRepository
        implements SiteRepository, InventoryRepository, SiteProfileCommandPort, SiteInventoryCommandPort {

        final Map<Integer, Site> sites = new LinkedHashMap<>();
        final Map<Integer, Map<Integer, Integer>> inventory = new LinkedHashMap<>();

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
            return sites.values().stream()
                .filter(site -> site.getSiteCode().equalsIgnoreCase(siteCode))
                .findFirst()
                .orElse(null);
        }

        @Override
        public int countAll() {
            return sites.size();
        }

        @Override
        public Map<Integer, Integer> getInventoryBySiteId(int siteId) {
            return Map.copyOf(inventory.getOrDefault(siteId, Map.of()));
        }

        @Override
        public int getStockQuantity(int siteId, int merchandiseId) {
            return inventory.getOrDefault(siteId, Map.of()).getOrDefault(merchandiseId, 0);
        }

        @Override
        public int getTotalStock(int merchandiseId) {
            return inventory.values().stream()
                .mapToInt(items -> items.getOrDefault(merchandiseId, 0))
                .sum();
        }

        @Override
        public int countMerchandiseAtSite(int siteId) {
            return (int) inventory.getOrDefault(siteId, Map.of()).values().stream()
                .filter(stock -> stock > 0)
                .count();
        }

        @Override
        public void updateProfile(int siteId, SiteProfileDraft draft) {
            Site site = sites.get(siteId);
            site.setName(draft.name());
            site.setDescription(draft.description());
            site.setShipDeliveryDays(draft.shipDeliveryDays());
            site.setAirDeliveryDays(draft.airDeliveryDays());
        }

        @Override
        public void upsertInventoryItem(int siteId, int merchandiseId, int stockQuantity) {
            inventory.computeIfAbsent(siteId, ignored -> new LinkedHashMap<>()).put(merchandiseId, stockQuantity);
        }

        @Override
        public void removeInventoryItem(int siteId, int merchandiseId) {
            inventory.computeIfAbsent(siteId, ignored -> new LinkedHashMap<>()).remove(merchandiseId);
        }
    }

    static final class FakeCatalogRepository implements MerchandiseRepository {
        final Map<Integer, Merchandise> merchandise = new LinkedHashMap<>();

        @Override
        public List<Merchandise> findAll() {
            return List.copyOf(merchandise.values());
        }

        @Override
        public List<Merchandise> findActive() {
            return merchandise.values().stream()
                .filter(Merchandise::isActive)
                .toList();
        }

        @Override
        public Merchandise findById(int id) {
            return merchandise.get(id);
        }

        @Override
        public Merchandise findByCode(String code) {
            return merchandise.values().stream()
                .filter(item -> item.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
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

    static final class FakeSiteOrderRepository implements SiteOrderRepository {
        final Map<Integer, Order> orders = new LinkedHashMap<>();
        final Map<Integer, List<OrderMerchandise>> items = new LinkedHashMap<>();
        int updatedOrderId;

        FakeSiteOrderRepository(Order... sourceOrders) {
            for (Order order : sourceOrders) {
                orders.put(order.getId(), order);
            }
        }

        @Override
        public List<Order> findBySiteId(int siteId) {
            return orders.values().stream()
                .filter(order -> order.getSiteId() == siteId)
                .toList();
        }

        @Override
        public Order findByIdForSite(int orderId, int siteId) {
            Order order = orders.get(orderId);
            return order != null && order.getSiteId() == siteId ? order : null;
        }

        @Override
        public List<OrderMerchandise> findItemsByOrderId(int orderId) {
            return items.getOrDefault(orderId, List.of());
        }

        @Override
        public boolean updateStatusForSite(int orderId, int siteId, String newStatus) {
            Order order = findByIdForSite(orderId, siteId);
            if (order == null) {
                return false;
            }
            updatedOrderId = orderId;
            order.setStatus(newStatus);
            return true;
        }
    }
}
