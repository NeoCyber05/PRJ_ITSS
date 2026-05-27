package org.itss.prj_itss.model.catalog;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.catalog.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.catalog.infrastructure.persistence.JdbcMerchandiseRepository;

public final class CatalogModule {

    private final MerchandiseRepository merchandiseRepository;
    private final CatalogUseCase catalogUseCase;

    public CatalogModule(ConnectionProvider connectionProvider) {
        this.merchandiseRepository = new JdbcMerchandiseRepository(connectionProvider);
        this.catalogUseCase = new CatalogUseCase(merchandiseRepository);
    }

    public MerchandiseRepository merchandiseRepository() {
        return merchandiseRepository;
    }

    public CatalogUseCase catalogUseCase() {
        return catalogUseCase;
    }
}
