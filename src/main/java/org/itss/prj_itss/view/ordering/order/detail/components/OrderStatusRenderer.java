package org.itss.prj_itss.view.ordering.order.detail.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.itss.prj_itss.view.shared.ui.StatusBadgeFactory;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;

public final class OrderStatusRenderer {

    private OrderStatusRenderer() {
    }

    public static VBox buildProgressCard(String status) {
        VBox card = OrderDetailCardBuilder.buildCard("Tiến trình đơn hàng");

        String normalizedStatus = OrderingFormatters.normalizeStatusKey(status);
        boolean delivered = OrderingFormatters.STATUS_COMPLETED.equals(normalizedStatus);
        boolean shipping = delivered || OrderingFormatters.STATUS_SHIPPING.equals(normalizedStatus);
        boolean confirmed = shipping || OrderingFormatters.STATUS_PENDING.equals(normalizedStatus);

        HBox progress = new HBox(0);
        progress.setAlignment(Pos.CENTER);

        progress.getChildren().add(buildProgressStep("1", "Chờ xác nhận", confirmed ? "#F59E0B" : "#CBD5E1", confirmed));
        progress.getChildren().add(buildProgressLine(shipping ? "#60A5FA" : "#D6DFEA"));
        progress.getChildren().add(buildProgressStep("2", "Đang giao", shipping ? "#3B82F6" : "#CBD5E1", shipping));
        progress.getChildren().add(buildProgressLine(delivered ? "#22C55E" : "#D6DFEA"));
        progress.getChildren().add(buildProgressStep("3", "Hoàn thành", delivered ? "#22C55E" : "#CBD5E1", delivered));

        card.getChildren().add(progress);
        return card;
    }

    private static VBox buildProgressStep(String iconText, String label, String color, boolean active) {
        VBox step = new VBox(10);
        step.setAlignment(Pos.CENTER);

        Circle circle = new Circle(18);
        circle.setStyle("-fx-fill: " + (active ? color : "#FFFFFF") + "; -fx-stroke: " + color + "; -fx-stroke-width: 3;");

        Label iconLabel = new Label(iconText);
        iconLabel.setStyle("-fx-text-fill: " + (active ? "white" : color) + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        StackPane iconWrapper = new StackPane(circle, iconLabel);
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        step.getChildren().addAll(iconWrapper, labelNode);
        return step;
    }

    private static Region buildProgressLine(String color) {
        Region line = new Region();
        double lineWidth = 74;
        line.setPrefWidth(lineWidth);
        line.setMinWidth(lineWidth);
        line.setMaxWidth(lineWidth);
        line.setMinHeight(4);
        line.setPrefHeight(4);
        line.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 999;");
        VBox wrapper = new VBox(line);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    public static Label buildTopStatusBadge(String status) {
        return StatusBadgeFactory.topStatusBadge(status, StatusBadgeFactory.StatusKind.ORDER);
    }

    public static HBox buildTransportCell(String deliveryMethod, double width) {
        HBox box = new HBox(StatusBadgeFactory.transportBadge(deliveryMethod));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(width);
        box.setPrefWidth(width);
        return box;
    }
}
