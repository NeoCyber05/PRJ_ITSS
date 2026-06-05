package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesRequestEditUseCaseTest {

    private final FakeGateway gateway = new FakeGateway();
    private final SalesRequestEditUseCase useCase = new SalesRequestEditUseCase(
        gateway,
        new SalesRequestEditMapper(),
        new SalesRequestEditValidator()
    );

    @Test
    void loadsEditDataThroughGateway() throws SalesRequestEditException {
        MerchandiseOption option = new MerchandiseOption(10, "MH-001", "Item 1", "box");
        gateway.form = formWith(option);
        gateway.options = List.of(option);

        SalesRequestEditData data = useCase.loadEditData(1);

        assertTrue(data.found());
        assertEquals(1, data.requestId());
        assertEquals(gateway.form, data.form());
        assertEquals(List.of(option), data.merchandiseOptions());
    }

    @Test
    void returnsEmptyEditDataWhenRequestDoesNotExist() throws SalesRequestEditException {
        gateway.form = null;

        SalesRequestEditData data = useCase.loadEditData(99);

        assertFalse(data.found());
        assertEquals(99, data.requestId());
        assertTrue(data.merchandiseOptions().isEmpty());
    }

    @Test
    void propagatesGatewayFailureWhenLoadingEditData() {
        gateway.failOnLoad = true;

        assertThrows(SalesRequestEditException.class, () -> useCase.loadEditData(1));
    }

    @Test
    void validatesBeforeSubmittingUpdate() {
        SalesRequestEditState state = new SalesRequestEditState(1, "YC-2026-001", "25/05/2026", "pending");
        state.addBlankItem();

        assertThrows(SalesRequestEditValidationException.class, () ->
            useCase.updateRequest(state.snapshot(), LocalDate.of(2026, 5, 25))
        );
        assertTrue(gateway.submittedItems.isEmpty());
    }

    @Test
    void mapsValidatedDraftAndSubmitsThroughGateway() throws SalesRequestEditException {
        MerchandiseOption option = new MerchandiseOption(10, "MH-001", "Item 1", "box");
        SalesRequestEditState state = new SalesRequestEditState(1, "YC-2026-001", "25/05/2026", "pending");
        state.replaceItems(List.of(new SalesRequestEditItemDraft(
            1,
            option,
            new BigDecimal("2.5"),
            LocalDate.of(2026, 5, 26)
        )));

        useCase.updateRequest(state.snapshot(), LocalDate.of(2026, 5, 25));

        assertEquals(1, gateway.submittedRequestId);
        assertEquals(1, gateway.submittedItems.size());
        assertEquals(10, gateway.submittedItems.get(0).merchandiseId());
        assertEquals(new BigDecimal("2.5"), gateway.submittedItems.get(0).quantityOrdered());
        assertEquals(LocalDate.of(2026, 5, 26), gateway.submittedItems.get(0).desiredDeliveryDate());
    }

    private RequestFormView formWith(MerchandiseOption option) {
        return new RequestFormView(
            1,
            "YC-2026-001",
            "25/05/2026",
            "pending",
            "Cho xu ly",
            "",
            List.of(new RequestFormView.RequestItemFormRow(option, "2.5", "26/05/2026"))
        );
    }

    private static final class FakeGateway implements SalesRequestEditGateway {
        private RequestFormView form;
        private List<MerchandiseOption> options = List.of();
        private int submittedRequestId;
        private boolean failOnLoad;
        private final List<SalesRequestItemSubmission> submittedItems = new ArrayList<>();

        @Override
        public RequestFormView loadRequest(int requestId) {
            if (failOnLoad) {
                throw new SalesRequestEditGatewayException("Cannot load request");
            }
            return form;
        }

        @Override
        public List<MerchandiseOption> findMerchandiseOptions() {
            return options;
        }

        @Override
        public void updateRequest(int requestId, List<SalesRequestItemSubmission> items) {
            submittedRequestId = requestId;
            submittedItems.clear();
            submittedItems.addAll(items);
        }
    }
}
