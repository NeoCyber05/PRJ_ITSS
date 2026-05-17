package org.itss.prj_itss.common.config;

import org.itss.prj_itss.auth.AuthenticatedUser;
import org.itss.prj_itss.auth.application.AuthSession;
import org.itss.prj_itss.auth.application.AuthenticationService;
import org.itss.prj_itss.auth.infrastructure.AccountRepository;
import org.itss.prj_itss.auth.infrastructure.IAccountRepository;
import org.itss.prj_itss.repository.IInventoryRepository;
import org.itss.prj_itss.repository.MerchandiseRepository;
import org.itss.prj_itss.repository.OrderRepository;
import org.itss.prj_itss.repository.RequestRepository;
import org.itss.prj_itss.repository.RequestProcessingRepositoryGateway;
import org.itss.prj_itss.repository.SiteRepository;
import org.itss.prj_itss.repository.WarehouseReceiptRepository;
import org.itss.prj_itss.repository.IMerchandiseRepository;
import org.itss.prj_itss.repository.IOrderRepository;
import org.itss.prj_itss.repository.IRequestRepository;
import org.itss.prj_itss.repository.ISiteRepository;
import org.itss.prj_itss.service.DashboardService;
import org.itss.prj_itss.service.MerchandiseService;
import org.itss.prj_itss.service.OrderService;
import org.itss.prj_itss.ordering.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.ordering.order.application.OrderManagementApplicationService;
import org.itss.prj_itss.ordering.request.application.ReceivedRequestsApplicationService;
import org.itss.prj_itss.ordering.request.application.RequestDetailApplicationService;
import org.itss.prj_itss.ordering.request.application.RequestProcessingApplicationService;
import org.itss.prj_itss.ordering.request.process.RequestProcessingService;
import org.itss.prj_itss.ordering.site.application.SiteManagementApplicationService;
import org.itss.prj_itss.request.business.service.RequestProcessingUseCase;
import org.itss.prj_itss.request.data.JdbcRequestProcessingGateway;
import org.itss.prj_itss.service.RequestService;
import org.itss.prj_itss.service.SiteService;

public final class ApplicationContext {

    private static final ApplicationContext INSTANCE = new ApplicationContext();

    private final TransactionManager transactionManager = new TransactionManager();
    private final IConnectionProvider connectionProvider = new DatabaseConnectionProvider(transactionManager);

    private final WarehouseTransactionManager warehouseTransactionManager = new WarehouseTransactionManager();
    private final IConnectionProvider warehouseConnectionProvider = new WarehouseConnectionProvider(warehouseTransactionManager);

    private final IAccountRepository accountRepository = new AccountRepository(connectionProvider);
    private final SiteRepository siteRepository = new SiteRepository(connectionProvider);
    private final IRequestRepository requestRepository = new RequestRepository(connectionProvider);
    private final IOrderRepository orderRepository = new OrderRepository(connectionProvider);
    private final WarehouseReceiptRepository warehouseReceiptRepository = new WarehouseReceiptRepository(warehouseConnectionProvider);
    private final IMerchandiseRepository merchandiseRepository = new MerchandiseRepository(connectionProvider);
    private final ISiteRepository siteReadRepository = siteRepository;
    private final IInventoryRepository inventoryRepository = siteRepository;

    private final AuthenticationService authenticationService = new AuthenticationService(accountRepository);
    private final RequestService requestService = new RequestService(requestRepository);
    private final OrderService orderService = new OrderService(orderRepository);
    private final SiteService siteService = new SiteService(siteReadRepository, inventoryRepository);
    private final MerchandiseService merchandiseService = new MerchandiseService(merchandiseRepository);
    private final DashboardService dashboardService = new DashboardService(requestService, orderService, siteService);
    private final org.itss.prj_itss.warehouse.order.confirm_arrival.ConfirmOrderArrivalService confirmOrderArrivalService =
        new org.itss.prj_itss.warehouse.order.confirm_arrival.ConfirmOrderArrivalService(
            orderService,
            siteService,
            merchandiseService,
            warehouseReceiptRepository,
            warehouseTransactionManager,
            this::currentAuthenticatedUser
        );
    private final RequestProcessingService requestProcessingService = new RequestProcessingService(
        new RequestProcessingRepositoryGateway(
            requestRepository,
            orderRepository,
            siteReadRepository,
            inventoryRepository,
            merchandiseRepository,
            transactionManager
        )
    );
    private final RequestProcessingUseCase requestProcessingUseCaseV2 = new RequestProcessingUseCase(
        new JdbcRequestProcessingGateway(
            requestRepository,
            orderRepository,
            siteReadRepository,
            inventoryRepository,
            merchandiseRepository,
            transactionManager
        )
    );
    private final SiteManagementApplicationService siteManagementApplicationService =
        new SiteManagementApplicationService(siteService, merchandiseService);
    private final OrderManagementApplicationService orderManagementApplicationService =
        new OrderManagementApplicationService(orderService, siteService, merchandiseService);
    private final OrderCancellationApplicationService orderCancellationApplicationService =
        new OrderCancellationApplicationService(orderService);
    private final ReceivedRequestsApplicationService receivedRequestsApplicationService =
        new ReceivedRequestsApplicationService(requestService);
    private final RequestDetailApplicationService requestDetailApplicationService =
        new RequestDetailApplicationService(requestService, orderService);
    private final RequestProcessingApplicationService requestProcessingApplicationService =
        new RequestProcessingApplicationService(requestProcessingService);

    private final AuthSession authSession = new AuthSession();

    private ApplicationContext() {
    }

    public static ApplicationContext getInstance() {
        return INSTANCE;
    }

    public AuthenticationService authenticationService() {
        return authenticationService;
    }

    public void warmUpDatabaseConnection() {
        try {
            connectionProvider.getConnection();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize database connection", exception);
        }
    }

    public RequestService requestService() {
        return requestService;
    }

    public OrderService orderService() {
        return orderService;
    }

    public SiteService siteService() {
        return siteService;
    }

    public MerchandiseService merchandiseService() {
        return merchandiseService;
    }

    public DashboardService dashboardService() {
        return dashboardService;
    }

    public org.itss.prj_itss.warehouse.order.confirm_arrival.ConfirmOrderArrivalService confirmOrderArrivalService() {
        return confirmOrderArrivalService;
    }

    public RequestProcessingService requestProcessingService() {
        return requestProcessingService;
    }

    public SiteManagementApplicationService siteManagementApplicationService() {
        return siteManagementApplicationService;
    }

    public OrderManagementApplicationService orderManagementApplicationService() {
        return orderManagementApplicationService;
    }

    public OrderCancellationApplicationService orderCancellationApplicationService() {
        return orderCancellationApplicationService;
    }

    public ReceivedRequestsApplicationService receivedRequestsApplicationService() {
        return receivedRequestsApplicationService;
    }

    public RequestDetailApplicationService requestDetailApplicationService() {
        return requestDetailApplicationService;
    }

    public RequestProcessingApplicationService requestProcessingApplicationService() {
        return requestProcessingApplicationService;
    }

    public RequestProcessingUseCase requestProcessingUseCaseV2() {
        return requestProcessingUseCaseV2;
    }

    public void setAuthenticatedUser(org.itss.prj_itss.auth.domain.AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            authSession.clear();
        } else {
            authSession.start(authenticatedUser);
        }
    }

    public AuthenticatedUser currentAuthenticatedUser() {
        org.itss.prj_itss.auth.domain.AuthenticatedUser currentUser = authSession.currentAuthenticatedUser();
        return currentUser == null ? null : currentUser.toLegacy();
    }
}
