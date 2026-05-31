package org.itss.prj_itss.model.request;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.dashboard.application.port.DashboardRequestPort;
import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailApplicationService;
import org.itss.prj_itss.model.request.application.sales.RequestSalesApplicationService;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestProcessingGateway;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestRepository;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcReceivedRequestDetailQuery;
import org.itss.prj_itss.model.site.SiteModule;

public final class RequestModule {

    private final JdbcRequestRepository jdbcRequestRepository;
    private final RequestProcessingUseCase requestProcessingUseCase;
    private final ReceivedRequestsApplicationService receivedRequestsApplicationService;
    private final ReceivedRequestDetailApplicationService receivedRequestDetailApplicationService;
    private final RequestSalesApplicationService requestSalesApplicationService;

    public RequestModule(
        ConnectionProvider connectionProvider,
        TransactionRunner transactionRunner,
        OrderModule orderModule,
        SiteModule siteModule,
        CatalogModule catalogModule
    ) {
        this.jdbcRequestRepository = new JdbcRequestRepository(connectionProvider);
        this.requestProcessingUseCase = new RequestProcessingUseCase(
            new JdbcRequestProcessingGateway(
                jdbcRequestRepository,
                orderModule.orderRepository(),
                siteModule.siteRepository(),
                siteModule.inventoryRepository(),
                catalogModule.merchandiseRepository(),
                transactionRunner
            )
        );
        this.receivedRequestsApplicationService =
            new ReceivedRequestsApplicationService(jdbcRequestRepository);
        this.receivedRequestDetailApplicationService = new ReceivedRequestDetailApplicationService(
            new JdbcReceivedRequestDetailQuery(connectionProvider)
        );
        this.requestSalesApplicationService =
            new RequestSalesApplicationService(jdbcRequestRepository, catalogModule.catalogUseCase());
    }

    public DashboardRequestPort dashboardRequestPort() {
        return jdbcRequestRepository;
    }

    public RequestProcessingUseCase requestProcessingUseCase() {
        return requestProcessingUseCase;
    }

    public ReceivedRequestsApplicationService receivedRequestsApplicationService() {
        return receivedRequestsApplicationService;
    }

    public ReceivedRequestDetailApplicationService receivedRequestDetailApplicationService() {
        return receivedRequestDetailApplicationService;
    }

    public RequestSalesApplicationService requestSalesApplicationService() {
        return requestSalesApplicationService;
    }
}
