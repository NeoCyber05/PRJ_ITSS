package org.itss.prj_itss.view.sales.request.update;

import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

import java.math.BigDecimal;
import java.util.Locale;

final class SalesRequestEditViewSupport {

    private SalesRequestEditViewSupport() {
    }

    static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    static BigDecimal parseQuantity(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(rawValue);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    static void applyInputStyle(Node node, boolean invalid) {
        node.setStyle(invalid ? "-fx-border-color: #EF4444;" : "");
    }

    static SVGPath trashIcon() {
        SVGPath trashIcon = new SVGPath();
        trashIcon.setContent("M9 3v1H4v2h1v13a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V6h1V4h-5V3H9m2 2h2v1h-2V5m-4 3h2v10H7V8m4 0h2v10h-2V8m4 0h2v10h-2V8Z");
        trashIcon.setFill(javafx.scene.paint.Color.web("#EF4444"));
        trashIcon.setScaleX(0.8);
        trashIcon.setScaleY(0.8);
        return trashIcon;
    }
}
