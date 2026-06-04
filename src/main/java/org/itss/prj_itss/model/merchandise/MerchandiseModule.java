package org.itss.prj_itss.model.merchandise;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.merchandise.application.MerchandiseManagementService;
import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;
import org.itss.prj_itss.model.merchandise.infrastructure.persistence.JdbcMerchandiseRepository;

public final class MerchandiseModule {

    private final MerchandiseRepository merchandiseRepository;
    private final MerchandiseUseCase merchandiseUseCase;
    private final MerchandiseManagementService merchandiseManagementService;

    public MerchandiseModule(ConnectionProvider connectionProvider) {
        this.merchandiseRepository = new JdbcMerchandiseRepository(connectionProvider);
        this.merchandiseUseCase = new MerchandiseUseCase(merchandiseRepository);
        this.merchandiseManagementService = new MerchandiseManagementService(merchandiseRepository);
    }

    public MerchandiseRepository merchandiseRepository() {
        return merchandiseRepository;
    }

    public MerchandiseUseCase merchandiseUseCase() {
        return merchandiseUseCase;
    }

    public MerchandiseManagementService merchandiseManagementService() {
        return merchandiseManagementService;
    }
}
