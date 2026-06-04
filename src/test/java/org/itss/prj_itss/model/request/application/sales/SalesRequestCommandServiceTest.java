package org.itss.prj_itss.model.request.application.sales;

import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesRequestCommandServiceTest {

    @Test
    void createRequestBuildsPendingRequestAggregateBeforeSaving() throws Exception {
        RecordingCommandPort commandPort = new RecordingCommandPort();
        SalesRequestCommandService service = new SalesRequestCommandService(commandPort);

        int requestId = service.createRequest(
            List.of(new SalesRequestItemSubmission(
                10,
                BigDecimal.valueOf(3),
                LocalDate.of(2026, 6, 4)
            )),
            "restock"
        );

        assertEquals(42, requestId);
        assertEquals(RequestStatus.PENDING, commandPort.savedRequest.getStatus());
        assertEquals("restock", commandPort.savedRequest.getNote());
        assertEquals(1, commandPort.savedRequest.getItems().size());
        assertEquals(10, commandPort.savedRequest.getItems().get(0).getMerchandiseId());
        assertEquals(BigDecimal.valueOf(3), commandPort.savedRequest.getItems().get(0).getQuantityOrdered());
        assertEquals(LocalDate.of(2026, 6, 4), commandPort.savedRequest.getItems().get(0).getDesiredDeliveryDate());
    }

    private static final class RecordingCommandPort implements SalesRequestCommandPort {
        private Request savedRequest;

        @Override
        public int createRequest(Request request) {
            this.savedRequest = request;
            return 42;
        }

        @Override
        public void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) {
        }

        @Override
        public boolean deleteById(int requestId) {
            return true;
        }
    }
}
