package org.itss.prj_itss.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public final class StatusNodes {

    private StatusNodes() {
    }

    public static HBox buildStatusDot(String status) {
        HBox box = new HBox(7);
        box.setAlignment(Pos.CENTER_LEFT);

        String[] colors = resolveStatusDotColors(status);
        Circle dot = new Circle(5);
        dot.setFill(Color.web(colors[0]));

        Label label = new Label(status);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + colors[1] + ";");

        box.getChildren().addAll(dot, label);
        return box;
    }

    public static Label buildStatusBadge(String status) {
        String[] colors = resolveStatusBadgeColors(status);

        Label badge = new Label("\u25cf " + status);
        badge.setStyle(
            "-fx-background-color: " + colors[0] + ";" +
            "-fx-text-fill: " + colors[1] + ";" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 7 12;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );
        return badge;
    }

    public static Label buildTransportBadge(String transport) {
        boolean isSea = "Duong bien".equals(transport)
            || "Đường biển".equals(transport)
            || "Tau".equals(transport)
            || "Tàu".equals(transport);
        String icon = isSea ? "\uD83D\uDEA2 " : "\u2708 ";
        String background = isSea ? "#E8F1FF" : "#FFF4E5";
        String foreground = isSea ? "#2563EB" : "#D97706";

        Label badge = new Label(icon + transport);
        badge.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + foreground + ";" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 7 10;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );
        return badge;
    }

    public static Label buildTransportBadgeCompact(String transport) {
        Label badge = buildTransportBadge(transport);
        badge.setStyle(badge.getStyle().replace("7 10", "5 10"));
        return badge;
    }

    public static HBox buildTransportCell(String type) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);

        boolean isSea = "Tau".equals(type)
            || "Tàu".equals(type)
            || "Duong bien".equals(type)
            || "Đường biển".equals(type);
        String icon = isSea ? "\uD83D\uDEA2" : "\u2708";
        String color = isSea ? "#1565C0" : "#E65100";

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");

        Label textLabel = new Label(type);
        textLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        box.getChildren().addAll(iconLabel, textLabel);
        return box;
    }

    private static String[] resolveStatusDotColors(String status) {
        return switch (status) {
            case "Chờ xử lý", "Chờ xác nhận" -> new String[]{"#F59E0B", "#B45309"};
            case "Đang xử lý" -> new String[]{"#3B82F6", "#1D4ED8"};
            case "Đang giao" -> new String[]{"#A855F7", "#7E22CE"};
            case "Đã hoàn thành", "Hoàn thành" -> new String[]{"#22C55E", "#15803D"};
            case "Đã hủy" -> new String[]{"#EF4444", "#B91C1C"};
            default -> new String[]{"#9CA3AF", "#6B7280"};
        };
    }

    private static String[] resolveStatusBadgeColors(String status) {
        return switch (status) {
            case "Chờ xử lý", "Chờ xác nhận" -> new String[]{"#FFF4E5", "#D97706"};
            case "Đang xử lý" -> new String[]{"#E8F1FF", "#2563EB"};
            case "Đang giao" -> new String[]{"#F2EAFF", "#7C3AED"};
            case "Đã hoàn thành", "Hoàn thành" -> new String[]{"#EAF8EF", "#15803D"};
            default -> new String[]{"#F3F4F6", "#6B7280"};
        };
    }
}
