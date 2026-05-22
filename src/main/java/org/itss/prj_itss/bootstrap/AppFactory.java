package org.itss.prj_itss.bootstrap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.itss.prj_itss.App;
import org.itss.prj_itss.controller.auth.LoginController;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.view.auth.LoginView;
import org.itss.prj_itss.view.layout.MainLayoutView;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public final class AppFactory {

    private final ModelContext modelContext;
    private final ControllerRegistry controllerRegistry;

    public AppFactory() {
        this.modelContext = new ModelContext();
        this.controllerRegistry = new ControllerRegistry(modelContext);
    }

    public ModelContext modelContext() {
        return modelContext;
    }

    public ControllerRegistry controllerRegistry() {
        return controllerRegistry;
    }

    public Parent loadLoginView(Consumer<AuthenticatedUser> loginHandler, LoginViewWarmingListener listener) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
            App.class.getResource("/org/itss/prj_itss/auth/login/login-view.fxml"),
            "Missing login view"
        ));
        Parent root = loader.load();
        LoginView view = loader.getController();

        // Tạo login controller động
        LoginController loginController = new LoginController(modelContext.authenticationService(), loginHandler);
        view.setController(loginController);

        if (listener != null) {
            listener.onLoginViewLoaded(view);
        }

        return root;
    }

    public Parent loadMainLayout(AuthenticatedUser user, Runnable logoutHandler) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
            App.class.getResource("/org/itss/prj_itss/layout/main-layout.fxml"),
            "Missing main layout FXML"
        ));
        Parent root = loader.load();
        MainLayoutView view = loader.getController();

        view.init(modelContext, controllerRegistry);
        controllerRegistry.navigator().setDelegate(view);
        view.setUser(user);
        view.setLogoutHandler(logoutHandler);

        return root;
    }

    public interface LoginViewWarmingListener {
        void onLoginViewLoaded(LoginView view);
    }
}
