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

    private final AppContainer appContainer;

    public AppFactory() {
        this.appContainer = new AppContainer();
    }

    public AppContainer appContainer() {
        return appContainer;
    }

    public Parent loadLoginView(Consumer<AuthenticatedUser> loginHandler, LoginViewWarmingListener listener) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
            App.class.getResource("/org/itss/prj_itss/auth/login/login-view.fxml"),
            "Missing login view"
        ));
        Parent root = loader.load();
        LoginView view = loader.getController();

        LoginController loginController = new LoginController(appContainer.authenticationService(), loginHandler);
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

        view.init(appContainer);
        appContainer.navigator().setDelegate(view);
        view.setUser(user);
        view.setLogoutHandler(logoutHandler);

        return root;
    }

    public interface LoginViewWarmingListener {
        void onLoginViewLoaded(LoginView view);
    }
}
