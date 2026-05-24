package org.itss.prj_itss.bootstrap;

import org.itss.prj_itss.common.config.SharedInfrastructure;
import org.itss.prj_itss.controller.auth.AuthControllerModule;
import org.itss.prj_itss.controller.home.HomeControllerModule;
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
import org.itss.prj_itss.model.warehouse.WarehouseModule;

public final class AppContainer {

    private final SharedInfrastructure infrastructure = new SharedInfrastructure();

    private final AuthModule authModule = new AuthModule(infrastructure);
    private final CatalogModule catalogModule = new CatalogModule(infrastructure);
    private final SiteModule siteModule = new SiteModule(infrastructure, catalogModule);
    private final OrderModule orderModule = new OrderModule(infrastructure, siteModule, catalogModule);
    private final RequestModule requestModule = new RequestModule(infrastructure, orderModule, siteModule, catalogModule);
    private final WarehouseModule warehouseModule =
        new WarehouseModule(infrastructure, authModule, orderModule, siteModule, catalogModule);
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

    public void warmUpDatabaseConnection() {
        infrastructure.warmUpDatabaseConnection();
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
}
