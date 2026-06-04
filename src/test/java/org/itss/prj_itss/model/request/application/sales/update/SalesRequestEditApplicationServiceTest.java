package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandPort;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryPort;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesRequestEditApplicationServiceTest {

    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-05-25T00:00:00Z"),
        ZoneId.of("UTC")
    );

    @Test
    void openSessionLoadsRequestAndCurrentValidation() {
        FakeRequestRepository requestRepository = new FakeRequestRepository();
        SalesRequestEditUseCase service = newService(requestRepository);

        SalesRequestEditSession session = service.openSession(1);

        assertNotNull(session);
        SalesRequestEditLoadResult result = session.currentView();
        assertEquals(1, result.draft().requestId());
        assertEquals(LocalDateTime.of(2026, 5, 24, 10, 30), result.draft().createdAt());
        assertEquals(1, result.draft().items().size());
        assertEquals(2, result.merchandiseOptions().size());
        assertTrue(result.validationResult().validForm());
    }

    @Test
    void saveStopsBeforePersistenceWhenDraftIsInvalid() {
        FakeRequestRepository requestRepository = new FakeRequestRepository();
        SalesRequestEditSession session = newService(requestRepository).openSession(1);

        session.removeItem(1);
        SalesRequestEditSaveResult result = session.save();

        assertFalse(result.success());
        assertFalse(result.validationResult().validForm());
        assertEquals(0, requestRepository.updateCount);
    }

    @Test
    void savePersistsMappedDomainItemsWhenDraftIsValid() {
        FakeRequestRepository requestRepository = new FakeRequestRepository();
        SalesRequestEditSession session = newService(requestRepository).openSession(1);

        session.changeQuantity(1, new BigDecimal("5"));
        SalesRequestEditSaveResult result = session.save();

        assertTrue(result.success());
        assertEquals(1, requestRepository.updateCount);
        assertEquals(new BigDecimal("5"), requestRepository.updatedItems.get(0).getQuantityOrdered());
    }

    private SalesRequestEditApplicationService newService(FakeRequestRepository requestRepository) {
        MerchandiseUseCase merchandiseUseCase = new MerchandiseUseCase(new FakeMerchandiseRepository());
        return new SalesRequestEditApplicationService(
            requestRepository,
            requestRepository,
            merchandiseUseCase,
            new SalesRequestEditMapper(),
            new SalesRequestEditValidator(),
            CLOCK
        );
    }

    private static final class FakeRequestRepository implements SalesRequestQueryPort, SalesRequestCommandPort {

        private int updateCount;
        private List<RequestMerchandise> updatedItems = List.of();

        @Override
        public Request findById(int id) {
            return Request.reconstituteFromDb(
                id,
                LocalDateTime.of(2026, 5, 24, 10, 30),
                RequestStatus.PENDING,
                ""
            );
        }

        @Override
        public List<RequestMerchandise> findItemsByRequestId(int requestId) {
            return List.of(new RequestMerchandise(
                requestId,
                10,
                new BigDecimal("2"),
                LocalDate.of(2026, 5, 26)
            ));
        }

        @Override
        public void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) {
            updateCount++;
            updatedItems = new ArrayList<>(items);
        }

        @Override
        public int createRequest(Request request) {
            return 0;
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
        public List<Merchandise> findActive() {
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

        @Override
        public int create(Merchandise merchandise) {
            return 0;
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
