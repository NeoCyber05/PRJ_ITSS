package org.itss.prj_itss.request;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.itss.prj_itss.common.AppStyles;
import org.itss.prj_itss.common.ToastHelper;
import org.itss.prj_itss.request.RequestModels.Allocation;
import org.itss.prj_itss.request.RequestModels.ItemReq;
import org.itss.prj_itss.request.RequestModels.SiteInfo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllocationSection {

    private final List<ItemReq> items;
    private final List<SiteInfo> allSites;
    private final Set<Integer> excludedSiteIds;
    private final Set<Integer> prioritySiteIds;
    private final Map<Integer, Map<Integer, Allocation>> allocations;
    private final int deadlineDays;

    private VBox allocationPlanBox;
    private Label[] allocFractionLabels;
    private Runnable onAllocationChanged;

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

    private VBox buildSection() {
        VBox card = new VBox(16);
        card.setStyle(AppStyles.cardStyle());
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
        optimizeButton.setStyle(AppStyles.btnSecondary());
        optimizeButton.setOnAction(event -> applyOptimalAllocation());

        Button showAllButton = new Button("Xem tất cả phương án");
        showAllButton.setStyle(AppStyles.btnPrimary());
        showAllButton.setOnAction(event -> showAllAllocationsDialog());

        header.getChildren().addAll(headerText, spacer, optimizeButton, showAllButton);

        VBox inputGrid = new VBox(14);
        for (int index = 0; index < items.size(); index++) {
            inputGrid.getChildren().add(buildAllocInputForItem(items.get(index), index));
        }

        card.getChildren().addAll(header, inputGrid);
        return card;
    }

    private VBox buildAllocInputForItem(ItemReq item, int index) {
        VBox section = new VBox(8);
        section.setStyle("-fx-background-color: #FAFDF9; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #D8E8DD; -fx-border-width: 1; -fx-padding: 14 16;");

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label itemLabel = new Label(item.code + " — " + item.name);
        itemLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int allocated = getAllocated(item.merchandiseId);
        Label remainLabel = new Label("Còn cần: " + Math.max(0, item.required - allocated) + " chiếc");
        remainLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (allocated >= item.required ? "#2E7D32" : "#E65100") + "; -fx-font-weight: bold;");
        titleRow.getChildren().addAll(itemLabel, spacer, remainLabel);

        VBox siteRows = new VBox(6);
        for (SiteInfo site : allSites) {
            if (excludedSiteIds.contains(site.id)) {
                continue;
            }
            int stockQty = site.stock.getOrDefault(item.merchandiseId, 0);
            if (stockQty == 0) {
                continue;
            }
            siteRows.getChildren().add(buildAllocRow(item, index, site));
        }

        if (siteRows.getChildren().isEmpty()) {
            Label emptyLabel = new Label("Không có site nào cung cấp mặt hàng này.");
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #A0B0A6; -fx-padding: 8 0;");
            siteRows.getChildren().add(emptyLabel);
        }

        section.getChildren().addAll(titleRow, siteRows);
        return section;
    }

    private HBox buildAllocRow(ItemReq item, int itemIndex, SiteInfo site) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #EEF3EF; -fx-border-width: 1;");

        Label siteNameLabel = new Label(site.name);
        siteNameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #3A4A40;");
        siteNameLabel.setMinWidth(200);

        ToggleGroup transportGroup = new ToggleGroup();
        boolean canShip = site.shipDays <= deadlineDays;
        boolean canAir = site.airDays <= deadlineDays;

        RadioButton shipRadio = new RadioButton("Tàu (" + site.shipDays + " ngày)");
        shipRadio.setToggleGroup(transportGroup);
        shipRadio.setStyle("-fx-font-size: 12px;");
        shipRadio.setDisable(!canShip);

        RadioButton airRadio = new RadioButton("Bay (" + site.airDays + " ngày)");
        airRadio.setToggleGroup(transportGroup);
        airRadio.setStyle("-fx-font-size: 12px;");
        airRadio.setDisable(!canAir);

        Allocation existing = allocations.getOrDefault(item.merchandiseId, Collections.emptyMap()).get(site.id);
        if (existing != null) {
            if ("Tàu".equals(existing.transport) && canShip) {
                shipRadio.setSelected(true);
            } else if (canAir) {
                airRadio.setSelected(true);
            }
        } else if (canShip) {
            shipRadio.setSelected(true);
        } else if (canAir) {
            airRadio.setSelected(true);
        }

        int stock = site.stock.getOrDefault(item.merchandiseId, 0);
        Label stockLabel = new Label("Tồn: " + stock);
        stockLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8FA899;");

        TextField quantityField = new TextField();
        quantityField.setPrefWidth(80);
        int existingQuantity = existing == null ? 0 : existing.qty.get();
        quantityField.setText(existingQuantity > 0 ? String.valueOf(existingQuantity) : "");
        quantityField.setPromptText("0");
        quantityField.setStyle("-fx-background-color: white; -fx-border-color: #D0DAD5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 10; -fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: center;");

        Label unitLabel = new Label("chiếc");
        unitLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");

        Label warningLabel = new Label();
        warningLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #DC2626;");

        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int quantity = newValue.trim().isEmpty() ? 0 : Integer.parseInt(newValue.trim());
                if (quantity < 0) {
                    warningLabel.setText("Số lượng không âm");
                    return;
                }
                if (quantity > stock) {
                    warningLabel.setText("Vượt tồn kho (" + stock + ")");
                } else {
                    warningLabel.setText("");
                }

                String transport = transportGroup.getSelectedToggle() == airRadio ? "Máy bay" : "Tàu";
                if (quantity > 0) {
                    allocations.computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>())
                        .computeIfAbsent(site.id, key -> new Allocation(site.id, item.merchandiseId, 0, transport));
                    Allocation allocation = allocations.get(item.merchandiseId).get(site.id);
                    allocation.qty.set(quantity);
                    allocation.transport = transport;
                } else {
                    Map<Integer, Allocation> itemAllocations = allocations.get(item.merchandiseId);
                    if (itemAllocations != null) {
                        itemAllocations.remove(site.id);
                    }
                }

                updateItemFractionLabel(item, itemIndex);
                notifyAllocationChanged();
            } catch (NumberFormatException exception) {
                warningLabel.setText("Nhập số");
            }
        });

        transportGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            Map<Integer, Allocation> itemAllocations = allocations.get(item.merchandiseId);
            if (itemAllocations != null) {
                Allocation allocation = itemAllocations.get(site.id);
                if (allocation != null) {
                    allocation.transport = newValue == airRadio ? "Máy bay" : "Tàu";
                }
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(siteNameLabel, stockLabel, shipRadio, airRadio, spacer, quantityField, unitLabel, warningLabel);
        return row;
    }

    public void updateItemFractionLabel(ItemReq item, int index) {
        if (allocFractionLabels == null || index >= allocFractionLabels.length || allocFractionLabels[index] == null) {
            return;
        }

        int allocated = getAllocated(item.merchandiseId);
        boolean done = allocated >= item.required;
        allocFractionLabels[index].setText(allocated + "/" + item.required + " chiếc");
        allocFractionLabels[index].setStyle("-fx-font-size: 13px; -fx-text-fill: " + (done ? "#2E6F40" : "#E65100") + "; -fx-font-weight: bold;");
    }

    public void applyOptimalAllocation() {
        for (ItemReq item : items) {
            allocations.computeIfAbsent(item.merchandiseId, key -> new LinkedHashMap<>()).clear();
        }

        for (int index = 0; index < items.size(); index++) {
            ItemReq item = items.get(index);
            int remaining = item.required;

            List<SiteInfo> sortedSites = allSites.stream()
                .filter(site -> !excludedSiteIds.contains(site.id))
                .filter(site -> site.stock.getOrDefault(item.merchandiseId, 0) > 0)
                .filter(site -> site.shipDays <= deadlineDays || site.airDays <= deadlineDays)
                .sorted((left, right) -> {
                    boolean leftPriority = prioritySiteIds.contains(left.id);
                    boolean rightPriority = prioritySiteIds.contains(right.id);
                    if (leftPriority != rightPriority) {
                        return leftPriority ? -1 : 1;
                    }

                    boolean leftCanShip = left.shipDays <= deadlineDays;
                    boolean rightCanShip = right.shipDays <= deadlineDays;
                    if (leftCanShip != rightCanShip) {
                        return leftCanShip ? -1 : 1;
                    }

                    return right.stock.getOrDefault(item.merchandiseId, 0)
                        - left.stock.getOrDefault(item.merchandiseId, 0);
                })
                .toList();

            for (SiteInfo site : sortedSites) {
                if (remaining <= 0) {
                    break;
                }

                int stock = site.stock.getOrDefault(item.merchandiseId, 0);
                int quantity = Math.min(remaining, stock);
                String transport = site.shipDays <= deadlineDays ? "Tàu" : "Máy bay";
                allocations.get(item.merchandiseId).put(
                    site.id,
                    new Allocation(site.id, item.merchandiseId, quantity, transport)
                );
                remaining -= quantity;
            }
        }

        rebuildSection();
        for (int index = 0; index < items.size(); index++) {
            updateItemFractionLabel(items.get(index), index);
        }
        notifyAllocationChanged();
        ToastHelper.showToast("Đã áp dụng phương án phân bổ tối ưu.");
    }

    public void rebuildSection() {
        VBox parent = (VBox) allocationPlanBox.getParent();
        parent.getChildren().clear();
        allocationPlanBox = buildSection();
        parent.getChildren().add(allocationPlanBox);
    }

    private void showAllAllocationsDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Tất cả phương án phân bổ hợp lệ");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #F5F9F6;");
        root.setPrefWidth(600);

        Label titleLabel = new Label("Các phương án phân bổ hợp lệ");
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");
        Label subtitleLabel = new Label("Hệ thống đã tính toán các phương án phân bổ khả thi:");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");

        VBox plansBox = new VBox(12);
        plansBox.getChildren().add(buildPlanCard(1, "Tối ưu (ít site nhất)", "#2E6F40", true));
        plansBox.getChildren().add(buildPlanCard(2, "Ưu tiên tàu biển", "#1565C0", false));
        plansBox.getChildren().add(buildPlanCard(3, "Giao nhanh (hàng không)", "#6A1B9A", false));

        ScrollPane scrollPane = new ScrollPane(plansBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(360);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button closeButton = new Button("Đóng");
        closeButton.setStyle(AppStyles.btnSecondary());
        closeButton.setOnAction(event -> dialog.close());
        HBox footer = new HBox(closeButton);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titleLabel, subtitleLabel, scrollPane, footer);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private VBox buildPlanCard(int number, String planName, String color, boolean recommended) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: " + color + (recommended ? "" : "44") + "; -fx-border-width: " + (recommended ? "2" : "1") + "; -fx-padding: 14;");

        Label numberLabel = new Label("Phương án " + number + (recommended ? "  ★ Khuyến nghị" : ""));
        numberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label nameLabel = new Label(planName);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7C72;");
        HBox header = new HBox(8, numberLabel, nameLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        Button applyButton = new Button("Áp dụng phương án này");
        applyButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 6 16; -fx-font-weight: bold;");
        applyButton.setOnAction(event -> ToastHelper.showToast("Đã áp dụng " + planName + "."));

        card.getChildren().addAll(header, applyButton);
        return card;
    }

    private void notifyAllocationChanged() {
        if (onAllocationChanged != null) {
            onAllocationChanged.run();
        }
    }
}
