package org.itss.prj_itss.home;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import org.itss.prj_itss.layout.MainLayoutController;


public class HomeView {

    private final BorderPane view;
    private final MainLayoutController mainController;

    public HomeView(MainLayoutController mainController) {
        this.mainController = mainController;
        view = new BorderPane();
        view.getStyleClass().add("content-area");
        buildView();
    }

    private void buildView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(4, 4, 24, 4));

        HBox infoGrid = new HBox(20);
        VBox leftColumn = new VBox(20, buildPendingSection());
        VBox rightColumn = new VBox(20, buildActivitySection());
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        infoGrid.getChildren().addAll(leftColumn, rightColumn);

        content.getChildren().addAll(
            buildWelcomeSection(),
            buildQuickActionsSection(),
            infoGrid
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        view.setCenter(scrollPane);
    }

    private HBox buildWelcomeSection() {
        HBox hero = new HBox(24);
        hero.setPadding(new Insets(24, 24, 24, 24));
        hero.setStyle(
            "-fx-background-color: linear-gradient(to right, #0F172A, #134E4A 72%);" +
            "-fx-background-radius: 24;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.18), 22, 0, 0, 8);"
        );

        VBox left = new VBox(14);
        left.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label eyebrow = new Label("TRANG CHỦ");
        eyebrow.setStyle(
            "-fx-background-color: rgba(255,255,255,0.12);" +
            "-fx-text-fill: #CCFBF1;" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 5 12;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );

        Label title = new Label("Bộ phận đặt hàng quốc tế");
        title.setWrapText(true);
        title.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );

        Label subtitle = new Label(
            "Trang này là điểm vào nhanh để xem yêu cầu đã nhận, quản lý site và theo dõi đơn hàng đã tạo."
        );
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #D6F4F1;");

        HBox actionRow = new HBox(12);
        actionRow.getChildren().addAll(
            buildHeroButton("Yêu cầu đã nhận", true, () -> mainController.showView("received-requests")),
            buildHeroButton("Đơn hàng đã tạo", false, () -> mainController.showView("orders"))
        );

        left.getChildren().addAll(eyebrow, title, subtitle, actionRow);

        VBox right = new VBox(10);
        right.setPrefWidth(250);
        right.setPadding(new Insets(18));
        right.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(255,255,255,0.14);" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1;"
        );

        Label rightTitle = new Label("Tóm tắt");
        rightTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        right.getChildren().addAll(
            rightTitle,
            buildSummaryLine("03", "yêu cầu chờ xử lý"),
            buildSummaryLine("04", "site đang hoạt động"),
            buildSummaryLine("05", "đơn hàng đã tạo")
        );

        hero.getChildren().addAll(left, right);
        return hero;
    }

    private Button buildHeroButton(String text, boolean primary, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(e -> action.run());

        if (primary) {
            button.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #0F172A;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 10 18;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            );
        } else {
            button.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 999;" +
                "-fx-border-color: rgba(255,255,255,0.22);" +
                "-fx-border-radius: 999;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 10 18;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            );
        }

        return button;
    }

    private HBox buildSummaryLine(String value, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-background-color: rgba(255,255,255,0.12);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 6 10;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #D6F4F1;");

        row.getChildren().addAll(valueLabel, textLabel);
        return row;
    }

    private VBox buildQuickActionsSection() {
        VBox section = new VBox(14);

        Label title = new Label("Các màn chính");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        HBox cards = new HBox(16);
        cards.getChildren().addAll(
            buildQuickCard("YC", "Yêu cầu đã nhận", "Xem danh sách yêu cầu từ bộ phận bán hàng.", "#0F766E", "received-requests"),
            buildQuickCard("ST", "Quản lý site", "Cập nhật thông tin site và thời gian vận chuyển.", "#0EA5E9", "site-management"),
            buildQuickCard("ĐH", "Đơn hàng đã tạo", "Theo dõi đơn đang chờ xác nhận hoặc đang giao.", "#7C3AED", "orders")
        );

        section.getChildren().addAll(title, cards);
        return section;
    }

    private VBox buildQuickCard(String token, String title, String description, String accent, String viewId) {
        VBox card = new VBox(14);
        String normalStyle =
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #DCE5F1;" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 14, 0, 0, 4);";
        String hoverStyle =
            "-fx-background-color: #F8FBFE;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + accent + "55;" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1.2;" +
            "-fx-padding: 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.09), 18, 0, 0, 6);";
        card.setStyle(normalStyle);
        card.setPrefWidth(280);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(normalStyle));
        card.setOnMouseClicked(e -> mainController.showView(viewId));

        Label tokenLabel = new Label(token);
        tokenLabel.setAlignment(Pos.CENTER);
        tokenLabel.setStyle(
            "-fx-background-color: " + accent + "18;" +
            "-fx-text-fill: " + accent + ";" +
            "-fx-background-radius: 14;" +
            "-fx-min-width: 50;" +
            "-fx-min-height: 50;" +
            "-fx-pref-width: 50;" +
            "-fx-pref-height: 50;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label arrow = new Label("Mở màn hình");
        arrow.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + accent + ";");

        card.getChildren().addAll(tokenLabel, titleLabel, descriptionLabel, arrow);
        return card;
    }

    private VBox buildPendingSection() {
        VBox section = new VBox(0);
        section.setStyle(panelStyle());

        HBox header = buildSectionHeader("Công việc cần xử lý", "Mở danh sách", () -> mainController.showView("received-requests"));
        VBox rows = new VBox(0);
        rows.getChildren().addAll(
            buildPendingRow("YC-2026-001", "Nguyễn Văn A", "Hạn nhận: 15/04/2026", "2 mặt hàng"),
            buildPendingRow("YC-2026-006", "Nguyễn Văn A", "Hạn nhận: 28/04/2026", "3 mặt hàng"),
            buildPendingRow("YC-2026-007", "Lê Văn C", "Hạn nhận: 30/04/2026", "2 mặt hàng")
        );

        section.getChildren().addAll(header, rows);
        return section;
    }

    private VBox buildActivitySection() {
        VBox section = new VBox(0);
        section.setStyle(panelStyle());

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 16, 20));
        header.setStyle("-fx-border-color: transparent transparent #E6EDF5 transparent; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Cập nhật gần đây");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        header.getChildren().add(title);

        VBox items = new VBox(0);
        items.getChildren().addAll(
            buildActivityItem("ĐH", "Đơn hàng DH-2026-004 đã được Shenzhen Import Co. xác nhận.", "2 giờ trước", "#0EA5E9"),
            buildActivityItem("YC", "Yêu cầu YC-2026-003 đã hoàn thành sau khi giao đủ hàng.", "5 giờ trước", "#0F766E"),
            buildActivityItem("ST", "Site Singapore Trade Center vừa được thêm và đang chờ duyệt.", "Hôm nay 07:30", "#7C3AED")
        );

        section.getChildren().addAll(header, items);
        return section;
    }

    private HBox buildPendingRow(String requestId, String owner, String deadline, String itemCount) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label title = new Label(requestId + "  •  " + owner);
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label meta = new Label(deadline + "  •  " + itemCount);
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        textBox.getChildren().addAll(title, meta);

        Button button = new Button("Xem yêu cầu");
        button.setOnAction(e -> mainController.showView("received-requests"));
        button.setStyle(
            "-fx-background-color: #0F172A;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 8 14;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );

        row.getChildren().addAll(textBox, button);
        return row;
    }

    private HBox buildActivityItem(String token, String message, String time, String accent) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle("-fx-border-color: transparent transparent #EEF3F8 transparent; -fx-border-width: 0 0 1 0;");

        Label badge = new Label(token);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle(
            "-fx-background-color: " + accent + "18;" +
            "-fx-text-fill: " + accent + ";" +
            "-fx-background-radius: 12;" +
            "-fx-min-width: 40;" +
            "-fx-min-height: 40;" +
            "-fx-pref-width: 40;" +
            "-fx-pref-height: 40;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

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

    private HBox buildSectionHeader(String titleText, String actionText, Runnable action) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 16, 20));
        header.setStyle("-fx-border-color: transparent transparent #E6EDF5 transparent; -fx-border-width: 0 0 1 0;");

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionButton = new Button(actionText);
        actionButton.setOnAction(e -> action.run());
        actionButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #0F766E;" +
            "-fx-padding: 0;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );

        header.getChildren().addAll(title, spacer, actionButton);
        return header;
    }

    private String panelStyle() {
        return "-fx-background-color: white;" +
               "-fx-background-radius: 20;" +
               "-fx-border-color: #DCE5F1;" +
               "-fx-border-radius: 20;" +
               "-fx-border-width: 1;" +
               "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 14, 0, 0, 4);";
    }

    public Node getView() {
        return view;
    }
}
