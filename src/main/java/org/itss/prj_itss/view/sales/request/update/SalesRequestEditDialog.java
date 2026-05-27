package org.itss.prj_itss.view.sales.request.update;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.itss.prj_itss.controller.sales.request.shared.SalesRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditController;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditDialogInput;
import org.itss.prj_itss.view.sales.request.shared.SalesRequestEditDialogLauncher;
import org.itss.prj_itss.view.sales.request.shared.PopupOverlayHelper;

import java.io.IOException;
import java.util.Objects;

public final class SalesRequestEditDialog implements SalesRequestEditDialogLauncher {

    private static final String VIEW_RESOURCE = "/org/itss/prj_itss/view/sales/request/update/sales-request-edit-view.fxml";
    private static final String MAIN_STYLESHEET = "/org/itss/prj_itss/styles/main-style.css";

    private final SalesRequestEditController controller;

    public SalesRequestEditDialog(SalesRequestEditController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    public void showEdit(
            Window owner,
            SalesRequestEditDialogInput input,
            SalesRequestDialogListener listener
    ) {
        Stage dialog = createDialog(owner);
        BorderPane root = loadRoot(dialog, input, listener);
        Scene scene = createScene(root);
        applyMainStylesheet(scene);
        dialog.setScene(scene);
        centerOnOwner(dialog, owner, scene);
        PopupOverlayHelper.showWithOverlay(owner, dialog::showAndWait);
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

    private BorderPane loadRoot(
            Stage dialog,
            SalesRequestEditDialogInput input,
            SalesRequestDialogListener listener
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                SalesRequestEditDialog.class.getResource(VIEW_RESOURCE),
                "Missing update order request FXML"
            ));
            BorderPane root = loader.load();
            SalesRequestEditView view = loader.getController();
            view.setCloseHandler(dialog::close);
            controller.start(view, input, listener);
            root.setMaxWidth(1000);
            root.setStyle(
                "-fx-background-color: white;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 20, 0, 0, 0);" +
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;"
            );
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load update order request popup", exception);
        }
    }

    private static Scene createScene(BorderPane root) {
        Scene scene = new Scene(root, 1000, 700);
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    private static void applyMainStylesheet(Scene scene) {
        var stylesheet = SalesRequestEditDialog.class.getResource(MAIN_STYLESHEET);
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
