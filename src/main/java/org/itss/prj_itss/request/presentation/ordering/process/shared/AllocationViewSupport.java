package org.itss.prj_itss.request.presentation.ordering.process.shared;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.itss.prj_itss.request.business.allocation.AllocationControl.ItemAllocationState;

import java.util.List;

public final class AllocationViewSupport {

    static final String MAIN_STYLESHEET = "/org/itss/prj_itss/styles/main-style.css";

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

    public static final String ITEMS_CARD_STYLE =
        "-fx-background-color: white; -fx-background-radius: 12;"
            + "-fx-border-radius: 12; -fx-border-color: #E0EBE4; -fx-border-width: 1;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);";

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

    public static Label buildColumnHeader(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        addStyleClass(label, "allocation-column-header");
        return label;
    }

    public static void applyMainStylesheet(Scene scene) {
        var stylesheet = AllocationViewSupport.class.getResource(MAIN_STYLESHEET);
        if (stylesheet != null && !scene.getStylesheets().contains(stylesheet.toExternalForm())) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }

    public static void showToast(String message) {
        Stage toast = new Stage();
        toast.setAlwaysOnTop(true);
        toast.initModality(Modality.NONE);

        Label label = new Label(message);
        label.setStyle(
            "-fx-background-color: #253D2C; -fx-text-fill: white;" +
            "-fx-padding: 14 24; -fx-background-radius: 10;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;"
        );

        Scene scene = new Scene(new StackPane(label));
        scene.setFill(null);
        toast.setScene(scene);
        toast.show();

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(2.5), event -> toast.close())
        );
        timeline.play();
    }

    public static void applyItemAllocationState(Label label, ItemAllocationState state) {
        switch (state) {
            case OVER -> {
                label.setText("VÆ°á»£t má»©c");
                label.setStyle("-fx-background-color:#FEE2E2;-fx-text-fill:#B91C1C;-fx-background-radius:10;"
                    + "-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
            }
            case COMPLETE -> {
                label.setText("Äá»§");
                label.setStyle("-fx-background-color:#E8F5E9;-fx-text-fill:#2E7D32;-fx-background-radius:10;"
                    + "-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
            }
            case PARTIAL -> {
                label.setText("ChÆ°a Ä‘á»§");
                label.setStyle("-fx-background-color:#FFF3E0;-fx-text-fill:#E65100;-fx-background-radius:10;"
                    + "-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
            }
            case NONE -> {
                label.setText("ChÆ°a cÃ³ phÆ°Æ¡ng Ã¡n");
                label.setStyle("-fx-background-color:#F0F4F2;-fx-text-fill:#6B7C72;-fx-background-radius:10;"
                    + "-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
            }
        }
    }
}

