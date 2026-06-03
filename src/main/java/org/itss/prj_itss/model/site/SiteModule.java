package org.itss.prj_itss.model.site;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.order.application.port.SiteOrderRepository;
import org.itss.prj_itss.model.site.application.SiteManagementApplicationService;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.site.application.port.SiteAccountProvisioningPort;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.itss.prj_itss.model.site.application.self.OverseasSiteApplicationService;
import org.itss.prj_itss.model.site.infrastructure.persistence.JdbcSiteRepository;

public final class SiteModule {

    private final JdbcSiteRepository siteRepository;
    private final SiteUseCase siteUseCase;
    private final SiteManagementApplicationService siteManagementApplicationService;
    private final CatalogModule catalogModule;
    private OverseasSiteApplicationService overseasSiteApplicationService;

    public SiteModule(
            ConnectionProvider connectionProvider,
            TransactionRunner transactionRunner,
            CatalogModule catalogModule,
            SiteAccountProvisioningPort siteAccountProvisioningPort) {
        this.siteRepository = new JdbcSiteRepository(connectionProvider);
        this.siteUseCase = new SiteUseCase(siteRepository, siteRepository);
        this.siteManagementApplicationService = new SiteManagementApplicationService(
            siteUseCase,
            catalogModule.catalogUseCase(),
            siteRepository,
            siteAccountProvisioningPort,
            transactionRunner
        );
        this.catalogModule = catalogModule;
    }

    public void initializeSiteOrderRepository(SiteOrderRepository siteOrderRepository) {
        this.overseasSiteApplicationService = new OverseasSiteApplicationService(
            siteUseCase,
            catalogModule.catalogUseCase(),
            siteRepository,
            siteRepository,
            siteOrderRepository
        );
    }

    public OverseasSiteApplicationService overseasSiteApplicationService() {
        if (overseasSiteApplicationService == null) {
            throw new IllegalStateException("Site order repository has not been initialized");
        }
        return overseasSiteApplicationService;
    }

    public SiteRepository siteRepository() {
        return siteRepository;
    }

    public InventoryRepository inventoryRepository() {
        return siteRepository;
    }

    public SiteUseCase siteUseCase() {
        return siteUseCase;
    }

    public SiteManagementApplicationService siteManagementApplicationService() {
        return siteManagementApplicationService;
    }
}
