package org.itss.prj_itss.bootstrap;

import org.itss.prj_itss.bootstrap.navigation.LoadedView;
import org.itss.prj_itss.bootstrap.navigation.RouteRegistry;
import org.itss.prj_itss.controller.auth.AuthControllerModule;
import org.itss.prj_itss.controller.home.HomeControllerModule;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.navigation.SimpleNavigator;
import org.itss.prj_itss.controller.ordering.order.OrderControllerModule;
import org.itss.prj_itss.controller.ordering.request.RequestControllerModule;
import org.itss.prj_itss.controller.ordering.site.SiteControllerModule;
import org.itss.prj_itss.controller.sales.request.SalesRequestControllerModule;
import org.itss.prj_itss.controller.warehouse.WarehouseControllerModule;
import org.itss.prj_itss.model.auth.AuthModule;
import org.itss.prj_itss.model.auth.application.AuthenticationService;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.catalog.CatalogModule;
import org.itss.prj_itss.model.dashboard.DashboardModule;
import org.itss.prj_itss.model.order.OrderModule;
import org.itss.prj_itss.model.request.RequestModule;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.site.SiteModule;
import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.DatabaseConnectionProvider;
import org.itss.prj_itss.model.shared.database.TransactionManager;
import org.itss.prj_itss.model.shared.database.WarehouseConnectionProvider;
import org.itss.prj_itss.model.shared.database.WarehouseTransactionManager;
import org.itss.prj_itss.model.warehouse.WarehouseModule;
import org.itss.prj_itss.view.auth.RoleWorkspaceView;
import org.itss.prj_itss.view.home.HomeView;
import org.itss.prj_itss.view.ordering.order.OrderCancellationView;
import org.itss.prj_itss.view.ordering.order.OrderDetailView;
import org.itss.prj_itss.view.ordering.order.OrderManagementView;
import org.itss.prj_itss.view.ordering.request.ReceivedRequestsView;
import org.itss.prj_itss.view.ordering.request.RequestDetailContext;
import org.itss.prj_itss.view.ordering.request.process.layout.RequestProcessingLayoutView;
import org.itss.prj_itss.view.ordering.site.SiteManagementView;
import org.itss.prj_itss.view.sales.request.list.SalesRequestListView;
import org.itss.prj_itss.view.sales.request.update.SalesRequestEditDialog;
import org.itss.prj_itss.view.warehouse.ConfirmOrderArrivalView;

import java.util.List;

public final class MvcContext {

    private static final String ORDER_DETAIL_PREFIX = "order-detail:";
    private static final String REQUEST_PROCESSING_PREFIX = "request-processing:";
    private static final String SALES_REQUEST_UPDATE_PREFIX = "sales-request-update:";
    private static final String SALES_REQUEST_DETAIL_PREFIX = "sales-request-detail:";

    private final TransactionManager transactionManager = new TransactionManager();
    private final ConnectionProvider connectionProvider = new DatabaseConnectionProvider(transactionManager);
    private final WarehouseTransactionManager warehouseTransactionManager = new WarehouseTransactionManager();
    private final ConnectionProvider warehouseConnectionProvider =
        new WarehouseConnectionProvider(warehouseTransactionManager);

    private final AuthModule authModule = new AuthModule(connectionProvider);
    private final CatalogModule catalogModule = new CatalogModule(connectionProvider);
    private final SiteModule siteModule = new SiteModule(connectionProvider, catalogModule);
    private final OrderModule orderModule = new OrderModule(connectionProvider, siteModule, catalogModule);
    private final RequestModule requestModule =
        new RequestModule(connectionProvider, transactionManager, orderModule, siteModule, catalogModule);
    private final WarehouseModule warehouseModule =
        new WarehouseModule(
            warehouseConnectionProvider,
            warehouseTransactionManager,
            authModule,
            orderModule,
            siteModule,
            catalogModule
        );
    private final DashboardModule dashboardModule = new DashboardModule(requestModule, orderModule, siteModule);

    private final SimpleNavigator navigator = new SimpleNavigator();

    private final AuthControllerModule authControllers = new AuthControllerModule(navigator);
    private final HomeControllerModule homeControllers =
        new HomeControllerModule(navigator, dashboardModule, requestModule);
    private final SiteControllerModule siteControllers = new SiteControllerModule(siteModule);
    private final OrderControllerModule orderControllers =
        new OrderControllerModule(orderModule, siteModule, catalogModule);
    private final RequestControllerModule requestControllers =
        new RequestControllerModule(requestModule, orderModule);
    private final SalesRequestControllerModule salesRequestControllers =
        new SalesRequestControllerModule(requestModule);
    private final WarehouseControllerModule warehouseControllers =
        new WarehouseControllerModule(warehouseModule, siteModule, catalogModule);
    private final RouteRegistry routeRegistry = new RouteRegistry(List.of(
        RouteRegistry.fxml(
            "home",
            "/org/itss/prj_itss/view/home/home-view.fxml",
            (viewId, viewInstance, navigator) ->
                ((HomeView) viewInstance).setController(homeControllers.homeController())
        ),
        RouteRegistry.fxml(
            "site-management",
            "/org/itss/prj_itss/view/ordering/site/site-management-view.fxml",
            (viewId, viewInstance, navigator) ->
                ((SiteManagementView) viewInstance).init(navigator, siteControllers.siteManagementController())
        ),
        RouteRegistry.fxml(
            "received-requests",
            "/org/itss/prj_itss/view/ordering/request/received-requests-view.fxml",
            (viewId, viewInstance, navigator) ->
                ((ReceivedRequestsView) viewInstance).init(
                    navigator,
                    requestControllers.receivedRequestsController(),
                    new RequestDetailContext(
                        requestControllers.requestDetailPopupController(),
                        orderControllers.orderDetailController(),
                        orderControllers.orderManagementController(),
                        navigator
                    )
                )
        ),
        RouteRegistry.fxml(
            "orders",
            "/org/itss/prj_itss/view/ordering/order/order-management-view.fxml",
            (viewId, viewInstance, navigator) ->
                ((OrderManagementView) viewInstance).init(navigator, orderControllers.orderManagementController())
        ),
        RouteRegistry.dynamic(
            viewId -> viewId.startsWith(ORDER_DETAIL_PREFIX),
            viewId -> viewId,
            viewId -> "orders",
            false,
            this::loadOrderDetailView
        ),
        RouteRegistry.fxml(
            "ordering-order-handle-cancellation",
            "/org/itss/prj_itss/view/ordering/order/handle-order-cancellation-view.fxml",
            (viewId, viewInstance, navigator) ->
                ((OrderCancellationView) viewInstance).init(navigator, orderControllers.orderCancellationController())
        ),
        RouteRegistry.dynamic(
            viewId -> "request-processing".equals(viewId) || viewId.startsWith(REQUEST_PROCESSING_PREFIX),
            viewId -> viewId,
            viewId -> "received-requests",
            false,
            this::loadRequestProcessingView
        ),
        RouteRegistry.fxml(
            "sales-requests",
            "/org/itss/prj_itss/view/sales/request/list/sales-request-list-view.fxml",
            this::configureSalesRequestList
        ),
        RouteRegistry.dynamic(
            "sales-request-create"::equals,
            viewId -> "sales-requests",
            viewId -> "sales-requests",
            true,
            (viewId, navigator) -> RouteRegistry.loadFxml(
                "/org/itss/prj_itss/view/sales/request/list/sales-request-list-view.fxml",
                viewId,
                navigator,
                this::configureSalesRequestList
            )
        ),
        RouteRegistry.prefixedFxml(
            SALES_REQUEST_UPDATE_PREFIX,
            "sales-requests",
            "sales-requests",
            "/org/itss/prj_itss/view/sales/request/list/sales-request-list-view.fxml",
            this::configureSalesRequestList
        ),
        RouteRegistry.prefixedFxml(
            SALES_REQUEST_DETAIL_PREFIX,
            "sales-requests",
            "sales-requests",
            "/org/itss/prj_itss/view/sales/request/list/sales-request-list-view.fxml",
            this::configureSalesRequestList
        ),
        RouteRegistry.fxml(
            "warehouse-order-confirm-arrival",
            "/org/itss/prj_itss/view/warehouse/confirm-order-arrival-view.fxml",
            (viewId, viewInstance, navigator) ->
                ((ConfirmOrderArrivalView) viewInstance)
                    .setController(warehouseControllers.confirmOrderArrivalController())
        ),
        RouteRegistry.fxml(
            "role-workspace",
            "/org/itss/prj_itss/view/auth/role-workspace-view.fxml",
            (viewId, viewInstance, navigator) -> {
                RoleWorkspaceView roleWorkspaceView = (RoleWorkspaceView) viewInstance;
                roleWorkspaceView.setController(authControllers.roleWorkspaceController());
                roleWorkspaceView.setUser(currentAuthenticatedUser());
            }
        )
    ));

    public void warmUpDatabaseConnection() {
        try {
            connectionProvider.getConnection();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize database connection", exception);
        }
    }

    public AuthenticationService authenticationService() {
        return authModule.authenticationService();
    }

    public void setAuthenticatedUser(AuthenticatedUser user) {
        authModule.setAuthenticatedUser(user);
    }

    public AuthenticatedUser currentAuthenticatedUser() {
        return authModule.currentAuthenticatedUser();
    }

    public SimpleNavigator navigator() {
        return navigator;
    }

    public RouteRegistry routeRegistry() {
        return routeRegistry;
    }

    public RequestProcessingUseCase requestProcessingUseCase() {
        return requestModule.requestProcessingUseCase();
    }

    public AuthControllerModule authControllers() {
        return authControllers;
    }

    public HomeControllerModule homeControllers() {
        return homeControllers;
    }

    public SiteControllerModule siteControllers() {
        return siteControllers;
    }

    public OrderControllerModule orderControllers() {
        return orderControllers;
    }

    public RequestControllerModule requestControllers() {
        return requestControllers;
    }

    public SalesRequestControllerModule salesRequestControllers() {
        return salesRequestControllers;
    }

    public WarehouseControllerModule warehouseControllers() {
        return warehouseControllers;
    }

    private LoadedView loadOrderDetailView(String viewId, Navigator navigator) {
        String orderId = viewId.substring(ORDER_DETAIL_PREFIX.length());
        OrderDetailView detailView = new OrderDetailView();
        detailView.init(
            navigator,
            orderControllers.orderDetailController(),
            orderControllers.orderManagementController(),
            orderId
        );
        return new LoadedView(detailView.getView(), detailView);
    }

    private LoadedView loadRequestProcessingView(String viewId, Navigator navigator) throws Exception {
        int requestId = viewId.startsWith(REQUEST_PROCESSING_PREFIX)
            ? parsePositiveInt(viewId.substring(REQUEST_PROCESSING_PREFIX.length()), 1)
            : 1;
        return RouteRegistry.loadFxml(
            "/org/itss/prj_itss/view/ordering/request/process/layout/request-processing-layout-view.fxml",
            viewId,
            navigator,
            (requestedViewId, viewInstance, routeNavigator) -> {
                RequestProcessingLayoutView requestProcessingView = (RequestProcessingLayoutView) viewInstance;
                requestProcessingView.init(requestProcessingUseCase(), routeNavigator::showView);
                requestProcessingView.setRequestId(requestId);
            }
        );
    }

    private void configureSalesRequestList(String viewId, Object viewInstance, Navigator navigator) {
        ((SalesRequestListView) viewInstance).init(
            navigator,
            salesRequestControllers.salesRequestListController(),
            salesRequestControllers.salesRequestCreationController(),
            new SalesRequestEditDialog(salesRequestControllers.salesRequestEditController()),
            salesRequestControllers.viewOrderRequestController()
        );
    }

    private int parsePositiveInt(String rawValue, int fallback) {
        try {
            int parsed = Integer.parseInt(rawValue.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
