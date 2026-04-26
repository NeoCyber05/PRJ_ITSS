package org.itss.prj_itss.common.config;

import org.itss.prj_itss.repository.IAccountRepository;
import org.itss.prj_itss.repository.IInventoryRepository;
import org.itss.prj_itss.repository.AccountRepository;
import org.itss.prj_itss.repository.MerchandiseRepository;
import org.itss.prj_itss.repository.OrderRepository;
import org.itss.prj_itss.repository.RequestRepository;
import org.itss.prj_itss.repository.SiteRepository;
import org.itss.prj_itss.repository.IMerchandiseRepository;
import org.itss.prj_itss.repository.IOrderRepository;
import org.itss.prj_itss.repository.IRequestRepository;
import org.itss.prj_itss.repository.ISiteRepository;
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
    private final IConnectionProvider connectionProvider = new DatabaseConnectionProvider(transactionManager);

    private final IAccountRepository accountRepository = new AccountRepository(connectionProvider);
    private final SiteRepository siteRepository = new SiteRepository(connectionProvider);
    private final IRequestRepository requestRepository = new RequestRepository(connectionProvider);
    private final IOrderRepository orderRepository = new OrderRepository(connectionProvider);
    private final IMerchandiseRepository merchandiseRepository = new MerchandiseRepository(connectionProvider);
    private final ISiteRepository siteReadRepository = siteRepository;
    private final IInventoryRepository inventoryRepository = siteRepository;

    private final AuthenticationService authenticationService = new AuthenticationService(accountRepository);
    private final RequestService requestService = new RequestService(requestRepository);
    private final OrderService orderService = new OrderService(orderRepository);
    private final SiteService siteService = new SiteService(siteReadRepository, inventoryRepository);
    private final MerchandiseService merchandiseService = new MerchandiseService(merchandiseRepository);
    private final DashboardService dashboardService = new DashboardService(requestService, orderService, siteService);
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

    public RequestProcessingService requestProcessingService() {
        return requestProcessingService;
    }
}
