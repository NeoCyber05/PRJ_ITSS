package org.itss.prj_itss.common.config;

import org.itss.prj_itss.repository.AccountRepository;
import org.itss.prj_itss.repository.InventoryRepository;
import org.itss.prj_itss.repository.JdbcAccountRepository;
import org.itss.prj_itss.repository.JdbcMerchandiseRepository;
import org.itss.prj_itss.repository.JdbcOrderRepository;
import org.itss.prj_itss.repository.JdbcRequestRepository;
import org.itss.prj_itss.repository.JdbcSiteRepository;
import org.itss.prj_itss.repository.MerchandiseRepository;
import org.itss.prj_itss.repository.OrderRepository;
import org.itss.prj_itss.repository.RequestRepository;
import org.itss.prj_itss.repository.SiteRepository;
import org.itss.prj_itss.service.AllocationPlanningService;
import org.itss.prj_itss.service.AuthenticationService;
import org.itss.prj_itss.service.DashboardService;
import org.itss.prj_itss.service.MerchandiseService;
import org.itss.prj_itss.service.OrderService;
import org.itss.prj_itss.service.RequestProcessingService;
import org.itss.prj_itss.service.RequestService;
import org.itss.prj_itss.service.SiteService;

public final class ApplicationContext {

    private static final ApplicationContext INSTANCE = new ApplicationContext();

    private final TransactionManager transactionManager = new TransactionManager();
    private final ConnectionProvider connectionProvider = new DatabaseConnectionProvider(transactionManager);

    private final AccountRepository accountRepository = new JdbcAccountRepository(connectionProvider);
    private final JdbcSiteRepository siteRepository = new JdbcSiteRepository(connectionProvider);
    private final RequestRepository requestRepository = new JdbcRequestRepository(connectionProvider);
    private final OrderRepository orderRepository = new JdbcOrderRepository(connectionProvider);
    private final MerchandiseRepository merchandiseRepository = new JdbcMerchandiseRepository(connectionProvider);
    private final SiteRepository siteReadRepository = siteRepository;
    private final InventoryRepository inventoryRepository = siteRepository;

    private final AuthenticationService authenticationService = new AuthenticationService(accountRepository);
    private final RequestService requestService = new RequestService(requestRepository);
    private final OrderService orderService = new OrderService(orderRepository);
    private final SiteService siteService = new SiteService(siteReadRepository, inventoryRepository);
    private final MerchandiseService merchandiseService = new MerchandiseService(merchandiseRepository);
    private final DashboardService dashboardService = new DashboardService(requestService, orderService, siteService);
    private final AllocationPlanningService allocationPlanningService = new AllocationPlanningService();
    private final RequestProcessingService requestProcessingService = new RequestProcessingService(
        requestRepository,
        orderRepository,
        siteReadRepository,
        inventoryRepository,
        merchandiseRepository,
        transactionManager
    );

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

    public AllocationPlanningService allocationPlanningService() {
        return allocationPlanningService;
    }

    public RequestProcessingService requestProcessingService() {
        return requestProcessingService;
    }
}
