package org.itss.prj_itss.common.config;

import org.itss.prj_itss.auth.application.AuthSession;
import org.itss.prj_itss.auth.application.AuthenticationService;
import org.itss.prj_itss.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.auth.infrastructure.persistence.JdbcAccountRepository;
import org.itss.prj_itss.auth.application.port.AccountRepository;
import org.itss.prj_itss.request.infrastructure.persistence.JdbcRequestProcessingGateway;
import org.itss.prj_itss.site.application.port.InventoryRepository;
import org.itss.prj_itss.catalog.infrastructure.persistence.JdbcMerchandiseRepository;
import org.itss.prj_itss.order.infrastructure.persistence.JdbcOrderRepository;
import org.itss.prj_itss.request.infrastructure.persistence.JdbcRequestRepository;
import org.itss.prj_itss.site.infrastructure.persistence.JdbcSiteRepository;
import org.itss.prj_itss.warehouse.infrastructure.persistence.JdbcWarehouseReceiptRepository;
import org.itss.prj_itss.catalog.application.port.MerchandiseRepository;
import org.itss.prj_itss.order.application.port.OrderRepository;
import org.itss.prj_itss.request.application.port.RequestRepository;
import org.itss.prj_itss.site.application.port.SiteRepository;
import org.itss.prj_itss.warehouse.application.port.WarehouseReceiptRepository;
import org.itss.prj_itss.dashboard.application.DashboardQuery;
import org.itss.prj_itss.catalog.application.CatalogUseCase;
import org.itss.prj_itss.order.application.OrderUseCase;
import org.itss.prj_itss.order.application.OrderCancellationApplicationService;
import org.itss.prj_itss.order.application.OrderManagementApplicationService;
import org.itss.prj_itss.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.request.application.sales.RequestDetailApplicationService;
import org.itss.prj_itss.request.application.sales.RequestSalesApplicationService;
import org.itss.prj_itss.site.application.SiteManagementApplicationService;
import org.itss.prj_itss.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.request.application.RequestManagementUseCase;
import org.itss.prj_itss.site.application.SiteUseCase;

public final class ApplicationContext {

    private static final ApplicationContext INSTANCE = new ApplicationContext();

    private final TransactionManager transactionManager = new TransactionManager();
    private final IConnectionProvider connectionProvider = new DatabaseConnectionProvider(transactionManager);

    private final WarehouseTransactionManager warehouseTransactionManager = new WarehouseTransactionManager();
    private final IConnectionProvider warehouseConnectionProvider = new WarehouseConnectionProvider(warehouseTransactionManager);

    private final AccountRepository accountRepository = new JdbcAccountRepository(connectionProvider);
    private final JdbcSiteRepository siteRepository = new JdbcSiteRepository(connectionProvider);
    private final RequestRepository requestRepository = new JdbcRequestRepository(connectionProvider);
    private final OrderRepository orderRepository = new JdbcOrderRepository(connectionProvider);
    private final WarehouseReceiptRepository warehouseReceiptRepository = new JdbcWarehouseReceiptRepository(warehouseConnectionProvider);
    private final MerchandiseRepository merchandiseRepository = new JdbcMerchandiseRepository(connectionProvider);
    private final SiteRepository siteReadRepository = siteRepository;
    private final InventoryRepository inventoryRepository = siteRepository;

    private final AuthenticationService authenticationService = new AuthenticationService(accountRepository);
    private final RequestManagementUseCase requestManagementUseCase = new RequestManagementUseCase(requestRepository);
    private final OrderUseCase orderUseCase = new OrderUseCase(orderRepository);
    private final SiteUseCase siteUseCase = new SiteUseCase(siteReadRepository, inventoryRepository);
    private final CatalogUseCase catalogUseCase = new CatalogUseCase(merchandiseRepository);
    private final DashboardQuery dashboardQuery = new DashboardQuery(requestManagementUseCase, orderUseCase, siteUseCase);
    private final org.itss.prj_itss.warehouse.application.WarehouseReceivingUseCase warehouseReceivingUseCase =
        new org.itss.prj_itss.warehouse.application.WarehouseReceivingUseCase(
            orderUseCase,
            siteUseCase,
            catalogUseCase,
            warehouseReceiptRepository,
            new TransactionRunnerAdapter(warehouseTransactionManager),
            this::currentAuthenticatedUser
        );
    private final RequestProcessingUseCase requestProcessingUseCase = new RequestProcessingUseCase(
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
        new SiteManagementApplicationService(siteUseCase, catalogUseCase);
    private final OrderManagementApplicationService orderManagementApplicationService =
        new OrderManagementApplicationService(orderUseCase, siteUseCase, catalogUseCase);
    private final OrderCancellationApplicationService orderCancellationApplicationService =
        new OrderCancellationApplicationService(orderUseCase);
    private final ReceivedRequestsApplicationService receivedRequestsApplicationService =
        new ReceivedRequestsApplicationService(requestManagementUseCase);
    private final RequestDetailApplicationService requestDetailApplicationService =
        new RequestDetailApplicationService(requestManagementUseCase, orderUseCase, siteUseCase, catalogUseCase);
    private final RequestSalesApplicationService requestSalesApplicationService =
        new RequestSalesApplicationService(requestManagementUseCase, catalogUseCase);

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

    public RequestManagementUseCase requestManagementUseCase() {
        return requestManagementUseCase;
    }

    public OrderUseCase orderUseCase() {
        return orderUseCase;
    }

    public SiteUseCase siteUseCase() {
        return siteUseCase;
    }

    public CatalogUseCase catalogUseCase() {
        return catalogUseCase;
    }

    public DashboardQuery dashboardQuery() {
        return dashboardQuery;
    }

    public org.itss.prj_itss.warehouse.application.WarehouseReceivingUseCase warehouseReceivingUseCase() {
        return warehouseReceivingUseCase;
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

    public RequestSalesApplicationService requestSalesApplicationService() {
        return requestSalesApplicationService;
    }

    public RequestProcessingUseCase requestProcessingUseCase() {
        return requestProcessingUseCase;
    }

    public void setAuthenticatedUser(org.itss.prj_itss.auth.domain.AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            authSession.clear();
        } else {
            authSession.start(authenticatedUser);
        }
    }

    public AuthenticatedUser currentAuthenticatedUser() {
        return authSession.currentAuthenticatedUser();
    }
}
