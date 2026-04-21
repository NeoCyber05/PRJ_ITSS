package org.itss.prj_itss;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.itss.prj_itss.layout.MainLayoutController;


public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Create the main controller which builds the entire UI
        MainLayoutController mainLayout = new MainLayoutController();
        BorderPane root = mainLayout.getRoot();

        Scene scene = new Scene(root, 1280, 800);

        // Load the CSS stylesheet
        String css = getClass().getResource("/org/itss/prj_itss/styles/main-style.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Hệ thống đặt hàng nhập khẩu");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
