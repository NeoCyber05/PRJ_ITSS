package org.itss.prj_itss.sales.request.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.itss.prj_itss.common.config.ApplicationContext;

import java.io.IOException;
import java.util.Objects;

public final class ViewOrderRequestPopup {

    private static final String VIEW_RESOURCE = "/org/itss/prj_itss/sales/request/view/view-order-request-view.fxml";
    private static final String MAIN_STYLESHEET = "/org/itss/prj_itss/styles/main-style.css";

    private ViewOrderRequestPopup() {
    }

    public static void show(Window owner, int requestId, ApplicationContext context) {
        Stage dialog = createDialog(owner);
        BorderPane root = loadRoot(dialog, requestId, context);
        Scene scene = createScene(root);
        applyMainStylesheet(scene);
        dialog.setScene(scene);
        centerOnOwner(dialog, owner, scene);
        dialog.showAndWait();
    }

    private static Stage createDialog(Window owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        return dialog;
    }

    private static BorderPane loadRoot(Stage dialog, int requestId, ApplicationContext context) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                ViewOrderRequestPopup.class.getResource(VIEW_RESOURCE),
                "Missing view order request FXML"
            ));
            BorderPane root = loader.load();
            ViewOrderRequestController controller = loader.getController();
            controller.init(dialog, requestId, context);
            root.setMaxWidth(1000);
            root.setStyle(
                "-fx-background-color: white;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 20, 0, 0, 0);" +
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;"
            );
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load view order request popup", exception);
        }
    }

    private static Scene createScene(BorderPane root) {
        Scene scene = new Scene(root, 1000, 700);
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    private static void applyMainStylesheet(Scene scene) {
        var stylesheet = ViewOrderRequestPopup.class.getResource(MAIN_STYLESHEET);
        if (stylesheet != null && !scene.getStylesheets().contains(stylesheet.toExternalForm())) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }

    private static void centerOnOwner(Stage dialog, Window owner, Scene scene) {
        if (owner != null) {
            dialog.setX(owner.getX() + (owner.getWidth() - scene.getWidth()) / 2);
            dialog.setY(owner.getY() + (owner.getHeight() - scene.getHeight()) / 2);
        }
    }
}
