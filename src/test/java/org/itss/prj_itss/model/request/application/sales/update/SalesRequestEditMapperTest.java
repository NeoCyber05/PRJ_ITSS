package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesRequestEditMapperTest {

    private final SalesRequestEditMapper mapper = new SalesRequestEditMapper();

    @Test
    void mapsFormViewToStateAndBackToServiceInput() {
        MerchandiseOption option = new MerchandiseOption(10, "MH-001", "Item 1", "box");
        Request request = Request.reconstituteFromDb(
            1,
            LocalDateTime.of(2026, 5, 25, 0, 0),
            RequestStatus.PENDING,
            ""
        );
        RequestMerchandise item = new RequestMerchandise(
            1,
            10,
            new BigDecimal("2.5"),
            LocalDate.of(2026, 5, 26)
        );

        SalesRequestEditState state = mapper.toState(request, List.of(item), List.of(option));
        SalesRequestEditDraft draft = state.snapshot();
        List<SalesRequestItemSubmission> inputs = mapper.toInput(draft);

        assertEquals(1, draft.requestId());
        assertEquals(LocalDateTime.of(2026, 5, 25, 0, 0), draft.createdAt());
        assertEquals(1, draft.items().size());
        assertEquals(option, draft.items().get(0).merchandise());
        assertEquals(new BigDecimal("2.5"), draft.items().get(0).quantity());
        assertEquals(LocalDate.of(2026, 5, 26), draft.items().get(0).desiredDate());

        assertEquals(1, inputs.size());
        assertEquals("MH-001", inputs.get(0).merchandiseCode());
        assertEquals(new BigDecimal("2.5"), inputs.get(0).quantityOrdered());
        assertEquals(LocalDate.of(2026, 5, 26), inputs.get(0).desiredDeliveryDate());
    }
}
