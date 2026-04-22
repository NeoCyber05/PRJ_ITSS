package org.itss.prj_itss;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                getClass().getResource("/org/itss/prj_itss/layout/main-layout.fxml"),
                "Missing main layout FXML"
            ));
            root = loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load application layout", exception);
        }

        Scene scene = new Scene(root, 1280, 800);
        String css = Objects.requireNonNull(
            getClass().getResource("/org/itss/prj_itss/styles/main-style.css"),
            "Missing main stylesheet"
        ).toExternalForm();
        scene.getStylesheets().add(css);
        Image appIcon = new Image(Objects.requireNonNull(
            getClass().getResource("/org/itss/prj_itss/images/logo.png"),
            "Missing app icon"
        ).toExternalForm());

        stage.setTitle("Hệ thống đặt hàng nhập khẩu");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.getIcons().add(appIcon);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
