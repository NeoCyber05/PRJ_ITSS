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

public final class ViewLoader {

    private final MvcContext mvcContext;

    public ViewLoader() {
        this.mvcContext = new MvcContext();
    }

    public void warmUpDatabaseConnection() {
        mvcContext.warmUpDatabaseConnection();
    }

    public Parent loadLoginView(Consumer<AuthenticatedUser> loginHandler, LoginViewWarmingListener listener) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
            App.class.getResource("/org/itss/prj_itss/view/auth/login-view.fxml"),
            "Missing login view"
        ));
        Parent root = loader.load();
        LoginView view = loader.getController();

        LoginController loginController = new LoginController(mvcContext.authenticationService(), loginHandler);
        view.setController(loginController);

        if (listener != null) {
            listener.onLoginViewLoaded(view);
        }

        return root;
    }

    public Parent loadMainLayout(AuthenticatedUser user, Runnable logoutHandler) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
            App.class.getResource("/org/itss/prj_itss/view/layout/main-layout.fxml"),
            "Missing main layout FXML"
        ));
        Parent root = loader.load();
        MainLayoutView view = loader.getController();

        view.init(mvcContext.routeRegistry(), mvcContext::setAuthenticatedUser);
        mvcContext.navigator().setDelegate(view);
        view.setUser(user);
        view.setLogoutHandler(logoutHandler);

        return root;
    }

    public interface LoginViewWarmingListener {
        void onLoginViewLoaded(LoginView view);
    }
}
