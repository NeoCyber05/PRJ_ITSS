package org.itss.prj_itss.view.sales.request.update;

import javafx.scene.control.Label;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;

final class SalesRequestEditStatusBadgeFactory {

    private SalesRequestEditStatusBadgeFactory() {
    }

    static Label create(String status) {
        String normalized = OrderingFormatters.normalizeStatusKey(status);
        String[] colors = switch (normalized) {
            case OrderingFormatters.STATUS_PENDING -> new String[]{"#FFF4E5", "#D97706"};
            case OrderingFormatters.STATUS_PROCESSING -> new String[]{"#E8F1FF", "#2563EB"};
            case OrderingFormatters.STATUS_SHIPPING -> new String[]{"#F2EAFF", "#7C3AED"};
            case OrderingFormatters.STATUS_COMPLETED -> new String[]{"#EAF8EF", "#15803D"};
            case OrderingFormatters.STATUS_CANCELLED -> new String[]{"#FEE2E2", "#B91C1C"};
            default -> new String[]{"#F3F4F6", "#6B7280"};
        };

        Label badge = new Label("● " + OrderingFormatters.requestStatusText(status));
        badge.setStyle("-fx-background-color:" + colors[0] + "; -fx-text-fill:" + colors[1]
            + "; -fx-background-radius:999; -fx-padding:4 12; -fx-font-size:12px; -fx-font-weight:bold;");
        return badge;
    }
}
