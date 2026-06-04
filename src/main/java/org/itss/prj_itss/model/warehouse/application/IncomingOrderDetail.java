package org.itss.prj_itss.model.warehouse.application;

import java.util.List;

public record IncomingOrderDetail(
    IncomingOrderRow summary,
    List<IncomingOrderItemRow> items
) {
}
