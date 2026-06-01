package org.itss.prj_itss.model.request;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.request.application.RequestManagementUseCase;
import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.model.request.application.port.RequestRepository;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.request.application.sales.detail.RequestDetailApplicationService;
import org.itss.prj_itss.model.request.application.sales.RequestSalesApplicationService;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestProcessingGateway;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestRepository;
import org.itss.prj_itss.model.site.SiteModule;

public final class RequestModule {

    private final JdbcRequestRepository requestRepository;
    private final RequestManagementUseCase requestManagementUseCase;
    private final RequestProcessingUseCase requestProcessingUseCase;
    private final ReceivedRequestsApplicationService receivedRequestsApplicationService;
    private final RequestDetailApplicationService requestDetailApplicationService;
    private final RequestSalesApplicationService requestSalesApplicationService;

    public RequestModule(
            ConnectionProvider connectionProvider,
            TransactionRunner transactionRunner,
            OrderModule orderModule,
            SiteModule siteModule,
            CatalogModule catalogModule) {
        this.requestRepository = new JdbcRequestRepository(connectionProvider);
        this.requestManagementUseCase = new RequestManagementUseCase(requestRepository, requestRepository);
        this.requestProcessingUseCase = new RequestProcessingUseCase(
                new JdbcRequestProcessingGateway(
                        requestRepository,
                        requestRepository,
                        orderModule.orderRepository(),
                        siteModule.siteRepository(),
                        siteModule.inventoryRepository(),
                        catalogModule.merchandiseRepository(),
                        transactionRunner));
        this.receivedRequestsApplicationService = new ReceivedRequestsApplicationService(requestManagementUseCase);
        this.requestDetailApplicationService = new RequestDetailApplicationService(
                requestManagementUseCase,
                orderModule.orderUseCase(),
                siteModule.siteUseCase(),
                catalogModule.catalogUseCase());
        this.requestSalesApplicationService = new RequestSalesApplicationService(requestManagementUseCase,
                catalogModule.catalogUseCase());
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
}
