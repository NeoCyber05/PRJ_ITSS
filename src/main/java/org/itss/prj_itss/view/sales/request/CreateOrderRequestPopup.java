package org.itss.prj_itss.view.sales.request;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.itss.prj_itss.controller.sales.request.CreateOrderRequestController;

import java.io.IOException;
import java.util.Objects;

public final class CreateOrderRequestPopup {

    private static final String VIEW_RESOURCE = "/org/itss/prj_itss/sales/request/create/create-order-request-popup.fxml";
    private static final String MAIN_STYLESHEET = "/org/itss/prj_itss/styles/main-style.css";

    private CreateOrderRequestPopup() {
    }

    public static void show(Window owner, CreateOrderRequestController controller, Runnable onSave) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) {
            dialog.initOwner(owner);
        }

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                CreateOrderRequestPopup.class.getResource(VIEW_RESOURCE),
                "Missing create order request popup FXML"
            ));
            StackPane root = loader.load();
            CreateOrderRequestView view = loader.getController();
            view.init(dialog, controller);
            view.setOnSave(onSave);

            double sceneWidth = owner != null ? owner.getWidth() : 1440;
            double sceneHeight = owner != null ? owner.getHeight() : 900;

            Scene scene = new Scene(root, sceneWidth, sceneHeight);
            scene.setFill(Color.TRANSPARENT);
            
            var stylesheet = CreateOrderRequestPopup.class.getResource(MAIN_STYLESHEET);
            if (stylesheet != null && !scene.getStylesheets().contains(stylesheet.toExternalForm())) {
                scene.getStylesheets().add(stylesheet.toExternalForm());
            }
            
            dialog.setScene(scene);

            if (owner != null) {
                dialog.setX(owner.getX());
                dialog.setY(owner.getY());
            }

            dialog.showAndWait();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load create order request popup", exception);
        }
    }
}
