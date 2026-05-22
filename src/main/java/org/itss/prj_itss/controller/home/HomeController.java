package org.itss.prj_itss.controller.home;

import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.dashboard.application.DashboardData;
import org.itss.prj_itss.dashboard.application.DashboardQuery;
import org.itss.prj_itss.model.request.application.RequestManagementUseCase;

import java.time.LocalDate;

public class HomeController {
    private final Navigator navigator;
    private final DashboardQuery dashboardQuery;
    private final RequestManagementUseCase requestService;

    public HomeController(Navigator navigator, DashboardQuery dashboardQuery, RequestManagementUseCase requestService) {
        this.navigator = navigator;
        this.dashboardQuery = dashboardQuery;
        this.requestService = requestService;
    }

    public DashboardData loadDashboardData() {
        return dashboardQuery.loadDashboardData();
    }

    public LocalDate getEarliestDeliveryDate(int requestId) {
        return requestService.getEarliestDeliveryDate(requestId);
    }

    public int countItemTypes(int requestId) {
        return requestService.countItemTypes(requestId);
    }

    public void navigateTo(String viewId) {
        navigator.showView(viewId);
    }
}
