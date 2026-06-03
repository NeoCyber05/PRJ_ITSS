package org.itss.prj_itss.view.ordering.request;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderDetailController;
import org.itss.prj_itss.controller.ordering.order.OrderManagementController;
import org.itss.prj_itss.controller.ordering.request.RequestDetailPopupController;
import org.itss.prj_itss.model.request.application.sales.detail.RequestDetailViewModel;
import org.itss.prj_itss.view.ordering.request.detail.RequestDetailPopupView;
import org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport;

import java.io.IOException;
import java.util.Objects;

public final class RequestDetailPopup {

    private static final String VIEW_RESOURCE = "/org/itss/prj_itss/view/ordering/request/detail/request-detail-popup.fxml";

    private RequestDetailPopup() {
    }

    public static void show(
            Window owner,
            String requestCode,
            RequestDetailPopupController controller,
            OrderDetailController orderDetailController,
            OrderManagementController orderManagementController,
            Navigator navigator) {
        RequestDetailViewModel detail = controller.load(requestCode);

        Stage dialog = createDialog(owner);

        double sceneWidth = owner != null ? owner.getWidth() : 1440;
        double sceneHeight = owner != null ? owner.getHeight() : 900;
        double dialogMaxWidth = Math.min(sceneWidth - 64, 1660);
        double requestExpandedWidth = Math.min(1024, dialogMaxWidth);
        double requestCollapsedWidth = Math.max(720, Math.min(780, dialogMaxWidth * 0.46));
        double orderPanelWidth = Math.max(740, Math.min(880, dialogMaxWidth - requestCollapsedWidth - 20));

        StackPane root = loadRoot(
                dialog,
                requestCode,
                controller,
                orderDetailController,
                orderManagementController,
                navigator,
                detail,
                sceneWidth,
                requestCollapsedWidth,
                requestExpandedWidth,
                orderPanelWidth);

        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        scene.setFill(Color.TRANSPARENT);
        AllocationViewSupport.applyMainStylesheet(scene);
        dialog.setScene(scene);

        if (owner != null) {
            dialog.setX(owner.getX());
            dialog.setY(owner.getY());
        }

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

    private static StackPane loadRoot(
            Stage dialog,
            String requestCode,
            RequestDetailPopupController controller,
            OrderDetailController orderDetailController,
            OrderManagementController orderManagementController,
            Navigator navigator,
            RequestDetailViewModel detail,
            double sceneWidth,
            double requestCollapsedWidth,
            double requestExpandedWidth,
            double orderPanelWidth) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    RequestDetailPopup.class.getResource(VIEW_RESOURCE),
                    "Missing request detail popup FXML"));
            StackPane root = loader.load();
            RequestDetailPopupView view = loader.getController();
            view.init(
                    dialog,
                    requestCode,
                    controller,
                    orderDetailController,
                    orderManagementController,
                    navigator,
                    detail,
                    sceneWidth,
                    requestCollapsedWidth,
                    requestExpandedWidth,
                    orderPanelWidth);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load request detail popup", exception);
        }
    }
}
