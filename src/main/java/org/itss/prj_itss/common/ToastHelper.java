package org.itss.prj_itss.common;

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


public final class ToastHelper {

    private ToastHelper() { }


    public static void showToast(String message) {
        Stage toast = new Stage();
        toast.setAlwaysOnTop(true);
        toast.initModality(Modality.NONE);
        Label lbl = new Label(message);
        lbl.setStyle(
            "-fx-background-color: #253D2C; -fx-text-fill: white;" +
            "-fx-padding: 14 24; -fx-background-radius: 10;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;"
        );
        Scene s = new Scene(new StackPane(lbl));
        s.setFill(null);
        toast.setScene(s);
        toast.show();

        Timeline tl = new Timeline(
            new KeyFrame(Duration.seconds(2.5), ev -> toast.close())
        );
        tl.play();
    }

    public static void styleDialog(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.setStyle("-fx-background-color: white; -fx-font-size: 13px;");
    }
}
