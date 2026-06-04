package org.itss.prj_itss.controller.warehouse;

import org.itss.prj_itss.model.warehouse.application.IncomingOrderDetail;
import org.itss.prj_itss.model.warehouse.application.IncomingOrderRow;
import org.itss.prj_itss.model.warehouse.application.WarehouseIncomingOrderQuery;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase.ConfirmationResult;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase.InspectionItemInput;

import java.util.List;

public final class WarehouseIncomingOrderController {

    private final WarehouseIncomingOrderQuery warehouseIncomingOrderQuery;
    private final WarehouseReceivingUseCase warehouseReceivingUseCase;

    public WarehouseIncomingOrderController(
        WarehouseIncomingOrderQuery warehouseIncomingOrderQuery,
        WarehouseReceivingUseCase warehouseReceivingUseCase
    ) {
        this.warehouseIncomingOrderQuery = warehouseIncomingOrderQuery;
        this.warehouseReceivingUseCase = warehouseReceivingUseCase;
    }

    public List<IncomingOrderRow> loadIncomingOrders() {
        return warehouseIncomingOrderQuery.findIncomingRows();
    }

    public IncomingOrderDetail findIncomingDetail(int orderId) {
        return warehouseIncomingOrderQuery.findIncomingDetail(orderId);
    }

    public ConfirmationResult confirmArrival(int orderId, List<InspectionItemInput> itemInputs, String overallNote) {
        return warehouseReceivingUseCase.confirmArrival(orderId, itemInputs, overallNote);
    }
}
