package org.itss.prj_itss.model.request.domain.request;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestTest {

    @Test
    void newRequestStartsPendingAndOwnsSubmittedItems() {
        Request request = new Request("urgent");

        request.addItem(10, BigDecimal.valueOf(2), LocalDate.of(2026, 6, 3));

        assertEquals(RequestStatus.PENDING, request.getStatus());
        assertEquals("pending", request.getStatusKey());
        assertEquals("urgent", request.getNote());
        assertEquals(1, request.getItems().size());
        assertEquals(10, request.getItems().get(0).getMerchandiseId());
        assertEquals(BigDecimal.valueOf(2), request.getItems().get(0).getQuantityOrdered());
        assertEquals(LocalDate.of(2026, 6, 3), request.getItems().get(0).getDesiredDeliveryDate());
        assertThrows(UnsupportedOperationException.class, () -> request.getItems().clear());
    }
}
