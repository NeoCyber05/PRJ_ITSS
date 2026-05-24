package org.itss.prj_itss.model.warehouse;

import org.itss.prj_itss.common.config.SharedInfrastructure;
import org.itss.prj_itss.common.config.TransactionRunnerAdapter;
import org.itss.prj_itss.model.auth.AuthModule;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.site.SiteModule;
import org.itss.prj_itss.model.warehouse.application.WarehouseReceivingUseCase;
import org.itss.prj_itss.model.warehouse.application.port.WarehouseReceiptRepository;
import org.itss.prj_itss.model.warehouse.infrastructure.persistence.JdbcWarehouseReceiptRepository;

public final class WarehouseModule {

    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final WarehouseReceivingUseCase warehouseReceivingUseCase;

    public WarehouseModule(
        SharedInfrastructure infrastructure,
        AuthModule authModule,
        OrderModule orderModule,
        SiteModule siteModule,
        CatalogModule catalogModule
    ) {
        this.warehouseReceiptRepository = new JdbcWarehouseReceiptRepository(infrastructure.warehouseConnectionProvider());
        this.warehouseReceivingUseCase = new WarehouseReceivingUseCase(
            orderModule.orderUseCase(),
            siteModule.siteUseCase(),
            catalogModule.catalogUseCase(),
            warehouseReceiptRepository,
            new TransactionRunnerAdapter(infrastructure.warehouseTransactionManager()),
            authModule.currentUserSupplier()
        );
    }

    public WarehouseReceivingUseCase warehouseReceivingUseCase() {
        return warehouseReceivingUseCase;
    }
}
