package org.itss.prj_itss.view.ordering.request.process.preview;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.itss.prj_itss.controller.ordering.request.process.preview.RequestProcessingPreviewDialogController;
import org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport;

import java.io.IOException;
import java.util.Objects;

public final class RequestProcessingPreviewDialog {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/process/preview/request-processing-preview-dialog.fxml";

    private final Runnable onOrdersRequested;
    private final RequestProcessingPreviewDialogController controller;

    public RequestProcessingPreviewDialog(
        Runnable onOrdersRequested,
        RequestProcessingPreviewDialogController controller
    ) {
        this.onOrdersRequested = onOrdersRequested == null ? () -> {} : onOrdersRequested;
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    public void show(Node ownerNode) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        Window ownerWindow = resolveOwnerWindow(ownerNode);
        if (ownerWindow != null) {
            dialog.initOwner(ownerWindow);
        }

        dialog.setTitle("Chi tiết phân bổ đơn hàng");
        dialog.setResizable(true);

        Parent root = loadRoot(dialog);
        Scene scene = new Scene(root);
        AllocationViewSupport.applyMainStylesheet(scene);
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
            view.init(dialog, onOrdersRequested, controller);
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
}
