package org.itss.prj_itss.view.layout;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.model.auth.application.RoleAccessPolicy;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.RoleType;
import org.itss.prj_itss.view.auth.RoleWorkspaceContent;
import org.itss.prj_itss.view.auth.RoleWorkspaceContentFactory;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class MainLayoutView implements Navigator {

    private final Map<String, Button> navButtons = new LinkedHashMap<>();

    private MainLayoutRouteResolver routeResolver;
    private Consumer<AuthenticatedUser> authenticatedUserConsumer = user -> {};
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
    private VBox adminNavContainer;

    @FXML
    private Button accountManagementButton;

    @FXML
    private VBox siteNavContainer;

    @FXML
    private Button siteWorkspaceButton;

    @FXML
    private void initialize() {
        registerNavButton("home", homeButton);
        registerNavButton("site-management", siteManagementButton);
        registerNavButton("received-requests", receivedRequestsButton);
        registerNavButton("orders", ordersButton);
        registerNavButton("sales-requests", salesRequestsButton);
        registerNavButton("account-management", accountManagementButton);
        registerNavButton("site-workspace", siteWorkspaceButton);
    }

    public void init(
        MainLayoutRouteResolver routeResolver,
        Consumer<AuthenticatedUser> authenticatedUserConsumer
    ) {
        this.routeResolver = Objects.requireNonNull(routeResolver, "routeResolver");
        this.authenticatedUserConsumer = authenticatedUserConsumer == null
            ? user -> {}
            : authenticatedUserConsumer;
    }

    public void setUser(AuthenticatedUser user) {
        this.currentUser = user;
        authenticatedUserConsumer.accept(user);
        routeResolver.clearCache();
        activeViewId = null;
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

        ResolvedLayoutView resolvedView = resolveNavigation(viewId);
        if (resolvedView.viewId().equals(activeViewId)) {
            return;
        }

        contentArea.getChildren().setAll(resolvedView.node());
        if (resolvedView.viewInstance() instanceof ViewLifecycle viewLifecycle) {
            viewLifecycle.onViewShown();
        }
        setActiveNav(resolvedView.navTarget());
        activeViewId = resolvedView.viewId();
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

    private ResolvedLayoutView resolveNavigation(String requestedViewId) {
        String targetViewId = resolveAccessibleViewId(requestedViewId);
        if (!routeResolver.canResolve(targetViewId)) {
            String defaultViewId = RoleAccessPolicy.defaultViewId(currentUser);
            if (!Objects.equals(defaultViewId, targetViewId) && routeResolver.canResolve(defaultViewId)) {
                targetViewId = defaultViewId;
            }
        }
        return routeResolver.resolve(targetViewId, this);
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

        boolean adminRole = role.isAdminRole();
        adminNavContainer.setVisible(adminRole);
        adminNavContainer.setManaged(adminRole);

        boolean siteRole = role.isSiteRole();
        siteNavContainer.setVisible(siteRole);
        siteNavContainer.setManaged(siteRole);
    }

    @FXML
    private void handleLogout() {
        routeResolver.clearCache();
        activeViewId = null;
        authenticatedUserConsumer.accept(null);
        if (logoutHandler != null) {
            logoutHandler.run();
        }
    }
}
