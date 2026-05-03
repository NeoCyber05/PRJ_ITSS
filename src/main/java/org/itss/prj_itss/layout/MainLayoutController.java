package org.itss.prj_itss.layout;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.App;
import org.itss.prj_itss.auth.AuthenticatedUser;
import org.itss.prj_itss.auth.role.RoleAccessPolicy;
import org.itss.prj_itss.auth.role.RoleType;
import org.itss.prj_itss.auth.workspace.RoleWorkspaceContent;
import org.itss.prj_itss.auth.workspace.RoleWorkspaceContentFactory;
import org.itss.prj_itss.auth.workspace.RoleWorkspaceController;
import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.ordering.order.OrderDetailView;
import org.itss.prj_itss.ordering.request.process.RequestProcessingController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MainLayoutController implements INavigator {

    private static final String ORDER_DETAIL_PREFIX = "order-detail:";
    private static final String REQUEST_PROCESSING_PREFIX = "request-processing:";
    private static final String SALES_REQUEST_UPDATE_PREFIX = "sales-request-update:";
    private static final String SALES_REQUEST_DETAIL_PREFIX = "sales-request-detail:";

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, LoadedView> cachedViews = new HashMap<>();
    private final ApplicationContext context = ApplicationContext.getInstance();
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

    public void setUser(AuthenticatedUser user) {
        this.currentUser = user;
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
        if (resolvedNavigation.view().controller() instanceof IViewController viewController) {
            viewController.onViewShown(resolvedNavigation.viewId());
        }
        setActiveNav(resolvedNavigation.navTarget());
        activeViewId = resolvedNavigation.viewId();
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
            return new LoadedView(new OrderDetailView(this, context, orderId).getView(), null);
        }

        if (viewId.startsWith(REQUEST_PROCESSING_PREFIX)) {
            int requestId = parsePositiveInt(viewId.substring(REQUEST_PROCESSING_PREFIX.length()), 1);
            return loadRequestProcessingView(requestId);
        }

        if (viewId.startsWith(SALES_REQUEST_DETAIL_PREFIX)) {
            return getOrLoadCachedView("sales-requests", "/org/itss/prj_itss/sales/request/sales-request-list-view.fxml");
        }

        return switch (viewId) {
            case "home" -> getOrLoadCachedView("home", "/org/itss/prj_itss/home/home-view.fxml");
            case "site-management" -> getOrLoadCachedView("site-management", "/org/itss/prj_itss/ordering/site/site-management-view.fxml");
            case "received-requests" -> getOrLoadCachedView("received-requests", "/org/itss/prj_itss/ordering/request/received/received-requests-view.fxml");
            case "orders" -> getOrLoadCachedView("orders", "/org/itss/prj_itss/ordering/order/order-management-view.fxml");
            case "sales-requests" -> getOrLoadCachedView("sales-requests", "/org/itss/prj_itss/sales/request/sales-request-list-view.fxml");
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
            "/org/itss/prj_itss/ordering/request/process/request-processing-view.fxml",
            controller -> {
                if (controller instanceof RequestProcessingController requestProcessingController) {
                    requestProcessingController.setRequestId(requestId);
                }
            }
        );
    }

    private LoadedView getOrLoadCachedView(String cacheKey, String resourcePath) {
        return cachedViews.computeIfAbsent(cacheKey, key -> loadView(resourcePath, null));
    }

    private LoadedView loadView(String resourcePath, Consumer<Object> controllerConfigurer) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(resourcePath));
            Node view = loader.load();
            Object controller = loader.getController();
            initializeController(controller);
            if (controllerConfigurer != null) {
                controllerConfigurer.accept(controller);
            }
            return new LoadedView(view, controller);
        } catch (Exception exception) {
            exception.printStackTrace();
            String message = exception.getMessage();
            if (message == null || message.isBlank()) {
                message = exception.getClass().getSimpleName();
            }
            return buildErrorView("Khong the tai man hinh: " + resourcePath + "\n" + message);
        }
    }

    private void initializeController(Object controller) {
        if (controller instanceof IViewController viewController) {
            viewController.init(this, context);
        }
        if (controller instanceof RoleWorkspaceController roleWorkspaceController) {
            roleWorkspaceController.setUser(currentUser);
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
        if (logoutHandler != null) {
            logoutHandler.run();
        }
    }

    private record ResolvedNavigation(String viewId, String navTarget, LoadedView view) {
    }

    private record LoadedView(Node node, Object controller) {
    }
}
