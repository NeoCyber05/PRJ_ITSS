package org.itss.prj_itss.model.request;

import org.itss.prj_itss.model.dashboard.application.port.DashboardRequestPort;
import org.itss.prj_itss.model.merchandise.MerchandiseModule;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailApplicationService;
import org.itss.prj_itss.model.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.model.request.application.port.RequestDisplayFormatter;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandService;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryService;
import org.itss.prj_itss.model.request.application.sales.create.SalesRequestCreationApplicationService;
import org.itss.prj_itss.model.request.application.sales.create.SalesRequestCreationValidator;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditApplicationService;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditMapper;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditUseCase;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidator;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestProcessingGateway;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcReceivedRequestDetailQuery;
import org.itss.prj_itss.model.request.infrastructure.persistence.JdbcRequestRepository;
import org.itss.prj_itss.model.request.domain.processing.allocation.policy.FastDeliveryObjective;
import org.itss.prj_itss.model.request.domain.processing.allocation.validator.DefaultAllocationValidator;
import org.itss.prj_itss.model.request.domain.processing.suggestion.DefaultAllocationSuggester;
import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.TransactionRunner;
import org.itss.prj_itss.model.shared.formatting.OrderingRequestDisplayFormatter;
import org.itss.prj_itss.model.site.SiteModule;

import java.time.Clock;

public final class RequestModule {

    private final JdbcRequestRepository jdbcRequestRepository;
    private final OrderingRequestDisplayFormatter requestDisplayFormatter = new OrderingRequestDisplayFormatter();
    private final RequestProcessingUseCase requestProcessingUseCase;
    private final ReceivedRequestsApplicationService receivedRequestsApplicationService;
    private final ReceivedRequestDetailApplicationService receivedRequestDetailApplicationService;
    private final SalesRequestQueryService salesRequestQueryService;
    private final SalesRequestCommandService salesRequestCommandService;
    private final SalesRequestCreationApplicationService salesRequestCreationApplicationService;
    private final SalesRequestEditUseCase salesRequestEditUseCase;

    public RequestModule(
        ConnectionProvider connectionProvider,
        TransactionRunner transactionRunner,
        OrderModule orderModule,
        SiteModule siteModule,
        MerchandiseModule merchandiseModule
    ) {
        this.jdbcRequestRepository = new JdbcRequestRepository(connectionProvider);
        this.requestProcessingUseCase = new RequestProcessingUseCase(
            new JdbcRequestProcessingGateway(
                jdbcRequestRepository,
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
            new ReceivedRequestsApplicationService(jdbcRequestRepository, requestDisplayFormatter);
        this.receivedRequestDetailApplicationService = new ReceivedRequestDetailApplicationService(
            new JdbcReceivedRequestDetailQuery(connectionProvider)
        );
        this.salesRequestQueryService = new SalesRequestQueryService(
            jdbcRequestRepository,
            merchandiseModule.merchandiseUseCase(),
            requestDisplayFormatter
        );
        this.salesRequestCommandService = new SalesRequestCommandService(jdbcRequestRepository);
        this.salesRequestCreationApplicationService =
            new SalesRequestCreationApplicationService(
                jdbcRequestRepository,
                merchandiseModule.merchandiseUseCase(),
                new SalesRequestCreationValidator(),
                Clock.systemDefaultZone()
            );
        this.salesRequestEditUseCase =
            new SalesRequestEditApplicationService(
                jdbcRequestRepository,
                jdbcRequestRepository,
                merchandiseModule.merchandiseUseCase(),
                new SalesRequestEditMapper(),
                new SalesRequestEditValidator(),
                Clock.systemDefaultZone()
            );
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

    public SalesRequestQueryService salesRequestQueryService() {
        return salesRequestQueryService;
    }

    public SalesRequestCommandService salesRequestCommandService() {
        return salesRequestCommandService;
    }

    public org.itss.prj_itss.model.request.application.processing.ProcessingRequestPort requestRepository() {
        return jdbcRequestRepository;
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
