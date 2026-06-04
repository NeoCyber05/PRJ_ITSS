package org.itss.prj_itss.model.request;

import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.merchandise.MerchandiseModule;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.dashboard.application.port.DashboardRequestPort;
import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailApplicationService;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryService;
import org.itss.prj_itss.model.request.application.sales.create.CreateSalesRequestService;
import org.itss.prj_itss.model.request.application.sales.create.CreateSalesRequestUseCase;
import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandService;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestProcessingGateway;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcReceivedRequestDetailQuery;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcSalesRequestCommandRepository;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcSalesRequestQueryRepository;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcReceivedRequestsRepository;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcDashboardRequestRepository;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcProcessingRequestRepository;
import org.itss.prj_itss.model.site.SiteModule;
import org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidator;
import org.itss.prj_itss.model.request.domain.processing.suggestion.DefaultAllocationSuggester;
import org.itss.prj_itss.model.request.domain.processing.allocation.policy.FastDeliveryObjective;

public final class RequestModule {

    private final JdbcSalesRequestCommandRepository jdbcSalesRequestCommandRepository;
    private final JdbcSalesRequestQueryRepository jdbcSalesRequestQueryRepository;
    private final JdbcReceivedRequestsRepository jdbcReceivedRequestsRepository;
    private final JdbcDashboardRequestRepository jdbcDashboardRequestRepository;
    private final JdbcProcessingRequestRepository jdbcProcessingRequestRepository;

    private final RequestProcessingUseCase requestProcessingUseCase;
    private final ReceivedRequestsApplicationService receivedRequestsApplicationService;
    private final ReceivedRequestDetailApplicationService receivedRequestDetailApplicationService;
    private final SalesRequestQueryService salesRequestQueryService;
    private final SalesRequestCommandService salesRequestCommandService;
    private final CreateSalesRequestService createSalesRequestService;

    public RequestModule(
        ConnectionProvider connectionProvider,
        TransactionRunner transactionRunner,
        OrderModule orderModule,
        SiteModule siteModule,
        MerchandiseModule merchandiseModule
    ) {
        this.jdbcSalesRequestCommandRepository = new JdbcSalesRequestCommandRepository(connectionProvider);
        this.jdbcSalesRequestQueryRepository = new JdbcSalesRequestQueryRepository(connectionProvider);
        this.jdbcReceivedRequestsRepository = new JdbcReceivedRequestsRepository(connectionProvider);
        this.jdbcDashboardRequestRepository = new JdbcDashboardRequestRepository(connectionProvider);
        this.jdbcProcessingRequestRepository = new JdbcProcessingRequestRepository(connectionProvider);

        this.requestProcessingUseCase = new RequestProcessingUseCase(
            new JdbcRequestProcessingGateway(
                jdbcProcessingRequestRepository,
                orderModule.orderRepository(),
                siteModule.siteRepository(),
                siteModule.inventoryRepository(),
                merchandiseModule.merchandiseRepository(),
                transactionRunner
            ),
            new DefaultAllocationValidator(),
            new DefaultAllocationSuggester(new FastDeliveryObjective())
        );
        this.receivedRequestsApplicationService =
            new ReceivedRequestsApplicationService(jdbcReceivedRequestsRepository);
        this.receivedRequestDetailApplicationService = new ReceivedRequestDetailApplicationService(
            new JdbcReceivedRequestDetailQuery(connectionProvider)
        );
        this.salesRequestQueryService = new SalesRequestQueryService(jdbcSalesRequestQueryRepository, merchandiseModule.merchandiseUseCase(), siteModule.inventoryRepository());
        this.salesRequestCommandService = new SalesRequestCommandService(jdbcSalesRequestCommandRepository);
        this.createSalesRequestService = new CreateSalesRequestService(jdbcSalesRequestCommandRepository);
    }

    public DashboardRequestPort dashboardRequestPort() {
        return jdbcDashboardRequestRepository;
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

    public SalesRequestQueryService salesRequestQueryService() {
        return salesRequestQueryService;
    }

    public SalesRequestCommandService salesRequestCommandService() {
        return salesRequestCommandService;
    }

    public CreateSalesRequestUseCase createSalesRequestUseCase() {
        return createSalesRequestService;
    }

    public org.itss.prj_itss.model.request.application.processing.ProcessingRequestPort requestRepository() {
        return jdbcProcessingRequestRepository;
    }
}
