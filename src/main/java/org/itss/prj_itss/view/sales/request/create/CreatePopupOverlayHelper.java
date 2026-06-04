package org.itss.prj_itss.view.sales.request.create;

import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;

final class CreatePopupOverlayHelper {

    private CreatePopupOverlayHelper() {
    }

    static void showWithOverlay(Window owner, Runnable showPopup) {
        if (owner == null || owner.getScene() == null) {
            showPopup.run();
            return;
        }

        javafx.scene.Node root = owner.getScene().getRoot();
        root.setEffect(new GaussianBlur(8));

        Rectangle dimOverlay = null;
        if (root instanceof javafx.scene.layout.Pane pane) {
            dimOverlay = new Rectangle();
            dimOverlay.setFill(Color.rgb(0, 0, 0, 0.35));
            dimOverlay.widthProperty().bind(pane.widthProperty());
            dimOverlay.heightProperty().bind(pane.heightProperty());
            dimOverlay.setMouseTransparent(true);
            pane.getChildren().add(dimOverlay);
        }

        Rectangle overlay = dimOverlay;
        try {
            showPopup.run();
        } finally {
            root.setEffect(null);
            if (overlay != null && root instanceof javafx.scene.layout.Pane pane) {
                pane.getChildren().remove(overlay);
            }
        }
    }
}
