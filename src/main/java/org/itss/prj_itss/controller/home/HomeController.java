package org.itss.prj_itss.controller.home;

import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.model.dashboard.application.DashboardData;
import org.itss.prj_itss.model.dashboard.application.DashboardQuery;

import java.time.LocalDate;

public class HomeController {
    private final Navigator navigator;
    private final DashboardQuery dashboardQuery;

    public HomeController(Navigator navigator, DashboardQuery dashboardQuery) {
        this.navigator = navigator;
        this.dashboardQuery = dashboardQuery;
    }

    public DashboardData loadDashboardData() {
        return dashboardQuery.loadDashboardData();
    }

    public void navigateTo(String viewId) {
        navigator.showView(viewId);
    }
}
