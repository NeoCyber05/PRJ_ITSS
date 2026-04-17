package org.itss.prj_itss;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;


public class OrderDetailView {

    private final BorderPane view;
    private final MainLayoutController mainController;
    private final String orderId;

    // Lookup data giả lập cho từng đơn hàng
    private String maSite, tenSite, ngayTao, trangThai;
    private String[][] items;

    public OrderDetailView(MainLayoutController mainController, String orderId) {
        this.mainController = mainController;
        this.orderId = orderId;
        view = new BorderPane();
        view.setStyle("-fx-background-color: #F5F9F6;");

        loadSampleData();
        buildView();
    }

    /** Giả lập lookup dữ liệu */
    private void loadSampleData() {
        switch (orderId) {
            case "DH-2026-001":
                maSite = "SITE004"; tenSite = "Singapore Trade Center";
                ngayTao = "03/04/2026"; trangThai = "Hoàn thành";
                items = new String[][]{
                    {"1", "MH003", "MacBook Pro M4", "50", "Chiếc", "Đường biển"}
                };
                break;
            case "DH-2026-002":
                maSite = "SITE001"; tenSite = "Tokyo Electronics Hub";
                ngayTao = "02/04/2026"; trangThai = "Đang giao";
                items = new String[][]{
                    {"1", "MH001", "iPhone 16 Pro Max", "100", "Chiếc", "Hàng không"},
                    {"2", "MH006", "AirPods Pro 3", "300", "Chiếc", "Hàng không"}
                };
                break;
            case "DH-2026-003":
                maSite = "SITE004"; tenSite = "Singapore Trade Center";
                ngayTao = "02/04/2026"; trangThai = "Chờ xác nhận";
                items = new String[][]{
                    {"1", "MH001", "iPhone 16 Pro Max", "50", "Chiếc", "Đường biển"}
                };
                break;
            case "DH-2026-004":
            default:
                maSite = "SITE003"; tenSite = "Shenzhen Import Co.";
                ngayTao = "01/04/2026"; trangThai = "Đang giao";
                items = new String[][]{
                    {"1", "MH002", "Samsung Galaxy S25 Ultra", "300", "Chiếc", "Đường biển"},
                    {"2", "MH005", "Sony WH-1000XM5", "150", "Chiếc", "Đường biển"},
                    {"3", "MH004", "iPad Air M3", "80", "Chiếc", "Đường biển"}
                };
                break;
        }
    }

    private void buildView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 40, 40, 40));
        content.setStyle("-fx-background-color: #F5F9F6;");

        // 1) Back + Header
        VBox headerSection = buildHeader();

        // 2) Thông tin tổng quan card
        VBox infoCard = buildInfoCard();

        // 3) Tiến trình đơn hàng card
        VBox progressCard = buildProgressCard();

        // 4) Danh sách mặt hàng card
        VBox itemsCard = buildItemsCard();

        content.getChildren().addAll(headerSection, infoCard, progressCard, itemsCard);

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #F5F9F6; -fx-background: #F5F9F6;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        view.setCenter(sp);
    }

    // =========================================================================
    // HEADER
    // =========================================================================
    private VBox buildHeader() {
        VBox header = new VBox(10);

        // Back button
        Button backBtn = new Button("← Chi tiết đơn hàng");
        backBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #3A4A40;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 4 0;"
        );
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #2E6F40; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 4 0;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #3A4A40; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 4 0;"));
        backBtn.setOnAction(e -> mainController.showView("orders"));

        // Title
        Label title = new Label("Chi tiết đơn hàng");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        Label subtitle = new Label("Mã đơn hàng: " + orderId);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7C72;");

        // Status badge
        Label statusBadge = buildStatusBadge(trangThai);

        header.getChildren().addAll(backBtn, title, subtitle, statusBadge);
        return header;
    }

    private Label buildStatusBadge(String status) {
        Label badge = new Label();
        String bgColor, textColor, icon;
        switch (status) {
            case "Chờ xác nhận":
                icon = "⏳"; bgColor = "#FFF3E0"; textColor = "#E65100"; break;
            case "Đang giao":
                icon = "🚚"; bgColor = "#E3F2FD"; textColor = "#1565C0"; break;
            case "Hoàn thành":
                icon = "✅"; bgColor = "#E8F5E9"; textColor = "#2E7D32"; break;
            case "Đã hủy":
                icon = "❌"; bgColor = "#FFEBEE"; textColor = "#C62828"; break;
            default:
                icon = "📌"; bgColor = "#F5F5F5"; textColor = "#616161"; break;
        }
        badge.setText(icon + " " + status);
        badge.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 6 16;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );
        return badge;
    }

    // =========================================================================
    // INFO CARD
    // =========================================================================
    private VBox buildInfoCard() {
        VBox card = new VBox(16);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #E0EBE4;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 24;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        Label cardTitle = new Label("Thông tin tổng quan");
        cardTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        // Info grid — 3 columns, 2 rows
        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(18);
        grid.setPadding(new Insets(10, 0, 0, 0));

        // Row 0
        grid.add(buildInfoItem("📋 Mã đơn hàng", orderId), 0, 0);
        grid.add(buildInfoItem("🏭 Mã Site", maSite), 1, 0);
        grid.add(buildInfoItem("📍 Tên Site", tenSite), 2, 0);

        // Row 1
        grid.add(buildInfoItem("📅 Ngày tạo", ngayTao), 0, 1);
        grid.add(buildInfoStatus("⏱ Trạng thái", trangThai), 1, 1);
        grid.add(buildInfoItem("📦 Tổng số mặt hàng", items.length + " mặt hàng"), 2, 1);

        // Make columns grow evenly
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            grid.getColumnConstraints().add(cc);
        }

        card.getChildren().addAll(cardTitle, grid);
        return card;
    }

    private VBox buildInfoItem(String label, String value) {
        VBox item = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8FA899;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        item.getChildren().addAll(lbl, val);
        return item;
    }

    private VBox buildInfoStatus(String label, String status) {
        VBox item = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8FA899;");

        // Status with colored dot
        HBox statusBox = new HBox(6);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        String dotColor;
        String textColor;
        switch (status) {
            case "Chờ xác nhận": dotColor = "#F59E0B"; textColor = "#B45309"; break;
            case "Đang giao":   dotColor = "#3B82F6"; textColor = "#1D4ED8"; break;
            case "Hoàn thành":  dotColor = "#22C55E"; textColor = "#15803D"; break;
            case "Đã hủy":      dotColor = "#EF4444"; textColor = "#B91C1C"; break;
            default:            dotColor = "#9CA3AF"; textColor = "#6B7280"; break;
        }
        Circle dot = new Circle(5);
        dot.setFill(Color.web(dotColor));
        Label statusLbl = new Label(status);
        statusLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        statusBox.getChildren().addAll(dot, statusLbl);
        item.getChildren().addAll(lbl, statusBox);
        return item;
    }

    // =========================================================================
    // PROGRESS CARD
    // =========================================================================
    private VBox buildProgressCard() {
        VBox card = new VBox(20);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #E0EBE4;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 24;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        Label cardTitle = new Label("Tiến trình đơn hàng");
        cardTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        // Progress steps
        HBox progress = buildProgressSteps();

        card.getChildren().addAll(cardTitle, progress);
        return card;
    }

    private HBox buildProgressSteps() {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10, 20, 10, 20));

        // Determine which step is active (0-based)
        int activeStep;
        switch (trangThai) {
            case "Chờ xác nhận": activeStep = 0; break;
            case "Đang giao":    activeStep = 1; break;
            case "Hoàn thành":   activeStep = 2; break;
            default:             activeStep = -1; break; // Đã hủy
        }

        String[] stepNames  = {"Chờ xác nhận", "Đang giao", "Hoàn thành"};
        String[] stepIcons  = {"⏳", "🚚", "✅"};

        for (int i = 0; i < 3; i++) {
            VBox step = buildStepNode(stepIcons[i], stepNames[i], i, activeStep);
            container.getChildren().add(step);

            // Add connecting line between steps
            if (i < 2) {
                Region line = new Region();
                boolean linePassed = i < activeStep;
                line.setStyle(
                    "-fx-background-color: " + (linePassed ? "#2E6F40" : "#D8E8DD") + ";" +
                    "-fx-min-height: 3; -fx-max-height: 3; -fx-pref-height: 3;" +
                    "-fx-min-width: 80; -fx-pref-width: 120;" +
                    "-fx-background-radius: 2;"
                );
                VBox lineWrapper = new VBox(line);
                lineWrapper.setAlignment(Pos.CENTER);
                lineWrapper.setPadding(new Insets(0, 4, 20, 4));
                HBox.setHgrow(lineWrapper, Priority.ALWAYS);
                container.getChildren().add(lineWrapper);
            }
        }

        return container;
    }

    private VBox buildStepNode(String icon, String name, int stepIndex, int activeStep) {
        VBox step = new VBox(8);
        step.setAlignment(Pos.CENTER);
        step.setMinWidth(100);

        boolean isPassed   = stepIndex < activeStep;
        boolean isCurrent  = stepIndex == activeStep;
        boolean isFuture   = stepIndex > activeStep;

        // Circle icon
        String circleBg, circleTextColor;
        String circleSize = "44";
        if (isPassed) {
            circleBg = "#2E6F40"; circleTextColor = "white";
        } else if (isCurrent) {
            circleBg = "#3B82F6"; circleTextColor = "white";
        } else {
            circleBg = "#F0F4F2"; circleTextColor = "#A0B0A6";
        }

        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-background-color: " + circleBg + ";" +
            "-fx-background-radius: 50;" +
            "-fx-min-width: " + circleSize + ";" +
            "-fx-min-height: " + circleSize + ";" +
            "-fx-pref-width: " + circleSize + ";" +
            "-fx-pref-height: " + circleSize + ";" +
            "-fx-alignment: center;" +
            "-fx-text-fill: " + circleTextColor + ";"
        );

        // Step name
        Label nameLabel = new Label(name);
        String nameColor = isFuture ? "#A0B0A6" : (isCurrent ? "#1D4ED8" : "#2E6F40");
        String nameWeight = isCurrent ? "bold" : "normal";
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + nameColor + "; -fx-font-weight: " + nameWeight + ";");

        step.getChildren().addAll(iconLabel, nameLabel);
        return step;
    }

    // =========================================================================
    // ITEMS CARD
    // =========================================================================
    private VBox buildItemsCard() {
        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #E0EBE4;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        // Title
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setPadding(new Insets(20, 24, 16, 24));
        titleRow.setStyle("-fx-border-color: transparent transparent #EEF3EF transparent; -fx-border-width: 0 0 1 0;");

        Label cardTitle = new Label("Danh sách mặt hàng");
        cardTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        titleRow.getChildren().add(cardTitle);

        // Table header
        HBox tableHeader = buildItemTableHeader();

        // Table data rows
        VBox tableRows = new VBox(0);
        for (String[] item : items) {
            tableRows.getChildren().add(buildItemRow(item));
        }

        card.getChildren().addAll(titleRow, tableHeader, tableRows);
        return card;
    }

    private HBox buildItemTableHeader() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 24, 12, 24));
        row.setStyle(
            "-fx-background-color: #F8FAF8;" +
            "-fx-border-color: transparent transparent #E8EEEA transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        row.getChildren().addAll(
            itemHdr("STT",              50),
            itemHdr("MÃ HÀNG",         100),
            itemHdr("TÊN MẶT HÀNG",    220),
            itemHdr("SỐ LƯỢNG ĐẶT",    110),
            itemHdr("ĐƠN VỊ TÍNH",     100),
            itemHdr("PHƯƠNG THỨC VẬN CHUYỂN", 200)
        );

        return row;
    }

    private Label itemHdr(String text, double w) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        l.setMinWidth(w);
        l.setPrefWidth(w);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private HBox buildItemRow(String[] item) {
        // item: {stt, mã hàng, tên, số lượng, đơn vị, phương thức vận chuyển}
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 24, 14, 24));
        row.setStyle(
            "-fx-border-color: transparent transparent #F0F4F2 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );
        row.setOnMouseEntered(e -> row.setStyle(
            "-fx-background-color: #F7FDF9; -fx-border-color: transparent transparent #F0F4F2 transparent; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle(
            "-fx-border-color: transparent transparent #F0F4F2 transparent; -fx-border-width: 0 0 1 0;"));

        Label stt = new Label(item[0]);
        stt.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7C72;");
        stt.setMinWidth(50); stt.setAlignment(Pos.CENTER);

        Label maHang = new Label(item[1]);
        maHang.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        maHang.setMinWidth(100); maHang.setAlignment(Pos.CENTER);

        Label tenHang = new Label(item[2]);
        tenHang.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        tenHang.setMinWidth(220);

        Label soLuong = new Label(item[3]);
        soLuong.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        soLuong.setMinWidth(110); soLuong.setAlignment(Pos.CENTER);

        Label donVi = new Label(item[4]);
        donVi.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        donVi.setMinWidth(100); donVi.setAlignment(Pos.CENTER);

        // Transport badge
        Label transport = new Label();
        String method = item[5];
        if ("Đường biển".equals(method)) {
            transport.setText("🚢 Đường biển");
            transport.setStyle(
                "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0;" +
                "-fx-background-radius: 14; -fx-padding: 4 14;" +
                "-fx-font-size: 12px; -fx-font-weight: bold;"
            );
        } else {
            transport.setText("✈ Hàng không");
            transport.setStyle(
                "-fx-background-color: #FFF3E0; -fx-text-fill: #E65100;" +
                "-fx-background-radius: 14; -fx-padding: 4 14;" +
                "-fx-font-size: 12px; -fx-font-weight: bold;"
            );
        }
        HBox transportBox = new HBox(transport);
        transportBox.setMinWidth(200);
        transportBox.setAlignment(Pos.CENTER);

        row.getChildren().addAll(stt, maHang, tenHang, soLuong, donVi, transportBox);
        return row;
    }

    public Node getView() {
        return view;
    }
}
