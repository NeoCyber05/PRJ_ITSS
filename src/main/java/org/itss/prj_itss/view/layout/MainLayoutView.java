package org.itss.prj_itss.view.layout;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.App;
import org.itss.prj_itss.bootstrap.MvcContext;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.application.RoleAccessPolicy;
import org.itss.prj_itss.model.auth.domain.RoleType;
import org.itss.prj_itss.view.auth.RoleWorkspaceContent;
import org.itss.prj_itss.view.auth.RoleWorkspaceContentFactory;
import org.itss.prj_itss.view.auth.RoleWorkspaceView;
import org.itss.prj_itss.view.home.HomeView;
import org.itss.prj_itss.view.ordering.order.OrderDetailView;
import org.itss.prj_itss.view.ordering.order.OrderManagementView;
import org.itss.prj_itss.view.ordering.order.OrderCancellationView;
import org.itss.prj_itss.view.ordering.site.SiteManagementView;
import org.itss.prj_itss.view.ordering.request.ReceivedRequestsView;
import org.itss.prj_itss.view.ordering.request.process.layout.RequestProcessingLayoutView;
import org.itss.prj_itss.view.sales.request.list.SalesRequestListView;
import org.itss.prj_itss.view.sales.request.update.UpdateOrderRequestPopup;
import org.itss.prj_itss.view.warehouse.ConfirmOrderArrivalView;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class MainLayoutView implements Navigator {

    private static final String ORDER_DETAIL_PREFIX = "order-detail:";
    private static final String REQUEST_PROCESSING_PREFIX = "request-processing:";
    private static final String SALES_REQUEST_UPDATE_PREFIX = "sales-request-update:";
    private static final String SALES_REQUEST_DETAIL_PREFIX = "sales-request-detail:";

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, LoadedView> cachedViews = new HashMap<>();
    
    private MvcContext mvcContext;
    private AuthenticatedUser currentUser;
    private Runnable logoutHandler;
    private String activeViewId;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label brandSubtitleLabel;

    @FXML
    private Button homeButton;

    @FXML
    private Button siteManagementButton;

    @FXML
    private Button receivedRequestsButton;

    @FXML
    private Button ordersButton;

    @FXML
    private Label userInitialsLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private VBox ordersNavContainer;

    @FXML
    private Button salesRequestsButton;

    @FXML
    private VBox salesNavContainer;

    @FXML
    private void initialize() {
        registerNavButton("home", homeButton);
        registerNavButton("site-management", siteManagementButton);
        registerNavButton("received-requests", receivedRequestsButton);
        registerNavButton("orders", ordersButton);
        registerNavButton("sales-requests", salesRequestsButton);
    }

    public void init(MvcContext mvcContext) {
        this.mvcContext = mvcContext;
    }

    public void setUser(AuthenticatedUser user) {
        this.currentUser = user;
        if (mvcContext != null) {
            mvcContext.setAuthenticatedUser(user);
        }
        updateUIForUser();
        showView(RoleAccessPolicy.defaultViewId(currentUser));
    }

    public void setLogoutHandler(Runnable logoutHandler) {
        this.logoutHandler = logoutHandler;
    }

    @Override
    public void showView(String viewId) {
        if (currentUser == null) {
            return;
        }

        ResolvedNavigation resolvedNavigation = resolveNavigation(viewId);
        if (resolvedNavigation.viewId().equals(activeViewId)) {
            return;
        }

        contentArea.getChildren().setAll(resolvedNavigation.view().node());
        if (resolvedNavigation.view().viewInstance() instanceof ViewLifecycle viewLifecycle) {
            viewLifecycle.onViewShown();
        }
        setActiveNav(resolvedNavigation.navTarget());
        activeViewId = resolvedNavigation.viewId();
    }

    @Override
    public void showViewWithData(String viewId, Object data) {
        showView(viewId);
    }

    private void registerNavButton(String viewId, Button button) {
        button.setOnAction(event -> showView(viewId));
        navButtons.put(viewId, button);
    }

    private void setActiveNav(String navTarget) {
        for (Button button : navButtons.values()) {
            button.getStyleClass().remove("shell-nav-button-active");
        }
        Button active = navButtons.get(navTarget);
        if (active != null) {
            active.getStyleClass().add("shell-nav-button-active");
        }
    }

    private ResolvedNavigation resolveNavigation(String requestedViewId) {
        String targetViewId = resolveAccessibleViewId(requestedViewId);
        LoadedView loadedView = resolveView(targetViewId);
        return new ResolvedNavigation(targetViewId, resolveNavTarget(targetViewId), loadedView);
    }

    private String resolveAccessibleViewId(String requestedViewId) {
        String defaultViewId = RoleAccessPolicy.defaultViewId(currentUser);
        if (requestedViewId == null || requestedViewId.isBlank()) {
            return defaultViewId;
        }
        return RoleAccessPolicy.canAccess(currentUser, requestedViewId)
            ? requestedViewId
            : defaultViewId;
    }

    private String resolveNavTarget(String viewId) {
        if (viewId.startsWith(ORDER_DETAIL_PREFIX)) {
            return "orders";
        }
        if (viewId.startsWith("request-processing")) {
            return "received-requests";
        }
        if (viewId.startsWith("role-workspace")) {
            return "home";
        }
        if (viewId.startsWith(SALES_REQUEST_UPDATE_PREFIX) || viewId.startsWith(SALES_REQUEST_DETAIL_PREFIX)) {
            return "sales-requests";
        }
        if ("sales-request-create".equals(viewId)) {
            return "sales-requests";
        }
        return viewId;
    }

    private LoadedView resolveView(String viewId) {
        if (viewId.startsWith(ORDER_DETAIL_PREFIX)) {
            String orderId = viewId.substring(ORDER_DETAIL_PREFIX.length());
            OrderDetailView detailView = new OrderDetailView();
            detailView.init(
                this,
                mvcContext.orderControllers().orderDetailController(),
                mvcContext.orderControllers().orderManagementController(),
                orderId
            );
            return new LoadedView(detailView.getView(), detailView);
        }

        if (viewId.startsWith(REQUEST_PROCESSING_PREFIX)) {
            int requestId = parsePositiveInt(viewId.substring(REQUEST_PROCESSING_PREFIX.length()), 1);
            return loadRequestProcessingView(requestId);
        }

        if (viewId.startsWith(SALES_REQUEST_DETAIL_PREFIX)) {
            return getOrLoadCachedView("sales-requests", "/org/itss/prj_itss/view/sales/request/list/sales-request-list-view.fxml");
        }

        return switch (viewId) {
            case "home" -> getOrLoadCachedView("home", "/org/itss/prj_itss/home/home-view.fxml");
            case "site-management" -> getOrLoadCachedView("site-management", "/org/itss/prj_itss/ordering/site/site-management-view.fxml");
            case "received-requests" -> getOrLoadCachedView("received-requests", "/org/itss/prj_itss/ordering/request/received/received-requests-view.fxml");
            case "orders" -> getOrLoadCachedView("orders", "/org/itss/prj_itss/ordering/order/order-management-view.fxml");
            case "sales-requests" -> getOrLoadCachedView("sales-requests", "/org/itss/prj_itss/view/sales/request/list/sales-request-list-view.fxml");
            case "warehouse-order-confirm-arrival" -> getOrLoadCachedView("warehouse-order-confirm-arrival", "/org/itss/prj_itss/warehouse/order/confirm_arrival/confirm-order-arrival-view.fxml");
            case "ordering-order-handle-cancellation" -> getOrLoadCachedView("ordering-order-handle-cancellation", "/org/itss/prj_itss/ordering/order/handle_cancellation/handle-order-cancellation-view.fxml");
            case "role-workspace" -> getOrLoadCachedView("role-workspace", "/org/itss/prj_itss/auth/workspace/role-workspace-view.fxml");
            case "request-processing" -> loadRequestProcessingView(1);
            default -> fallbackToDefaultView(viewId);
        };
    }

    private LoadedView fallbackToDefaultView(String viewId) {
        String defaultViewId = RoleAccessPolicy.defaultViewId(currentUser);
        if (defaultViewId == null || defaultViewId.equals(viewId)) {
            return buildErrorView("Khong the xac dinh man hinh: " + viewId);
        }
        return resolveView(defaultViewId);
    }

    private LoadedView loadRequestProcessingView(int requestId) {
        return loadView(
            "/org/itss/prj_itss/ordering/request/process/layout/request-processing-view.fxml",
            viewInstance -> {
                if (viewInstance instanceof RequestProcessingLayoutView requestProcessingView) {
                    requestProcessingView.init(mvcContext.requestProcessingUseCase(), this::showView);
                    requestProcessingView.setRequestId(requestId);
                }
            }
        );
    }

    private LoadedView getOrLoadCachedView(String cacheKey, String resourcePath) {
        return cachedViews.computeIfAbsent(cacheKey, key -> loadView(resourcePath, null));
    }

    private LoadedView loadView(String resourcePath, Consumer<Object> viewConfigurer) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(resourcePath));
            Node node = loader.load();
            Object viewInstance = loader.getController();
            initializeView(viewInstance);
            if (viewConfigurer != null) {
                viewConfigurer.accept(viewInstance);
            }
            return new LoadedView(node, viewInstance);
        } catch (Exception exception) {
            exception.printStackTrace();
            String message = exception.getMessage();
            if (message == null || message.isBlank()) {
                message = exception.getClass().getSimpleName();
            }
            return buildErrorView("Khong the tai man hinh: " + resourcePath + "\n" + message);
        }
    }

    private void initializeView(Object viewInstance) {
        if (viewInstance instanceof HomeView homeView) {
            homeView.setController(mvcContext.homeControllers().homeController());
        } else if (viewInstance instanceof SiteManagementView siteManagementView) {
            siteManagementView.init(this, mvcContext.siteControllers().siteManagementController());
        } else if (viewInstance instanceof ReceivedRequestsView receivedRequestsView) {
            receivedRequestsView.init(
                this,
                mvcContext.requestControllers().receivedRequestsController(),
                mvcContext.requestControllers().requestDetailPopupController(),
                mvcContext.orderControllers().orderDetailController(),
                mvcContext.orderControllers().orderManagementController()
            );
        } else if (viewInstance instanceof OrderManagementView orderManagementView) {
            orderManagementView.init(this, mvcContext.orderControllers().orderManagementController());
        } else if (viewInstance instanceof OrderCancellationView orderCancellationView) {
            orderCancellationView.init(this, mvcContext.orderControllers().orderCancellationController());
        } else if (viewInstance instanceof SalesRequestListView salesRequestListView) {
            salesRequestListView.init(
                this,
                mvcContext.salesRequestControllers().salesRequestListController(),
                mvcContext.salesRequestControllers().createOrderRequestController(),
                new UpdateOrderRequestPopup(mvcContext.salesRequestControllers().updateOrderRequestController()),
                mvcContext.salesRequestControllers().viewOrderRequestController()
            );
        } else if (viewInstance instanceof ConfirmOrderArrivalView confirmOrderArrivalView) {
            confirmOrderArrivalView.setController(mvcContext.warehouseControllers().confirmOrderArrivalController());
        } else if (viewInstance instanceof RoleWorkspaceView roleWorkspaceView) {
            roleWorkspaceView.setController(mvcContext.authControllers().roleWorkspaceController());
            roleWorkspaceView.setUser(currentUser);
        }
    }

    private LoadedView buildErrorView(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setWrapText(true);
        StackPane errorPane = new StackPane(errorLabel);
        errorPane.getStyleClass().add("content-area");
        return new LoadedView(errorPane, null);
    }

    private int parsePositiveInt(String rawValue, int fallback) {
        try {
            int parsed = Integer.parseInt(rawValue.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private void updateUIForUser() {
        RoleWorkspaceContent content = RoleWorkspaceContentFactory.create(currentUser);
        brandSubtitleLabel.setText(content.sidebarSubtitle());
        homeButton.setText(content.homeLabel());
        userInitialsLabel.setText(currentUser.initials());
        userNameLabel.setText(currentUser.displayName());
        userRoleLabel.setText(currentUser.roleName());

        RoleType role = RoleType.from(currentUser);
        boolean orderingRole = role.isOrderingRole();
        ordersNavContainer.setVisible(orderingRole);
        ordersNavContainer.setManaged(orderingRole);

        boolean salesRole = role.isSalesRole();
        salesNavContainer.setVisible(salesRole);
        salesNavContainer.setManaged(salesRole);
    }

    @FXML
    private void handleLogout() {
        cachedViews.clear();
        activeViewId = null;
        if (mvcContext != null) {
            mvcContext.setAuthenticatedUser(null);
        }
        if (logoutHandler != null) {
            logoutHandler.run();
        }
    }

    private record ResolvedNavigation(String viewId, String navTarget, LoadedView view) {
    }

    private record LoadedView(Node node, Object viewInstance) {
    }
}
