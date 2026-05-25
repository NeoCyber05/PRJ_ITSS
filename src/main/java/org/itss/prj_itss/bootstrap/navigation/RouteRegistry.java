package org.itss.prj_itss.bootstrap.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.itss.prj_itss.App;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.view.layout.MainLayoutRouteResolver;
import org.itss.prj_itss.view.layout.ResolvedLayoutView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class RouteRegistry implements MainLayoutRouteResolver {

    private final List<Route> routes;
    private final Map<String, LoadedView> cachedViews = new HashMap<>();

    public RouteRegistry(List<Route> routes) {
        this.routes = List.copyOf(routes);
    }

    @Override
    public boolean canResolve(String viewId) {
        return findRoute(viewId) != null;
    }

    @Override
    public ResolvedLayoutView resolve(String viewId, Navigator navigator) {
        Route route = findRoute(viewId);
        if (route == null) {
            LoadedView errorView = buildErrorView("Khong the xac dinh man hinh: " + viewId);
            return new ResolvedLayoutView(viewId, viewId, errorView.node(), errorView.viewInstance());
        }

        String canonicalViewId = route.canonicalViewId(viewId);
        LoadedView loadedView = route.cacheable(viewId)
            ? cachedViews.computeIfAbsent(canonicalViewId, ignored -> loadRoute(route, viewId, navigator))
            : loadRoute(route, viewId, navigator);

        return new ResolvedLayoutView(
            canonicalViewId,
            route.navTarget(viewId),
            loadedView.node(),
            loadedView.viewInstance()
        );
    }

    @Override
    public void clearCache() {
        cachedViews.clear();
    }

    public static Route fxml(
        String viewId,
        String resourcePath,
        ViewConfigurer configurer
    ) {
        return fxml(viewId, viewId, resourcePath, configurer);
    }

    public static Route fxml(
        String viewId,
        String navTarget,
        String resourcePath,
        ViewConfigurer configurer
    ) {
        return route(
            viewId::equals,
            ignored -> viewId,
            ignored -> navTarget,
            true,
            (requestedViewId, navigator) -> loadFxml(resourcePath, requestedViewId, navigator, configurer)
        );
    }

    public static Route prefixedFxml(
        String prefix,
        String canonicalViewId,
        String navTarget,
        String resourcePath,
        ViewConfigurer configurer
    ) {
        return route(
            viewId -> viewId.startsWith(prefix),
            ignored -> canonicalViewId,
            ignored -> navTarget,
            true,
            (requestedViewId, navigator) -> loadFxml(resourcePath, requestedViewId, navigator, configurer)
        );
    }

    public static Route dynamic(
        Predicate<String> matcher,
        Function<String, String> canonicalViewId,
        Function<String, String> navTarget,
        boolean cacheable,
        RouteLoader loader
    ) {
        return route(matcher, canonicalViewId, navTarget, cacheable, loader);
    }

    private static Route route(
        Predicate<String> matcher,
        Function<String, String> canonicalViewId,
        Function<String, String> navTarget,
        boolean cacheable,
        RouteLoader loader
    ) {
        return new Route() {
            @Override
            public boolean matches(String viewId) {
                return matcher.test(viewId);
            }

            @Override
            public String canonicalViewId(String viewId) {
                return canonicalViewId.apply(viewId);
            }

            @Override
            public String navTarget(String viewId) {
                return navTarget.apply(viewId);
            }

            @Override
            public boolean cacheable(String viewId) {
                return cacheable;
            }

            @Override
            public LoadedView load(String viewId, Navigator navigator) throws Exception {
                return loader.load(viewId, navigator);
            }
        };
    }

    public static LoadedView loadFxml(
        String resourcePath,
        String viewId,
        Navigator navigator,
        ViewConfigurer configurer
    ) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
            App.class.getResource(resourcePath),
            "Missing view resource: " + resourcePath
        ));
        Node node = loader.load();
        Object viewInstance = loader.getController();
        if (configurer != null) {
            configurer.configure(viewId, viewInstance, navigator);
        }
        return new LoadedView(node, viewInstance);
    }

    private Route findRoute(String viewId) {
        if (viewId == null || viewId.isBlank()) {
            return null;
        }
        for (Route route : routes) {
            if (route.matches(viewId)) {
                return route;
            }
        }
        return null;
    }

    private LoadedView loadRoute(Route route, String viewId, Navigator navigator) {
        try {
            return route.load(viewId, navigator);
        } catch (Exception exception) {
            exception.printStackTrace();
            String message = exception.getMessage();
            if (message == null || message.isBlank()) {
                message = exception.getClass().getSimpleName();
            }
            return buildErrorView("Khong the tai man hinh: " + viewId + "\n" + message);
        }
    }

    private static LoadedView buildErrorView(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setWrapText(true);
        StackPane errorPane = new StackPane(errorLabel);
        errorPane.getStyleClass().add("content-area");
        return new LoadedView(errorPane, null);
    }

    @FunctionalInterface
    public interface ViewConfigurer {
        void configure(String viewId, Object viewInstance, Navigator navigator);
    }

    @FunctionalInterface
    public interface RouteLoader {
        LoadedView load(String viewId, Navigator navigator) throws Exception;
    }
}
