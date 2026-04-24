package org.itss.prj_itss.layout;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import org.itss.prj_itss.App;
import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.order.OrderDetailView;
import org.itss.prj_itss.request.processing.RequestProcessingView;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MainLayoutController implements Navigator {

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, LoadedView> cachedViews = new HashMap<>();
    private final ApplicationContext context = ApplicationContext.getInstance();
    private String activeViewId;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button homeButton;

    @FXML
    private Button siteManagementButton;

    @FXML
    private Button receivedRequestsButton;

    @FXML
    private Button ordersButton;

    @FXML
    private void initialize() {
        registerNavButton("home", homeButton);
        registerNavButton("site-management", siteManagementButton);
        registerNavButton("received-requests", receivedRequestsButton);
        registerNavButton("orders", ordersButton);
        showView("home");
        warmSidebarCacheAsync();
    }

    @Override
    public void showView(String viewId) {
        if (viewId != null && viewId.equals(activeViewId)) {
            return;
        }

        LoadedView loadedView = resolveView(viewId);
        contentArea.getChildren().setAll(loadedView.node());
        if (loadedView.controller() instanceof ViewController viewController) {
            viewController.onViewShown(viewId);
        }
        setActiveNav(resolveNavTarget(viewId));
        activeViewId = viewId;
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

    private String resolveNavTarget(String viewId) {
        if (viewId.startsWith("order-detail:")) {
            return "orders";
        }
        if (viewId.startsWith("request-processing")) {
            return "received-requests";
        }
        return viewId;
    }

    private LoadedView resolveView(String viewId) {
        if (viewId.startsWith("order-detail:")) {
            String orderId = viewId.substring("order-detail:".length());
            return new LoadedView(new OrderDetailView(this, context, orderId).getView(), null);
        }

        if (viewId.startsWith("request-processing:")) {
            int requestId = parsePositiveInt(viewId.substring("request-processing:".length()), 1);
            return loadRequestProcessingView(requestId);
        }

        return switch (viewId) {
            case "home" -> getOrLoadCachedView("home", "/org/itss/prj_itss/home/home-view.fxml");
            case "site-management" -> getOrLoadCachedView("site-management", "/org/itss/prj_itss/site/site-management-view.fxml");
            case "received-requests" -> getOrLoadCachedView("received-requests", "/org/itss/prj_itss/request/received-requests-view.fxml");
            case "orders" -> getOrLoadCachedView("orders", "/org/itss/prj_itss/order/order-management-view.fxml");
            case "request-processing" -> loadRequestProcessingView(1);
            default -> getOrLoadCachedView("home", "/org/itss/prj_itss/home/home-view.fxml");
        };
    }

    private LoadedView loadRequestProcessingView(int requestId) {
        return new LoadedView(new RequestProcessingView(this, context, requestId).getView(), null);
    }

    private LoadedView getOrLoadCachedView(String cacheKey, String resourcePath) {
        return cachedViews.computeIfAbsent(cacheKey, key -> loadView(resourcePath, null));
    }

    private LoadedView loadView(String resourcePath, Consumer<Object> controllerConfigurer) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(resourcePath));
            Node view = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ViewController viewController) {
                viewController.init(this, context);
            }
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
            Label errorLabel = new Label("Khong the tai man hinh: " + resourcePath + "\n" + message);
            errorLabel.setWrapText(true);
            StackPane errorPane = new StackPane(errorLabel);
            errorPane.getStyleClass().add("content-area");
            return new LoadedView(errorPane, null);
        }
    }

    private int parsePositiveInt(String rawValue, int fallback) {
        try {
            int parsed = Integer.parseInt(rawValue.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private void warmSidebarCacheAsync() {
        preloadCachedViewsSequentially(List.of("site-management", "received-requests", "orders"), 0);
    }

    private void preloadCachedViewsSequentially(List<String> viewIds, int index) {
        if (index >= viewIds.size()) {
            return;
        }

        Platform.runLater(() -> {
            resolveView(viewIds.get(index));
            preloadCachedViewsSequentially(viewIds, index + 1);
        });
    }

    private record LoadedView(Node node, Object controller) {
    }
}
