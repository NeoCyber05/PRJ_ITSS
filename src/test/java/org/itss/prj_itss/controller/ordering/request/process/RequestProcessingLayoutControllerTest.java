package org.itss.prj_itss.controller.ordering.request.process;

import org.itss.prj_itss.model.shared.database.TransactionException;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeCommand;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeResultView;
import org.itss.prj_itss.model.request.application.processing.ProcessingItemView;
import org.itss.prj_itss.model.request.application.processing.ProcessingSiteView;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingViewModel;
import org.itss.prj_itss.model.request.application.processing.SuggestedPlanView;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestProcessingGateway;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.catalog.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.order.application.port.OrderRepository;
import org.itss.prj_itss.model.request.application.processing.ProcessingRequestPort;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestProcessingLayoutControllerTest {

    @Test
    void loadRequestInitializesProcessingState() {
        RequestProcessingLayoutController controller = controllerWith(defaultScenario());

        controller.setRequestId(99);

        RequestProcessingViewModel vm = controller.snapshot();
        assertEquals(99, vm.requestId());
        assertEquals(1, vm.items().size());
        assertEquals(1, vm.sites().size());
        assertEquals(0, vm.allocationItems().get(0).allocated());
        assertEquals(LocalDate.now().plusDays(7), vm.earliestDeliveryDate() != null ? LocalDate.parse(vm.earliestDeliveryDate(), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null);
    }

    @Test
    void excludedSiteRemovesMatchingAllocation() {
        RequestProcessingLayoutController controller = controllerWith(defaultScenario());
        controller.setRequestId(99);
        RequestProcessingViewModel vm = controller.snapshot();
        ProcessingItemView item = vm.items().get(0);
        ProcessingSiteView site = vm.sites().get(0);

        AllocationChangeResultView result = controller.handleAllocationInputChanged(
            new AllocationChangeCommand(item.merchandiseId(), site.id(), "3", "Tàu")
        );
        assertTrue(result.applied());

        vm = controller.snapshot();
        assertEquals(3, vm.allocationItems().get(0).allocated());

        controller.handleSiteFilterChanged(Set.of(site.id()), Set.of());

        vm = controller.snapshot();
        assertEquals(0, vm.allocationItems().get(0).allocated());
    }

    @Test
    void optimalAndSuggestedPlansApplyAllocations() {
        RequestProcessingLayoutController controller = controllerWith(defaultScenario());
        controller.setRequestId(99);

        controller.handleOptimizeAllocation();

        RequestProcessingViewModel vm = controller.snapshot();
        assertEquals(5, vm.allocationItems().get(0).allocated());

        List<SuggestedPlanView> plans = controller.handleShowAllPlans();
        assertFalse(plans.isEmpty());

        controller.applySelectedPlan(plans.get(0).signature());

        vm = controller.snapshot();
        assertEquals(5, vm.allocationItems().get(0).allocated());
    }

    @Test
    void confirmReturnsValidationBeforePreview() {
        RequestProcessingLayoutController controller = controllerWith(defaultScenario());
        controller.setRequestId(99);

        RequestProcessingLayoutController.ConfirmResult missing = controller.handleConfirm();

        assertFalse(missing.valid());

        controller.handleOptimizeAllocation();
        RequestProcessingLayoutController.ConfirmResult valid = controller.handleConfirm();

        assertTrue(valid.valid());
        assertFalse(valid.previewOrders().isEmpty());
    }

    @Test
    void confirmRejectsLateAllocatedTransport() {
        Scenario scenario = defaultScenario();
        scenario.desiredDeliveryDate = LocalDate.now().plusDays(3);
        scenario.earliestDeliveryDate = scenario.desiredDeliveryDate;
        scenario.requestItems = List.of(
            new RequestMerchandise(99, 10, BigDecimal.valueOf(5), scenario.desiredDeliveryDate)
        );
        scenario.sites = List.of(new Site(1, "S1", "Site 1", "", 6, 999));

        RequestProcessingLayoutController controller = controllerWith(scenario);
        controller.setRequestId(99);
        RequestProcessingViewModel vm = controller.snapshot();
        ProcessingItemView item = vm.items().get(0);
        ProcessingSiteView site = vm.sites().get(0);

        controller.handleAllocationInputChanged(
            new AllocationChangeCommand(item.merchandiseId(), site.id(), "5", "Tàu")
        );

        RequestProcessingLayoutController.ConfirmResult result = controller.handleConfirm();

        assertFalse(result.valid());
    }

    private RequestProcessingLayoutController controllerWith(Scenario scenario) {
        RequestProcessingUseCase useCase = new RequestProcessingUseCase(
            new JdbcRequestProcessingGateway(
                new FakeProcessingRequestPort(scenario),
                new FakeOrderRepository(),
                new FakeSiteRepository(scenario),
                new FakeInventoryRepository(scenario),
                new FakeMerchandiseRepository(scenario),
                new RecordingTransactionRunner()
            )
        );
        return new RequestProcessingLayoutController(useCase);
    }

    private Scenario defaultScenario() {
        Scenario scenario = new Scenario();
        scenario.desiredDeliveryDate = LocalDate.now().plusDays(7);
        scenario.earliestDeliveryDate = scenario.desiredDeliveryDate;
        scenario.requestItems = List.of(
            new RequestMerchandise(99, 10, BigDecimal.valueOf(5), scenario.desiredDeliveryDate)
        );
        scenario.merchandiseById = Map.of(
            10, new Merchandise(10, "M10", "Part 10", "pcs")
        );
        scenario.sites = List.of(new Site(1, "S1", "Site 1", "", 2, 1));
        scenario.inventoryBySiteId = Map.of(1, Map.of(10, 10));
        return scenario;
    }

    private static final class Scenario {
        private List<RequestMerchandise> requestItems = List.of();
        private Map<Integer, Merchandise> merchandiseById = Map.of();
        private List<Site> sites = List.of();
        private Map<Integer, Map<Integer, Integer>> inventoryBySiteId = Map.of();
        private LocalDate earliestDeliveryDate;
        private LocalDate desiredDeliveryDate;
    }

    private static final class RecordingTransactionRunner implements TransactionRunner {
        @Override
        public void execute(TransactionCallback callback) throws TransactionException {
            callback.execute();
        }
    }

    private static final class FakeProcessingRequestPort implements ProcessingRequestPort {
        private final Scenario scenario;

        private FakeProcessingRequestPort(Scenario scenario) {
            this.scenario = scenario;
        }


        @Override
        public List<RequestMerchandise> findItemsByRequestId(int requestId) { return scenario.requestItems; }

        @Override
        public LocalDate getEarliestDeliveryDate(int requestId) { return scenario.earliestDeliveryDate; }
        @Override
        public boolean updateStatus(int requestId, org.itss.prj_itss.model.request.domain.request.RequestStatus newStatus) { return true; }

    }

    private static final class FakeSiteRepository implements SiteRepository {
        private final Scenario scenario;
        private FakeSiteRepository(Scenario scenario) { this.scenario = scenario; }
        @Override
        public List<Site> findAll() { return scenario.sites; }
        @Override
        public List<Site> findAvailableForMerchandiseIds(List<Integer> merchandiseIds) { return scenario.sites; }
        @Override
        public Site findById(int id) { return scenario.sites.stream().filter(s -> s.getId() == id).findFirst().orElse(null); }
        @Override
        public Site findBySiteCode(String siteCode) { return null; }
        @Override
        public int countAll() { return scenario.sites.size(); }
    }

    private static final class FakeInventoryRepository implements InventoryRepository {
        private final Scenario scenario;
        private FakeInventoryRepository(Scenario scenario) { this.scenario = scenario; }
        @Override
        public Map<Integer, Integer> getInventoryBySiteId(int siteId) { return scenario.inventoryBySiteId.getOrDefault(siteId, Map.of()); }
        @Override
        public int getStockQuantity(int siteId, int merchandiseId) { return getInventoryBySiteId(siteId).getOrDefault(merchandiseId, 0); }
        @Override
        public int getTotalStock(int merchandiseId) { return scenario.inventoryBySiteId.values().stream().mapToInt(i -> i.getOrDefault(merchandiseId, 0)).sum(); }
        @Override
        public int countMerchandiseAtSite(int siteId) { return getInventoryBySiteId(siteId).size(); }
    }

    private static final class FakeMerchandiseRepository implements MerchandiseRepository {
        private final Scenario scenario;
        private FakeMerchandiseRepository(Scenario scenario) { this.scenario = scenario; }
        @Override
        public List<Merchandise> findAll() { return new ArrayList<>(scenario.merchandiseById.values()); }
        @Override
        public Merchandise findById(int id) { return scenario.merchandiseById.get(id); }
        @Override
        public Merchandise findByCode(String code) { return scenario.merchandiseById.values().stream().filter(m -> m.getCode().equals(code)).findFirst().orElse(null); }
        @Override
        public int countAll() { return scenario.merchandiseById.size(); }
    }

    private static final class FakeOrderRepository implements OrderRepository {
        private final List<Order> createdOrders = new ArrayList<>();
        private final List<OrderMerchandise> createdItems = new ArrayList<>();
        private int nextOrderId = 1;
        @Override
        public List<Order> findAll() { return List.of(); }
        @Override
        public List<Order> findByStatus(String status) { return List.of(); }
        @Override
        public Order findById(int id) { return null; }
        @Override
        public List<OrderMerchandise> findItemsByOrderId(int orderId) { return List.of(); }
        @Override
        public int create(Order order) { order.setId(nextOrderId++); createdOrders.add(order); return order.getId(); }
        @Override
        public boolean addItem(OrderMerchandise item) { createdItems.add(item); return true; }
        @Override
        public boolean updateStatus(int orderId, String newStatus) { return true; }
    }
}
