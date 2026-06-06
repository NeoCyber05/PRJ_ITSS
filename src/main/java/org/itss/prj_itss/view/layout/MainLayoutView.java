package org.itss.prj_itss.view.layout;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.model.auth.application.RoleAccessPolicy;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.RoleType;
import org.itss.prj_itss.view.auth.RoleWorkspaceContent;
import org.itss.prj_itss.view.auth.RoleWorkspaceContentFactory;
import org.itss.prj_itss.view.shared.ViewLifecycle;
import org.itss.prj_itss.view.site.workspace.SiteWorkspaceView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class MainLayoutView implements Navigator {

    private static final double SIDEBAR_COLLAPSED_WIDTH = 56;
    private static final double SIDEBAR_EXPANDED_WIDTH  = 230;
    private static final int    SIDEBAR_ANIM_MS         = 200;

    private final Map<String, Button> navButtons = new LinkedHashMap<>();

    private MainLayoutRouteResolver routeResolver;
    private Consumer<AuthenticatedUser> authenticatedUserConsumer = user -> {};
    private AuthenticatedUser currentUser;
    private Runnable logoutHandler;
    private String activeViewId;
    private SiteWorkspaceView siteWorkspaceView;
    private int activeSiteTabIndex = 0;

    private Timeline expandTimeline;
    private Timeline collapseTimeline;

    // ── Sidebar ────────────────────────────────────────────────────────────
    @FXML private VBox shellSidebar;

    // ── Content area ───────────────────────────────────────────────────────
    @FXML private StackPane contentArea;

    // ── Brand ──────────────────────────────────────────────────────────────
    @FXML private Label brandSubtitleLabel;

    // ── Nav buttons (shared roles) ─────────────────────────────────────────
    @FXML private Button homeButton;
    @FXML private Button siteManagementButton;
    @FXML private Button receivedRequestsButton;
    @FXML private Button ordersButton;
    @FXML private Button salesRequestsButton;
    @FXML private Button merchandiseManagementButton;
    @FXML private Button accountManagementButton;
    @FXML private Button warehouseIncomingOrdersButton;

    // ── Nav containers ─────────────────────────────────────────────────────
    @FXML private VBox ordersNavContainer;
    @FXML private VBox salesNavContainer;
    @FXML private VBox adminNavContainer;
    @FXML private VBox warehouseNavContainer;

    // ── Site direct nav ────────────────────────────────────────────────────
    @FXML private VBox    siteDirectNavContainer;
    @FXML private Button  siteProfileDirectBtn;
    @FXML private Button  siteInventoryDirectBtn;
    @FXML private Button  siteOrderDirectBtn;

    // ── User card ──────────────────────────────────────────────────────────
    @FXML private Label userInitialsLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    @FXML
    private void initialize() {
        // Register standard nav buttons
        registerNavButton("home",                    homeButton);
        registerNavButton("site-management",         siteManagementButton);
        registerNavButton("received-requests",       receivedRequestsButton);
        registerNavButton("orders",                  ordersButton);
        registerNavButton("sales-requests",          salesRequestsButton);
        registerNavButton("merchandise-management",  merchandiseManagementButton);
        registerNavButton("account-management",      accountManagementButton);
        registerNavButton("warehouse-order-confirm-arrival", warehouseIncomingOrdersButton);

        // Site direct tab buttons
        siteProfileDirectBtn .setOnAction(e -> showSiteTab(0));
        siteInventoryDirectBtn.setOnAction(e -> showSiteTab(1));
        siteOrderDirectBtn   .setOnAction(e -> showSiteTab(2));

        // Sidebar hover-expand animation
        setupSidebarAnimation();
    }

    // ── Public API ─────────────────────────────────────────────────────────

    public void init(MainLayoutRouteResolver routeResolver,
                     Consumer<AuthenticatedUser> authenticatedUserConsumer) {
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
        siteWorkspaceView = null;
        updateUIForUser();
        showView(RoleAccessPolicy.defaultViewId(currentUser));
    }

    public void setLogoutHandler(Runnable logoutHandler) {
        this.logoutHandler = logoutHandler;
    }

    @Override
    public void showView(String viewId) {
        if (currentUser == null) return;

        ResolvedLayoutView resolvedView = resolveNavigation(viewId);
        if (resolvedView.viewId().equals(activeViewId)) return;

        contentArea.getChildren().setAll(resolvedView.node());
        if (resolvedView.viewInstance() instanceof ViewLifecycle viewLifecycle) {
            viewLifecycle.onViewShown();
        }
        if (resolvedView.viewInstance() instanceof SiteWorkspaceView swv) {
            siteWorkspaceView = swv;
        }
        setActiveNav(resolvedView.navTarget());
        activeViewId = resolvedView.viewId();
    }

    @Override
    public void showViewWithData(String viewId, Object data) {
        showView(viewId);
    }

    // ── Sidebar animation ──────────────────────────────────────────────────

    private void setupSidebarAnimation() {
        // Clip so child content doesn't bleed outside the sidebar width
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(shellSidebar.widthProperty());
        clip.heightProperty().bind(shellSidebar.heightProperty());
        shellSidebar.setClip(clip);

        // Start collapsed
        shellSidebar.setPrefWidth(SIDEBAR_COLLAPSED_WIDTH);

        shellSidebar.setOnMouseEntered(e -> animateSidebar(SIDEBAR_EXPANDED_WIDTH));
        shellSidebar.setOnMouseExited(e  -> animateSidebar(SIDEBAR_COLLAPSED_WIDTH));
    }

    private void animateSidebar(double targetWidth) {
        if (expandTimeline != null)  expandTimeline.stop();
        if (collapseTimeline != null) collapseTimeline.stop();
        Timeline tl = new Timeline(new KeyFrame(
            Duration.millis(SIDEBAR_ANIM_MS),
            new KeyValue(shellSidebar.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH)
        ));
        if (targetWidth > SIDEBAR_COLLAPSED_WIDTH) {
            expandTimeline  = tl;
        } else {
            collapseTimeline = tl;
        }
        tl.play();
    }

    // ── Site tab navigation ────────────────────────────────────────────────

    private void showSiteTab(int tabIndex) {
        activeSiteTabIndex = tabIndex;
        updateSiteDirectNavActive();
        if (siteWorkspaceView != null) {
            siteWorkspaceView.selectTab(tabIndex);
        } else {
            activeViewId = null; // force re-render if needed
            showView("site-workspace");
            if (siteWorkspaceView != null) {
                siteWorkspaceView.selectTab(tabIndex);
            }
        }
    }

    private void updateSiteDirectNavActive() {
        List<Button> btns = List.of(siteProfileDirectBtn, siteInventoryDirectBtn, siteOrderDirectBtn);
        for (int i = 0; i < btns.size(); i++) {
            if (i == activeSiteTabIndex) {
                if (!btns.get(i).getStyleClass().contains("shell-nav-button-active")) {
                    btns.get(i).getStyleClass().add("shell-nav-button-active");
                }
            } else {
                btns.get(i).getStyleClass().remove("shell-nav-button-active");
            }
        }
    }

    // ── Internal nav helpers ───────────────────────────────────────────────

    private void registerNavButton(String viewId, Button button) {
        button.setOnAction(event -> showView(viewId));
        navButtons.put(viewId, button);
    }

    private void setActiveNav(String navTarget) {
        for (Button button : navButtons.values()) {
            button.getStyleClass().remove("shell-nav-button-active");
        }
        // Also clear site direct buttons when leaving site workspace
        List.of(siteProfileDirectBtn, siteInventoryDirectBtn, siteOrderDirectBtn)
            .forEach(b -> b.getStyleClass().remove("shell-nav-button-active"));

        Button active = navButtons.get(navTarget);
        if (active != null) {
            active.getStyleClass().add("shell-nav-button-active");
        } else if ("site-workspace".equals(navTarget)) {
            // Highlight the active site direct button
            updateSiteDirectNavActive();
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
        boolean siteRole     = role.isSiteRole();
        boolean orderingRole = role.isOrderingRole();
        boolean salesRole    = role.isSalesRole();
        boolean adminRole    = role.isAdminRole();
        boolean warehouseRole= role.isWarehouseRole();

        // Home button: hidden for site role (site uses direct nav instead) and sales role
        homeButton.setVisible(!siteRole && !salesRole);
        homeButton.setManaged(!siteRole && !salesRole);

        ordersNavContainer.setVisible(orderingRole);
        ordersNavContainer.setManaged(orderingRole);

        salesNavContainer.setVisible(salesRole);
        salesNavContainer.setManaged(salesRole);

        adminNavContainer.setVisible(adminRole);
        adminNavContainer.setManaged(adminRole);

        warehouseNavContainer.setVisible(warehouseRole);
        warehouseNavContainer.setManaged(warehouseRole);

        // Site direct nav: shown only for site role, tab 0 active by default
        siteDirectNavContainer.setVisible(siteRole);
        siteDirectNavContainer.setManaged(siteRole);
        if (siteRole) {
            activeSiteTabIndex = 0;
            updateSiteDirectNavActive();
        }
    }

    @FXML
    private void handleLogout() {
        routeResolver.clearCache();
        activeViewId = null;
        siteWorkspaceView = null;
        authenticatedUserConsumer.accept(null);
        if (logoutHandler != null) {
            logoutHandler.run();
        }
    }
}
