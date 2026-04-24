package org.itss.prj_itss.request.processing;

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

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.request.processing.AllocationPlanner.OrderLineSuggestion;
import org.itss.prj_itss.request.processing.AllocationPlanner.SiteOrderSuggestion;
import org.itss.prj_itss.request.processing.AllocationPlanner.SuggestedPlan;
import org.itss.prj_itss.service.AllocationPlanningService;
import org.itss.prj_itss.ui.Notifications;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllocationSection {

    private static final int MAX_SUGGESTED_PLANS = 10;
    private static final int MAX_ITEM_VARIANTS = 12;
    private static final List<String> FRACTION_STATE_CLASSES = List.of(
        "allocation-fraction-muted",
        "allocation-fraction-over",
        "allocation-fraction-complete",
        "allocation-fraction-partial"
    );
    private static final List<String> SUMMARY_STATE_CLASSES = List.of(
        "allocation-summary-short",
        "allocation-summary-over",
        "allocation-summary-complete"
    );
    private static final List<String> ETA_STATE_CLASSES = List.of(
        "allocation-eta-unavailable",
        "allocation-eta-early",
        "allocation-eta-on-time",
        "allocation-eta-late"
    );

    private final List<ItemRequirement> items;
    private final List<SiteStockOption> allSites;
    private final Set<Integer> excludedSiteIds;
    private final Set<Integer> prioritySiteIds;
    private final Map<Integer, Map<Integer, Allocation>> allocations;
    private final int deadlineDays;
    private final AllocationPlanMutator planMutator;

    private VBox allocationPlanBox;
    private Label[] allocFractionLabels;
    private Runnable onAllocationChanged;
    private Runnable onPlanApplied;

    public AllocationSection(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
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
        this.planMutator = new AllocationPlanMutator(items, allocations);
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
            .mapToInt(Allocation::getQuantity)
            .sum();
    }

    public VBox buildWrapper() {
        VBox wrapper = new VBox(0);
        allocationPlanBox = buildSection();
        wrapper.getChildren().add(allocationPlanBox);
        return wrapper;
    }

    public VBox buildInlineEditor(ItemRequirement item, int itemIndex) {
        VBox section = new VBox(16);
        section.setPadding(new Insets(22, 20, 18, 20));
        RequestProcessingUiSupport.addStyleClass(section, "allocation-inline-editor");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        Label sectionLabel = new Label("TỒN KHO VÀ PHÂN BỔ THEO SITE");
        RequestProcessingUiSupport.addStyleClass(sectionLabel, "allocation-eyebrow-muted");

        Label titleLabel = new Label(item.code + " - " + item.name);
        RequestProcessingUiSupport.addStyleClass(titleLabel, "allocation-title");

        Label subtitleLabel = new Label("Có mặt hàng tại " + countAvailableSites(item) + " site");
        RequestProcessingUiSupport.addStyleClass(subtitleLabel, "allocation-subtitle");
        textBox.getChildren().addAll(sectionLabel, titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label allocatedBadge = new Label();
        Label remainingBadge = new Label();
        Runnable refreshSummary = () -> updateSummaryBadges(item, allocatedBadge, remainingBadge);
        refreshSummary.run();

        header.getChildren().addAll(textBox, spacer, allocatedBadge, remainingBadge);

        VBox table = new VBox(0);
        RequestProcessingUiSupport.addStyleClass(table, "allocation-table");
        table.getChildren().add(buildInlineTableHeader());

        boolean hasRows = false;
        for (SiteStockOption site : allSites) {
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
            RequestProcessingUiSupport.addStyleClass(emptyLabel, "allocation-empty-label");
            table.getChildren().add(emptyLabel);
        }

        section.getChildren().addAll(header, table);
        return section;
    }

    public void applyOptimalAllocation() {
        planMutator.applyOptimalAllocation(createPlanner());
        refreshAfterPlanChange(false);
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
        List<SuggestedPlan> suggestedPlans = createPlanner().buildSuggestedPlans(MAX_SUGGESTED_PLANS, MAX_ITEM_VARIANTS);

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Các phương án phân bổ thỏa mãn");
        dialog.setResizable(true);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(960);
        root.setPrefHeight(720);
        RequestProcessingUiSupport.addStyleClass(root, "allocation-dialog-root");

        Label titleLabel = new Label("Các phương án phân bổ thỏa mãn");
        RequestProcessingUiSupport.addStyleClass(titleLabel, "allocation-dialog-title");

        String subtitleText = suggestedPlans.isEmpty()
            ? "Không tìm được phương án đáp ứng đủ số lượng và thời hạn hiện tại."
            : "Hiển thị " + suggestedPlans.size() + " phương án khác nhau theo từng đơn hàng gửi tới site.";
        Label subtitleLabel = new Label(subtitleText);
        subtitleLabel.setWrapText(true);
        RequestProcessingUiSupport.addStyleClass(subtitleLabel, "allocation-subtitle");

        VBox plansBox = new VBox(14);
        if (suggestedPlans.isEmpty()) {
            VBox emptyCard = new VBox(8);
            emptyCard.setPadding(new Insets(18));
            RequestProcessingUiSupport.addStyleClass(emptyCard, "allocation-table");

            Label emptyTitle = new Label("Chưa có phương án thỏa mãn");
            RequestProcessingUiSupport.addStyleClass(emptyTitle, "allocation-card-title");

            Label emptyText = new Label("Kiểm tra lại site bị loại bỏ, số lượng tồn kho hoặc ngày giao yêu cầu.");
            emptyText.setWrapText(true);
            RequestProcessingUiSupport.addStyleClass(emptyText, "allocation-subtitle");

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
        RequestProcessingUiSupport.addStyleClass(scrollPane, "transparent-scroll-pane");

        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().add("forest-secondary-button");
        closeButton.setOnAction(event -> dialog.close());

        HBox footer = new HBox(closeButton);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titleLabel, subtitleLabel, scrollPane, footer);

        Scene scene = new Scene(root);
        RequestProcessingUiSupport.applyMainStylesheet(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public void updateItemFractionLabel(ItemRequirement item, int index) {
        if (allocFractionLabels == null || index >= allocFractionLabels.length || allocFractionLabels[index] == null) {
            return;
        }

        int allocated = getAllocated(item.merchandiseId);
        String stateClass = "allocation-fraction-muted";
        if (allocated > item.required) {
            stateClass = "allocation-fraction-over";
        } else if (allocated == item.required) {
            stateClass = "allocation-fraction-complete";
        } else if (allocated > 0) {
            stateClass = "allocation-fraction-partial";
        }

        allocFractionLabels[index].setText(allocated + "/" + item.required);
        RequestProcessingUiSupport.addStyleClass(allocFractionLabels[index], "allocation-fraction-label");
        RequestProcessingUiSupport.setStateClass(allocFractionLabels[index], FRACTION_STATE_CLASSES, stateClass);
    }

    private AllocationPlanner createPlanner() {
        return new AllocationPlanner(items, allSites, excludedSiteIds, prioritySiteIds, deadlineDays);
    }

    private VBox buildSection() {
        VBox card = new VBox(16);
        card.getStyleClass().add("forest-card");
        card.setPadding(new Insets(20));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(3);
        Label sectionLabel = new Label("PHÂN BỔ ĐƠN HÀNG");
        RequestProcessingUiSupport.addStyleClass(sectionLabel, "allocation-eyebrow");
        Label titleLabel = new Label("Phân bổ số lượng cho từng site");
        RequestProcessingUiSupport.addStyleClass(titleLabel, "allocation-title");
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
        RequestProcessingUiSupport.addStyleClass(header, "allocation-table-header");
        header.getChildren().addAll(
            buildColumnHeader("SITE", 380),
            buildColumnHeader("TỒN KHO", 100),
            buildColumnHeader("SL PHÂN BỔ", 170),
            buildColumnHeader("VẬN CHUYỂN", 180),
            buildColumnHeader("TRẠNG THÁI", 120)
        );
        return header;
    }

    private VBox buildSiteAllocationRow(ItemRequirement item, int itemIndex, SiteStockOption site, Runnable refreshSummary) {
        VBox rowBox = new VBox(4);
        rowBox.setPadding(new Insets(0, 0, 0, 0));
        RequestProcessingUiSupport.addStyleClass(rowBox, "allocation-table-row");

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));

        VBox siteBox = new VBox(4);
        siteBox.setMinWidth(380);
        siteBox.setPrefWidth(380);

        Label siteNameLabel = new Label(site.name);
        RequestProcessingUiSupport.addStyleClass(siteNameLabel, "allocation-site-name");

        String detailText = site.siteCode;
        if (site.description != null && !site.description.isBlank()) {
            detailText += " - " + site.description;
        }
        Label siteDetailLabel = new Label(detailText);
        RequestProcessingUiSupport.addStyleClass(siteDetailLabel, "allocation-site-detail");
        siteBox.getChildren().addAll(siteNameLabel, siteDetailLabel);

        int stock = site.stock.getOrDefault(item.merchandiseId, 0);

        Label stockLabel = new Label(String.valueOf(stock));
        stockLabel.setMinWidth(100);
        stockLabel.setPrefWidth(100);
        RequestProcessingUiSupport.addStyleClass(stockLabel, "allocation-stock-label");

        Allocation existing = allocations.getOrDefault(item.merchandiseId, Collections.emptyMap()).get(site.id);
        String selectedTransport = AllocationTransportSupport.normalizeTransport(
            existing == null ? null : existing.transport,
            site,
            deadlineDays
        );

        TextField quantityField = new TextField();
        quantityField.setPrefWidth(110);
        quantityField.setText(existing == null || existing.getQuantity() == 0 ? "0" : String.valueOf(existing.getQuantity()));
        RequestProcessingUiSupport.addStyleClass(quantityField, "allocation-quantity-field");

        Label unitLabel = new Label("chiếc");
        RequestProcessingUiSupport.addStyleClass(unitLabel, "allocation-unit-label");
        HBox quantityBox = new HBox(8, quantityField, unitLabel);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        quantityBox.setMinWidth(170);
        quantityBox.setPrefWidth(170);

        ComboBox<String> transportBox = new ComboBox<>();
        if (site.shipDays < 999) {
            transportBox.getItems().add(AllocationTransportSupport.transportLabel(AllocationPlanningService.TRANSPORT_SHIP));
        }
        if (site.airDays < 999) {
            transportBox.getItems().add(AllocationTransportSupport.transportLabel(AllocationPlanningService.TRANSPORT_AIR));
        }
        if (transportBox.getItems().isEmpty()) {
            transportBox.getItems().add("Không khả dụng");
            transportBox.setDisable(true);
        } else {
            transportBox.setValue(AllocationTransportSupport.transportLabel(selectedTransport));
        }
        transportBox.setPrefWidth(180);
        transportBox.setMinWidth(180);
        RequestProcessingUiSupport.addStyleClass(transportBox, "allocation-transport-box");

        Label etaBadge = new Label();
        etaBadge.setMinWidth(120);
        RequestProcessingUiSupport.addStyleClass(etaBadge, "allocation-eta-badge");
        updateEtaBadge(etaBadge, site, selectedTransport);

        Label warningLabel = new Label();
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
        RequestProcessingUiSupport.addStyleClass(warningLabel, "allocation-warning-label");

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
        RequestProcessingUiSupport.addStyleClass(card, "allocation-plan-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBox = new VBox(6);
        Label titleLabel = new Label("Phương án " + String.format("%02d", number));
        RequestProcessingUiSupport.addStyleClass(titleLabel, "allocation-title");

        Label summaryLabel = new Label(
            plan.siteOrders().size() + " site"
                + " • " + plan.totalLineCount() + " dòng đặt hàng"
                + " • " + plan.totalQuantity() + " chiếc"
        );
        RequestProcessingUiSupport.addStyleClass(summaryLabel, "allocation-subtitle");
        titleBox.getChildren().addAll(titleLabel, summaryLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox tagRow = new HBox(8);
        tagRow.setAlignment(Pos.CENTER_RIGHT);
        tagRow.getChildren().addAll(
            buildMetricTag("Đủ số lượng", "allocation-metric-success"),
            buildMetricTag("Kịp ngày nhận", "allocation-metric-info")
        );

        Button applyButton = new Button("Áp dụng phương án này");
        RequestProcessingUiSupport.addStyleClass(applyButton, "allocation-apply-plan-button");
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
        RequestProcessingUiSupport.addStyleClass(card, "allocation-site-order-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox siteBox = new VBox(4);
        Label siteNameLabel = new Label(siteOrder.site().name);
        RequestProcessingUiSupport.addStyleClass(siteNameLabel, "allocation-card-title");

        Label siteMetaLabel = new Label(
            siteOrder.site().siteCode
                + " • " + siteOrder.lines().size() + " mặt hàng"
                + " • " + siteOrder.totalQuantity() + " chiếc"
        );
        RequestProcessingUiSupport.addStyleClass(siteMetaLabel, "allocation-subtitle");
        siteBox.getChildren().addAll(siteNameLabel, siteMetaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox tagRow = new HBox(8);
        tagRow.setAlignment(Pos.CENTER_RIGHT);
        tagRow.getChildren().addAll(
            buildMetricTag(siteOrder.transportSummary(), "allocation-metric-info"),
            buildMetricTag("ETA " + siteOrder.deliveryDays() + " ngày", "allocation-metric-success")
        );

        header.getChildren().addAll(siteBox, spacer, tagRow);

        VBox table = new VBox(0);
        RequestProcessingUiSupport.addStyleClass(table, "allocation-suggested-table");
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
        RequestProcessingUiSupport.addStyleClass(header, "allocation-suggested-table-header");
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
        RequestProcessingUiSupport.addStyleClass(row, "allocation-table-row");

        Label codeLabel = buildValueCell(line.item().code, 130, true);
        Label nameLabel = buildValueCell(line.item().name, 280, false);
        Label qtyLabel = buildValueCell(line.quantity() + " chiếc", 120, true);
        Label transportLabel = buildValueCell(AllocationTransportSupport.transportLabel(line.transport()), 150, false);

        row.getChildren().addAll(codeLabel, nameLabel, qtyLabel, transportLabel);
        return row;
    }

    private Label buildMetricTag(String text, String modifierClass) {
        Label label = new Label(text);
        RequestProcessingUiSupport.addStyleClass(label, "allocation-metric-tag", modifierClass);
        return label;
    }

    private Label buildValueCell(String text, double width, boolean emphasize) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setWrapText(true);
        RequestProcessingUiSupport.addStyleClass(label, "allocation-value-cell");
        if (emphasize) {
            RequestProcessingUiSupport.addStyleClass(label, "allocation-value-cell-emphasis");
        }
        return label;
    }

    private void applyAllocationChange(
        ItemRequirement item,
        int itemIndex,
        SiteStockOption site,
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

        String transport = AllocationTransportSupport.normalizeTransport(transportBox.getValue(), site, deadlineDays);
        updateEtaBadge(etaBadge, site, transport);

        if (quantity > 0) {
            Allocation allocation = allocations
                .computeIfAbsent(item.merchandiseId, key -> new java.util.LinkedHashMap<>())
                .computeIfAbsent(site.id, key -> new Allocation(site.id, item.merchandiseId, 0, transport));
            allocation.setQuantity(quantity);
            allocation.transport = transport;
        } else {
            Map<Integer, Allocation> itemAllocations = allocations.get(item.merchandiseId);
            if (itemAllocations != null) {
                itemAllocations.remove(site.id);
            }
        }

        onUpdated.run();
    }

    private void updateSummaryBadges(ItemRequirement item, Label allocatedBadge, Label remainingBadge) {
        int allocated = getAllocated(item.merchandiseId);
        int remaining = item.required - allocated;

        allocatedBadge.setText("Đã phân bổ " + allocated + "/" + item.required);
        RequestProcessingUiSupport.addStyleClass(allocatedBadge, "allocation-summary-badge", "allocation-summary-allocated");

        if (remaining > 0) {
            remainingBadge.setText("Còn thiếu " + remaining);
            RequestProcessingUiSupport.addStyleClass(remainingBadge, "allocation-summary-badge");
            RequestProcessingUiSupport.setStateClass(remainingBadge, SUMMARY_STATE_CLASSES, "allocation-summary-short");
        } else if (remaining < 0) {
            remainingBadge.setText("Vượt " + Math.abs(remaining));
            RequestProcessingUiSupport.addStyleClass(remainingBadge, "allocation-summary-badge");
            RequestProcessingUiSupport.setStateClass(remainingBadge, SUMMARY_STATE_CLASSES, "allocation-summary-over");
        } else {
            remainingBadge.setText("Đã đủ");
            RequestProcessingUiSupport.addStyleClass(remainingBadge, "allocation-summary-badge");
            RequestProcessingUiSupport.setStateClass(remainingBadge, SUMMARY_STATE_CLASSES, "allocation-summary-complete");
        }
    }

    private void applySuggestedPlan(SuggestedPlan plan) {
        planMutator.applySuggestedPlan(plan);
        refreshAfterPlanChange(true);
    }

    private int countAvailableSites(ItemRequirement item) {
        return (int) allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
            .count();
    }

    private Label buildColumnHeader(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        RequestProcessingUiSupport.addStyleClass(label, "allocation-column-header");
        return label;
    }

    private void updateEtaBadge(Label badge, SiteStockOption site, String transport) {
        int deliveryDays = AllocationTransportSupport.getDeliveryDays(site, transport);
        if (deliveryDays >= 999) {
            badge.setText("Không khả dụng");
            RequestProcessingUiSupport.setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-unavailable");
            return;
        }

        int delta = deadlineDays - deliveryDays;
        if (delta > 0) {
            badge.setText("Sớm " + delta + " ngày");
            RequestProcessingUiSupport.setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-early");
        } else if (delta == 0) {
            badge.setText("Kịp hạn");
            RequestProcessingUiSupport.setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-on-time");
        } else {
            badge.setText("Trễ " + Math.abs(delta) + " ngày");
            RequestProcessingUiSupport.setStateClass(badge, ETA_STATE_CLASSES, "allocation-eta-late");
        }
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

    private void refreshAfterPlanChange(boolean notifyPlanApplied) {
        if (allocationPlanBox != null && allocationPlanBox.getParent() instanceof VBox) {
            rebuildSection();
        }
        for (int index = 0; index < items.size(); index++) {
            updateItemFractionLabel(items.get(index), index);
        }
        notifyAllocationChanged();
        if (notifyPlanApplied && onPlanApplied != null) {
            onPlanApplied.run();
        }
    }
}
