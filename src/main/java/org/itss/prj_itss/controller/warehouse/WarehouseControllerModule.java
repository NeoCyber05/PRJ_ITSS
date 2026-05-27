package org.itss.prj_itss.controller.warehouse;

import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.site.SiteModule;
import org.itss.prj_itss.model.warehouse.WarehouseModule;

public final class WarehouseControllerModule {

    private final ConfirmOrderArrivalController confirmOrderArrivalController;

    public WarehouseControllerModule(WarehouseModule warehouseModule, SiteModule siteModule, CatalogModule catalogModule) {
        this.confirmOrderArrivalController = new ConfirmOrderArrivalController(
            warehouseModule.warehouseReceivingUseCase(),
            siteModule.siteUseCase(),
            catalogModule.catalogUseCase()
        );
    }

    public ConfirmOrderArrivalController confirmOrderArrivalController() {
        return confirmOrderArrivalController;
    }
}
