package org.itss.prj_itss.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class Notifications {

    private Notifications() {
    }

    public static void showToast(String message) {
        Stage toast = new Stage();
        toast.setAlwaysOnTop(true);
        toast.initModality(Modality.NONE);

        Label label = new Label(message);
        label.setStyle(
            "-fx-background-color: #253D2C; -fx-text-fill: white;" +
            "-fx-padding: 14 24; -fx-background-radius: 10;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;"
        );

        Scene scene = new Scene(new StackPane(label));
        scene.setFill(null);
        toast.setScene(scene);
        toast.show();

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(2.5), event -> toast.close())
        );
        timeline.play();
    }

    public static void styleDialog(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-size: 13px;");
    }
}
