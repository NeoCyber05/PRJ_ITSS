package org.itss.prj_itss.request.application;

import org.itss.prj_itss.request.domain.allocation.Allocation;
import org.itss.prj_itss.request.domain.delivery.DeliveryMethod;
import org.itss.prj_itss.request.domain.processing.ItemRequirement;
import org.itss.prj_itss.request.domain.processing.RequestProcessingData;
import org.itss.prj_itss.request.domain.processing.SiteStockOption;
import org.itss.prj_itss.request.application.port.RequestProcessingGateway;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestProcessingUseCaseTest {
    @Test
    void delegatesLoadAndSubmitThroughGateway() throws RequestProcessingException {
        RecordingGateway gateway = new RecordingGateway();
        RequestProcessingUseCase useCase = new RequestProcessingUseCase(gateway);

        RequestProcessingData data = useCase.loadProcessingData(99);
        useCase.createAllocatedOrders(99, allocations(10, new Allocation(1, 10, 2, DeliveryMethod.SHIP.storageValue())));

        assertEquals(99, gateway.loadedRequestId);
        assertEquals(99, gateway.submittedRequestId);
        assertEquals(1, data.sites().size());
    }

    @Test
    void validatesSubmissionWithDefaultValidator() {
        RecordingGateway gateway = new RecordingGateway();
        RequestProcessingUseCase useCase = new RequestProcessingUseCase(gateway);
        ItemRequirement item = gateway.data.items().get(0);
        SiteStockOption site = gateway.data.sites().get(0);

        String validationMessage = useCase.validateSubmission(
            List.of(item),
            List.of(site),
            allocations(item.merchandiseId, new Allocation(site.id, item.merchandiseId, 5, DeliveryMethod.SHIP.storageValue())),
            Map.of(item.merchandiseId, LocalDate.now().plusDays(7)),
            7
        );

        assertNull(validationMessage);
    }

    @Test
    void validatesMissingExactAndExcessQuantity() {
        RequestProcessingUseCase useCase = new RequestProcessingUseCase(new RecordingGateway());
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 5);

        Map<Integer, Map<Integer, Allocation>> missing = allocations(
            item.merchandiseId,
            new Allocation(1, item.merchandiseId, 3, DeliveryMethod.SHIP.storageValue())
        );
        Map<Integer, Map<Integer, Allocation>> exact = allocations(
            item.merchandiseId,
            new Allocation(1, item.merchandiseId, 5, DeliveryMethod.SHIP.storageValue())
        );
        Map<Integer, Map<Integer, Allocation>> excess = allocations(
            item.merchandiseId,
            new Allocation(1, item.merchandiseId, 7, DeliveryMethod.SHIP.storageValue())
        );

        assertEquals(1, useCase.validateAllocations(List.of(item), missing).size());
        assertTrue(useCase.validateAllocations(List.of(item), exact).isEmpty());
        assertEquals(1, useCase.validateAllocations(List.of(item), excess).size());
    }

    @Test
    void rejectsSubmissionWhenDesiredDeliveryCannotBeMet() {
        RequestProcessingUseCase useCase = new RequestProcessingUseCase(new RecordingGateway());
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 5);
        SiteStockOption site = new SiteStockOption(1, "S1", "Site 1", "", 6, 2, Map.of(item.merchandiseId, 5));

        String validationMessage = useCase.validateSubmission(
            List.of(item),
            List.of(site),
            allocations(item.merchandiseId, new Allocation(1, item.merchandiseId, 5, DeliveryMethod.SHIP.storageValue())),
            Map.of(item.merchandiseId, LocalDate.now().plusDays(3)),
            14
        );

        assertEquals("Khong dap ung ngay nhan mong muon", validationMessage);
    }

    @Test
    void acceptsSubmissionWhenDesiredDeliveryCanBeMet() {
        RequestProcessingUseCase useCase = new RequestProcessingUseCase(new RecordingGateway());
        ItemRequirement item = new ItemRequirement(10, "M10", "Part", 5);
        SiteStockOption site = new SiteStockOption(1, "S1", "Site 1", "", 2, 5, Map.of(item.merchandiseId, 5));

        String validationMessage = useCase.validateSubmission(
            List.of(item),
            List.of(site),
            allocations(item.merchandiseId, new Allocation(1, item.merchandiseId, 5, DeliveryMethod.SHIP.storageValue())),
            Map.of(item.merchandiseId, LocalDate.now().plusDays(4)),
            14
        );

        assertNull(validationMessage);
    }

    private Map<Integer, Map<Integer, Allocation>> allocations(int merchandiseId, Allocation allocation) {
        Map<Integer, Map<Integer, Allocation>> result = new LinkedHashMap<>();
        result.put(merchandiseId, Map.of(allocation.siteId, allocation));
        return result;
    }

    private static final class RecordingGateway implements RequestProcessingGateway {
        private final RequestProcessingData data = new RequestProcessingData(
            99,
            LocalDate.now().plusDays(7),
            7,
            List.of(new ItemRequirement(10, "M10", "Part", 5)),
            List.of(new SiteStockOption(1, "S1", "Site 1", "", 3, 1, Map.of(10, 5))),
            Map.of(10, LocalDate.now().plusDays(7))
        );
        private int loadedRequestId;
        private int submittedRequestId;

        @Override
        public RequestProcessingData loadProcessingData(int requestId) {
            loadedRequestId = requestId;
            return data;
        }

        @Override
        public void createAllocatedOrders(
            int requestId,
            Map<Integer, Map<Integer, Allocation>> allocations
        ) {
            submittedRequestId = requestId;
        }
    }
}
