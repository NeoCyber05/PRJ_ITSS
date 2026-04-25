package org.itss.prj_itss.request.processing.preview;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.service.RequestProcessingService;
import org.itss.prj_itss.ui.RequestProcessingUiSupport;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RequestProcessingPreviewDialog {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/request/processing/preview/request-processing-preview-dialog.fxml";

    private final INavigator navigator;
    private final RequestProcessingService requestProcessingService;
    private final int requestId;
    private final Map<Integer, Map<Integer, Allocation>> allocations;

    public RequestProcessingPreviewDialog(
        INavigator navigator,
        RequestProcessingService requestProcessingService,
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations
    ) {
        this.navigator = navigator;
        this.requestProcessingService = requestProcessingService;
        this.requestId = requestId;
        this.allocations = allocations;
    }

    public void show(Node ownerNode, List<RequestProcessingPreviewBuilder.PreviewOrder> previewOrders) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        Window ownerWindow = resolveOwnerWindow(ownerNode);
        if (ownerWindow != null) {
            dialog.initOwner(ownerWindow);
        }

        dialog.setTitle("Chi tiết phân bổ đơn hàng");
        dialog.setResizable(true);

        Parent root = loadRoot(dialog, previewOrders);
        Scene scene = new Scene(root);
        RequestProcessingUiSupport.applyMainStylesheet(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private Parent loadRoot(Stage dialog, List<RequestProcessingPreviewBuilder.PreviewOrder> previewOrders) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                RequestProcessingPreviewDialog.class.getResource(VIEW_RESOURCE),
                "Missing request processing preview FXML"
            ));
            Parent root = loader.load();
            RequestProcessingPreviewDialogController controller = loader.getController();
            controller.init(
                dialog,
                navigator,
                requestProcessingService,
                requestId,
                allocations,
                previewOrders
            );
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
