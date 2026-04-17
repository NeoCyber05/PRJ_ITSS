package org.itss.prj_itss;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Yêu cầu đã nhận (Received Requests) View.
 * Hiển thị danh sách yêu cầu đặt hàng dưới dạng bảng với:
 * - Cột: Mã YC, Người tạo, Ngày tạo, Số loại hàng, Ngày cần gấp nhất, Trạng thái, Thao tác
 * - Chấm màu biểu thị trạng thái
 * - Nút "Xử lý yêu cầu" chỉ hiện với trạng thái Chờ xử lý
 * - Phân trang ở dưới
 */
public class ReceivedRequestsView {

    private final BorderPane view;
    private final MainLayoutController mainController;

    // Sample data: {mã YC, người tạo, ngày tạo, số loại hàng, ngày cần gấp nhất, trạng thái}
    private static final String[][] SAMPLE_DATA = {
        {"YC-2026-001", "Nguyễn Văn A", "28/03/2026", "2 loại", "15/04/2026", "Chờ xử lý"},
        {"YC-2026-002", "Trần Thị B",   "30/03/2026", "3 loại", "20/04/2026", "Đang xử lý"},
        {"YC-2026-003", "Nguyễn Văn A", "01/04/2026", "2 loại", "25/04/2026", "Đang giao"},
        {"YC-2026-004", "Lê Văn C",     "02/04/2026", "1 loại", "18/04/2026", "Đã hoàn thành"},
        {"YC-2026-005", "Trần Thị B",   "03/04/2026", "2 loại", "22/04/2026", "Đã hủy"},
        {"YC-2026-006", "Nguyễn Văn A", "04/04/2026", "3 loại", "28/04/2026", "Chờ xử lý"},
        {"YC-2026-007", "Lê Văn C",     "05/04/2026", "2 loại", "30/04/2026", "Chờ xử lý"},
    };

    public ReceivedRequestsView(MainLayoutController mainController) {
        this.mainController = mainController;
        view = new BorderPane();
        view.getStyleClass().add("content-area");
        buildView();
    }

    private void buildView() {
        // === Page Header (light green background like mockup) ===
        VBox header = new VBox(6);
        header.setStyle(
            "-fx-background-color: #EDFAF2;" +
            "-fx-padding: 28 32 24 32;" +
            "-fx-border-color: transparent transparent #D4EDE0 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        Label pageTitle = new Label("Yêu cầu đặt hàng đã nhận");
        pageTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        header.getChildren().add(pageTitle);
        view.setTop(header);

        // === Main Content ===
        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: white;");

        // Filter bar
        HBox filterBar = buildFilterBar();

        // Table
        VBox tableSection = buildTable();
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        // Pagination
        VBox pagination = buildPagination();

        content.getChildren().addAll(filterBar, tableSection, pagination);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        view.setCenter(scrollPane);
    }

    // =========================================================================
    // FILTER BAR
    // =========================================================================
    private HBox buildFilterBar() {
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(20, 32, 20, 32));
        filterBar.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: transparent transparent #EEF3EF transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        // Search field
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #D0DAD5;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 0 14 0 14;"
        );
        searchBox.setPrefWidth(280);
        searchBox.setMinHeight(40);

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 13px;");

        TextField searchField = new TextField();
        searchField.setPromptText("Tìm mã yêu cầu...");
        searchField.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-padding: 0;" +
            "-fx-font-size: 13px;" +
            "-fx-prompt-text-fill: #A0B0A6;"
        );
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchBox.getChildren().addAll(searchIcon, searchField);

        // Status filter dropdown
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
            "Mọi trạng thái", "Chờ xử lý", "Đang xử lý",
            "Đang giao", "Đã hoàn thành", "Đã hủy"
        );
        statusFilter.setValue("Mọi trạng thái");
        statusFilter.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #D0DAD5;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 6 10;"
        );
        statusFilter.setPrefWidth(180);

        filterBar.getChildren().addAll(searchBox, statusFilter);
        return filterBar;
    }

    // =========================================================================
    // TABLE
    // =========================================================================
    private VBox buildTable() {
        VBox tableContainer = new VBox(0);
        tableContainer.setStyle("-fx-background-color: white;");

        // Column headers row
        HBox headerRow = buildColumnHeaderRow();
        tableContainer.getChildren().add(headerRow);

        // Data rows
        for (int i = 0; i < SAMPLE_DATA.length; i++) {
            HBox dataRow = buildDataRow(SAMPLE_DATA[i], i % 2 == 0);
            tableContainer.getChildren().add(dataRow);
        }

        return tableContainer;
    }

    private HBox buildColumnHeaderRow() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 32, 14, 32));
        row.setStyle(
            "-fx-background-color: #FAFAFA;" +
            "-fx-border-color: transparent transparent #E8EEEA transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        row.getChildren().addAll(
            makeHeader("MÃ YC",              120),
            makeHeader("NGƯỜI TẠO",           160),
            makeHeader("NGÀY TẠO",            120),
            makeHeader("SỐ LOẠI HÀNG",        110),
            makeHeader("NGÀY CẦN GẤP NHẤT",   160),
            makeHeader("TRẠNG THÁI",          160),
            makeHeader("THAO TÁC",            200)
        );

        return row;
    }

    private Label makeHeader(String text, double width) {
        Label lbl = new Label(text);
        lbl.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #8FA899;"
        );
        lbl.setMinWidth(width);
        lbl.setPrefWidth(width);
        return lbl;
    }

    private HBox buildDataRow(String[] data, boolean isEven) {
        // data: {mã YC, người tạo, ngày tạo, số loại hàng, ngày cần gấp nhất, trạng thái}
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 32, 16, 32));
        row.setStyle(
            "-fx-background-color: " + (isEven ? "white" : "white") + ";" +
            "-fx-border-color: transparent transparent #F0F4F2 transparent;" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-cursor: hand;"
        );

        // Hover effect
        String normalStyle = "-fx-background-color: white; -fx-border-color: transparent transparent #F0F4F2 transparent; -fx-border-width: 0 0 1 0;";
        String hoverStyle  = "-fx-background-color: #F7FDF9; -fx-border-color: transparent transparent #F0F4F2 transparent; -fx-border-width: 0 0 1 0;";
        row.setOnMouseEntered(e -> row.setStyle(hoverStyle + "-fx-cursor: hand;"));
        row.setOnMouseExited(e  -> row.setStyle(normalStyle + "-fx-cursor: hand;"));

        String maYC       = data[0];
        String nguoiTao   = data[1];
        String ngayTao    = data[2];
        String soLoai     = data[3];
        String ngayGap    = data[4];
        String trangThai  = data[5];

        // --- Cột MÃ YC ---
        Label lblMa = new Label(maYC);
        lblMa.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        lblMa.setMinWidth(120); lblMa.setPrefWidth(120);

        // --- Cột NGƯỜI TẠO ---
        Label lblNguoi = new Label(nguoiTao);
        lblNguoi.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        lblNguoi.setMinWidth(160); lblNguoi.setPrefWidth(160);

        // --- Cột NGÀY TẠO ---
        Label lblNgayTao = new Label(ngayTao);
        lblNgayTao.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        lblNgayTao.setMinWidth(120); lblNgayTao.setPrefWidth(120);

        // --- Cột SỐ LOẠI HÀNG ---
        Label lblSoLoai = new Label(soLoai);
        lblSoLoai.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        lblSoLoai.setMinWidth(110); lblSoLoai.setPrefWidth(110);

        // --- Cột NGÀY CẦN GẤP NHẤT (màu đỏ/cam, nổi bật) ---
        Label lblNgayGap = new Label(ngayGap);
        lblNgayGap.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #D84315;");
        lblNgayGap.setMinWidth(160); lblNgayGap.setPrefWidth(160);

        // --- Cột TRẠNG THÁI (chấm màu + text) ---
        HBox statusBox = buildStatusCell(trangThai);
        statusBox.setMinWidth(160); statusBox.setPrefWidth(160);

        // --- Cột THAO TÁC ---
        HBox actionBox = buildActionCell(trangThai, maYC);
        HBox.setHgrow(actionBox, Priority.ALWAYS);

        row.getChildren().addAll(
            lblMa, lblNguoi, lblNgayTao, lblSoLoai,
            lblNgayGap, statusBox, actionBox
        );

        return row;
    }

    /** Chấm tròn màu + tên trạng thái */
    private HBox buildStatusCell(String status) {
        HBox box = new HBox(7);
        box.setAlignment(Pos.CENTER_LEFT);

        // Dot color mapping
        String dotColor;
        String textColor;
        switch (status) {
            case "Chờ xử lý":    dotColor = "#F59E0B"; textColor = "#B45309"; break;
            case "Đang xử lý":   dotColor = "#3B82F6"; textColor = "#1D4ED8"; break;
            case "Đang giao":    dotColor = "#A855F7"; textColor = "#7E22CE"; break;
            case "Đã hoàn thành":dotColor = "#22C55E"; textColor = "#15803D"; break;
            case "Đã hủy":       dotColor = "#EF4444"; textColor = "#B91C1C"; break;
            default:             dotColor = "#9CA3AF"; textColor = "#6B7280"; break;
        }

        Circle dot = new Circle(5);
        dot.setFill(Color.web(dotColor));

        Label lbl = new Label(status);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + textColor + ";");

        box.getChildren().addAll(dot, lbl);
        return box;
    }

    /** Nút xem (mắt) + nút "Xử lý yêu cầu" chỉ khi Chờ xử lý */
    private HBox buildActionCell(String status, String maYC) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        // Eye button
        Button eyeBtn = new Button("👁");
        eyeBtn.setStyle(
            "-fx-background-color: #F0F4F2;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 6 10;"
        );
        eyeBtn.setOnMouseEntered(e -> eyeBtn.setStyle(
            "-fx-background-color: #D8EAE0; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 6 10;"));
        eyeBtn.setOnMouseExited(e -> eyeBtn.setStyle(
            "-fx-background-color: #F0F4F2; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 6 10;"));
        eyeBtn.setTooltip(new Tooltip("Xem chi tiết yêu cầu " + maYC));

        box.getChildren().add(eyeBtn);

        // "Xử lý yêu cầu" button - chỉ hiện khi Chờ xử lý
        if ("Chờ xử lý".equals(status)) {
            Button processBtn = new Button("⚙ Xử lý yêu cầu");
            processBtn.setStyle(
                "-fx-background-color: #253D2C;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 7 16;"
            );
            processBtn.setOnMouseEntered(e -> processBtn.setStyle(
                "-fx-background-color: #1a2e20; -fx-text-fill: white; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 7 16;"));
            processBtn.setOnMouseExited(e -> processBtn.setStyle(
                "-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 7 16;"));
            processBtn.setOnAction(e -> mainController.showView("request-processing"));

            box.getChildren().add(processBtn);
        }

        return box;
    }

    // =========================================================================
    // PAGINATION
    // =========================================================================
    private VBox buildPagination() {
        VBox wrapper = new VBox(12);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(20, 32, 24, 32));
        wrapper.setStyle("-fx-background-color: white;");

        // "Hiển thị 1-7 của 7 yêu cầu"
        Label info = new Label("Hiển thị 1 - 7 của 7 yêu cầu");
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7C72;");

        // Page buttons row
        HBox pageRow = new HBox(6);
        pageRow.setAlignment(Pos.CENTER);

        Button prevBtn = buildPageNavButton("‹");
        Button page1   = buildPageNumberButton("1", true);
        Button page2   = buildPageNumberButton("2", false);
        Button page3   = buildPageNumberButton("3", false);
        Button nextBtn = buildPageNavButton("›");

        prevBtn.setDisable(true);   // on first page

        pageRow.getChildren().addAll(prevBtn, page1, page2, page3, nextBtn);
        wrapper.getChildren().addAll(info, pageRow);
        return wrapper;
    }

    private Button buildPageNavButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #D0DAD5;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 15px;" +
            "-fx-padding: 5 12;" +
            "-fx-text-fill: #3A4A40;"
        );
        return btn;
    }

    private Button buildPageNumberButton(String text, boolean active) {
        Button btn = new Button(text);
        if (active) {
            btn.setStyle(
                "-fx-background-color: #253D2C;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-border-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 5 12;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #D0DAD5;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 5 12;" +
                "-fx-text-fill: #3A4A40;"
            );
        }
        return btn;
    }

    public Node getView() {
        return view;
    }
}
