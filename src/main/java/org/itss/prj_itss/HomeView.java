package org.itss.prj_itss;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;


public class HomeView {

    private final BorderPane view;
    private final MainLayoutController mainController;

    public HomeView(MainLayoutController mainController) {
        this.mainController = mainController;
        view = new BorderPane();
        view.setStyle("-fx-background-color: #F5F9F6;");
        buildView();
    }

    private void buildView() {
        // === SCROLLABLE CONTENT ===
        VBox content = new VBox(28);
        content.setPadding(new Insets(32, 36, 32, 36));
        content.setStyle("-fx-background-color: #F5F9F6;");

        // 1. Welcome banner
        VBox welcomeBanner = buildWelcomeBanner();

        // 2. Quick actions
        VBox quickActionsSection = buildQuickActionsSection();

        // 3. Pending work table
        VBox pendingSection = buildPendingSection();

        // 4. Recent activity
        VBox activitySection = buildRecentActivitySection();

        content.getChildren().addAll(
            welcomeBanner,
            quickActionsSection,
            pendingSection,
            activitySection
        );

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F5F9F6; -fx-background: #F5F9F6;");
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        view.setCenter(scrollPane);
    }

    // =========================================================================
    // TOP BAR
    // =========================================================================
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 36, 16, 36));
        bar.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: transparent transparent #E0EBE4 transparent;" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 2);"
        );

        // App logo / breadcrumb
        Label logo = new Label("🌐  Import Order System");
        logo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2E6F40;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new java.util.Locale("vi", "VN"));
        Label dateLabel = new Label(today.format(fmt));
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7C72;");

        bar.getChildren().addAll(logo, spacer, dateLabel);
        return bar;
    }

    // =========================================================================
    // WELCOME BANNER
    // =========================================================================
    private VBox buildWelcomeBanner() {
        VBox banner = new VBox(6);
        banner.setPadding(new Insets(28, 32, 28, 32));
        banner.setStyle(
            "-fx-background-color: linear-gradient(to right, #253D2C, #2E6F40);" +
            "-fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(37,61,44,0.30), 10, 0, 0, 4);"
        );

        Label greeting = new Label("Chào buổi sáng, Nguyễn Văn A 👋");
        greeting.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sub = new Label("Bộ phận đặt hàng quốc tế  •  Hệ thống đặt hàng nhập khẩu");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #A8D8B4;");

        // Status chips
        HBox chips = new HBox(10);
        chips.setPadding(new Insets(10, 0, 0, 0));
        chips.getChildren().addAll(
            buildChip("3 yêu cầu chờ xử lý", "#68BA7F", "#1C3A25"),
            buildChip("5 đơn hàng chờ xác nhận", "#A8D8B4", "#1C3A25"),
            buildChip("1 site mới chờ duyệt", "#CFFFDC", "#1C3A25")
        );

        banner.getChildren().addAll(greeting, sub, chips);
        return banner;
    }

    private Label buildChip(String text, String bg, String fg) {
        Label chip = new Label(text);
        chip.setStyle(
            "-fx-background-color: " + bg + "33;" +
            "-fx-text-fill: " + bg + ";" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 5 14;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: " + bg + "55;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );
        return chip;
    }

    // =========================================================================
    // QUICK ACTIONS
    // =========================================================================
    private VBox buildQuickActionsSection() {
        VBox section = new VBox(14);

        Label sectionTitle = new Label("Truy cập nhanh");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        HBox cards = new HBox(16);

        cards.getChildren().addAll(
            buildQuickCard(
                "📥", "Xử lý yêu cầu",
                "3 yêu cầu đang chờ phân bổ đơn hàng",
                "#2E6F40", "received-requests"
            ),
            buildQuickCard(
                "📦", "Đơn hàng",
                "5 đơn hàng chờ xác nhận từ site",
                "#1565C0", "orders"
            ),
            buildQuickCard(
                "📋", "Quản lý Site",
                "4 site đối tác đang hoạt động",
                "#6A1B9A", "site-management"
            )
        );

        section.getChildren().addAll(sectionTitle, cards);
        return section;
    }

    private VBox buildQuickCard(String icon, String title, String desc, String color, String viewId) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #E0EBE4;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);"
        );
        HBox.setHgrow(card, Priority.ALWAYS);

        String normalStyle = "-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E0EBE4; -fx-border-width: 1; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);";
        String hoverStyle  = "-fx-background-color: #F7FDF9; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #68BA7F; -fx-border-width: 1.5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(46,111,64,0.12), 10, 0, 0, 4);";
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e  -> card.setStyle(normalStyle));
        card.setOnMouseClicked(e -> mainController.showView(viewId));

        // Icon badge
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-background-color: " + color + "18;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 8 12;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");
        descLabel.setWrapText(true);

        // Arrow indicator
        Label arrow = new Label("→");
        arrow.setStyle("-fx-font-size: 16px; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(iconLabel, titleLabel, descLabel, arrow);
        return card;
    }

    // =========================================================================
    // PENDING WORK TABLE
    // =========================================================================
    private VBox buildPendingSection() {
        VBox section = new VBox(0);
        section.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #E0EBE4;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        // Section header
        HBox sectionHeader = new HBox();
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.setPadding(new Insets(18, 22, 18, 22));
        sectionHeader.setStyle(
            "-fx-border-color: transparent transparent #EEF3EF transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        Label title = new Label("Yêu cầu cần xử lý");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button viewAllBtn = new Button("Xem tất cả →");
        viewAllBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #2E6F40;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 4 0;"
        );
        viewAllBtn.setOnAction(e -> mainController.showView("received-requests"));

        sectionHeader.getChildren().addAll(title, sp, viewAllBtn);

        // Pending rows
        VBox rows = new VBox(0);
        rows.getChildren().addAll(
            buildPendingRow("YC-2026-001", "Nguyễn Văn A", "15/04/2026", "2 loại", "Chờ xử lý"),
            buildPendingRow("YC-2026-006", "Nguyễn Văn A", "28/04/2026", "3 loại", "Chờ xử lý"),
            buildPendingRow("YC-2026-007", "Lê Văn C",     "30/04/2026", "2 loại", "Chờ xử lý")
        );

        section.getChildren().addAll(sectionHeader, rows);
        return section;
    }

    private HBox buildPendingRow(String ma, String nguoi, String ngayGap, String soLoai, String status) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 22, 14, 22));
        row.setStyle("-fx-border-color: transparent transparent #F5F9F6 transparent; -fx-border-width: 0 0 1 0;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F7FDF9; -fx-border-color: transparent transparent #F5F9F6 transparent; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #F5F9F6 transparent; -fx-border-width: 0 0 1 0;"));

        Label maLabel = new Label(ma);
        maLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        maLabel.setMinWidth(130);

        Label nguoiLabel = new Label(nguoi);
        nguoiLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        nguoiLabel.setMinWidth(150);

        Label soLoaiLabel = new Label(soLoai);
        soLoaiLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        soLoaiLabel.setMinWidth(90);

        Label ngayLabel = new Label("Hạn: " + ngayGap);
        ngayLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #D84315;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btn = new Button("⚙ Xử lý");
        btn.setStyle(
            "-fx-background-color: #253D2C;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 6 14;"
        );
        btn.setOnAction(e -> mainController.showView("request-processing"));

        row.getChildren().addAll(maLabel, nguoiLabel, soLoaiLabel, ngayLabel, sp, btn);
        return row;
    }

    // =========================================================================
    // RECENT ACTIVITY
    // =========================================================================
    private VBox buildRecentActivitySection() {
        VBox section = new VBox(0);
        section.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #E0EBE4;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 22, 18, 22));
        header.setStyle("-fx-border-color: transparent transparent #EEF3EF transparent; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Hoạt động gần đây");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        header.getChildren().add(title);

        // Activity items
        VBox items = new VBox(0);
        items.getChildren().addAll(
            buildActivityItem("📦", "Đơn hàng DH-004 đã được Seoul Tech Supply xác nhận",
                "2 giờ trước", "#1565C0"),
            buildActivityItem("✅", "Yêu cầu YC-2026-003 đã hoàn thành — tất cả đơn hàng đã giao",
                "5 giờ trước", "#2E6F40"),
            buildActivityItem("📥", "Yêu cầu mới YC-2026-007 từ Lê Văn C — 2 loại hàng, hạn 30/04",
                "Hôm nay 08:15", "#E65100"),
            buildActivityItem("🏭", "Site mới Singapore Trade Center đã đăng ký — chờ duyệt",
                "Hôm nay 07:30", "#6A1B9A"),
            buildActivityItem("❌", "Đơn hàng DH-007 bị hủy bởi Seoul Tech Supply",
                "Hôm qua 17:20", "#B91C1C")
        );

        section.getChildren().addAll(header, items);
        return section;
    }

    private HBox buildActivityItem(String icon, String message, String time, String accentColor) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 22, 14, 22));
        row.setStyle("-fx-border-color: transparent transparent #F5F9F6 transparent; -fx-border-width: 0 0 1 0;");

        // Icon circle
        Label iconLbl = new Label(icon);
        iconLbl.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-background-color: " + accentColor + "18;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 8 10;" +
            "-fx-min-width: 40;" +
            "-fx-alignment: center;"
        );

        // Text section
        VBox textBox = new VBox(2);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2A3D30;");
        msgLabel.setWrapText(true);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9DB0A4;");

        textBox.getChildren().addAll(msgLabel, timeLabel);

        // Accent dot
        Circle dot = new Circle(4);
        dot.setFill(Color.web(accentColor));

        row.getChildren().addAll(iconLbl, textBox, dot);
        return row;
    }

    public Node getView() {
        return view;
    }
}
