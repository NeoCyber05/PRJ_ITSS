package org.itss.prj_itss.view.shared.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;

public final class StatusBadgeFactory {

    public enum StatusKind {
        ORDER,
        REQUEST
    }

    public record BadgeColors(String background, String foreground, String accent) {
    }

    private StatusBadgeFactory() {
    }

    public static Label statusBadge(String status, StatusKind kind) {
        BadgeColors colors = colorsFor(status);
        Label badge = new Label("\u25cf " + statusText(status, kind));
        badge.setStyle(
            "-fx-background-color: " + colors.background() + ";" +
                "-fx-text-fill: " + colors.foreground() + ";" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 7 12;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );
        return badge;
    }

    public static Label statusBadge(String status, boolean requestStatus) {
        return statusBadge(status, requestStatus ? StatusKind.REQUEST : StatusKind.ORDER);
    }

    public static Label topStatusBadge(String status, StatusKind kind) {
        BadgeColors colors = colorsFor(status);
        Label badge = new Label(statusText(status, kind));
        badge.setStyle(
            "-fx-background-color: " + colors.background() + ";" +
                "-fx-text-fill: " + colors.foreground() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 10 16;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );
        return badge;
    }

    public static HBox statusDot(String status, StatusKind kind) {
        return statusDot(status, kind, 0);
    }

    public static HBox statusDot(String status, boolean requestStatus) {
        return statusDot(status, requestStatus ? StatusKind.REQUEST : StatusKind.ORDER);
    }

    public static HBox statusDot(String status, StatusKind kind, double minWidth) {
        HBox box = new HBox(7);
        box.setAlignment(Pos.CENTER_LEFT);
        if (minWidth > 0) {
            box.setMinWidth(minWidth);
        }

        BadgeColors colors = colorsFor(status);
        Circle dot = new Circle(5);
        dot.setFill(Color.web(colors.accent()));

        Label label = new Label(statusText(status, kind));
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + colors.foreground() + ";");
        box.getChildren().addAll(dot, label);
        return box;
    }

    public static Label transportBadge(String deliveryMethod) {
        boolean seaTransport = OrderingFormatters.isSeaTransport(deliveryMethod);
        String background = seaTransport ? "#E8F1FF" : "#FFF4E5";
        String foreground = seaTransport ? "#2563EB" : "#D97706";

        Label badge = new Label(OrderingFormatters.deliveryMethodText(deliveryMethod));
        badge.setStyle(
            "-fx-background-color: " + background + ";" +
                "-fx-text-fill: " + foreground + ";" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 5 10;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );
        return badge;
    }

    public static BadgeColors colorsFor(String status) {
        return switch (OrderingFormatters.normalizeStatusKey(status)) {
            case OrderingFormatters.STATUS_PENDING -> new BadgeColors("#FFF4E5", "#D97706", "#F59E0B");
            case OrderingFormatters.STATUS_PROCESSING -> new BadgeColors("#E8F1FF", "#2563EB", "#3B82F6");
            case OrderingFormatters.STATUS_SHIPPING -> new BadgeColors("#F2EAFF", "#7C3AED", "#A855F7");
            case OrderingFormatters.STATUS_COMPLETED -> new BadgeColors("#EAF8EF", "#15803D", "#22C55E");
            case OrderingFormatters.STATUS_CANCELLED -> new BadgeColors("#FEE2E2", "#B91C1C", "#EF4444");
            default -> new BadgeColors("#F3F4F6", "#6B7280", "#9CA3AF");
        };
    }

    public static String statusText(String status, StatusKind kind) {
        return kind == StatusKind.REQUEST
            ? OrderingFormatters.requestStatusText(status)
            : OrderingFormatters.orderStatusText(status);
    }
}
