package org.itss.prj_itss;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.bootstrap.ViewLoader;
import org.itss.prj_itss.view.auth.LoginView;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    private final ViewLoader viewLoader = new ViewLoader();
    private Stage primaryStage;
    private String mainStylesheet;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.mainStylesheet = Objects.requireNonNull(
                getClass().getResource("/org/itss/prj_itss/styles/main-style.css"),
                "Missing main stylesheet").toExternalForm();
        Image appIcon = new Image(Objects.requireNonNull(
                getClass().getResource("/org/itss/prj_itss/images/logo.png"),
                "Missing app icon").toExternalForm());

        stage.setTitle("Hệ thống đặt hàng nhập khẩu");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.getIcons().add(appIcon);
        stage.setOnCloseRequest(event -> viewLoader.releaseLocksForCurrentUser());
        showLogin();
        stage.show();
    }

    private void showLogin() {
        try {
            Parent root = viewLoader.loadLoginView(
                this::showMainLayout,
                this::warmUpDatabaseConnection
            );
            setScene(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load login view", exception);
        }
    }

    private void warmUpDatabaseConnection(LoginView view) {
        view.beginStartupConnection();

        Task<Void> warmUpTask = new Task<>() {
            @Override
            protected Void call() {
                viewLoader.warmUpDatabaseConnection();
                return null;
            }
        };

        warmUpTask.setOnSucceeded(event -> view.completeStartupConnection());
        warmUpTask.setOnFailed(event -> view.failStartupConnection());

        Thread warmUpThread = new Thread(warmUpTask, "database-startup-connection");
        warmUpThread.setDaemon(true);
        warmUpThread.start();
    }

    private void showMainLayout(AuthenticatedUser user) {
        try {
            Parent root = viewLoader.loadMainLayout(user, () -> {
                viewLoader.releaseLocksForCurrentUser();
                showLogin();
            });
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
