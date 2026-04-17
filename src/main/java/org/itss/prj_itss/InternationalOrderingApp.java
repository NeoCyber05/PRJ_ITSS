package org.itss.prj_itss;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class InternationalOrderingApp extends Application {

    @Override
    public void start(Stage stage) {
        // Create the main controller which builds the entire UI
        MainLayoutController mainLayout = new MainLayoutController();
        BorderPane root = mainLayout.getRoot();

        Scene scene = new Scene(root, 1280, 800);

        // Load the CSS stylesheet
        String css = getClass().getResource("styles/main-style.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Hệ thống đặt hàng nhập khẩu - Bộ phận đặt hàng quốc tế");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
