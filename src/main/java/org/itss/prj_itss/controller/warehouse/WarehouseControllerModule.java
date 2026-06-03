package org.itss.prj_itss.controller.warehouse;

import org.itss.prj_itss.model.merchandise.MerchandiseModule;
import org.itss.prj_itss.model.site.SiteModule;
import org.itss.prj_itss.model.warehouse.WarehouseModule;

public final class WarehouseControllerModule {

    private final ConfirmOrderArrivalController confirmOrderArrivalController;
    private final WarehouseIncomingOrderController warehouseIncomingOrderController;

    public WarehouseControllerModule(WarehouseModule warehouseModule, SiteModule siteModule, MerchandiseModule merchandiseModule) {
        this.confirmOrderArrivalController = new ConfirmOrderArrivalController(
            warehouseModule.warehouseReceivingUseCase(),
            siteModule.siteUseCase(),
            merchandiseModule.merchandiseUseCase()
        );
        this.warehouseIncomingOrderController = new WarehouseIncomingOrderController(
            warehouseModule.warehouseIncomingOrderQuery(),
            warehouseModule.warehouseReceivingUseCase()
        );
    }

    public ConfirmOrderArrivalController confirmOrderArrivalController() {
        return confirmOrderArrivalController;
    }

    public WarehouseIncomingOrderController warehouseIncomingOrderController() {
        return warehouseIncomingOrderController;
    }
}
