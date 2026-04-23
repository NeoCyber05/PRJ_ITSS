package org.itss.prj_itss.request;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.itss.prj_itss.request.RequestModels.Allocation;
import org.itss.prj_itss.request.RequestModels.ItemReq;
import org.itss.prj_itss.request.RequestModels.SiteInfo;
import org.itss.prj_itss.ui.Notifications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllocationSection {

    private static final String TRANSPORT_SHIP = "ship";
    private static final String TRANSPORT_AIR = "air";
    private static final int MAX_SUGGESTED_PLANS = 10;
    private static final int MAX_ITEM_VARIANTS = 12;
    private static final int MAX_COMBINATION_ATTEMPTS = 240;

    private final List<ItemReq> items;
    private final List<SiteInfo> allSites;
    private final Set<Integer> excludedSiteIds;
    private final Set<Integer> prioritySiteIds;
    private final Map<Integer, Map<Integer, Allocation>> allocations;
    private final int deadlineDays;

    private VBox allocationPlanBox;
    private Label[] allocFractionLabels;
    private Runnable onAllocationChanged;
    private Runnable onPlanApplied;

    public AllocationSection(
        List<ItemReq> items,
        List<SiteInfo> allSites,
        Set<Integer> excludedSiteIds,
        Set<Integer> prioritySiteIds,
        Map<Integer, Map<Integer, Allocation>> allocations,
        int deadlineDays
    ) {
        this.items = items;
        this.allSites = allSites;
        this.excludedSiteIds = excludedSiteIds;
        this.prioritySiteIds = prioritySiteIds;
        this.allocations = allocations;
        this.deadlineDays = deadlineDays;
    }

    public void setAllocFractionLabels(Label[] labels) {
        this.allocFractionLabels = labels;
    }

    public void setOnAllocationChanged(Runnable callback) {
        this.onAllocationChanged = callback;
    }

    public void setOnPlanApplied(Runnable callback) {
        this.onPlanApplied = callback;
    }

    public int getAllocated(int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Collections.emptyMap())
            .values()
            .stream()
            .mapToInt(allocation -> allocation.qty.get())
            .sum();
    }

    public VBox buildWrapper() {
        VBox wrapper = new VBox(0);
        allocationPlanBox = buildSection();
        wrapper.getChildren().add(allocationPlanBox);
        return wrapper;
    }

    public VBox buildInlineEditor(ItemReq item, int itemIndex) {
        VBox section = new VBox(16);
        section.setPadding(new Insets(22, 20, 18, 20));
        section.setStyle(
            "-fx-background-color:#FCFEFD;"
                + "-fx-border-color:transparent transparent #F0F4F2 transparent;"
                + "-fx-border-width:0 0 1 0;"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        Label sectionLabel = new Label("TỒN KHO VÀ PHÂN BỔ THEO SITE");
        sectionLabel.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#6B7F95;");

        Label titleLabel = new Label(item.code + " - " + item.name);
        titleLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1a2e22;");

        Label subtitleLabel = new Label("Có mặt hàng tại " + countAvailableSites(item) + " site");
        subtitleLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#6B7C72;");

        textBox.getChildren().addAll(sectionLabel, titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label allocatedBadge = new Label();
        Label remainingBadge = new Label();
        Runnable refreshSummary = () -> updateSummaryBadges(item, allocatedBadge, remainingBadge);
        refreshSummary.run();

        header.getChildren().addAll(textBox, spacer, allocatedBadge, remainingBadge);

        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color:white;"
                + "-fx-background-radius:12;"
                + "-fx-border-radius:12;"
                + "-fx-border-color:#D8E8DD;"
                + "-fx-border-width:1;"
        );
        table.getChildren().add(buildInlineTableHeader());

        boolean hasRows = false;
        for (SiteInfo site : allSites) {
            if (excludedSiteIds.contains(site.id)) {
                continue;
            }
            if (site.stock.getOrDefault(item.merchandiseId, 0) <= 0) {
                continue;
            }
            table.getChildren().add(buildSiteAllocationRow(item, itemIndex, site, refreshSummary));
            hasRows = true;
        }

        if (!hasRows) {
            Label emptyLabel = new Label("Không có site khả dụng cho mặt hàng này.");
            emptyLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#8FA899; -fx-padding:16 18;");
            table.getChildren().add(emptyLabel);
        }

        section.getChildren().addAll(header, table);
        return section;
    }

    public void applyOptimalAllocation() {
        clearCurrentAllocations();

        for (int index = 0; index < items.size(); index++) {
            ItemReq item = items.get(index);
            int remaining = item.required;

            List<SiteInfo> sortedSites = buildCandidateSites(item);
            for (SiteInfo site : sortedSites) {
                if (remaining <= 0) {
                    break;
                }

                String transport = pickSuggestedTransport(site);
                if (transport == null) {
                    continue;
                }

                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                int quantity = Math.min(remaining, stock);
                if (quantity <= 0) {
                    continue;
                }

                allocations.get(item.merchandiseId).put(
                    site.id,
                    new Allocation(site.id, item.merchandiseId, quantity, transport)
                );
                remaining -= quantity;
            }
        }

        if (allocationPlanBox != null && allocationPlanBox.getParent() instanceof VBox) {
            rebuildSection();
        }
        for (int index = 0; index < items.size(); index++) {
            updateItemFractionLabel(items.get(index), index);
        }
        notifyAllocationChanged();
    }

    public void rebuildSection() {
        if (allocationPlanBox == null || !(allocationPlanBox.getParent() instanceof VBox parent)) {
            return;
        }

        parent.getChildren().clear();
        allocationPlanBox = buildSection();
        parent.getChildren().add(allocationPlanBox);
    }

    public void showAllAllocationsDialog() {
        List<SuggestedPlan> suggestedPlans = buildSuggestedPlans(MAX_SUGGESTED_PLANS);

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Các phương án phân bổ thỏa mãn");
        dialog.setResizable(true);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #F5F9F6;");
        root.setPrefWidth(960);
        root.setPrefHeight(720);

        Label titleLabel = new Label("Các phương án phân bổ thỏa mãn");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        String subtitleText = suggestedPlans.isEmpty()
            ? "Không tìm được phương án đáp ứng đủ số lượng và thời hạn hiện tại."
            : "Hiển thị " + suggestedPlans.size() + " phương án khác nhau theo từng đơn hàng gửi tới site.";
        Label subtitleLabel = new Label(subtitleText);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");

        VBox plansBox = new VBox(14);
        if (suggestedPlans.isEmpty()) {
            VBox emptyCard = new VBox(8);
            emptyCard.setPadding(new Insets(18));
            emptyCard.setStyle(
                "-fx-background-color:white;"
                    + "-fx-background-radius:12;"
                    + "-fx-border-radius:12;"
                    + "-fx-border-color:#D8E8DD;"
                    + "-fx-border-width:1;"
            );

            Label emptyTitle = new Label("Chưa có phương án thỏa mãn");
            emptyTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a2e22;");
            Label emptyText = new Label("Kiểm tra lại site bị loại bỏ, số lượng tồn kho hoặc ngày giao yêu cầu.");
            emptyText.setWrapText(true);
            emptyText.setStyle("-fx-font-size:12px; -fx-text-fill:#6B7C72;");

            emptyCard.getChildren().addAll(emptyTitle, emptyText);
            plansBox.getChildren().add(emptyCard);
        } else {
            for (int index = 0; index < suggestedPlans.size(); index++) {
                plansBox.getChildren().add(buildSuggestedPlanCard(suggestedPlans.get(index), index + 1, dialog));
            }
        }

        ScrollPane scrollPane = new ScrollPane(plansBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().add("forest-secondary-button");
        closeButton.setOnAction(event -> dialog.close());

        HBox footer = new HBox(closeButton);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titleLabel, subtitleLabel, scrollPane, footer);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    public void updateItemFractionLabel(ItemReq item, int index) {
        if (allocFractionLabels == null || index >= allocFractionLabels.length || allocFractionLabels[index] == null) {
            return;
        }

        int allocated = getAllocated(item.merchandiseId);
        String color = "#6B7C72";
        if (allocated > item.required) {
            color = "#B91C1C";
        } else if (allocated == item.required) {
            color = "#2E6F40";
        } else if (allocated > 0) {
            color = "#E65100";
        }

        allocFractionLabels[index].setText(allocated + "/" + item.required);
        allocFractionLabels[index].setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private VBox buildSection() {
        VBox card = new VBox(16);
        card.getStyleClass().add("forest-card");
        card.setPadding(new Insets(20));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(3);
        Label sectionLabel = new Label("PHÂN BỔ ĐƠN HÀNG");
        sectionLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2E6F40;");
        Label titleLabel = new Label("Phân bổ số lượng cho từng site");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        headerText.getChildren().addAll(sectionLabel, titleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button optimizeButton = new Button("Gợi ý tối ưu");
        optimizeButton.getStyleClass().add("forest-secondary-button");
        optimizeButton.setOnAction(event -> applyOptimalAllocation());

        Button showAllButton = new Button("Xem tất cả phương án");
        showAllButton.getStyleClass().add("forest-primary-button");
        showAllButton.setOnAction(event -> showAllAllocationsDialog());

        header.getChildren().addAll(headerText, spacer, optimizeButton, showAllButton);

        VBox inputGrid = new VBox(14);
        for (int index = 0; index < items.size(); index++) {
            inputGrid.getChildren().add(buildInlineEditor(items.get(index), index));
        }

        card.getChildren().addAll(header, inputGrid);
        return card;
    }

    private HBox buildInlineTableHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color:#F5F9F6; -fx-background-radius:12 12 0 0;");

        header.getChildren().addAll(
            buildColumnHeader("SITE", 380),
            buildColumnHeader("TỒN KHO", 100),
            buildColumnHeader("SL PHÂN BỔ", 170),
            buildColumnHeader("VẬN CHUYỂN", 180),
            buildColumnHeader("TRẠNG THÁI", 120)
        );

        return header;
    }

    private VBox buildSiteAllocationRow(ItemReq item, int itemIndex, SiteInfo site, Runnable refreshSummary) {
        VBox rowBox = new VBox(4);
        rowBox.setStyle("-fx-border-color:transparent transparent #EEF3EF transparent; -fx-border-width:0 0 1 0;");
        rowBox.setPadding(new Insets(0, 0, 0, 0));

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));

        VBox siteBox = new VBox(4);
        siteBox.setMinWidth(380);
        siteBox.setPrefWidth(380);

        Label siteNameLabel = new Label(site.name);
        siteNameLabel.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1a2e22;");

        String detailText = site.siteCode;
        if (site.description != null && !site.description.isBlank()) {
            detailText += " - " + site.description;
        }
        Label siteDetailLabel = new Label(detailText);
        siteDetailLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#8FA899;");

        siteBox.getChildren().addAll(siteNameLabel, siteDetailLabel);

        int stock = site.stock.getOrDefault(item.merchandiseId, 0);

        Label stockLabel = new Label(String.valueOf(stock));
        stockLabel.setMinWidth(100);
        stockLabel.setPrefWidth(100);
        stockLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#3A4A40;");

        Allocation existing = allocations.getOrDefault(item.merchandiseId, Collections.emptyMap()).get(site.id);
        String selectedTransport = normalizeTransport(existing == null ? null : existing.transport, site);

        TextField quantityField = new TextField();
        quantityField.setPrefWidth(110);
        quantityField.setText(existing == null || existing.qty.get() == 0 ? "0" : String.valueOf(existing.qty.get()));
        quantityField.setStyle(
            "-fx-background-color:white;"
                + "-fx-border-color:#D0DAD5;"
                + "-fx-border-radius:8;"
                + "-fx-background-radius:8;"
                + "-fx-padding:9 12;"
                + "-fx-font-size:13px;"
                + "-fx-font-weight:bold;"
        );

        Label unitLabel = new Label("chiếc");
        unitLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#6B7C72;");
        HBox quantityBox = new HBox(8, quantityField, unitLabel);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        quantityBox.setMinWidth(170);
        quantityBox.setPrefWidth(170);

        ComboBox<String> transportBox = new ComboBox<>();
        if (site.shipDays < 999) {
            transportBox.getItems().add(transportLabel(TRANSPORT_SHIP));
        }
        if (site.airDays < 999) {
            transportBox.getItems().add(transportLabel(TRANSPORT_AIR));
        }
        if (transportBox.getItems().isEmpty()) {
            transportBox.getItems().add("Không khả dụng");
            transportBox.setDisable(true);
        } else {
            transportBox.setValue(transportLabel(selectedTransport));
        }
        transportBox.setPrefWidth(180);
        transportBox.setMinWidth(180);
        transportBox.setStyle("-fx-background-color:white; -fx-font-size:13px;");

        Label etaBadge = new Label();
        etaBadge.setMinWidth(120);
        etaBadge.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 14; -fx-background-radius:999;");
        updateEtaBadge(etaBadge, site, selectedTransport);

        Label warningLabel = new Label();
        warningLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#B91C1C; -fx-padding:0 16 10 16;");
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);

        Runnable syncSummary = () -> {
            updateItemFractionLabel(item, itemIndex);
            refreshSummary.run();
            notifyAllocationChanged();
        };

        quantityField.textProperty().addListener((observable, oldValue, newValue) ->
            applyAllocationChange(item, itemIndex, site, quantityField, transportBox, etaBadge, warningLabel, syncSummary)
        );
        transportBox.valueProperty().addListener((observable, oldValue, newValue) ->
            applyAllocationChange(item, itemIndex, site, quantityField, transportBox, etaBadge, warningLabel, syncSummary)
        );

        row.getChildren().addAll(siteBox, stockLabel, quantityBox, transportBox, etaBadge);
        rowBox.getChildren().addAll(row, warningLabel);
        return rowBox;
    }

    private VBox buildSuggestedPlanCard(SuggestedPlan plan, int number, Stage dialog) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color:white;"
                + "-fx-background-radius:14;"
                + "-fx-border-radius:14;"
                + "-fx-border-color:#D8E8DD;"
                + "-fx-border-width:1;"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBox = new VBox(6);
        Label titleLabel = new Label("Phương án " + String.format("%02d", number));
        titleLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1a2e22;");
        Label summaryLabel = new Label(
            plan.siteOrders().size() + " site"
                + " • " + plan.totalLineCount() + " dòng đặt hàng"
                + " • " + plan.totalQuantity() + " chiếc"
        );
        summaryLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#6B7C72;");
        titleBox.getChildren().addAll(titleLabel, summaryLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox tagRow = new HBox(8);
        tagRow.setAlignment(Pos.CENTER_RIGHT);
        tagRow.getChildren().addAll(
            buildMetricTag("Đủ số lượng", "#E8F5E9", "#2E7D32"),
            buildMetricTag("Kịp ngày nhận", "#EEF4FF", "#2456C2")
        );

        Button applyButton = new Button("Áp dụng phương án này");
        applyButton.setStyle(
            "-fx-background-color:#253D2C;"
                + "-fx-text-fill:white;"
                + "-fx-background-radius:8;"
                + "-fx-cursor:hand;"
                + "-fx-font-size:12px;"
                + "-fx-font-weight:bold;"
                + "-fx-padding:8 16;"
        );
        applyButton.setOnAction(event -> {
            applySuggestedPlan(plan);
            dialog.close();
            Notifications.showToast("Đã áp dụng phương án " + number + ".");
        });

        VBox headerRight = new VBox(10, tagRow, applyButton);
        headerRight.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(titleBox, spacer, headerRight);

        VBox siteOrdersBox = new VBox(12);
        for (SiteOrderSuggestion siteOrder : plan.siteOrders()) {
            siteOrdersBox.getChildren().add(buildSiteOrderCard(siteOrder));
        }

        card.getChildren().addAll(header, siteOrdersBox);
        return card;
    }

    private VBox buildSiteOrderCard(SiteOrderSuggestion siteOrder) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle(
            "-fx-background-color:#FCFEFD;"
                + "-fx-background-radius:12;"
                + "-fx-border-radius:12;"
                + "-fx-border-color:#E5ECE7;"
                + "-fx-border-width:1;"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox siteBox = new VBox(4);
        Label siteNameLabel = new Label(siteOrder.site().name);
        siteNameLabel.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a2e22;");
        Label siteMetaLabel = new Label(
            siteOrder.site().siteCode
                + " • " + siteOrder.lines().size() + " mặt hàng"
                + " • " + siteOrder.totalQuantity() + " chiếc"
        );
        siteMetaLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#6B7C72;");
        siteBox.getChildren().addAll(siteNameLabel, siteMetaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox tagRow = new HBox(8);
        tagRow.setAlignment(Pos.CENTER_RIGHT);
        tagRow.getChildren().addAll(
            buildMetricTag(siteOrder.transportSummary(), "#EEF4FF", "#2456C2"),
            buildMetricTag("ETA " + siteOrder.deliveryDays() + " ngày", "#F0FDF4", "#2E7D32")
        );

        header.getChildren().addAll(siteBox, spacer, tagRow);

        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color:white;"
                + "-fx-background-radius:10;"
                + "-fx-border-radius:10;"
                + "-fx-border-color:#EDF3EE;"
                + "-fx-border-width:1;"
        );
        table.getChildren().add(buildSuggestedOrderHeader());
        for (OrderLineSuggestion line : siteOrder.lines()) {
            table.getChildren().add(buildSuggestedOrderRow(line));
        }

        card.getChildren().addAll(header, table);
        return card;
    }

    private HBox buildSuggestedOrderHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setStyle("-fx-background-color:#F7FAF8; -fx-background-radius:10 10 0 0;");
        header.getChildren().addAll(
            buildColumnHeader("MÃ HÀNG", 130),
            buildColumnHeader("TÊN MẶT HÀNG", 280),
            buildColumnHeader("SỐ LƯỢNG", 120),
            buildColumnHeader("VẬN CHUYỂN", 150)
        );
        return header;
    }

    private HBox buildSuggestedOrderRow(OrderLineSuggestion line) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle("-fx-border-color:transparent transparent #EEF3EF transparent; -fx-border-width:0 0 1 0;");

        Label codeLabel = buildValueCell(line.item().code, 130, true);
        Label nameLabel = buildValueCell(line.item().name, 280, false);
        Label qtyLabel = buildValueCell(line.quantity() + " chiếc", 120, true);
        Label transportLabel = buildValueCell(transportLabel(line.transport()), 150, false);

        row.getChildren().addAll(codeLabel, nameLabel, qtyLabel, transportLabel);
        return row;
    }

    private Label buildMetricTag(String text, String background, String textColor) {
        Label label = new Label(text);
        label.setStyle(
            "-fx-background-color:" + background + ";"
                + "-fx-text-fill:" + textColor + ";"
                + "-fx-background-radius:999;"
                + "-fx-padding:6 12;"
                + "-fx-font-size:11px;"
                + "-fx-font-weight:bold;"
        );
        return label;
    }

    private Label buildValueCell(String text, double width, boolean emphasize) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setWrapText(true);
        label.setStyle(
            "-fx-font-size:12px;"
                + "-fx-text-fill:#1a2e22;"
                + (emphasize ? "-fx-font-weight:bold;" : "")
        );
        return label;
    }

    private void applyAllocationChange(
        ItemReq item,
        int itemIndex,
        SiteInfo site,
        TextField quantityField,
        ComboBox<String> transportBox,
        Label etaBadge,
        Label warningLabel,
        Runnable onUpdated
    ) {
        String rawValue = quantityField.getText() == null ? "" : quantityField.getText().trim();
        int quantity;
        try {
            quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);
        } catch (NumberFormatException exception) {
            showWarning(warningLabel, "Nhập số nguyên hợp lệ.");
            return;
        }

        if (quantity < 0) {
            showWarning(warningLabel, "Số lượng không được âm.");
            return;
        }

        int stock = site.stock.getOrDefault(item.merchandiseId, 0);
        if (quantity > stock) {
            showWarning(warningLabel, "Vượt tồn kho của site (" + stock + ").");
            return;
        }

        hideWarning(warningLabel);

        String transport = normalizeTransport(transportBox.getValue(), site);
        updateEtaBadge(etaBadge, site, transport);

        if (quantity > 0) {
            Allocation allocation = allocations
                .computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>())
                .computeIfAbsent(site.id, key -> new Allocation(site.id, item.merchandiseId, 0, transport));
            allocation.qty.set(quantity);
            allocation.transport = transport;
        } else {
            Map<Integer, Allocation> itemAllocations = allocations.get(item.merchandiseId);
            if (itemAllocations != null) {
                itemAllocations.remove(site.id);
            }
        }

        onUpdated.run();
    }

    private void updateSummaryBadges(ItemReq item, Label allocatedBadge, Label remainingBadge) {
        int allocated = getAllocated(item.merchandiseId);
        int remaining = item.required - allocated;

        allocatedBadge.setText("Đã phân bổ " + allocated + "/" + item.required);
        allocatedBadge.setStyle(
            "-fx-background-color:#FFFFFF;"
                + "-fx-border-color:#D8E8DD;"
                + "-fx-border-radius:999;"
                + "-fx-background-radius:999;"
                + "-fx-padding:7 14;"
                + "-fx-font-size:12px;"
                + "-fx-font-weight:bold;"
                + "-fx-text-fill:#4C5D73;"
        );

        if (remaining > 0) {
            remainingBadge.setText("Còn thiếu " + remaining);
            remainingBadge.setStyle(
                "-fx-background-color:#FFF5E6;"
                    + "-fx-border-color:#F6D7A8;"
                    + "-fx-border-radius:999;"
                    + "-fx-background-radius:999;"
                    + "-fx-padding:7 14;"
                    + "-fx-font-size:12px;"
                    + "-fx-font-weight:bold;"
                    + "-fx-text-fill:#D97706;"
            );
        } else if (remaining < 0) {
            remainingBadge.setText("Vượt " + Math.abs(remaining));
            remainingBadge.setStyle(
                "-fx-background-color:#FEE2E2;"
                    + "-fx-border-color:#F5B7B7;"
                    + "-fx-border-radius:999;"
                    + "-fx-background-radius:999;"
                    + "-fx-padding:7 14;"
                    + "-fx-font-size:12px;"
                    + "-fx-font-weight:bold;"
                    + "-fx-text-fill:#B91C1C;"
            );
        } else {
            remainingBadge.setText("Đã đủ");
            remainingBadge.setStyle(
                "-fx-background-color:#E8F5E9;"
                    + "-fx-border-color:#BDE1C4;"
                    + "-fx-border-radius:999;"
                    + "-fx-background-radius:999;"
                    + "-fx-padding:7 14;"
                    + "-fx-font-size:12px;"
                    + "-fx-font-weight:bold;"
                    + "-fx-text-fill:#2E7D32;"
            );
        }
    }

    private List<SuggestedPlan> buildSuggestedPlans(int limit) {
        if (items.isEmpty()) {
            return List.of();
        }

        List<List<ItemVariant>> variantsByItem = new ArrayList<>();
        for (ItemReq item : items) {
            List<ItemVariant> variants = buildItemVariants(item, MAX_ITEM_VARIANTS);
            if (variants.isEmpty()) {
                return List.of();
            }
            variantsByItem.add(variants);
        }

        long maxAttempts = 1;
        for (List<ItemVariant> variants : variantsByItem) {
            maxAttempts = Math.min((long) MAX_COMBINATION_ATTEMPTS, maxAttempts * variants.size());
        }
        maxAttempts = Math.min((long) MAX_COMBINATION_ATTEMPTS, Math.max(limit * 12L, maxAttempts));

        List<SuggestedPlan> plans = new ArrayList<>();
        Set<String> seenPlanSignatures = new LinkedHashSet<>();
        for (long variantIndex = 0; variantIndex < maxAttempts; variantIndex++) {
            SuggestedPlan plan = buildPlanFromVariantIndex(variantsByItem, variantIndex);
            if (plan != null && seenPlanSignatures.add(plan.signature())) {
                plans.add(plan);
            }
        }

        plans.sort(Comparator
            .comparingInt(SuggestedPlan::siteCount)
            .thenComparingInt(SuggestedPlan::totalDeliveryDays)
            .thenComparingInt(SuggestedPlan::totalLineCount)
            .thenComparing(SuggestedPlan::signature));

        return plans.size() > limit ? plans.subList(0, limit) : plans;
    }

    private List<ItemVariant> buildItemVariants(ItemReq item, int limit) {
        List<SiteInfo> candidateSites = buildCandidateSites(item);
        if (candidateSites.isEmpty()) {
            return List.of();
        }

        int totalStock = candidateSites.stream()
            .mapToInt(site -> site.stock.getOrDefault(item.merchandiseId, 0))
            .sum();
        if (totalStock < item.required) {
            return List.of();
        }

        List<List<SiteInfo>> orderings = buildSiteOrderings(item, candidateSites);
        List<ItemVariant> variants = new ArrayList<>();
        Set<String> seenSignatures = new LinkedHashSet<>();
        for (List<SiteInfo> ordering : orderings) {
            collectItemVariants(
                item,
                ordering,
                0,
                item.required,
                new LinkedHashMap<>(),
                variants,
                seenSignatures,
                limit
            );
            if (variants.size() >= limit) {
                break;
            }
        }

        variants.sort(Comparator
            .comparingInt(ItemVariant::siteCount)
            .thenComparingInt(ItemVariant::totalDeliveryDays)
            .thenComparing(ItemVariant::signature));
        return variants;
    }

    private List<SiteInfo> buildCandidateSites(ItemReq item) {
        return allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
            .filter(site -> pickSuggestedTransport(site) != null)
            .sorted(buildSiteComparator(item))
            .toList();
    }

    private Comparator<SiteInfo> buildSiteComparator(ItemReq item) {
        return Comparator
            .comparingInt(this::bestFeasibleDeliveryDays)
            .thenComparing(Comparator.comparingInt((SiteInfo site) -> site.stock.getOrDefault(item.merchandiseId, 0)).reversed())
            .thenComparingInt(site -> site.id);
    }

    private List<List<SiteInfo>> buildSiteOrderings(ItemReq item, List<SiteInfo> candidateSites) {
        List<List<SiteInfo>> orderings = new ArrayList<>();
        Set<String> seenOrderings = new LinkedHashSet<>();

        addOrdering(candidateSites, orderings, seenOrderings);
        for (int rotation = 1; rotation < candidateSites.size(); rotation++) {
            addOrdering(rotate(candidateSites, rotation), orderings, seenOrderings);
        }

        List<SiteInfo> reversed = new ArrayList<>(candidateSites);
        Collections.reverse(reversed);
        addOrdering(reversed, orderings, seenOrderings);
        for (int rotation = 1; rotation < reversed.size(); rotation++) {
            addOrdering(rotate(reversed, rotation), orderings, seenOrderings);
        }

        List<SiteInfo> stockAscending = new ArrayList<>(candidateSites);
        stockAscending.sort(Comparator
            .comparingInt((SiteInfo site) -> site.stock.getOrDefault(item.merchandiseId, 0))
            .thenComparingInt(this::bestFeasibleDeliveryDays)
            .thenComparingInt(site -> site.id));
        addOrdering(stockAscending, orderings, seenOrderings);
        for (int rotation = 1; rotation < stockAscending.size(); rotation++) {
            addOrdering(rotate(stockAscending, rotation), orderings, seenOrderings);
        }

        return orderings;
    }

    private void addOrdering(List<SiteInfo> ordering, List<List<SiteInfo>> target, Set<String> seenOrderings) {
        String signature = ordering.stream()
            .map(site -> String.valueOf(site.id))
            .reduce((left, right) -> left + "|" + right)
            .orElse("");
        if (seenOrderings.add(signature)) {
            target.add(List.copyOf(ordering));
        }
    }

    private List<SiteInfo> rotate(List<SiteInfo> sites, int offset) {
        if (sites.isEmpty()) {
            return List.of();
        }
        int rotation = offset % sites.size();
        List<SiteInfo> rotated = new ArrayList<>(sites.size());
        rotated.addAll(sites.subList(rotation, sites.size()));
        rotated.addAll(sites.subList(0, rotation));
        return rotated;
    }

    private void collectItemVariants(
        ItemReq item,
        List<SiteInfo> orderedSites,
        int siteIndex,
        int remaining,
        LinkedHashMap<Integer, AllocationDraft> current,
        List<ItemVariant> variants,
        Set<String> seenSignatures,
        int limit
    ) {
        if (variants.size() >= limit) {
            return;
        }
        if (remaining == 0) {
            ItemVariant variant = buildItemVariant(current);
            if (variant != null && seenSignatures.add(variant.signature())) {
                variants.add(variant);
            }
            return;
        }
        if (siteIndex >= orderedSites.size()) {
            return;
        }

        int remainingCapacity = calculateRemainingCapacity(item, orderedSites, siteIndex);
        if (remainingCapacity < remaining) {
            return;
        }

        SiteInfo site = orderedSites.get(siteIndex);
        int stock = site.stock.getOrDefault(item.merchandiseId, 0);
        int restCapacity = calculateRemainingCapacity(item, orderedSites, siteIndex + 1);
        int minTake = Math.max(0, remaining - restCapacity);
        int maxTake = Math.min(stock, remaining);
        if (maxTake < minTake) {
            return;
        }

        String transport = pickSuggestedTransport(site);
        if (transport == null) {
            collectItemVariants(item, orderedSites, siteIndex + 1, remaining, current, variants, seenSignatures, limit);
            return;
        }

        for (int quantity : buildQuantityChoices(minTake, maxTake, remaining, orderedSites.size() - siteIndex)) {
            if (quantity > 0) {
                current.put(site.id, new AllocationDraft(site.id, item.merchandiseId, quantity, transport));
            }
            collectItemVariants(
                item,
                orderedSites,
                siteIndex + 1,
                remaining - quantity,
                current,
                variants,
                seenSignatures,
                limit
            );
            if (quantity > 0) {
                current.remove(site.id);
            }
            if (variants.size() >= limit) {
                return;
            }
        }
    }

    private int calculateRemainingCapacity(ItemReq item, List<SiteInfo> orderedSites, int startIndex) {
        int total = 0;
        for (int index = startIndex; index < orderedSites.size(); index++) {
            total += orderedSites.get(index).stock.getOrDefault(item.merchandiseId, 0);
        }
        return total;
    }

    private List<Integer> buildQuantityChoices(int minTake, int maxTake, int remaining, int sitesLeft) {
        LinkedHashSet<Integer> choices = new LinkedHashSet<>();
        choices.add(maxTake);
        choices.add(clamp((remaining * 2 + 2) / 3, minTake, maxTake));
        choices.add(clamp((int) Math.ceil((double) remaining / Math.max(1, sitesLeft)), minTake, maxTake));
        choices.add(clamp((minTake + maxTake) / 2, minTake, maxTake));
        choices.add(minTake);
        return new ArrayList<>(choices);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private ItemVariant buildItemVariant(Map<Integer, AllocationDraft> current) {
        if (current.isEmpty()) {
            return null;
        }

        Map<Integer, AllocationDraft> allocationsBySite = new LinkedHashMap<>();
        List<AllocationDraft> drafts = current.values().stream()
            .sorted(Comparator.comparingInt(AllocationDraft::siteId))
            .toList();

        int totalDeliveryDays = 0;
        StringBuilder signature = new StringBuilder();
        for (AllocationDraft draft : drafts) {
            allocationsBySite.put(draft.siteId(), draft);
            SiteInfo site = findSiteById(draft.siteId());
            if (site != null) {
                totalDeliveryDays += getDeliveryDays(site, draft.transport());
            }
            if (!signature.isEmpty()) {
                signature.append('|');
            }
            signature.append(draft.siteId())
                .append(':')
                .append(draft.quantity())
                .append(':')
                .append(draft.transport());
        }

        return new ItemVariant(allocationsBySite, allocationsBySite.size(), totalDeliveryDays, signature.toString());
    }

    private SuggestedPlan buildPlanFromVariantIndex(List<List<ItemVariant>> variantsByItem, long variantIndex) {
        Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem = new LinkedHashMap<>();
        long workingIndex = variantIndex;

        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            List<ItemVariant> variants = variantsByItem.get(itemIndex);
            if (variants.isEmpty()) {
                return null;
            }

            int selectedIndex = (int) (workingIndex % variants.size());
            workingIndex /= variants.size();

            ItemVariant variant = variants.get(selectedIndex);
            Map<Integer, AllocationDraft> itemAllocations = new LinkedHashMap<>();
            for (AllocationDraft draft : variant.allocationsBySite().values()) {
                itemAllocations.put(draft.siteId(), draft);
            }
            allocationsByItem.put(items.get(itemIndex).merchandiseId, itemAllocations);
        }

        return buildSuggestedPlan(allocationsByItem);
    }

    private SuggestedPlan buildSuggestedPlan(Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem) {
        Map<Integer, MutableSiteOrder> siteOrdersById = new LinkedHashMap<>();
        int totalQuantity = 0;
        int totalLines = 0;
        int totalDeliveryDays = 0;
        int prioritySiteCount = 0;
        Set<Integer> countedPrioritySites = new LinkedHashSet<>();
        StringBuilder signature = new StringBuilder();

        for (ItemReq item : items) {
            Map<Integer, AllocationDraft> itemAllocations = allocationsByItem.getOrDefault(item.merchandiseId, Collections.emptyMap());
            List<AllocationDraft> drafts = itemAllocations.values().stream()
                .sorted(Comparator.comparingInt(AllocationDraft::siteId))
                .toList();
            for (AllocationDraft draft : drafts) {
                SiteInfo site = findSiteById(draft.siteId());
                if (site == null) {
                    continue;
                }

                int deliveryDays = getDeliveryDays(site, draft.transport());
                OrderLineSuggestion line = new OrderLineSuggestion(item, draft.quantity(), draft.transport(), deliveryDays);
                MutableSiteOrder siteOrder = siteOrdersById.computeIfAbsent(site.id, key -> new MutableSiteOrder(site));
                siteOrder.lines.add(line);
                siteOrder.totalQuantity += draft.quantity();
                siteOrder.deliveryDays = Math.max(siteOrder.deliveryDays, deliveryDays);
                siteOrder.transports.add(draft.transport());

                totalQuantity += draft.quantity();
                totalLines++;
                totalDeliveryDays += deliveryDays;

                if (prioritySiteIds.contains(site.id) && countedPrioritySites.add(site.id)) {
                    prioritySiteCount++;
                }

                if (!signature.isEmpty()) {
                    signature.append('|');
                }
                signature.append(item.merchandiseId)
                    .append('@')
                    .append(draft.siteId())
                    .append(':')
                    .append(draft.quantity())
                    .append(':')
                    .append(draft.transport());
            }
        }

        List<SiteOrderSuggestion> siteOrders = siteOrdersById.values().stream()
            .map(this::toSiteOrderSuggestion)
            .sorted(Comparator
                .comparingInt(SiteOrderSuggestion::totalQuantity).reversed()
                .thenComparing(siteOrder -> siteOrder.site().name))
            .toList();

        return new SuggestedPlan(
            allocationsByItem,
            siteOrders,
            totalQuantity,
            totalLines,
            siteOrders.size(),
            prioritySiteCount,
            totalDeliveryDays,
            signature.toString()
        );
    }

    private SiteOrderSuggestion toSiteOrderSuggestion(MutableSiteOrder siteOrder) {
        List<OrderLineSuggestion> lines = siteOrder.lines.stream()
            .sorted(Comparator.comparing(line -> line.item().code))
            .toList();
        String transportSummary;
        if (siteOrder.transports.size() == 1) {
            transportSummary = transportLabel(siteOrder.transports.iterator().next());
        } else {
            transportSummary = "Nhiều cách";
        }
        return new SiteOrderSuggestion(siteOrder.site, lines, siteOrder.totalQuantity, siteOrder.deliveryDays, transportSummary);
    }

    private void applySuggestedPlan(SuggestedPlan plan) {
        clearCurrentAllocations();

        for (Map.Entry<Integer, Map<Integer, AllocationDraft>> itemEntry : plan.allocationsByItem().entrySet()) {
            Map<Integer, Allocation> targetAllocations = allocations.computeIfAbsent(itemEntry.getKey(), key -> new LinkedHashMap<>());
            for (AllocationDraft draft : itemEntry.getValue().values()) {
                targetAllocations.put(
                    draft.siteId(),
                    new Allocation(draft.siteId(), draft.merchandiseId(), draft.quantity(), draft.transport())
                );
            }
        }

        if (allocationPlanBox != null && allocationPlanBox.getParent() instanceof VBox) {
            rebuildSection();
        }
        for (int index = 0; index < items.size(); index++) {
            updateItemFractionLabel(items.get(index), index);
        }
        notifyAllocationChanged();
        if (onPlanApplied != null) {
            onPlanApplied.run();
        }
    }

    private void clearCurrentAllocations() {
        for (ItemReq item : items) {
            allocations.computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>()).clear();
        }
    }

    private int countAvailableSites(ItemReq item) {
        return (int) allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
            .count();
    }

    private Label buildColumnHeader(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#6B7F95;");
        return label;
    }

    private void updateEtaBadge(Label badge, SiteInfo site, String transport) {
        int deliveryDays = getDeliveryDays(site, transport);
        if (deliveryDays >= 999) {
            badge.setText("Không khả dụng");
            badge.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 14; -fx-background-radius:999; -fx-background-color:#F0F4F2; -fx-text-fill:#6B7C72;");
            return;
        }

        int delta = deadlineDays - deliveryDays;
        if (delta > 0) {
            badge.setText("Sớm " + delta + " ngày");
            badge.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 14; -fx-background-radius:999; -fx-background-color:#DCFCE7; -fx-text-fill:#3F7A22;");
        } else if (delta == 0) {
            badge.setText("Kịp hạn");
            badge.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 14; -fx-background-radius:999; -fx-background-color:#E0F2FE; -fx-text-fill:#0369A1;");
        } else {
            badge.setText("Trễ " + Math.abs(delta) + " ngày");
            badge.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 14; -fx-background-radius:999; -fx-background-color:#FEE2E2; -fx-text-fill:#DC2626;");
        }
    }

    private int bestFeasibleDeliveryDays(SiteInfo site) {
        int best = 999;
        if (site.shipDays <= deadlineDays && site.shipDays < best) {
            best = site.shipDays;
        }
        if (site.airDays <= deadlineDays && site.airDays < best) {
            best = site.airDays;
        }
        return best;
    }

    private int getDeliveryDays(SiteInfo site, String transport) {
        return TRANSPORT_AIR.equals(transport) ? site.airDays : site.shipDays;
    }

    private String pickSuggestedTransport(SiteInfo site) {
        if (site.shipDays <= deadlineDays && site.shipDays < 999) {
            return TRANSPORT_SHIP;
        }
        if (site.airDays <= deadlineDays && site.airDays < 999) {
            return TRANSPORT_AIR;
        }
        return null;
    }

    private String pickDefaultTransport(SiteInfo site) {
        if (site.shipDays <= deadlineDays && site.shipDays < 999) {
            return TRANSPORT_SHIP;
        }
        if (site.airDays < 999) {
            return TRANSPORT_AIR;
        }
        return TRANSPORT_SHIP;
    }

    private String normalizeTransport(String rawTransport, SiteInfo site) {
        if (rawTransport == null || rawTransport.isBlank()) {
            return pickDefaultTransport(site);
        }

        String normalized = rawTransport.trim().toLowerCase();
        if (normalized.contains("air") || normalized.contains("máy") || normalized.contains("hang khong") || normalized.contains("hàng không")) {
            return TRANSPORT_AIR;
        }
        if (normalized.contains("ship") || normalized.contains("tàu") || normalized.contains("duong bien") || normalized.contains("đường biển")) {
            return TRANSPORT_SHIP;
        }

        return pickDefaultTransport(site);
    }

    private String transportLabel(String transport) {
        return TRANSPORT_AIR.equals(transport) ? "Hàng không" : "Đường biển";
    }

    private SiteInfo findSiteById(int siteId) {
        for (SiteInfo site : allSites) {
            if (site.id == siteId) {
                return site;
            }
        }
        return null;
    }

    private void showWarning(Label warningLabel, String message) {
        warningLabel.setText(message);
        warningLabel.setVisible(true);
        warningLabel.setManaged(true);
    }

    private void hideWarning(Label warningLabel) {
        warningLabel.setText("");
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
    }

    private void notifyAllocationChanged() {
        if (onAllocationChanged != null) {
            onAllocationChanged.run();
        }
    }

    private record AllocationDraft(int siteId, int merchandiseId, int quantity, String transport) {
    }

    private record ItemVariant(
        Map<Integer, AllocationDraft> allocationsBySite,
        int siteCount,
        int totalDeliveryDays,
        String signature
    ) {
    }

    private record OrderLineSuggestion(ItemReq item, int quantity, String transport, int deliveryDays) {
    }

    private record SiteOrderSuggestion(
        SiteInfo site,
        List<OrderLineSuggestion> lines,
        int totalQuantity,
        int deliveryDays,
        String transportSummary
    ) {
    }

    private record SuggestedPlan(
        Map<Integer, Map<Integer, AllocationDraft>> allocationsByItem,
        List<SiteOrderSuggestion> siteOrders,
        int totalQuantity,
        int totalLineCount,
        int siteCount,
        int prioritySiteCount,
        int totalDeliveryDays,
        String signature
    ) {
    }

    private static final class MutableSiteOrder {
        private final SiteInfo site;
        private final List<OrderLineSuggestion> lines = new ArrayList<>();
        private final Set<String> transports = new LinkedHashSet<>();
        private int totalQuantity;
        private int deliveryDays;

        private MutableSiteOrder(SiteInfo site) {
            this.site = site;
        }
    }
}
