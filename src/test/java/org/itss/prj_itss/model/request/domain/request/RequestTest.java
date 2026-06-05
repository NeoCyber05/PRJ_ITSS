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
        LocalDate desiredDate = LocalDate.now().plusDays(2);

        request.addItem(10, BigDecimal.valueOf(2), desiredDate);

        assertEquals(RequestStatus.PENDING, request.getStatus());
        assertEquals("pending", request.getStatusKey());
        assertEquals("urgent", request.getNote());
        assertEquals(1, request.getItems().size());
        assertEquals(10, request.getItems().get(0).getMerchandiseId());
        assertEquals(BigDecimal.valueOf(2), request.getItems().get(0).getQuantityOrdered());
        assertEquals(desiredDate, request.getItems().get(0).getDesiredDeliveryDate());
        assertThrows(UnsupportedOperationException.class, () -> request.getItems().clear());
    }
}
