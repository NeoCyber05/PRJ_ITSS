package org.itss.prj_itss.request;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.common.AppStyles;
import org.itss.prj_itss.request.RequestModels.SiteInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SiteFilterSection {

    private final List<SiteInfo> allSites;
    private final ObservableList<SiteInfo> visibleSites;
    private final Set<Integer> prioritySiteIds = new LinkedHashSet<>();
    private final Set<Integer> excludedSiteIds = new LinkedHashSet<>();

    private VBox siteListContainer;
    private HBox priorityTagsBox;
    private HBox excludeTagsBox;
    private Runnable onFiltersChanged;

    public SiteFilterSection(List<SiteInfo> allSites) {
        this.allSites = allSites;
        this.visibleSites = FXCollections.observableArrayList(allSites);
    }

    public Set<Integer> getPrioritySiteIds() {
        return prioritySiteIds;
    }

    public Set<Integer> getExcludedSiteIds() {
        return excludedSiteIds;
    }

    public void setOnFiltersChanged(Runnable onFiltersChanged) {
        this.onFiltersChanged = onFiltersChanged;
    }

    public VBox build() {
        VBox card = new VBox(14);
        card.setStyle(AppStyles.cardStyle());
        card.setPadding(new Insets(20));

        Label sectionLabel = new Label("BỘ LỌC SITE");
        sectionLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2E6F40;");
        Label title = new Label("Chọn site ưu tiên hoặc loại bỏ");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button clearAllButton = new Button("Xóa bộ lọc");
        clearAllButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C72; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 4 8;");
        clearAllButton.setOnAction(event -> clearAllFilters());

        VBox headerText = new VBox(2, sectionLabel, title);
        HBox headerRow = new HBox(headerText, spacer, clearAllButton);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchBox = new TextField();
        searchBox.setPromptText("Tìm theo tên site, mã site...");
        searchBox.setStyle("-fx-background-color: #F5F9F6; -fx-border-color: #D0DAD5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 9 14; -fx-font-size: 13px; -fx-prompt-text-fill: #A0B0A6;");
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> filterSites(newValue));

        HBox tagRow = new HBox(24);

        VBox priorityColumn = new VBox(6);
        Label priorityLabel = new Label("SITE ƯU TIÊN");
        priorityLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        priorityTagsBox = new HBox(8);
        priorityTagsBox.setAlignment(Pos.CENTER_LEFT);
        updatePriorityTagsUI();
        priorityColumn.getChildren().addAll(priorityLabel, priorityTagsBox);
        HBox.setHgrow(priorityColumn, Priority.ALWAYS);

        VBox excludeColumn = new VBox(6);
        Label excludeLabel = new Label("SITE LOẠI BỎ");
        excludeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        excludeTagsBox = new HBox(8);
        excludeTagsBox.setAlignment(Pos.CENTER_LEFT);
        updateExcludeTagsUI();
        excludeColumn.getChildren().addAll(excludeLabel, excludeTagsBox);
        HBox.setHgrow(excludeColumn, Priority.ALWAYS);

        tagRow.getChildren().addAll(priorityColumn, excludeColumn);

        HBox countRow = new HBox();
        countRow.setAlignment(Pos.CENTER_LEFT);
        Label countLabel = new Label(visibleSites.size() + "/" + allSites.size() + " site");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");
        Region countSpacer = new Region();
        HBox.setHgrow(countSpacer, Priority.ALWAYS);
        Label hintLabel = new Label("Site ưu tiên sẽ được sắp xếp lên trên");
        hintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #A0B0A6;");
        countRow.getChildren().addAll(countLabel, countSpacer, hintLabel);

        siteListContainer = new VBox(8);
        rebuildSiteList();

        card.getChildren().addAll(headerRow, searchBox, tagRow, countRow, siteListContainer);
        return card;
    }

    private void filterSites(String term) {
        visibleSites.clear();
        String keyword = term == null ? "" : term.toLowerCase();
        for (SiteInfo site : allSites) {
            if (excludedSiteIds.contains(site.id)) {
                continue;
            }
            if (keyword.isEmpty()
                || site.name.toLowerCase().contains(keyword)
                || site.siteCode.toLowerCase().contains(keyword)) {
                visibleSites.add(site);
            }
        }
        rebuildSiteList();
    }

    private void clearAllFilters() {
        prioritySiteIds.clear();
        excludedSiteIds.clear();
        visibleSites.setAll(allSites);
        updatePriorityTagsUI();
        updateExcludeTagsUI();
        rebuildSiteList();
        notifyFiltersChanged();
    }

    private void rebuildSiteList() {
        siteListContainer.getChildren().clear();
        List<SiteInfo> sortedSites = new ArrayList<>();

        for (int prioritySiteId : prioritySiteIds) {
            allSites.stream()
                .filter(site -> site.id == prioritySiteId)
                .findFirst()
                .ifPresent(sortedSites::add);
        }

        for (SiteInfo site : visibleSites) {
            if (!prioritySiteIds.contains(site.id) && !excludedSiteIds.contains(site.id)) {
                sortedSites.add(site);
            }
        }

        for (SiteInfo site : sortedSites) {
            siteListContainer.getChildren().add(buildSiteCard(site));
        }
    }

    private HBox buildSiteCard(SiteInfo site) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));

        boolean prioritized = prioritySiteIds.contains(site.id);
        card.setStyle("-fx-background-color: " + (prioritized ? "#F0FAF3" : "white")
            + "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: "
            + (prioritized ? "#68BA7F" : "#E2EAE5")
            + "; -fx-border-width: 1;"
            + (prioritized ? " -fx-effect: dropshadow(gaussian, rgba(104,186,127,0.15), 6, 0, 0, 2);" : ""));

        if (prioritized) {
            Label starLabel = new Label("★");
            starLabel.setStyle("-fx-font-size: 14px;");
            card.getChildren().add(starLabel);
        }

        VBox infoBox = new VBox(2);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(site.name + (prioritized ? " — Đang ưu tiên" : ""));
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (prioritized ? "#2E6F40" : "#1a2e22") + ";");
        Label codeLabel = new Label(site.siteCode + " · Tàu: " + site.shipDays + " ngày | Bay: " + site.airDays + " ngày");
        codeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8FA899;");
        infoBox.getChildren().addAll(nameLabel, codeLabel);

        Button priorityButton;
        if (prioritized) {
            priorityButton = new Button("Bỏ ưu tiên");
            priorityButton.setStyle(AppStyles.btnChip("#FFF3E0", "#E65100"));
            priorityButton.setOnAction(event -> {
                prioritySiteIds.remove(site.id);
                updatePriorityTagsUI();
                rebuildSiteList();
                notifyFiltersChanged();
            });
        } else {
            priorityButton = new Button("Ưu tiên");
            priorityButton.setStyle(AppStyles.btnChip("#E8F5E9", "#2E7D32"));
            priorityButton.setOnAction(event -> {
                prioritySiteIds.add(site.id);
                excludedSiteIds.remove(site.id);
                updatePriorityTagsUI();
                updateExcludeTagsUI();
                rebuildSiteList();
                notifyFiltersChanged();
            });
        }

        Button excludeButton = new Button("Loại bỏ");
        excludeButton.setStyle(AppStyles.btnChip("#FEE2E2", "#DC2626"));
        excludeButton.setOnAction(event -> {
            excludedSiteIds.add(site.id);
            prioritySiteIds.remove(site.id);
            visibleSites.remove(site);
            updateExcludeTagsUI();
            updatePriorityTagsUI();
            rebuildSiteList();
            notifyFiltersChanged();
        });

        card.getChildren().addAll(infoBox, priorityButton, excludeButton);
        return card;
    }

    private void updatePriorityTagsUI() {
        priorityTagsBox.getChildren().clear();
        if (prioritySiteIds.isEmpty()) {
            Label placeholder = new Label("Chưa chọn site ưu tiên");
            placeholder.setStyle("-fx-background-color: #F0F7F2; -fx-border-color: #D0E4D6; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 4 14; -fx-font-size: 12px; -fx-text-fill: #8FA899;");
            priorityTagsBox.getChildren().add(placeholder);
            return;
        }

        for (int prioritySiteId : prioritySiteIds) {
            allSites.stream()
                .filter(site -> site.id == prioritySiteId)
                .findFirst()
                .ifPresent(site -> {
                    Label tag = new Label("★ " + site.name + "  ✕");
                    tag.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-background-radius: 14; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
                    tag.setOnMouseClicked(event -> {
                        prioritySiteIds.remove(prioritySiteId);
                        updatePriorityTagsUI();
                        rebuildSiteList();
                        notifyFiltersChanged();
                    });
                    priorityTagsBox.getChildren().add(tag);
                });
        }
    }

    private void updateExcludeTagsUI() {
        excludeTagsBox.getChildren().clear();
        if (excludedSiteIds.isEmpty()) {
            Label placeholder = new Label("Chưa loại bỏ site nào");
            placeholder.setStyle("-fx-background-color: #F0F7F2; -fx-border-color: #D0E4D6; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 4 14; -fx-font-size: 12px; -fx-text-fill: #8FA899;");
            excludeTagsBox.getChildren().add(placeholder);
            return;
        }

        for (int excludedSiteId : excludedSiteIds) {
            allSites.stream()
                .filter(site -> site.id == excludedSiteId)
                .findFirst()
                .ifPresent(site -> {
                    Label tag = new Label("⊘ " + site.name + "  ✕");
                    tag.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 14; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
                    tag.setOnMouseClicked(event -> {
                        excludedSiteIds.remove(excludedSiteId);
                        allSites.stream()
                            .filter(candidate -> candidate.id == excludedSiteId)
                            .findFirst()
                            .ifPresent(visibleSites::add);
                        updateExcludeTagsUI();
                        rebuildSiteList();
                        notifyFiltersChanged();
                    });
                    excludeTagsBox.getChildren().add(tag);
                });
        }
    }

    private void notifyFiltersChanged() {
        if (onFiltersChanged != null) {
            onFiltersChanged.run();
        }
    }
}
