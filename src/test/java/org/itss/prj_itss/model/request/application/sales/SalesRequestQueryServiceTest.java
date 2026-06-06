package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesRequestQueryServiceTest {

    @Test
    void findFormViewLoadsMerchandiseOptionsInOneBulkLookup() {
        FakeSalesRequestQueryPort queryPort = new FakeSalesRequestQueryPort();
        queryPort.request = Request.reconstituteFromDb(
            7,
            LocalDateTime.of(2026, 6, 1, 8, 0),
            RequestStatus.PENDING,
            "Need stock"
        );
        queryPort.items = List.of(
            new RequestMerchandise(7, 100, BigDecimal.valueOf(5), LocalDate.of(2026, 6, 10)),
            new RequestMerchandise(7, 101, BigDecimal.valueOf(8), LocalDate.of(2026, 6, 11))
        );

        FakeMerchandiseRepository merchandiseRepository = new FakeMerchandiseRepository();
        merchandiseRepository.merchandise.put(100, new Merchandise(100, "M100", "Rice", "kg"));
        merchandiseRepository.merchandise.put(101, new Merchandise(101, "M101", "Tea", "box"));

        SalesRequestQueryService service = new SalesRequestQueryService(
            queryPort,
            new MerchandiseUseCase(merchandiseRepository),
            new EmptyInventoryRepository()
        );

        RequestFormView view = service.findFormView(7);

        assertEquals(List.of(100, 101), merchandiseRepository.requestedBulkIds);
        assertEquals(1, merchandiseRepository.bulkFindByIdsCalls);
        assertEquals(0, merchandiseRepository.findByIdCalls);
        assertEquals("M100", view.items().get(0).merchandise().code());
        assertEquals("M101", view.items().get(1).merchandise().code());
    }

    private static final class FakeSalesRequestQueryPort implements SalesRequestQueryPort {
        private Request request;
        private List<RequestMerchandise> items = List.of();

        @Override
        public Request findById(int id) {
            return request;
        }

        @Override
        public List<RequestMerchandise> findItemsByRequestId(int requestId) {
            return items;
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

    private static final class EmptyInventoryRepository implements InventoryRepository {
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
}
