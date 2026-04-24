package org.itss.prj_itss.request.processing.items;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.request.processing.allocation.AllocationSection;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

public final class RequestProcessingItemsSection {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<ItemRequirement> items;
    private final List<SiteStockOption> allSites;
    private final Set<Integer> excludedSiteIds;
    private final AllocationSection allocationSection;
    private final LocalDate earliestDeliveryDate;
    private final int expandedItemIndex;
    private final Runnable onOptimizeRequested;
    private final Runnable onShowAllPlansRequested;
    private final IntConsumer onToggleExpandedItem;

    private Label[] allocationStatusLabels = new Label[0];
    private Label[] allocationFractionLabels = new Label[0];

    public RequestProcessingItemsSection(
        List<ItemRequirement> items,
        List<SiteStockOption> allSites,
        Set<Integer> excludedSiteIds,
        AllocationSection allocationSection,
        LocalDate earliestDeliveryDate,
        int expandedItemIndex,
        Runnable onOptimizeRequested,
        Runnable onShowAllPlansRequested,
        IntConsumer onToggleExpandedItem
    ) {
        this.items = items;
        this.allSites = allSites;
        this.excludedSiteIds = excludedSiteIds;
        this.allocationSection = allocationSection;
        this.earliestDeliveryDate = earliestDeliveryDate;
        this.expandedItemIndex = expandedItemIndex;
        this.onOptimizeRequested = onOptimizeRequested;
        this.onShowAllPlansRequested = onShowAllPlansRequested;
        this.onToggleExpandedItem = onToggleExpandedItem;
    }

    public VBox build() {
        allocationStatusLabels = new Label[items.size()];
        allocationFractionLabels = new Label[items.size()];

        VBox card = new VBox(0);
        card.setStyle(cardStyle());
        card.setPadding(Insets.EMPTY);

        card.getChildren().add(buildTableToolbar());

        HBox header = new HBox();
        header.setPadding(new Insets(11, 20, 11, 20));
        header.setStyle("-fx-background-color:#F5F9F6;-fx-border-color:transparent transparent #E8EEEA transparent;-fx-border-width:0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(
            columnHeader("MÃ HÀNG", 200),
            columnHeader("SỐ LƯỢNG YÊU CẦU", 170),
            columnHeader("NGÀY CẦN GIAO", 150),
            columnHeader("PHÂN BỔ HIỆN TẠI", 180),
            spacer,
            columnHeader("TỒN KHO SITE", 160)
        );

        card.getChildren().add(header);
        for (int index = 0; index < items.size(); index++) {
            card.getChildren().add(buildItemBlock(items.get(index), index));
        }

        return card;
    }

    public Label[] getAllocationFractionLabels() {
        return allocationFractionLabels;
    }

    public void refreshAllocationLabels() {
        for (int index = 0; index < items.size(); index++) {
            updateAllocationLabels(items.get(index), index);
        }
    }

    private HBox buildTableToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(18, 20, 18, 20));
        toolbar.setStyle("-fx-border-color:transparent transparent #EEF3EF transparent;-fx-border-width:0 0 1 0;");

        VBox titleBox = new VBox(3);
        Label sectionLabel = new Label("PHÂN BỔ THEO MẶT HÀNG");
        sectionLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#2E6F40;");
        Label titleLabel = new Label("Điều chỉnh tồn kho theo từng yêu cầu");
        titleLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        titleBox.getChildren().addAll(sectionLabel, titleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button optimizeButton = new Button("Gợi ý tối ưu");
        optimizeButton.getStyleClass().add("request-toolbar-secondary-button");
        optimizeButton.setOnAction(event -> onOptimizeRequested.run());

        Button showAllButton = new Button("Xem tất cả phương án");
        showAllButton.getStyleClass().add("request-toolbar-primary-button");
        showAllButton.setOnAction(event -> onShowAllPlansRequested.run());

        toolbar.getChildren().addAll(titleBox, spacer, optimizeButton, showAllButton);
        return toolbar;
    }

    private VBox buildItemBlock(ItemRequirement item, int index) {
        VBox block = new VBox(0);
        block.getChildren().add(buildItemRow(item, index));
        if (expandedItemIndex == index) {
            block.getChildren().add(allocationSection.buildInlineEditor(item, index));
        }
        return block;
    }

    private HBox buildItemRow(ItemRequirement item, int index) {
        boolean expanded = expandedItemIndex == index;

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle(
            "-fx-border-color:transparent transparent #F0F4F2 transparent;"
                + "-fx-border-width:0 0 1 0;"
        );

        VBox codeColumn = new VBox(4);
        codeColumn.setMinWidth(200);

        Label codeLabel = new Label(item.code);
        codeLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        Label nameLabel = new Label(item.name);
        nameLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#6B7C72;");
        codeColumn.getChildren().addAll(codeLabel, nameLabel);

        Label requiredLabel = new Label(item.required + " chiếc");
        requiredLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;");
        requiredLabel.setMinWidth(170);

        Label deadlineLabel = new Label(earliestDeliveryDate != null ? earliestDeliveryDate.format(DATE_FORMAT) : "N/A");
        deadlineLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;");
        deadlineLabel.setMinWidth(150);

        VBox allocationColumn = new VBox(4);
        allocationColumn.setMinWidth(180);

        Label allocationStatusLabel = new Label();
        Label allocationFractionLabel = new Label();
        allocationStatusLabels[index] = allocationStatusLabel;
        allocationFractionLabels[index] = allocationFractionLabel;
        allocationColumn.getChildren().addAll(allocationStatusLabel, allocationFractionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int totalStock = allSites.stream()
            .filter(site -> !excludedSiteIds.contains(site.id))
            .mapToInt(site -> site.stock.getOrDefault(item.merchandiseId, 0))
            .sum();

        VBox stockColumn = new VBox(10);
        stockColumn.setAlignment(Pos.CENTER_RIGHT);
        stockColumn.setMinWidth(160);

        Label stockValueLabel = new Label(String.valueOf(totalStock));
        stockValueLabel.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");

        Button toggleButton = new Button(expanded ? "Ẩn tồn kho" : "Hiện tồn kho");
        toggleButton.getStyleClass().add(expanded ? "forest-dark-button" : "forest-outline-button");
        toggleButton.setOnAction(event -> onToggleExpandedItem.accept(index));

        stockColumn.getChildren().addAll(stockValueLabel, toggleButton);

        row.getChildren().addAll(codeColumn, requiredLabel, deadlineLabel, allocationColumn, spacer, stockColumn);
        updateAllocationLabels(item, index);
        return row;
    }

    private void updateAllocationLabels(ItemRequirement item, int index) {
        if (index >= allocationStatusLabels.length || index >= allocationFractionLabels.length) {
            return;
        }

        Label stateLabel = allocationStatusLabels[index];
        Label fractionLabel = allocationFractionLabels[index];
        if (stateLabel == null || fractionLabel == null) {
            return;
        }

        int allocated = allocationSection.getAllocated(item.merchandiseId);
        if (allocated > item.required) {
            stateLabel.setText("Vượt mức");
            stateLabel.setStyle("-fx-background-color:#FEE2E2;-fx-text-fill:#B91C1C;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        } else if (allocated == item.required) {
            stateLabel.setText("Đủ");
            stateLabel.setStyle("-fx-background-color:#E8F5E9;-fx-text-fill:#2E7D32;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        } else if (allocated > 0) {
            stateLabel.setText("Chưa đủ");
            stateLabel.setStyle("-fx-background-color:#FFF3E0;-fx-text-fill:#E65100;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        } else {
            stateLabel.setText("Chưa có phương án");
            stateLabel.setStyle("-fx-background-color:#F0F4F2;-fx-text-fill:#6B7C72;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        }

        allocationSection.updateItemFractionLabel(item, index);
        if (fractionLabel.getText() == null || fractionLabel.getText().isBlank()) {
            fractionLabel.setText(allocated + "/" + item.required);
        }
    }

    private String cardStyle() {
        return "-fx-background-color: white; -fx-background-radius: 12;"
            + "-fx-border-radius: 12; -fx-border-color: #E0EBE4; -fx-border-width: 1;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);";
    }

    private Label columnHeader(String text, double width) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        label.setMinWidth(width);
        label.setPrefWidth(width);
        return label;
    }
}
