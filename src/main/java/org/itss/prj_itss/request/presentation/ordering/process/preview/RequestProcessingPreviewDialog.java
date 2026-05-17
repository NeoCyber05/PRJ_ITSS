package org.itss.prj_itss.request.presentation.ordering.process.preview;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.itss.prj_itss.layout.INavigator;

import java.io.IOException;
import java.util.Objects;

public final class RequestProcessingPreviewDialog {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/request/presentation/ordering/process/preview/request-processing-preview-dialog.fxml";
    private static final String MAIN_STYLESHEET = "/org/itss/prj_itss/styles/main-style.css";

    private final INavigator navigator;
    private final RequestProcessingPreviewDialogController controller;

    public RequestProcessingPreviewDialog(
        INavigator navigator,
        RequestProcessingPreviewDialogController controller
    ) {
        this.navigator = navigator;
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    public void show(Node ownerNode) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        Window ownerWindow = resolveOwnerWindow(ownerNode);
        if (ownerWindow != null) {
            dialog.initOwner(ownerWindow);
        }

        dialog.setTitle("Chi tiáº¿t phÃ¢n bá»• Ä‘Æ¡n hÃ ng");
        dialog.setResizable(true);

        Parent root = loadRoot(dialog);
        Scene scene = new Scene(root);
        applyMainStylesheet(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private Parent loadRoot(Stage dialog) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                RequestProcessingPreviewDialog.class.getResource(VIEW_RESOURCE),
                "Missing request processing preview FXML"
            ));
            Parent root = loader.load();
            RequestProcessingPreviewDialogView view = loader.getController();
            view.init(dialog, navigator, controller);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load request processing preview dialog", exception);
        }
    }

    private Window resolveOwnerWindow(Node ownerNode) {
        if (ownerNode == null || ownerNode.getScene() == null) {
            return null;
        }
        return ownerNode.getScene().getWindow();
    }

    private void applyMainStylesheet(Scene scene) {
        var stylesheet = RequestProcessingPreviewDialog.class.getResource(MAIN_STYLESHEET);
        if (stylesheet != null && !scene.getStylesheets().contains(stylesheet.toExternalForm())) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }
}

