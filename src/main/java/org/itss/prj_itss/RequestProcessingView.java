package org.itss.prj_itss;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;


public class RequestProcessingView {

    // ─── Data models ─────────────────────────────────────────────────────────

    static class ItemReq {
        String code, name;
        int required;
        ItemReq(String c, String n, int q) { code=c; name=n; required=q; }
    }

    static class SiteInfo {
        String id, name, country;
        int shipDays, airDays;
        Map<String, Integer> stock; // itemCode → qty
        SiteInfo(String id, String name, String country, int ship, int air, Map<String,Integer> s) {
            this.id=id; this.name=name; this.country=country;
            this.shipDays=ship; this.airDays=air; this.stock=s;
        }
    }

    static class Allocation {
        String siteId, itemCode;
        SimpleIntegerProperty qty;
        String transport; // "Tàu" | "Máy bay"
        Allocation(String si, String ic, int q, String t) {
            siteId=si; itemCode=ic; qty=new SimpleIntegerProperty(q); transport=t;
        }
    }

    // ─── State ───────────────────────────────────────────────────────────────

    private final BorderPane view;
    private final MainLayoutController mainController;

    private final List<ItemReq> items = List.of(
        new ItemReq("MH001", "iPhone 16 Pro Max", 100),
        new ItemReq("MH002", "Samsung Galaxy S25 Ultra", 150),
        new ItemReq("MH004", "iPad Air M3", 80)
    );

    private final List<SiteInfo> allSites;
    private final ObservableList<SiteInfo> visibleSites;
    private final Set<String> prioritySiteIds  = new LinkedHashSet<>();
    private final Set<String> excludedSiteIds  = new LinkedHashSet<>();

    /** allocations[itemIndex][siteId] = Allocation */
    private final Map<String, Map<String, Allocation>> allocations = new LinkedHashMap<>();

    // UI refs for live update
    private VBox siteListContainer;
    private HBox priorityTagsBox;
    private HBox excludeTagsBox;
    private VBox allocationPlanBox;         // rebuilt on each change
    private Label[] allocFractionLabels;    // per item

    // deadline: "28/04/2026" → 14 days from now for demo
    private static final int DEADLINE_DAYS = 14;

    public RequestProcessingView(MainLayoutController mainController) {
        this.mainController = mainController;

        // ── Build site data ──
        allSites = buildSiteData();
        visibleSites = FXCollections.observableArrayList(allSites);

        // ── Init allocations (empty) ──
        for (ItemReq item : items) {
            allocations.put(item.code, new LinkedHashMap<>());
        }

        view = new BorderPane();
        view.setStyle("-fx-background-color: #F5F9F6;");
        allocFractionLabels = new Label[items.size()];
        buildView();
    }

    // ─── Sample data ─────────────────────────────────────────────────────────

    private List<SiteInfo> buildSiteData() {
        Map<String,Integer> s1 = Map.of("MH001",200,"MH002",150,"MH004",300);
        Map<String,Integer> s2 = Map.of("MH001",1030,"MH002",550,"MH004",480);
        Map<String,Integer> s3 = Map.of("MH001",80,"MH002",200,"MH004",100);
        Map<String,Integer> s4 = Map.of("MH001",50,"MH002",100);
        return new ArrayList<>(List.of(
            new SiteInfo("SITE001","Tokyo Electronics Hub","Nhật Bản",14,3,s1),
            new SiteInfo("SITE002","Seoul Tech Supply","Hàn Quốc",12,3,s2),
            new SiteInfo("SITE003","Shenzhen Import Co.","Trung Quốc",10,2,s3),
            new SiteInfo("SITE004","Singapore Trade Center","Singapore",8,2,s4)
        ));
    }

    // ─── Build UI ────────────────────────────────────────────────────────────

    private void buildView() {
        // Header
        HBox header = buildHeader();
        view.setTop(header);

        // Scrollable content
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 28, 20, 28));
        content.setStyle("-fx-background-color: #F5F9F6;");

        content.getChildren().addAll(
            buildSiteFilterSection(),
            buildItemsTableSection(),
            buildAllocationSectionWrapper()
        );

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #F5F9F6; -fx-background: #F5F9F6;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        view.setCenter(sp);

        // Bottom bar
        view.setBottom(buildBottomBar());
    }

    // ─── HEADER ──────────────────────────────────────────────────────────────

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 28, 18, 28));
        header.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: transparent transparent #D8E8DD transparent;" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 2);"
        );

        VBox left = new VBox(3);
        Label section = new Label("XỬ LÝ YÊU CẦU");
        section.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2E6F40;");
        Label title = new Label("Yêu cầu YC-2026-001");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        Label desc = new Label("Ngày cần giao: 28/04/2026  •  3 mặt hàng  •  330 chiếc");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");
        left.getChildren().addAll(section, title, desc);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label badge = new Label("⏳ Chờ xử lý");
        badge.setStyle(
            "-fx-background-color: #FFF3E0; -fx-text-fill: #E65100;" +
            "-fx-background-radius: 8; -fx-padding: 6 14;" +
            "-fx-font-size: 13px; -fx-font-weight: bold;"
        );

        header.getChildren().addAll(left, sp, badge);
        return header;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 1 — SITE FILTER
    // ═════════════════════════════════════════════════════════════════════════

    private VBox buildSiteFilterSection() {
        VBox card = new VBox(14);
        card.setStyle(card_style());
        card.setPadding(new Insets(20));

        // Title row
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label sLabel = new Label("BỘ LỌC SITE");
        sLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2E6F40;");
        Label title = new Label("Chọn site ưu tiên hoặc loại bỏ");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button clearAll = new Button("✕ Xóa bộ lọc");
        clearAll.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C72;" +
            "-fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 4 8;");
        clearAll.setOnAction(e -> clearAllFilters());

        VBox filterHeader = new VBox(2, sLabel, title);
        HBox headerRow = new HBox(filterHeader, sp, clearAll);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Search
        TextField searchBox = new TextField();
        searchBox.setPromptText("Tìm theo tên site, mã site, quốc gia...");
        searchBox.setStyle(
            "-fx-background-color: #F5F9F6; -fx-border-color: #D0DAD5;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-padding: 9 14; -fx-font-size: 13px; -fx-prompt-text-fill: #A0B0A6;");
        searchBox.textProperty().addListener((ob, ov, nv) -> filterSites(nv));

        // Priority / Exclude tag rows
        HBox tagRow = new HBox(24);

        VBox priCol = new VBox(6);
        Label priLabel = new Label("SITE ƯU TIÊN");
        priLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        priorityTagsBox = new HBox(8);
        priorityTagsBox.setAlignment(Pos.CENTER_LEFT);
        updatePriorityTagsUI();
        priCol.getChildren().addAll(priLabel, priorityTagsBox);
        HBox.setHgrow(priCol, Priority.ALWAYS);

        VBox exCol = new VBox(6);
        Label exLabel = new Label("SITE LOẠI BỎ");
        exLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        excludeTagsBox = new HBox(8);
        excludeTagsBox.setAlignment(Pos.CENTER_LEFT);
        updateExcludeTagsUI();
        exCol.getChildren().addAll(exLabel, excludeTagsBox);
        HBox.setHgrow(exCol, Priority.ALWAYS);

        tagRow.getChildren().addAll(priCol, exCol);

        // Site list — count row
        HBox countRow = new HBox();
        countRow.setAlignment(Pos.CENTER_LEFT);
        Label count = new Label(visibleSites.size() + "/" + allSites.size() + " site");
        count.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");
        Region csp = new Region(); HBox.setHgrow(csp, Priority.ALWAYS);
        Label hint = new Label("Site ưu tiên sẽ được sắp xếp lên trên");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #A0B0A6;");
        countRow.getChildren().addAll(count, csp, hint);

        siteListContainer = new VBox(8);
        rebuildSiteList();

        card.getChildren().addAll(headerRow, searchBox, tagRow, countRow, siteListContainer);
        return card;
    }

    /** Lọc danh sách site theo search term */
    private void filterSites(String term) {
        visibleSites.clear();
        String lower = term == null ? "" : term.toLowerCase();
        for (SiteInfo s : allSites) {
            if (excludedSiteIds.contains(s.id)) continue;
            if (lower.isEmpty() || s.name.toLowerCase().contains(lower)
                    || s.id.toLowerCase().contains(lower)
                    || s.country.toLowerCase().contains(lower)) {
                visibleSites.add(s);
            }
        }
        rebuildSiteList();
    }

    /** Xóa toàn bộ bộ lọc */
    private void clearAllFilters() {
        prioritySiteIds.clear();
        excludedSiteIds.clear();
        visibleSites.setAll(allSites);
        updatePriorityTagsUI();
        updateExcludeTagsUI();
        rebuildSiteList();
    }

    /** Xây lại danh sách site cards (priority sites lên đầu) */
    private void rebuildSiteList() {
        siteListContainer.getChildren().clear();
        List<SiteInfo> sorted = new ArrayList<>();
        // priority sites first
        for (String id : prioritySiteIds) {
            allSites.stream().filter(s -> s.id.equals(id)).findFirst().ifPresent(sorted::add);
        }
        // rest (not excluded, not already added)
        for (SiteInfo s : visibleSites) {
            if (!prioritySiteIds.contains(s.id) && !excludedSiteIds.contains(s.id)) {
                sorted.add(s);
            }
        }
        for (SiteInfo site : sorted) {
            siteListContainer.getChildren().add(buildSiteCard(site));
        }
    }

    private HBox buildSiteCard(SiteInfo site) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));

        boolean isPriority = prioritySiteIds.contains(site.id);

        card.setStyle(
            "-fx-background-color: " + (isPriority ? "#F0FAF3" : "white") + ";" +
            "-fx-background-radius: 8; -fx-border-radius: 8;" +
            "-fx-border-color: " + (isPriority ? "#68BA7F" : "#E2EAE5") + ";" +
            "-fx-border-width: 1;" +
            (isPriority ? "-fx-effect: dropshadow(gaussian, rgba(104,186,127,0.15), 6, 0, 0, 2);" : "")
        );

        // Priority star badge
        if (isPriority) {
            Label star = new Label("⭐");
            star.setStyle("-fx-font-size: 14px;");
            card.getChildren().add(star);
        }

        // Site info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(site.name + (isPriority ? " — Đang ưu tiên" : ""));
        nameLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " +
            (isPriority ? "#2E6F40" : "#1a2e22") + ";");
        Label codeLbl = new Label(site.id + " · " + site.country +
            " · Tàu: " + site.shipDays + " ngày | Bay: " + site.airDays + " ngày");
        codeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8FA899;");
        info.getChildren().addAll(nameLbl, codeLbl);

        // Buttons
        Button priBtn;
        if (isPriority) {
            priBtn = new Button("✕ Bỏ ưu tiên");
            priBtn.setStyle(btn_chip("#FFF3E0", "#E65100"));
            priBtn.setOnAction(e -> {
                prioritySiteIds.remove(site.id);
                updatePriorityTagsUI();
                rebuildSiteList();
            });
        } else {
            priBtn = new Button("⭐ Ưu tiên");
            priBtn.setStyle(btn_chip("#E8F5E9", "#2E7D32"));
            priBtn.setOnAction(e -> {
                prioritySiteIds.add(site.id);
                excludedSiteIds.remove(site.id); // can't be both
                updatePriorityTagsUI();
                updateExcludeTagsUI();
                rebuildSiteList();
            });
        }

        Button removeBtn = new Button("⊘ Loại bỏ");
        removeBtn.setStyle(btn_chip("#FEE2E2", "#DC2626"));
        removeBtn.setOnAction(e -> {
            excludedSiteIds.add(site.id);
            prioritySiteIds.remove(site.id);
            visibleSites.remove(site);
            updateExcludeTagsUI();
            updatePriorityTagsUI();
            rebuildSiteList();
        });

        card.getChildren().addAll(info, priBtn, removeBtn);
        return card;
    }

    private void updatePriorityTagsUI() {
        priorityTagsBox.getChildren().clear();
        if (prioritySiteIds.isEmpty()) {
            Label ph = new Label("Chưa chọn site ưu tiên");
            ph.setStyle("-fx-background-color: #F0F7F2; -fx-border-color: #D0E4D6; -fx-border-radius: 14;" +
                "-fx-background-radius: 14; -fx-padding: 4 14; -fx-font-size: 12px; -fx-text-fill: #8FA899;");
            priorityTagsBox.getChildren().add(ph);
        } else {
            for (String id : prioritySiteIds) {
                allSites.stream().filter(s -> s.id.equals(id)).findFirst().ifPresent(site -> {
                    Label tag = new Label("⭐ " + site.name + "  ✕");
                    tag.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32;" +
                        "-fx-background-radius: 14; -fx-padding: 4 12; -fx-font-size: 12px;" +
                        "-fx-font-weight: bold; -fx-cursor: hand;");
                    tag.setOnMouseClicked(e -> {
                        prioritySiteIds.remove(id);
                        updatePriorityTagsUI();
                        rebuildSiteList();
                    });
                    priorityTagsBox.getChildren().add(tag);
                });
            }
        }
    }

    private void updateExcludeTagsUI() {
        excludeTagsBox.getChildren().clear();
        if (excludedSiteIds.isEmpty()) {
            Label ph = new Label("Chưa loại bỏ site nào");
            ph.setStyle("-fx-background-color: #F0F7F2; -fx-border-color: #D0E4D6; -fx-border-radius: 14;" +
                "-fx-background-radius: 14; -fx-padding: 4 14; -fx-font-size: 12px; -fx-text-fill: #8FA899;");
            excludeTagsBox.getChildren().add(ph);
        } else {
            for (String id : excludedSiteIds) {
                allSites.stream().filter(s -> s.id.equals(id)).findFirst().ifPresent(site -> {
                    Label tag = new Label("⊘ " + site.name + "  ✕");
                    tag.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;" +
                        "-fx-background-radius: 14; -fx-padding: 4 12; -fx-font-size: 12px;" +
                        "-fx-font-weight: bold; -fx-cursor: hand;");
                    tag.setOnMouseClicked(e -> {
                        excludedSiteIds.remove(id);
                        visibleSites.add(allSites.stream().filter(sx -> sx.id.equals(id)).findFirst().orElse(null));
                        updateExcludeTagsUI();
                        rebuildSiteList();
                    });
                    excludeTagsBox.getChildren().add(tag);
                });
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 2 — ITEMS TABLE
    // ═════════════════════════════════════════════════════════════════════════

    private VBox buildItemsTableSection() {
        VBox card = new VBox(0);
        card.setStyle(card_style());
        card.setPadding(Insets.EMPTY);

        // Header row
        HBox colHdr = new HBox();
        colHdr.setPadding(new Insets(11, 20, 11, 20));
        colHdr.setStyle("-fx-background-color: #F5F9F6;" +
            "-fx-border-color: transparent transparent #E8EEEA transparent; -fx-border-width: 0 0 1 0;");
        colHdr.setAlignment(Pos.CENTER_LEFT);
        colHdr.getChildren().addAll(
            colHdr("MÃ HÀNG", 140), colHdr("SỐ LƯỢNG YÊU CẦU", 150),
            colHdr("NGÀY CẦN GIAO", 140), colHdr("ĐÃ PHÂN BỔ", 160),
            new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
            colHdr("TỔNG TỒN KHO", 120)
        );
        card.getChildren().add(colHdr);

        // Item rows
        for (int i = 0; i < items.size(); i++) {
            ItemReq item = items.get(i);
            card.getChildren().add(buildItemRow(item, i));
        }

        return card;
    }

    private HBox buildItemRow(ItemReq item, int idx) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setStyle("-fx-border-color: transparent transparent #F0F4F2 transparent; -fx-border-width: 0 0 1 0;");

        // Code + name
        VBox codeCol = new VBox(2);
        codeCol.setMinWidth(140); codeCol.setPrefWidth(140);
        Label codeLbl = new Label(item.code);
        codeLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        Label nameLbl = new Label(item.name);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");
        codeCol.getChildren().addAll(codeLbl, nameLbl);

        // Required qty
        Label reqLbl = new Label(item.required + " Chiếc");
        reqLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a2e22;");
        reqLbl.setMinWidth(150);

        // Date
        Label dateLbl = new Label("28/04/2026");
        dateLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a2e22;");
        dateLbl.setMinWidth(140);

        // Allocated counter (live)
        VBox allocCol = new VBox(4);
        allocCol.setMinWidth(160);
        int allocated = getAllocated(item.code);
        boolean done = allocated >= item.required;
        Label earlyBadge;
        if (done) {
            earlyBadge = new Label("✅ Đủ số lượng");
            earlyBadge.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32;" +
                "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else if (allocated > 0) {
            earlyBadge = new Label("⚠ Chưa đủ");
            earlyBadge.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #E65100;" +
                "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            earlyBadge = new Label("○ Chưa phân bổ");
            earlyBadge.setStyle("-fx-background-color: #F0F4F2; -fx-text-fill: #8FA899;" +
                "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
        Label fracLbl = new Label(allocated + "/" + item.required + " chiếc");
        fracLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (done ? "#2E6F40" : "#E65100") + "; -fx-font-weight: bold;");
        allocFractionLabels[idx] = fracLbl;
        allocCol.getChildren().addAll(earlyBadge, fracLbl);

        // Spacer
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // Total stock across non-excluded sites
        int totalStock = allSites.stream()
            .filter(s -> !excludedSiteIds.contains(s.id))
            .mapToInt(s -> s.stock.getOrDefault(item.code, 0))
            .sum();
        VBox stockCol = new VBox(2);
        stockCol.setAlignment(Pos.CENTER_RIGHT); stockCol.setMinWidth(120);
        Label stockVal = new Label(String.valueOf(totalStock));
        stockVal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        Label stockLbl = new Label("Hiện tồn kho");
        stockLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8FA899;");
        stockCol.getChildren().addAll(stockVal, stockLbl);

        row.getChildren().addAll(codeCol, reqLbl, dateLbl, allocCol, spacer, stockCol);
        return row;
    }

    private int getAllocated(String itemCode) {
        return allocations.getOrDefault(itemCode, Collections.emptyMap())
            .values().stream().mapToInt(a -> a.qty.get()).sum();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 3 — ALLOCATION
    // ═════════════════════════════════════════════════════════════════════════

    /** Wrapper that holds ref so we can rebuild */
    private VBox buildAllocationSectionWrapper() {
        VBox wrapper = new VBox(0);
        allocationPlanBox = buildAllocationSection();
        wrapper.getChildren().add(allocationPlanBox);
        return wrapper;
    }

    private VBox buildAllocationSection() {
        VBox card = new VBox(16);
        card.setStyle(card_style());
        card.setPadding(new Insets(20));

        // Header
        HBox hdr = new HBox(12);
        hdr.setAlignment(Pos.CENTER_LEFT);

        VBox hdrText = new VBox(3);
        Label sLbl = new Label("PHÂN BỔ ĐƠN HÀNG");
        sLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2E6F40;");
        Label tLbl = new Label("Phân bổ số lượng cho từng site");
        tLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        hdrText.getChildren().addAll(sLbl, tLbl);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button optimizeBtn = new Button("⚡ Gợi ý tối ưu");
        optimizeBtn.setStyle(btn_style_secondary());
        optimizeBtn.setOnAction(e -> applyOptimalAllocation());

        Button showAllBtn = new Button("📋 Xem tất cả phương án");
        showAllBtn.setStyle(btn_style_primary());
        showAllBtn.setOnAction(e -> showAllAllocationsDialog());

        hdr.getChildren().addAll(hdrText, sp, optimizeBtn, showAllBtn);

        // Allocation input grid — one section per item
        VBox inputGrid = new VBox(14);

        for (int i = 0; i < items.size(); i++) {
            ItemReq item = items.get(i);
            inputGrid.getChildren().add(buildAllocInputForItem(item, i));
        }

        card.getChildren().addAll(hdr, inputGrid);
        return card;
    }

    /** Phần nhập phân bổ cho một mặt hàng */
    private VBox buildAllocInputForItem(ItemReq item, int idx) {
        VBox section = new VBox(8);
        section.setStyle(
            "-fx-background-color: #FAFDF9; -fx-background-radius: 10;" +
            "-fx-border-radius: 10; -fx-border-color: #D8E8DD; -fx-border-width: 1; -fx-padding: 14 16;"
        );

        // Item title
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label itemLbl = new Label(item.code + " — " + item.name);
        itemLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        int allocated = getAllocated(item.code);
        Label remainLbl = new Label("Còn cần: " + Math.max(0, item.required - allocated) + " chiếc");
        remainLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
            (allocated >= item.required ? "#2E7D32" : "#E65100") + "; -fx-font-weight: bold;");
        titleRow.getChildren().addAll(itemLbl, sp, remainLbl);

        // Site rows
        VBox siteRows = new VBox(6);
        for (SiteInfo site : allSites) {
            if (excludedSiteIds.contains(site.id)) continue;
            int stockQty = site.stock.getOrDefault(item.code, 0);
            if (stockQty == 0) continue;
            siteRows.getChildren().add(buildAllocRowForSiteItem(item, idx, site));
        }

        if (siteRows.getChildren().isEmpty()) {
            Label empty = new Label("Không có site nào cung cấp mặt hàng này.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #A0B0A6; -fx-padding: 8 0;");
            siteRows.getChildren().add(empty);
        }

        section.getChildren().addAll(titleRow, siteRows);
        return section;
    }

    /** Một dòng site × item trong phân bổ */
    private HBox buildAllocRowForSiteItem(ItemReq item, int itemIdx, SiteInfo site) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle(
            "-fx-background-color: white; -fx-background-radius: 8;" +
            "-fx-border-radius: 8; -fx-border-color: #EEF3EF; -fx-border-width: 1;"
        );

        // Site name
        Label siteName = new Label(site.name);
        siteName.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        siteName.setMinWidth(200);

        // Transport selector
        ToggleGroup tg = new ToggleGroup();

        boolean canShip = site.shipDays <= DEADLINE_DAYS;
        boolean canAir  = site.airDays  <= DEADLINE_DAYS;

        RadioButton shipRB = new RadioButton("🚢 Tàu (" + site.shipDays + " ngày)");
        shipRB.setToggleGroup(tg);
        shipRB.setStyle("-fx-font-size: 12px;");
        shipRB.setDisable(!canShip);
        if (!canShip) shipRB.setTooltip(new Tooltip("Đường tàu " + site.shipDays + " ngày > hạn " + DEADLINE_DAYS + " ngày"));

        RadioButton airRB = new RadioButton("✈ Bay (" + site.airDays + " ngày)");
        airRB.setToggleGroup(tg);
        airRB.setStyle("-fx-font-size: 12px;");
        airRB.setDisable(!canAir);

        // Default selection
        Allocation existing = allocations.get(item.code).get(site.id);
        if (existing != null) {
            if ("Tàu".equals(existing.transport) && canShip) shipRB.setSelected(true);
            else if (canAir) airRB.setSelected(true);
            else { shipRB.setSelected(canShip); }
        } else {
            if (canShip) shipRB.setSelected(true);
            else if (canAir) airRB.setSelected(true);
        }

        // Stock label
        int stock = site.stock.getOrDefault(item.code, 0);
        Label stockLbl = new Label("Tồn: " + stock);
        stockLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8FA899;");

        // Quantity input
        TextField qtyField = new TextField();
        qtyField.setPrefWidth(80);
        int existingQty = (existing != null) ? existing.qty.get() : 0;
        qtyField.setText(existingQty > 0 ? String.valueOf(existingQty) : "");
        qtyField.setPromptText("0");
        qtyField.setStyle(
            "-fx-background-color: white; -fx-border-color: #D0DAD5;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-padding: 6 10; -fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-alignment: center;"
        );

        Label chiLbl = new Label("chiếc");
        chiLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");

        // Validation warning label
        Label warnLbl = new Label("");
        warnLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #DC2626;");

        // On qty change — update allocation + fraction display
        qtyField.textProperty().addListener((ob, ov, nv) -> {
            try {
                int q = nv.trim().isEmpty() ? 0 : Integer.parseInt(nv.trim());
                if (q < 0) { warnLbl.setText("Số lượng không âm"); return; }
                if (q > stock) {
                    warnLbl.setText("Vượt tồn kho (" + stock + ")");
                    qtyField.setStyle(qtyField.getStyle().replace("#D0DAD5","#DC2626"));
                } else {
                    warnLbl.setText("");
                    qtyField.setStyle(
                        "-fx-background-color: white; -fx-border-color: #D0DAD5;" +
                        "-fx-border-radius: 6; -fx-background-radius: 6;" +
                        "-fx-padding: 6 10; -fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: center;");
                }
                String transport = (tg.getSelectedToggle() == airRB) ? "Máy bay" : "Tàu";
                if (q > 0) {
                    Allocation alloc = allocations.get(item.code)
                        .computeIfAbsent(site.id, k -> new Allocation(site.id, item.code, 0, transport));
                    alloc.qty.set(q);
                    alloc.transport = transport;
                } else {
                    allocations.get(item.code).remove(site.id);
                }
                updateItemFractionLabel(item, itemIdx);
            } catch (NumberFormatException ignored) {
                warnLbl.setText("Nhập số!");
            }
        });

        tg.selectedToggleProperty().addListener((ob, ov, nv) -> {
            if (nv == null) return;
            Allocation alloc = allocations.get(item.code).get(site.id);
            if (alloc != null) {
                alloc.transport = (nv == airRB) ? "Máy bay" : "Tàu";
            }
        });

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        row.getChildren().addAll(siteName, stockLbl, shipRB, airRB, sp, qtyField, chiLbl, warnLbl);
        return row;
    }

    /** Cập nhật nhãn xx/yy chiếc trong bảng mặt hàng */
    private void updateItemFractionLabel(ItemReq item, int idx) {
        if (allocFractionLabels[idx] == null) return;
        int alloc = getAllocated(item.code);
        boolean done = alloc >= item.required;
        allocFractionLabels[idx].setText(alloc + "/" + item.required + " chiếc");
        allocFractionLabels[idx].setStyle(
            "-fx-font-size: 13px; -fx-text-fill: " + (done ? "#2E6F40" : "#E65100") + "; -fx-font-weight: bold;"
        );
    }

    // ─── OPTIMAL ALLOCATION ──────────────────────────────────────────────────

    /**
     * Thuật toán gợi ý tối ưu:
     *  1. Lọc sites có thể giao trong hạn (ưu tiên tàu trước)
     *  2. Sắp xếp ưu tiên: priority sites → tàu → tồn kho lớn
     *  3. Fill từng item, tối thiểu số site
     */
    private void applyOptimalAllocation() {
        // Clear current
        for (ItemReq item : items) {
            allocations.get(item.code).clear();
        }

        for (int idx = 0; idx < items.size(); idx++) {
            ItemReq item = items.get(idx);
            int remaining = item.required;

            // Sites sorted: priority first, then by ship availability, then by stock
            List<SiteInfo> sorted = allSites.stream()
                .filter(s -> !excludedSiteIds.contains(s.id))
                .filter(s -> s.stock.getOrDefault(item.code, 0) > 0)
                .filter(s -> s.shipDays <= DEADLINE_DAYS || s.airDays <= DEADLINE_DAYS)
                .sorted((a, b) -> {
                    boolean aPri = prioritySiteIds.contains(a.id);
                    boolean bPri = prioritySiteIds.contains(b.id);
                    if (aPri != bPri) return aPri ? -1 : 1;
                    boolean aShip = a.shipDays <= DEADLINE_DAYS;
                    boolean bShip = b.shipDays <= DEADLINE_DAYS;
                    if (aShip != bShip) return aShip ? -1 : 1;
                    return b.stock.getOrDefault(item.code, 0) - a.stock.getOrDefault(item.code, 0);
                })
                .toList();

            for (SiteInfo site : sorted) {
                if (remaining <= 0) break;
                int stock = site.stock.getOrDefault(item.code, 0);
                int take = Math.min(remaining, stock);
                String transport = site.shipDays <= DEADLINE_DAYS ? "Tàu" : "Máy bay";
                allocations.get(item.code).put(site.id, new Allocation(site.id, item.code, take, transport));
                remaining -= take;
            }
        }

        // Rebuild section
        VBox parent = (VBox) allocationPlanBox.getParent();
        parent.getChildren().clear();
        allocationPlanBox = buildAllocationSection();
        parent.getChildren().add(allocationPlanBox);

        // Update fraction labels
        for (int i = 0; i < items.size(); i++) {
            updateItemFractionLabel(items.get(i), i);
        }

        showToast("✅ Đã áp dụng phương án phân bổ tối ưu!");
    }

    // ─── SHOW ALL ALLOCATIONS DIALOG ─────────────────────────────────────────

    private void showAllAllocationsDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Tất cả phương án phân bổ hợp lệ");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #F5F9F6;");
        root.setPrefWidth(600);

        Label title = new Label("📋 Các phương án phân bổ hợp lệ");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        Label sub = new Label("Hệ thống đã tính toán các phương án phân bổ khả thi:");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");

        VBox plans = new VBox(12);

        // Plan 1 — optimal (fewest sites, all sea)
        plans.getChildren().add(buildPlanCard(1, "Tối ưu (ít site nhất)",
            new String[][]{
                {"MH001","Shenzhen Import Co.","100 chiếc","Tàu","Sớm 4 ngày"},
                {"MH002","Shenzhen Import Co.","150 chiếc","Tàu","Sớm 4 ngày"},
                {"MH004","Tokyo Electronics Hub","80 chiếc","Tàu","Sớm 0 ngày"},
            }, "#2E6F40", true));

        // Plan 2 — all from one site
        plans.getChildren().add(buildPlanCard(2, "Tất cả từ Seoul Tech Supply",
            new String[][]{
                {"MH001","Seoul Tech Supply","100 chiếc","Tàu","Sớm 2 ngày"},
                {"MH002","Seoul Tech Supply","150 chiếc","Tàu","Sớm 2 ngày"},
                {"MH004","Seoul Tech Supply","80 chiếc","Tàu","Sớm 2 ngày"},
            }, "#1565C0", false));

        // Plan 3 — air delivery
        plans.getChildren().add(buildPlanCard(3, "Giao nhanh (hàng không)",
            new String[][]{
                {"MH001","Seoul Tech Supply","100 chiếc","Máy bay","Sớm 11 ngày"},
                {"MH002","Shenzhen Import Co.","150 chiếc","Máy bay","Sớm 12 ngày"},
                {"MH004","Tokyo Electronics Hub","80 chiếc","Máy bay","Sớm 11 ngày"},
            }, "#6A1B9A", false));

        ScrollPane sp = new ScrollPane(plans);
        sp.setFitToWidth(true); sp.setPrefHeight(360);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button closeBtn = new Button("Đóng");
        closeBtn.setStyle(btn_style_secondary());
        closeBtn.setOnAction(e -> dialog.close());

        HBox footer = new HBox(closeBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, sub, sp, footer);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private VBox buildPlanCard(int n, String planName, String[][] rows, String color, boolean recommended) {
        VBox card = new VBox(8);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 10;" +
            "-fx-border-radius: 10; -fx-border-color: " + color + (recommended ? "" : "44") + ";" +
            "-fx-border-width: " + (recommended ? "2" : "1") + "; -fx-padding: 14;"
        );

        HBox hdr = new HBox(8);
        hdr.setAlignment(Pos.CENTER_LEFT);
        Label numLbl = new Label("Phương án " + n + (recommended ? "  ⭐ Khuyến nghị" : ""));
        numLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label nameLbl = new Label(planName);
        nameLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7C72;");
        hdr.getChildren().addAll(numLbl, nameLbl);

        VBox rowsBox = new VBox(4);
        for (String[] r : rows) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label code = new Label(r[0]);
            code.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
            code.setMinWidth(55);
            Label site = new Label(r[1]);
            site.setStyle("-fx-font-size: 12px; -fx-text-fill: #3A4A40;");
            HBox.setHgrow(site, Priority.ALWAYS);
            Label qty = new Label(r[2]);
            qty.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
            Label transport = new Label(r[3].equals("Tàu") ? "🚢 Tàu" : "✈ Bay");
            transport.setStyle("-fx-font-size: 11px; -fx-text-fill: " +
                (r[3].equals("Tàu") ? "#1565C0" : "#E65100") + ";");
            Label eta = new Label(r[4]);
            eta.setStyle("-fx-font-size: 11px; -fx-text-fill: #2E7D32;");
            row.getChildren().addAll(code, site, qty, transport, eta);
            rowsBox.getChildren().add(row);
        }

        Button applyBtn = new Button("Áp dụng phương án này →");
        applyBtn.setStyle(
            "-fx-background-color: " + color + "; -fx-text-fill: white;" +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;" +
            "-fx-padding: 6 16; -fx-font-weight: bold;"
        );
        applyBtn.setOnAction(e -> {
            showToast("✅ Đã áp dụng " + planName + "!");
        });

        card.getChildren().addAll(hdr, rowsBox, applyBtn);
        return card;
    }

    // ─── BOTTOM BAR ──────────────────────────────────────────────────────────

    private HBox buildBottomBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(16, 28, 16, 28));
        bar.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #D8E8DD transparent transparent transparent;" +
            "-fx-border-width: 1 0 0 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, -2);"
        );

        Button backBtn = new Button("← Danh sách yêu cầu");
        backBtn.setStyle(btn_style_secondary());
        backBtn.setOnAction(e -> mainController.showView("received-requests"));

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button confirmBtn = new Button("▶ Xác nhận và gửi");
        confirmBtn.setStyle(
            "-fx-background-color: #253D2C; -fx-text-fill: white;" +
            "-fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-cursor: hand; -fx-padding: 12 32;" +
            "-fx-effect: dropshadow(gaussian, rgba(37,61,44,0.3), 6, 0, 0, 2);"
        );
        confirmBtn.setOnAction(e -> handleConfirm());

        bar.getChildren().addAll(backBtn, sp, confirmBtn);
        return bar;
    }

    private void handleConfirm() {
        // Validate: all items fully allocated?
        List<String> errors = new ArrayList<>();
        for (ItemReq item : items) {
            int allocated = getAllocated(item.code);
            if (allocated < item.required) {
                errors.add("• " + item.code + " chỉ phân bổ " + allocated + "/" + item.required);
            }
            if (allocated > item.required) {
                errors.add("• " + item.code + " phân bổ vượt " + allocated + "/" + item.required);
            }
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Chưa hoàn tất phân bổ");
            alert.setHeaderText("Vui lòng kiểm tra lại phân bổ:");
            alert.setContentText(String.join("\n", errors));
            styleDialog(alert);
            alert.showAndWait();
            return;
        }

        // Confirm dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận gửi đơn hàng");
        confirm.setHeaderText("Bạn có chắc muốn tạo và gửi đơn hàng?");
        confirm.setContentText(
            "Hệ thống sẽ tạo đơn hàng cho các site và không thể hoàn tác.\n\n" +
            "Tổng: " + items.stream().mapToInt(i -> i.required).sum() + " chiếc | " +
            allocations.values().stream().flatMap(m -> m.keySet().stream()).distinct().count() + " site"
        );
        styleDialog(confirm);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Go back and show success toast
            mainController.showView("orders");
            showToast("🎉 Đã tạo đơn hàng thành công và gửi tới các site!");
        }
    }

    // ─── UTILS ───────────────────────────────────────────────────────────────

    private void styleDialog(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.setStyle("-fx-background-color: white; -fx-font-size: 13px;");
    }

    private void showToast(String message) {
        // Simple label toast hosted in a temporary stage
        Stage toast = new Stage();
        toast.setAlwaysOnTop(true);
        toast.initModality(Modality.NONE);
        Label lbl = new Label(message);
        lbl.setStyle(
            "-fx-background-color: #253D2C; -fx-text-fill: white;" +
            "-fx-padding: 14 24; -fx-background-radius: 10;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;"
        );
        Scene s = new Scene(new StackPane(lbl));
        s.setFill(null);
        toast.setScene(s);
        toast.show();
        // Auto close after 2.5s
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2.5), ev -> toast.close())
        );
        tl.play();
    }

    // ─── STYLE HELPERS ───────────────────────────────────────────────────────

    private String card_style() {
        return "-fx-background-color: white; -fx-background-radius: 12;" +
               "-fx-border-radius: 12; -fx-border-color: #E0EBE4; -fx-border-width: 1;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);";
    }

    private String btn_style_primary() {
        return "-fx-background-color: #2E6F40; -fx-text-fill: white;" +
               "-fx-background-radius: 7; -fx-cursor: hand; -fx-font-size: 13px;" +
               "-fx-font-weight: bold; -fx-padding: 9 20;";
    }

    private String btn_style_secondary() {
        return "-fx-background-color: white; -fx-text-fill: #2E6F40;" +
               "-fx-background-radius: 7; -fx-border-color: #2E6F40; -fx-border-radius: 7;" +
               "-fx-border-width: 1.5; -fx-cursor: hand; -fx-font-size: 13px;" +
               "-fx-font-weight: bold; -fx-padding: 9 20;";
    }

    private String btn_chip(String bg, String fg) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
               "-fx-background-radius: 14; -fx-cursor: hand; -fx-font-size: 12px;" +
               "-fx-font-weight: bold; -fx-padding: 5 14;";
    }

    private Label colHdr(String text, double w) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        l.setMinWidth(w); l.setPrefWidth(w);
        return l;
    }

    public Node getView() {
        return view;
    }
}
