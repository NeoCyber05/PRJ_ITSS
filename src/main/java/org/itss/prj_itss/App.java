package org.itss.prj_itss;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.itss.prj_itss.auth.login.LoginController;
import org.itss.prj_itss.auth.session.UserSession;
import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.layout.MainLayoutController;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    private final ApplicationContext context = ApplicationContext.getInstance();
    private Stage primaryStage;
    private String mainStylesheet;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.mainStylesheet = Objects.requireNonNull(
            getClass().getResource("/org/itss/prj_itss/styles/main-style.css"),
            "Missing main stylesheet"
        ).toExternalForm();
        Image appIcon = new Image(Objects.requireNonNull(
            getClass().getResource("/org/itss/prj_itss/images/logo.png"),
            "Missing app icon"
        ).toExternalForm());

        stage.setTitle("Hệ thống đặt hàng nhập khẩu");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.getIcons().add(appIcon);
        showLogin();
        stage.show();
    }

    private void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                getClass().getResource("/org/itss/prj_itss/auth/login/login-view.fxml"),
                "Missing login view"
            ));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.configure(context.authenticationService(), this::showMainLayout);
            setScene(root);
            warmUpDatabaseConnection(controller);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load login view", exception);
        }
    }

    private void warmUpDatabaseConnection(LoginController controller) {
        controller.beginStartupConnection();

        Task<Void> warmUpTask = new Task<>() {
            @Override
            protected Void call() {
                context.warmUpDatabaseConnection();
                return null;
            }
        };

        warmUpTask.setOnSucceeded(event -> controller.completeStartupConnection());
        warmUpTask.setOnFailed(event -> controller.failStartupConnection());

        Thread warmUpThread = new Thread(warmUpTask, "database-startup-connection");
        warmUpThread.setDaemon(true);
        warmUpThread.start();
    }

    private void showMainLayout(UserSession userSession) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                getClass().getResource("/org/itss/prj_itss/layout/main-layout.fxml"),
                "Missing main layout FXML"
            ));
            Parent root = loader.load();
            MainLayoutController controller = loader.getController();
            controller.setUserSession(userSession);
            controller.setLogoutHandler(this::showLogin);
            setScene(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load application layout", exception);
        }
    }

    private void setScene(Parent root) {
        double width = primaryStage.getScene() == null ? 1280 : primaryStage.getScene().getWidth();
        double height = primaryStage.getScene() == null ? 800 : primaryStage.getScene().getHeight();
        Scene scene = new Scene(root, Math.max(width, 1100), Math.max(height, 700));
        scene.getStylesheets().add(mainStylesheet);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
