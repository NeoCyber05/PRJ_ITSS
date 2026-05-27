package org.itss.prj_itss.view.sales.request.shared;

import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;

/**
 * Utility to apply a blur + dim overlay to the owner window's root
 * while a modal popup is shown, then restore it afterwards.
 */
public final class PopupOverlayHelper {

    private PopupOverlayHelper() {}

    /**
     * Applies blur + dark overlay to the owner scene root, runs {@code showPopup},
     * then restores the original state.
     */
    public static void showWithOverlay(Window owner, Runnable showPopup) {
        if (owner == null || owner.getScene() == null) {
            showPopup.run();
            return;
        }

        javafx.scene.Node root = owner.getScene().getRoot();

        // Apply blur to the background
        GaussianBlur blur = new GaussianBlur(8);
        root.setEffect(blur);

        // Add dim overlay on top of the root (only works if root is a StackPane or Pane)
        Rectangle dimOverlay = null;
        if (root instanceof javafx.scene.layout.Pane pane) {
            dimOverlay = new Rectangle();
            dimOverlay.setFill(Color.rgb(0, 0, 0, 0.35));
            dimOverlay.widthProperty().bind(pane.widthProperty());
            dimOverlay.heightProperty().bind(pane.heightProperty());
            dimOverlay.setMouseTransparent(true);
            pane.getChildren().add(dimOverlay);
        }

        final Rectangle overlay = dimOverlay;
        try {
            showPopup.run();
        } finally {
            // Restore background
            root.setEffect(null);
            if (overlay != null && root instanceof javafx.scene.layout.Pane pane) {
                pane.getChildren().remove(overlay);
            }
        }
    }
}
