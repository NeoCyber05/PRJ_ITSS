package org.itss.prj_itss.view.ordering.request.process.shared;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class AllocationViewSupport {

    static final String MAIN_STYLESHEET = "/org/itss/prj_itss/styles/main-style.css";

    private static final String TOAST_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/process/shared/toast-view.fxml";

    public static final List<String> FRACTION_STATE_CLASSES = List.of(
        "allocation-fraction-muted",
        "allocation-fraction-over",
        "allocation-fraction-complete",
        "allocation-fraction-partial"
    );

    public static final List<String> SUMMARY_STATE_CLASSES = List.of(
        "allocation-summary-short",
        "allocation-summary-over",
        "allocation-summary-complete"
    );

    public static final List<String> ETA_STATE_CLASSES = List.of(
        "allocation-eta-unavailable",
        "allocation-eta-early",
        "allocation-eta-on-time",
        "allocation-eta-late"
    );

    private AllocationViewSupport() {
    }

    public static void addStyleClass(Node node, String... styleClasses) {
        for (String styleClass : styleClasses) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        }
    }

    public static void setStateClass(Node node, List<String> stateClasses, String selectedClass) {
        node.getStyleClass().removeAll(stateClasses);
        if (!node.getStyleClass().contains(selectedClass)) {
            node.getStyleClass().add(selectedClass);
        }
    }

    public static void applyMainStylesheet(Scene scene) {
        var stylesheet = AllocationViewSupport.class.getResource(MAIN_STYLESHEET);
        if (stylesheet != null && !scene.getStylesheets().contains(stylesheet.toExternalForm())) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }

    public static void showToast(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                AllocationViewSupport.class.getResource(TOAST_RESOURCE),
                "Missing toast FXML"
            ));
            Parent root = loader.load();
            Label messageLabel = (Label) root.lookup("#messageLabel");
            if (messageLabel != null) {
                messageLabel.setText(message);
            }

            Stage toast = new Stage();
            toast.setAlwaysOnTop(true);
            toast.initModality(Modality.NONE);

            Scene scene = new Scene((StackPane) root);
            scene.setFill(null);
            toast.setScene(scene);
            toast.show();

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(2.5), event -> toast.close())
            );
            timeline.play();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load toast view", exception);
        }
    }
}
