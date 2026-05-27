package org.itss.prj_itss.model.site;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.site.application.SiteManagementApplicationService;
import org.itss.prj_itss.model.site.application.SiteUseCase;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.site.application.port.SiteRepository;
import org.itss.prj_itss.model.site.infrastructure.persistence.JdbcSiteRepository;

public final class SiteModule {

    private final JdbcSiteRepository siteRepository;
    private final SiteUseCase siteUseCase;
    private final SiteManagementApplicationService siteManagementApplicationService;

    public SiteModule(ConnectionProvider connectionProvider, CatalogModule catalogModule) {
        this.siteRepository = new JdbcSiteRepository(connectionProvider);
        this.siteUseCase = new SiteUseCase(siteRepository, siteRepository);
        this.siteManagementApplicationService =
            new SiteManagementApplicationService(siteUseCase, catalogModule.catalogUseCase());
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
