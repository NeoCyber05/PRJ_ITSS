package org.itss.prj_itss.controller.sales.merchandise;

import org.itss.prj_itss.model.merchandise.MerchandiseModule;

public final class SalesMerchandiseControllerModule {

    private final SalesMerchandiseController salesMerchandiseController;

    public SalesMerchandiseControllerModule(MerchandiseModule merchandiseModule) {
        this.salesMerchandiseController = new SalesMerchandiseController(merchandiseModule.merchandiseManagementService());
    }

    public SalesMerchandiseController salesMerchandiseController() {
        return salesMerchandiseController;
    }
}
