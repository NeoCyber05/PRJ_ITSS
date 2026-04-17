package org.itss.prj_itss;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class OrderManagementView {

    private final BorderPane view;
    private final MainLayoutController mainController;

    // {mã đơn, mã yc, site, số mặt hàng, vận chuyển (Tàu/Bay), ngày tạo, trạng thái}
    private static final String[][] SAMPLE_DATA = {
        {"DH-2026-001", "YC-2026-004", "Singapore Trade Center", "1", "Tàu",  "03/04/2026", "Hoàn thành"},
        {"DH-2026-002", "YC-2026-003", "Tokyo Electronics Hub",  "2", "Bay",  "02/04/2026", "Đang giao"},
        {"DH-2026-003", "YC-2026-003", "Singapore Trade Center", "1", "Tàu",  "02/04/2026", "Chờ xác nhận"},
        {"DH-2026-004", "YC-2026-002", "Shenzhen Import Co.",    "3", "Tàu",  "01/04/2026", "Chờ xác nhận"},
    };

    public OrderManagementView(MainLayoutController mainController) {
        this.mainController = mainController;
        view = new BorderPane();
        view.setStyle("-fx-background-color: #F5F9F6;");
        buildView();
    }

    private void buildView() {
        VBox all = new VBox(0);

        // === PAGE HEADER ===
        VBox header = new VBox(6);
        header.setPadding(new Insets(28, 36, 20, 36));
        header.setStyle(
            "-fx-background-color: #EDFAF2;" +
            "-fx-border-color: transparent transparent #D4EDE0 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        Label title = new Label("Quản lý đơn hàng");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        Label subtitle = new Label("Theo dõi trạng thái đơn hàng chính thức");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7C72;");

        header.getChildren().addAll(title, subtitle);

        // === FILTER BAR ===
        HBox filterBar = buildFilterBar();

        // === TABLE ===
        VBox tableSection = buildTable();

        all.getChildren().addAll(header, filterBar, tableSection);

        ScrollPane sp = new ScrollPane(all);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: white; -fx-background: white;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        view.setCenter(sp);
    }

    // =========================================================================
    // FILTER BAR
    // =========================================================================
    private HBox buildFilterBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(20, 36, 20, 36));
        bar.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: transparent transparent #EEF3EF transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        // Search
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #D0DAD5;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 0 14;"
        );
        searchBox.setMinHeight(40);
        searchBox.setPrefWidth(280);

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 13px;");

        TextField searchField = new TextField();
        searchField.setPromptText("Tìm theo mã đơn...");
        searchField.setStyle(
            "-fx-background-color: transparent; -fx-border-color: transparent;" +
            "-fx-padding: 0; -fx-font-size: 13px; -fx-prompt-text-fill: #A0B0A6;"
        );
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchBox.getChildren().addAll(searchIcon, searchField);

        // Status filter
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
            "Tất cả trạng thái", "Chờ xác nhận", "Đang giao", "Hoàn thành", "Đã hủy"
        );
        statusFilter.setValue("Tất cả trạng thái");
        statusFilter.setStyle(
            "-fx-background-color: white; -fx-border-color: #D0DAD5;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-font-size: 13px; -fx-padding: 6 10;"
        );
        statusFilter.setPrefWidth(180);

        bar.getChildren().addAll(searchBox, statusFilter);
        return bar;
    }

    // =========================================================================
    // TABLE
    // =========================================================================
    private VBox buildTable() {
        VBox container = new VBox(0);
        container.setStyle("-fx-background-color: white;");

        // Column header
        HBox headerRow = buildHeaderRow();
        container.getChildren().add(headerRow);

        // Data rows
        for (int i = 0; i < SAMPLE_DATA.length; i++) {
            container.getChildren().add(buildDataRow(SAMPLE_DATA[i]));
        }

        return container;
    }

    private HBox buildHeaderRow() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 36, 14, 36));
        row.setStyle(
            "-fx-background-color: #FAFAFA;" +
            "-fx-border-color: transparent transparent #E8EEEA transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        row.getChildren().addAll(
            hdr("MÃ ĐƠN",       120),
            hdr("MÃ YC",         110),
            hdr("SITE",          200),
            hdr("MẶT HÀNG",      90),
            hdr("VẬN CHUYỂN",    110),
            hdr("NGÀY TẠO",      120),
            hdr("TRẠNG THÁI",    150),
            hdr("THAO TÁC",      120)
        );

        return row;
    }

    private Label hdr(String text, double w) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        l.setMinWidth(w);
        l.setPrefWidth(w);
        return l;
    }

    private HBox buildDataRow(String[] data) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 36, 16, 36));
        row.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: transparent transparent #F0F4F2 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        String normalBg = "-fx-background-color: white; -fx-border-color: transparent transparent #F0F4F2 transparent; -fx-border-width: 0 0 1 0;";
        String hoverBg  = "-fx-background-color: #F7FDF9; -fx-border-color: transparent transparent #F0F4F2 transparent; -fx-border-width: 0 0 1 0;";
        row.setOnMouseEntered(e -> row.setStyle(hoverBg));
        row.setOnMouseExited(e  -> row.setStyle(normalBg));

        String maDon   = data[0];
        String maYC    = data[1];
        String site    = data[2];
        String matHang = data[3];
        String vanChuyen = data[4];
        String ngayTao = data[5];
        String trangThai = data[6];

        // Mã Đơn
        Label lblMaDon = new Label(maDon);
        lblMaDon.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        lblMaDon.setMinWidth(120); lblMaDon.setPrefWidth(120);

        // Mã YC
        Label lblMaYC = new Label(maYC);
        lblMaYC.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        lblMaYC.setMinWidth(110); lblMaYC.setPrefWidth(110);

        // Site
        Label lblSite = new Label(site);
        lblSite.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        lblSite.setMinWidth(200); lblSite.setPrefWidth(200);

        // Mặt hàng (số lượng)
        Label lblMH = new Label(matHang);
        lblMH.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        lblMH.setMinWidth(90); lblMH.setPrefWidth(90);

        // Vận chuyển (icon + text)
        HBox vcBox = buildTransportCell(vanChuyen);
        vcBox.setMinWidth(110); vcBox.setPrefWidth(110);

        // Ngày tạo
        Label lblNgay = new Label(ngayTao);
        lblNgay.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        lblNgay.setMinWidth(120); lblNgay.setPrefWidth(120);

        // Trạng thái
        HBox statusCell = buildStatusCell(trangThai);
        statusCell.setMinWidth(150); statusCell.setPrefWidth(150);

        // Thao tác
        HBox actionCell = buildActionCell(trangThai, maDon);
        actionCell.setMinWidth(120); actionCell.setPrefWidth(120);

        row.getChildren().addAll(
            lblMaDon, lblMaYC, lblSite, lblMH,
            vcBox, lblNgay, statusCell, actionCell
        );

        return row;
    }

    /** Vận chuyển: icon tàu/máy bay + text */
    private HBox buildTransportCell(String type) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);

        String icon;
        String color;
        if ("Tàu".equals(type)) {
            icon = "🚢";
            color = "#1565C0";
        } else {
            icon = "✈";
            color = "#E65100";
        }

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");

        Label textLabel = new Label(type);
        textLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        box.getChildren().addAll(iconLabel, textLabel);
        return box;
    }

    /** Chấm tròn + text trạng thái */
    private HBox buildStatusCell(String status) {
        HBox box = new HBox(7);
        box.setAlignment(Pos.CENTER_LEFT);

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

        Label lbl = new Label(status);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + textColor + ";");

        box.getChildren().addAll(dot, lbl);
        return box;
    }

    /** Nút 👁 xem + nút ⊗ hủy (chỉ khi Chờ xác nhận) */
    private HBox buildActionCell(String status, String maDon) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        // Eye button (always)
        Button eyeBtn = new Button("👁");
        eyeBtn.setStyle(
            "-fx-background-color: #F0F4F2; -fx-background-radius: 50;" +
            "-fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 6 10;" +
            "-fx-min-width: 34; -fx-min-height: 34;"
        );
        eyeBtn.setOnMouseEntered(e -> eyeBtn.setStyle(
            "-fx-background-color: #D8EAE0; -fx-background-radius: 50; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 6 10; -fx-min-width: 34; -fx-min-height: 34;"));
        eyeBtn.setOnMouseExited(e -> eyeBtn.setStyle(
            "-fx-background-color: #F0F4F2; -fx-background-radius: 50; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 6 10; -fx-min-width: 34; -fx-min-height: 34;"));
        eyeBtn.setTooltip(new Tooltip("Xem chi tiết " + maDon));
        eyeBtn.setOnAction(e -> mainController.showView("order-detail:" + maDon));

        box.getChildren().add(eyeBtn);

        // Cancel button (red circle) — only for "Chờ xác nhận"
        if ("Chờ xác nhận".equals(status)) {
            Button cancelBtn = new Button("⊗");
            cancelBtn.setStyle(
                "-fx-background-color: #FEE2E2; -fx-background-radius: 50;" +
                "-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: #DC2626; -fx-padding: 4 8;" +
                "-fx-min-width: 34; -fx-min-height: 34;"
            );
            cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
                "-fx-background-color: #FECACA; -fx-background-radius: 50; -fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: #DC2626; -fx-padding: 4 8; -fx-min-width: 34; -fx-min-height: 34;"));
            cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
                "-fx-background-color: #FEE2E2; -fx-background-radius: 50; -fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: #DC2626; -fx-padding: 4 8; -fx-min-width: 34; -fx-min-height: 34;"));
            cancelBtn.setTooltip(new Tooltip("Hủy đơn hàng " + maDon));

            box.getChildren().add(cancelBtn);
        }

        return box;
    }

    public Node getView() {
        return view;
    }
}
