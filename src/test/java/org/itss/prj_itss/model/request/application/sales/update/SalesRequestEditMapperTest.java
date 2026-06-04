package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesRequestEditMapperTest {

    private final SalesRequestEditMapper mapper = new SalesRequestEditMapper();

    @Test
    void mapsFormViewToStateAndBackToServiceInput() {
        MerchandiseOption option = new MerchandiseOption(10, "MH-001", "Item 1", "box", 0);
        RequestFormView form = new RequestFormView(
            1,
            "YC-2026-001",
            "25/05/2026",
            "pending",
            "Cho xu ly",
            "",
            List.of(new RequestFormView.RequestItemFormRow(
                option,
                "2.5",
                "26/05/2026"
            ))
        );

        SalesRequestEditState state = mapper.toState(form);
        SalesRequestEditDraft draft = state.snapshot();
        List<SalesRequestItemSubmission> inputs = mapper.toInput(draft);

        assertEquals(1, draft.requestId());
        assertEquals("YC-2026-001", draft.requestCode());
        assertEquals(1, draft.items().size());
        assertEquals(option, draft.items().get(0).merchandise());
        assertEquals(new BigDecimal("2.5"), draft.items().get(0).quantity());
        assertEquals(LocalDate.of(2026, 5, 26), draft.items().get(0).desiredDate());

        assertEquals(1, inputs.size());
        assertEquals(10, inputs.get(0).merchandiseId());
        assertEquals(new BigDecimal("2.5"), inputs.get(0).quantityOrdered());
        assertEquals(LocalDate.of(2026, 5, 26), inputs.get(0).desiredDeliveryDate());
    }
}
