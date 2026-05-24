package org.itss.prj_itss.view.home;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.model.dashboard.application.DashboardData;
import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.controller.home.HomeController;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class HomeView implements ViewLifecycle {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private HomeController controller;

    @FXML
    private HBox quickCardsContainer;

    @FXML
    private VBox pendingRowsContainer;

    @FXML
    private VBox activityRowsContainer;

    public void setController(HomeController controller) {
        this.controller = controller;
    }

    @Override
    public void onViewShown() {
        reload();
    }

    @FXML
    private void openReceivedRequests() {
        if (controller != null) {
            controller.navigateTo("received-requests");
        }
    }

    @FXML
    private void openOrders() {
        if (controller != null) {
            controller.navigateTo("orders");
        }
    }

    private void reload() {
        if (controller == null) {
            return;
        }
        DashboardData dashboardData = controller.loadDashboardData();
        List<Request> requests = dashboardData.requests();
        List<Order> orders = dashboardData.orders();

        rebuildQuickCards();
        rebuildPendingRows(requests);
        rebuildActivityRows(requests, orders);
    }

    private void rebuildQuickCards() {
        quickCardsContainer.getChildren().setAll(
            buildQuickCard("YC", "Yêu cầu đã nhận", "Xem danh sách yêu cầu từ bộ phận bán hàng.", "#0F766E", "received-requests"),
            buildQuickCard("ST", "Quản lý site", "Cập nhật thông tin site và thời gian vận chuyển.", "#0EA5E9", "site-management"),
            buildQuickCard("ĐH", "Đơn hàng đã tạo", "Theo dõi đơn đang chờ xác nhận hoặc đang giao.", "#7C3AED", "orders")
        );
    }

    private void rebuildPendingRows(List<Request> requests) {
        pendingRowsContainer.getChildren().clear();

        requests.stream()
            .filter(request -> "pending".equalsIgnoreCase(request.getStatus()))
            .sorted(Comparator.comparing(this::resolveDeadline, Comparator.nullsLast(Comparator.naturalOrder())))
            .limit(3)
            .map(this::buildPendingRow)
            .forEach(pendingRowsContainer.getChildren()::add);

        if (pendingRowsContainer.getChildren().isEmpty()) {
            Label empty = new Label("Không còn yêu cầu nào đang chờ xử lý.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B; -fx-padding: 16 20;");
            pendingRowsContainer.getChildren().add(empty);
        }
    }

    private void rebuildActivityRows(List<Request> requests, List<Order> orders) {
        activityRowsContainer.getChildren().clear();

        orders.stream()
            .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
            .limit(2)
            .map(this::buildOrderActivityItem)
            .forEach(activityRowsContainer.getChildren()::add);

        requests.stream()
            .sorted(Comparator.comparing(Request::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
            .limit(1)
            .map(this::buildRequestActivityItem)
            .forEach(activityRowsContainer.getChildren()::add);

        if (activityRowsContainer.getChildren().isEmpty()) {
            Label empty = new Label("Chưa có cập nhật nào gần đây.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B; -fx-padding: 16 20;");
            activityRowsContainer.getChildren().add(empty);
        }
    }

    private VBox buildQuickCard(String token, String title, String description, String accent, String viewId) {
        VBox card = new VBox(14);
        String normalStyle =
            "-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #DCE5F1;" +
            "-fx-border-radius: 20; -fx-border-width: 1; -fx-padding: 20; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 14, 0, 0, 4);";
        String hoverStyle =
            "-fx-background-color: #F8FBFE; -fx-background-radius: 20; -fx-border-color: " + accent + "55;" +
            "-fx-border-radius: 20; -fx-border-width: 1.2; -fx-padding: 20; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.09), 18, 0, 0, 6);";
        card.setStyle(normalStyle);
        card.setPrefWidth(280);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setOnMouseEntered(event -> card.setStyle(hoverStyle));
        card.setOnMouseExited(event -> card.setStyle(normalStyle));
        card.setOnMouseClicked(event -> {
            if (controller != null) {
                controller.navigateTo(viewId);
            }
        });

        Label tokenLabel = new Label(token);
        tokenLabel.setAlignment(Pos.CENTER);
        tokenLabel.setStyle(
            "-fx-background-color: " + accent + "18; -fx-text-fill: " + accent + ";" +
            "-fx-background-radius: 14; -fx-min-width: 50; -fx-min-height: 50;" +
            "-fx-pref-width: 50; -fx-pref-height: 50; -fx-font-size: 16px; -fx-font-weight: bold;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label arrowLabel = new Label("Mở màn hình");
        arrowLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + accent + ";");

        card.getChildren().addAll(tokenLabel, titleLabel, descriptionLabel, arrowLabel);
        return card;
    }

    private HBox buildPendingRow(Request request) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label title = new Label(String.format("YC-2026-%03d", request.getId()));
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        String deadline = resolveDeadline(request) == null ? "N/A" : resolveDeadline(request).format(DATE_FORMAT);
        int itemCount = controller != null ? controller.countItemTypes(request.getId()) : 0;
        Label meta = new Label("Hạn nhận: " + deadline + "  •  " + itemCount + " mặt hàng");
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        textBox.getChildren().addAll(title, meta);

        Button actionButton = new Button("Xử lý");
        actionButton.setOnAction(event -> {
            if (controller != null) {
                controller.navigateTo("request-processing:" + request.getId());
            }
        });
        actionButton.setStyle("-fx-background-color: #0F172A; -fx-text-fill: white; -fx-background-radius: 999; -fx-padding: 8 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");

        row.getChildren().addAll(textBox, actionButton);
        return row;
    }

    private HBox buildOrderActivityItem(Order order) {
        String message = String.format("Đơn hàng DH-2026-%03d đã được tạo cho site #%d.", order.getId(), order.getSiteId());
        return buildActivityItem("ĐH", message, formatActivityTime(order.getCreatedAt()), "#0EA5E9");
    }

    private HBox buildRequestActivityItem(Request request) {
        String message = String.format("Yêu cầu YC-2026-%03d hiện ở trạng thái %s.", request.getId(), toRequestStatusText(request.getStatus()));
        return buildActivityItem("YC", message, formatActivityTime(request.getCreatedAt()), "#0F766E");
    }

    private String toRequestStatusText(String status) {
        if (status == null) {
            return "N/A";
        }
        return switch (status.trim().toLowerCase()) {
            case "pending" -> "Chờ xử lý";
            case "processing" -> "Đang xử lý";
            case "shipping" -> "Đang giao";
            case "completed" -> "Đã hoàn thành";
            case "cancelled" -> "Đã hủy";
            default -> status;
        };
    }

    private HBox buildActivityItem(String token, String message, String time, String accent) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");

        Label badge = new Label(token);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color: " + accent + "18; -fx-text-fill: " + accent + "; -fx-background-radius: 12; -fx-min-width: 40; -fx-min-height: 40; -fx-pref-width: 40; -fx-pref-height: 40; -fx-font-size: 12px; -fx-font-weight: bold;");

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1E293B;");

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        textBox.getChildren().addAll(messageLabel, timeLabel);
        row.getChildren().addAll(badge, textBox);
        return row;
    }

    private LocalDate resolveDeadline(Request request) {
        return controller != null ? controller.getEarliestDeliveryDate(request.getId()) : null;
    }

    private String formatActivityTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "Không có thời gian";
        }
        return createdAt.toLocalDate().format(DATE_FORMAT);
    }
}
