package org.itss.prj_itss.view.ordering.order.detail.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.model.order.application.OrderDetailViewModel;
import org.itss.prj_itss.model.order.application.OrderDetailViewModel.OrderItemRow;
import org.itss.prj_itss.view.shared.ui.StatusBadgeFactory;

import java.util.List;

public final class OrderDetailCardBuilder {

    private OrderDetailCardBuilder() {
    }

    public static VBox buildCard(String title) {
        VBox card = new VBox(18);
        card.setPadding(new Insets(22));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-border-radius: 18;" +
            "-fx-border-color: #E5ECF4;" +
            "-fx-border-width: 1;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        card.getChildren().add(titleLabel);
        return card;
    }

    public static VBox buildOverviewCard(OrderDetailViewModel vm) {
        VBox card = buildCard("Thông tin tổng quan");

        VBox grid = new VBox(24);
        grid.getChildren().addAll(
            buildOverviewRow(
                buildInfoCell("Mã đơn hàng", vm.orderCode()),
                buildInfoCell("Mã Site", vm.siteCode()),
                buildInfoCell("Tên Site", vm.siteName())
            ),
            buildOverviewRow(
                buildInfoCell("Ngày tạo", vm.createdAt()),
                buildBadgeInfoCell("Trạng thái", vm.status()),
                buildInfoCell("Tổng số mặt hàng", vm.itemCount() + " mặt hàng")
            )
        );

        card.getChildren().add(grid);
        return card;
    }

    public static VBox buildItemsCard(List<OrderItemRow> items) {
        VBox card = buildCard("Danh sách mặt hàng");

        double indexWidth = 35;
        double codeWidth = 80;
        double nameWidth = 160;
        double quantityWidth = 80;
        double unitWidth = 70;
        double transportWidth = 100;
        double desiredDateWidth = 105;
        double deliveryStatusWidth = 110;

        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-radius: 16;" +
            "-fx-border-color: #E7EDF5;" +
            "-fx-border-width: 1;"
        );

        HBox header = new HBox();
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #FBFCFE; -fx-background-radius: 16 16 0 0;");
        header.getChildren().addAll(
            headerCell("STT", indexWidth),
            headerCell("MÃ HÀNG", codeWidth),
            headerCell("TÊN MẶT HÀNG", nameWidth),
            headerCell("SỐ LƯỢNG ĐẶT", quantityWidth),
            headerCell("ĐƠN VỊ TÍNH", unitWidth),
            headerCell("PHƯƠNG THỨC VẬN CHUYỂN", transportWidth),
            headerCell("NGÀY CẦN", desiredDateWidth),
            headerCell("TRẠNG THÁI ETA", deliveryStatusWidth)
        );
        table.getChildren().add(header);

        if (items.isEmpty()) {
            Label emptyLabel = new Label("Không có mặt hàng.");
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-padding: 18 20;");
            table.getChildren().add(emptyLabel);
        } else {
            int index = 1;
            for (OrderItemRow item : items) {
                Label statusLabel = new Label(item.etaStatusText());
                if (item.etaStatusStyleClass() != null && !item.etaStatusStyleClass().isBlank()) {
                    statusLabel.getStyleClass().add(item.etaStatusStyleClass());
                }
                statusLabel.setWrapText(true);
                statusLabel.setMinWidth(deliveryStatusWidth);
                statusLabel.setPrefWidth(deliveryStatusWidth);

                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(16, 18, 16, 18));
                row.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");
                row.getChildren().addAll(
                    tableCell(String.valueOf(index++), indexWidth, false),
                    tableCell(item.merchandiseCode(), codeWidth, true),
                    tableCell(item.merchandiseName(), nameWidth, false),
                    tableCell(item.quantity(), quantityWidth, true),
                    tableCell(item.unit(), unitWidth, false),
                    OrderStatusRenderer.buildTransportCell(item.deliveryMethod(), transportWidth),
                    tableCell(item.desiredDateText(), desiredDateWidth, false),
                    statusLabel
                );
                table.getChildren().add(row);
            }
        }

        card.getChildren().add(table);
        return card;
    }

    private static HBox buildOverviewRow(VBox first, VBox second, VBox third) {
        HBox row = new HBox(18, first, second, third);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private static VBox buildInfoCell(String label, String value) {
        VBox cell = new VBox(10);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setPrefWidth(140);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7B8DA6;");

        Label valueNode = new Label(value);
        valueNode.setWrapText(true);
        valueNode.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        cell.getChildren().addAll(labelNode, valueNode);
        return cell;
    }

    private static VBox buildBadgeInfoCell(String label, String status) {
        VBox cell = new VBox(10);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setPrefWidth(140);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7B8DA6;");

        cell.getChildren().addAll(labelNode, StatusBadgeFactory.statusBadge(status, false));
        return cell;
    }

    private static Label headerCell(String text, double width) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7B8DA6;");
        return label;
    }

    private static Label tableCell(String text, double width, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #334155;" +
            (bold ? "-fx-font-weight: bold;" : "")
        );
        return label;
    }
}
