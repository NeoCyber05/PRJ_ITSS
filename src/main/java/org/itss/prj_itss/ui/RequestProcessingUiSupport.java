package org.itss.prj_itss.ui;

import javafx.scene.Node;
import javafx.scene.Scene;

import java.util.List;

public final class RequestProcessingUiSupport {

    private static final String MAIN_STYLESHEET = "/org/itss/prj_itss/styles/main-style.css";

    private RequestProcessingUiSupport() {
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
        var stylesheet = RequestProcessingUiSupport.class.getResource(MAIN_STYLESHEET);
        if (stylesheet != null && !scene.getStylesheets().contains(stylesheet.toExternalForm())) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }
}
