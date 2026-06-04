package org.itss.prj_itss.model.request;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.request.application.RequestManagementUseCase;
import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.model.request.application.port.RequestRepository;
import org.itss.prj_itss.model.request.application.port.RequestDisplayFormatter;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.request.application.sales.detail.RequestDetailApplicationService;
import org.itss.prj_itss.model.request.application.sales.RequestSalesApplicationService;
import org.itss.prj_itss.model.request.application.sales.create.SalesRequestCreationApplicationService;
import org.itss.prj_itss.model.request.application.sales.create.SalesRequestCreationValidator;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditApplicationService;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditMapper;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditUseCase;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidator;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestProcessingGateway;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestRepository;
import org.itss.prj_itss.model.shared.formatting.OrderingRequestDisplayFormatter;
import org.itss.prj_itss.model.site.SiteModule;

import java.time.Clock;

public final class RequestModule {

    private final RequestRepository requestRepository;
    private final OrderingRequestDisplayFormatter requestDisplayFormatter = new OrderingRequestDisplayFormatter();
    private final RequestManagementUseCase requestManagementUseCase;
    private final RequestProcessingUseCase requestProcessingUseCase;
    private final ReceivedRequestsApplicationService receivedRequestsApplicationService;
    private final RequestDetailApplicationService requestDetailApplicationService;
    private final RequestSalesApplicationService requestSalesApplicationService;
    private final SalesRequestCreationApplicationService salesRequestCreationApplicationService;
    private final SalesRequestEditUseCase salesRequestEditUseCase;

    public RequestModule(
        ConnectionProvider connectionProvider,
        TransactionRunner transactionRunner,
        OrderModule orderModule,
        SiteModule siteModule,
        CatalogModule catalogModule
    ) {
        this.requestRepository = new JdbcRequestRepository(connectionProvider);
        this.requestManagementUseCase = new RequestManagementUseCase(requestRepository);
        this.requestProcessingUseCase = new RequestProcessingUseCase(
            new JdbcRequestProcessingGateway(
                requestRepository,
                orderModule.orderRepository(),
                siteModule.siteRepository(),
                siteModule.inventoryRepository(),
                catalogModule.merchandiseRepository(),
                transactionRunner
            )
        );
        this.receivedRequestsApplicationService =
            new ReceivedRequestsApplicationService(requestManagementUseCase, requestDisplayFormatter);
        this.requestDetailApplicationService = new RequestDetailApplicationService(
            requestManagementUseCase,
            orderModule.orderUseCase(),
            siteModule.siteUseCase(),
            catalogModule.catalogUseCase(),
            requestDisplayFormatter
        );
        this.requestSalesApplicationService =
            new RequestSalesApplicationService(
                requestManagementUseCase,
                catalogModule.catalogUseCase(),
                requestDisplayFormatter
            );
        this.salesRequestCreationApplicationService =
            new SalesRequestCreationApplicationService(
                requestManagementUseCase,
                catalogModule.catalogUseCase(),
                new SalesRequestCreationValidator(),
                Clock.systemDefaultZone()
            );
        this.salesRequestEditUseCase =
            new SalesRequestEditApplicationService(
                requestManagementUseCase,
                catalogModule.catalogUseCase(),
                new SalesRequestEditMapper(),
                new SalesRequestEditValidator(),
                Clock.systemDefaultZone()
            );
    }

    public RequestManagementUseCase requestManagementUseCase() {
        return requestManagementUseCase;
    }

    public RequestProcessingUseCase requestProcessingUseCase() {
        return requestProcessingUseCase;
    }

    public ReceivedRequestsApplicationService receivedRequestsApplicationService() {
        return receivedRequestsApplicationService;
    }

    public RequestDetailApplicationService requestDetailApplicationService() {
        return requestDetailApplicationService;
    }

    public RequestSalesApplicationService requestSalesApplicationService() {
        return requestSalesApplicationService;
    }

    public SalesRequestCreationApplicationService salesRequestCreationApplicationService() {
        return salesRequestCreationApplicationService;
    }

    public SalesRequestEditUseCase salesRequestEditUseCase() {
        return salesRequestEditUseCase;
    }

    public RequestDisplayFormatter requestDisplayFormatter() {
        return requestDisplayFormatter;
    }
}
