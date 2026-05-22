package org.itss.prj_itss.bootstrap;

import org.itss.prj_itss.controller.auth.RoleWorkspaceController;
import org.itss.prj_itss.controller.home.HomeController;
import org.itss.prj_itss.controller.ordering.order.OrderCancellationController;
import org.itss.prj_itss.controller.ordering.order.OrderDetailController;
import org.itss.prj_itss.controller.ordering.order.OrderManagementController;
import org.itss.prj_itss.controller.ordering.request.ReceivedRequestsController;
import org.itss.prj_itss.controller.ordering.request.RequestDetailPopupController;
import org.itss.prj_itss.controller.ordering.request.RequestProcessingController;
import org.itss.prj_itss.controller.ordering.site.SiteManagementController;
import org.itss.prj_itss.controller.sales.request.CreateOrderRequestController;
import org.itss.prj_itss.controller.sales.request.SalesRequestListController;
import org.itss.prj_itss.controller.sales.request.UpdateOrderRequestController;
import org.itss.prj_itss.controller.sales.request.ViewOrderRequestController;
import org.itss.prj_itss.controller.warehouse.ConfirmOrderArrivalController;
import org.itss.prj_itss.controller.navigation.SimpleNavigator;

public final class ControllerRegistry {

    private final ModelContext modelContext;
    private final SimpleNavigator navigator = new SimpleNavigator();

    private final RoleWorkspaceController roleWorkspaceController;
    private final HomeController homeController;
    private final SiteManagementController siteManagementController;
    private final OrderManagementController orderManagementController;
    private final OrderDetailController orderDetailController;
    private final OrderCancellationController orderCancellationController;
    private final ReceivedRequestsController receivedRequestsController;
    private final RequestDetailPopupController requestDetailPopupController;
    private final RequestProcessingController requestProcessingController;
    private final SalesRequestListController salesRequestListController;
    private final CreateOrderRequestController createOrderRequestController;
    private final UpdateOrderRequestController updateOrderRequestController;
    private final ViewOrderRequestController viewOrderRequestController;
    private final ConfirmOrderArrivalController confirmOrderArrivalController;

    public ControllerRegistry(ModelContext modelContext) {
        this.modelContext = modelContext;

        this.roleWorkspaceController = new RoleWorkspaceController(navigator);
        this.homeController = new HomeController(navigator, modelContext.dashboardQuery(), modelContext.requestManagementUseCase());
        this.siteManagementController = new SiteManagementController(modelContext.siteManagementApplicationService());
        this.orderManagementController = new OrderManagementController(modelContext.orderManagementApplicationService());
        this.orderDetailController = new OrderDetailController(
            modelContext.orderUseCase(),
            modelContext.orderCancellationApplicationService(),
            modelContext.siteUseCase(),
            modelContext.catalogUseCase()
        );
        this.orderCancellationController = new OrderCancellationController(modelContext.orderCancellationApplicationService());
        this.receivedRequestsController = new ReceivedRequestsController(modelContext.receivedRequestsApplicationService());
        this.requestDetailPopupController = new RequestDetailPopupController(
            modelContext.requestDetailApplicationService(),
            modelContext.orderCancellationApplicationService()
        );
        this.requestProcessingController = new RequestProcessingController(modelContext.requestProcessingUseCase());
        this.salesRequestListController = new SalesRequestListController(modelContext.receivedRequestsApplicationService());
        this.createOrderRequestController = new CreateOrderRequestController(modelContext.requestSalesApplicationService());
        this.updateOrderRequestController = new UpdateOrderRequestController(modelContext.requestSalesApplicationService());
        this.viewOrderRequestController = new ViewOrderRequestController(modelContext.requestSalesApplicationService());
        this.confirmOrderArrivalController = new ConfirmOrderArrivalController(
            modelContext.warehouseReceivingUseCase(),
            modelContext.siteUseCase(),
            modelContext.catalogUseCase()
        );
    }

    public SimpleNavigator navigator() {
        return navigator;
    }

    public RoleWorkspaceController roleWorkspaceController() {
        return roleWorkspaceController;
    }

    public HomeController homeController() {
        return homeController;
    }

    public SiteManagementController siteManagementController() {
        return siteManagementController;
    }

    public OrderManagementController orderManagementController() {
        return orderManagementController;
    }

    public OrderDetailController orderDetailController() {
        return orderDetailController;
    }

    public OrderCancellationController orderCancellationController() {
        return orderCancellationController;
    }

    public ReceivedRequestsController receivedRequestsController() {
        return receivedRequestsController;
    }

    public RequestDetailPopupController requestDetailPopupController() {
        return requestDetailPopupController;
    }

    public RequestProcessingController requestProcessingController() {
        return requestProcessingController;
    }

    public SalesRequestListController salesRequestListController() {
        return salesRequestListController;
    }

    public CreateOrderRequestController createOrderRequestController() {
        return createOrderRequestController;
    }

    public UpdateOrderRequestController updateOrderRequestController() {
        return updateOrderRequestController;
    }

    public ViewOrderRequestController viewOrderRequestController() {
        return viewOrderRequestController;
    }

    public ConfirmOrderArrivalController confirmOrderArrivalController() {
        return confirmOrderArrivalController;
    }
}
