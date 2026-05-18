package org.itss.prj_itss.request.presentation.ordering.detail;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.request.application.sales.RequestDetailApplicationService;
import org.itss.prj_itss.request.application.sales.RequestDetailViewModel;

import java.io.IOException;
import java.util.Objects;

public final class RequestDetailPopup {

    private static final String VIEW_RESOURCE = "/org/itss/prj_itss/ordering/request/detail/request-detail-popup.fxml";
    private static final String MAIN_STYLESHEET = "/org/itss/prj_itss/styles/main-style.css";

    private RequestDetailPopup() {
    }

    public static void show(Window owner, String requestCode, ApplicationContext context) {
        show(owner, requestCode, context, null);
    }

    public static void show(Window owner, String requestCode, ApplicationContext context, INavigator navigator) {
        RequestDetailApplicationService requestDetailApplicationService = context.requestDetailApplicationService();
        RequestDetailViewModel detail = requestDetailApplicationService.load(requestCode);

        Stage dialog = createDialog(owner);

        double sceneWidth = owner != null ? owner.getWidth() : 1440;
        double sceneHeight = owner != null ? owner.getHeight() : 900;
        double dialogMaxWidth = Math.min(sceneWidth - 64, 1660);
        double requestExpandedWidth = Math.min(980, dialogMaxWidth);
        double requestCollapsedWidth = Math.max(720, Math.min(780, dialogMaxWidth * 0.46));
        double orderPanelWidth = Math.max(740, Math.min(880, dialogMaxWidth - requestCollapsedWidth - 20));

        StackPane root = loadRoot(
                dialog,
                requestCode,
                context,
                detail,
                sceneWidth,
                requestCollapsedWidth,
                requestExpandedWidth,
                orderPanelWidth);

        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        scene.setFill(Color.TRANSPARENT);
        applyMainStylesheet(scene);
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
            ApplicationContext context,
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
            RequestDetailPopupController controller = loader.getController();
            controller.init(
                    dialog,
                    requestCode,
                    context,
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

    private static void applyMainStylesheet(Scene scene) {
        var stylesheet = RequestDetailPopup.class.getResource(MAIN_STYLESHEET);
        if (stylesheet != null && !scene.getStylesheets().contains(stylesheet.toExternalForm())) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }

}
