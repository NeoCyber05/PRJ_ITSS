package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.catalog.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.request.application.RequestManagementUseCase;
import org.itss.prj_itss.model.request.application.port.RequestRepository;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesRequestCreationApplicationServiceTest {

    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-05-25T00:00:00Z"),
        ZoneId.of("UTC")
    );

    @Test
    void createStopsBeforePersistenceWhenInputIsInvalid() {
        FakeRequestRepository requestRepository = new FakeRequestRepository();
        SalesRequestCreationApplicationService service = newService(requestRepository);

        SalesRequestCreationResult result = service.createRequest(List.of(new SalesRequestCreationItemDraft(
            "UNKNOWN",
            "0",
            LocalDate.of(2026, 5, 24)
        )), "");

        assertFalse(result.success());
        assertEquals(0, requestRepository.createCount);
    }

    @Test
    void createPersistsResolvedDomainItemsWhenInputIsValid() {
        FakeRequestRepository requestRepository = new FakeRequestRepository();
        SalesRequestCreationApplicationService service = newService(requestRepository);

        SalesRequestCreationResult result = service.createRequest(List.of(new SalesRequestCreationItemDraft(
            "MH-001",
            "3.5",
            LocalDate.of(2026, 5, 26)
        )), "");

        assertTrue(result.success());
        assertEquals(99, result.requestId());
        assertEquals(1, requestRepository.createCount);
        RequestMerchandise savedItem = requestRepository.createdItems.get(0);
        assertEquals(10, savedItem.getMerchandiseId());
        assertEquals(new BigDecimal("3.5"), savedItem.getQuantityOrdered());
        assertEquals(LocalDate.of(2026, 5, 26), savedItem.getDesiredDeliveryDate());
    }

    @Test
    void duplicateMerchandiseIsRejected() {
        FakeRequestRepository requestRepository = new FakeRequestRepository();
        SalesRequestCreationApplicationService service = newService(requestRepository);

        SalesRequestCreationResult result = service.createRequest(List.of(
            new SalesRequestCreationItemDraft("MH-001", "1", LocalDate.of(2026, 5, 26)),
            new SalesRequestCreationItemDraft("MH-001", "2", LocalDate.of(2026, 5, 27))
        ), "");

        assertFalse(result.success());
        assertEquals(0, requestRepository.createCount);
    }

    private SalesRequestCreationApplicationService newService(FakeRequestRepository requestRepository) {
        RequestManagementUseCase requestUseCase = new RequestManagementUseCase(requestRepository);
        CatalogUseCase catalogUseCase = new CatalogUseCase(new FakeMerchandiseRepository());
        return new SalesRequestCreationApplicationService(
            requestUseCase,
            catalogUseCase,
            new SalesRequestCreationValidator(),
            CLOCK
        );
    }

    private static final class FakeRequestRepository implements RequestRepository {

        private int createCount;
        private List<RequestMerchandise> createdItems = List.of();

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
            return false;
        }

        @Override
        public void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) {
        }

        @Override
        public int createRequest(List<RequestMerchandise> items, String note) {
            createCount++;
            createdItems = new ArrayList<>(items);
            return 99;
        }

        @Override
        public boolean deleteById(int requestId) {
            return false;
        }
    }

    private static final class FakeMerchandiseRepository implements MerchandiseRepository {

        private final List<Merchandise> merchandise = List.of(
            new Merchandise(10, "MH-001", "Item 1", "box"),
            new Merchandise(20, "MH-002", "Item 2", "kg")
        );

        @Override
        public List<Merchandise> findAll() {
            return merchandise;
        }

        @Override
        public Merchandise findById(int id) {
            return merchandise.stream()
                .filter(item -> item.getId() == id)
                .findFirst()
                .orElse(null);
        }

        @Override
        public Merchandise findByCode(String code) {
            return merchandise.stream()
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
        }

        @Override
        public int countAll() {
            return merchandise.size();
        }
    }
}
