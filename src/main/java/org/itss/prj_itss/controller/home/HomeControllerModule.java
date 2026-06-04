package org.itss.prj_itss.controller.home;

import org.itss.prj_itss.controller.navigation.SimpleNavigator;
import org.itss.prj_itss.model.dashboard.DashboardModule;
import org.itss.prj_itss.model.request.RequestModule;

public final class HomeControllerModule {

    private final HomeController homeController;

    public HomeControllerModule(SimpleNavigator navigator, DashboardModule dashboardModule, RequestModule requestModule) {
        this.homeController = new HomeController(
            navigator,
            dashboardModule.dashboardQuery()
        );
    }

    public HomeController homeController() {
        return homeController;
    }
}
