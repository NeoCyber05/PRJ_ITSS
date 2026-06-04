package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SalesRequestEditStateTest {

    @Test
    void stateChangesItemsThroughFocusedMethods() {
        SalesRequestEditState state = new SalesRequestEditState(
            1,
            LocalDateTime.of(2026, 5, 25, 0, 0),
            "pending"
        );
        state.replaceItems(List.of(new SalesRequestEditItemDraft(
            1,
            null,
            null,
            null
        )));

        MerchandiseOption option = new MerchandiseOption(10, "MH-001", "Item 1", "box");
        LocalDate desiredDate = LocalDate.of(2026, 5, 26);
        state.changeMerchandise(1, option);
        state.changeQuantity(1, new BigDecimal("3"));
        state.changeDesiredDate(1, desiredDate);

        SalesRequestEditItemDraft item = state.snapshot().items().get(0);
        assertEquals(option, item.merchandise());
        assertEquals(new BigDecimal("3"), item.quantity());
        assertEquals(desiredDate, item.desiredDate());
    }

    @Test
    void addedItemsReceiveStableLineIdsAndCanBeRemovedById() {
        SalesRequestEditState state = new SalesRequestEditState(
            1,
            LocalDateTime.of(2026, 5, 25, 0, 0),
            "pending"
        );

        state.addBlankItem();
        state.addBlankItem();
        state.removeItems(Set.of(1));

        SalesRequestEditDraft draft = state.snapshot();
        assertEquals(1, draft.items().size());
        assertEquals(2, draft.items().get(0).lineId());
        assertNull(draft.items().get(0).merchandise());
    }
}
