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

import org.itss.prj_itss.request.RequestModels.SiteInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SiteFilterSection {

    private final List<SiteInfo> allSites;
    private final ObservableList<SiteInfo> visibleSites;
    private final Set<Integer> prioritySiteIds = new LinkedHashSet<>();
    private final Set<Integer> excludedSiteIds = new LinkedHashSet<>();

    private VBox siteListContainer;
    private VBox filterContent;
    private HBox priorityTagsBox;
    private HBox excludeTagsBox;
    private TextField searchBox;
    private Label countLabel;
    private Label toggleSummaryLabel;
    private Label toggleChevronLabel;
    private Button toggleButton;
    private boolean expanded;
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
        card.getStyleClass().add("forest-card");
        card.setPadding(new Insets(20));

        Label sectionLabel = new Label("B\u1ed8 L\u1eccC SITE");
        sectionLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2E6F40;");

        Label title = new Label("Ch\u1ecdn site \u01b0u ti\u00ean ho\u1eb7c lo\u1ea1i b\u1ecf");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e22;");

        toggleSummaryLabel = new Label();
        toggleSummaryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");

        toggleChevronLabel = new Label();
        toggleChevronLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6B7C72;");

        VBox toggleTextBox = new VBox(2, sectionLabel, title);
        Region toggleSpacer = new Region();
        HBox.setHgrow(toggleSpacer, Priority.ALWAYS);

        HBox toggleGraphic = new HBox(12, toggleTextBox, toggleSpacer, toggleSummaryLabel, toggleChevronLabel);
        toggleGraphic.setAlignment(Pos.CENTER_LEFT);

        toggleButton = new Button();
        toggleButton.setGraphic(toggleGraphic);
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        toggleButton.setAlignment(Pos.CENTER_LEFT);
        toggleButton.setStyle(collapsedButtonStyle());
        toggleButton.setOnAction(event -> setExpanded(!expanded));

        Button clearAllButton = new Button("X\u00f3a b\u1ed9 l\u1ecdc");
        clearAllButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C72; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 4 8;");
        clearAllButton.setOnAction(event -> clearAllFilters());

        Region clearSpacer = new Region();
        HBox.setHgrow(clearSpacer, Priority.ALWAYS);
        HBox actionsRow = new HBox(clearSpacer, clearAllButton);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        searchBox = new TextField();
        searchBox.setPromptText("T\u00ecm theo t\u00ean site, m\u00e3 site...");
        searchBox.getStyleClass().add("forest-filter-search");
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> refreshVisibleSites());

        HBox tagRow = new HBox(24);

        VBox priorityColumn = new VBox(6);
        Label priorityLabel = new Label("SITE \u01afu TI\u00caN");
        priorityLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        priorityTagsBox = new HBox(8);
        priorityTagsBox.setAlignment(Pos.CENTER_LEFT);
        priorityColumn.getChildren().addAll(priorityLabel, priorityTagsBox);
        HBox.setHgrow(priorityColumn, Priority.ALWAYS);

        VBox excludeColumn = new VBox(6);
        Label excludeLabel = new Label("SITE LO\u1ea0I B\u1ece");
        excludeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        excludeTagsBox = new HBox(8);
        excludeTagsBox.setAlignment(Pos.CENTER_LEFT);
        excludeColumn.getChildren().addAll(excludeLabel, excludeTagsBox);
        HBox.setHgrow(excludeColumn, Priority.ALWAYS);

        tagRow.getChildren().addAll(priorityColumn, excludeColumn);

        HBox countRow = new HBox();
        countRow.setAlignment(Pos.CENTER_LEFT);

        countLabel = new Label();
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C72;");

        Region countSpacer = new Region();
        HBox.setHgrow(countSpacer, Priority.ALWAYS);

        Label hintLabel = new Label("Site \u01b0u ti\u00ean s\u1ebd \u0111\u01b0\u1ee3c s\u1eafp x\u1ebfp l\u00ean tr\u00ean");
        hintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #A0B0A6;");

        countRow.getChildren().addAll(countLabel, countSpacer, hintLabel);

        siteListContainer = new VBox(8);

        filterContent = new VBox(14, actionsRow, searchBox, tagRow, countRow, siteListContainer);

        updatePriorityTagsUI();
        updateExcludeTagsUI();
        refreshVisibleSites();
        setExpanded(false);

        card.getChildren().addAll(toggleButton, filterContent);
        return card;
    }

    private void clearAllFilters() {
        prioritySiteIds.clear();
        excludedSiteIds.clear();
        updatePriorityTagsUI();
        updateExcludeTagsUI();

        if (searchBox != null && !searchBox.getText().isEmpty()) {
            searchBox.clear();
        } else {
            refreshVisibleSites();
        }

        notifyFiltersChanged();
    }

    private void refreshVisibleSites() {
        visibleSites.clear();

        String keyword = "";
        if (searchBox != null && searchBox.getText() != null) {
            keyword = searchBox.getText().trim().toLowerCase(Locale.ROOT);
        }

        for (SiteInfo site : allSites) {
            if (excludedSiteIds.contains(site.id)) {
                continue;
            }

            String siteName = site.name == null ? "" : site.name.toLowerCase(Locale.ROOT);
            String siteCode = site.siteCode == null ? "" : site.siteCode.toLowerCase(Locale.ROOT);
            if (keyword.isEmpty() || siteName.contains(keyword) || siteCode.contains(keyword)) {
                visibleSites.add(site);
            }
        }

        rebuildSiteList();
    }

    private void rebuildSiteList() {
        if (siteListContainer == null) {
            return;
        }

        siteListContainer.getChildren().clear();
        List<SiteInfo> sortedSites = new ArrayList<>();

        for (int prioritySiteId : prioritySiteIds) {
            allSites.stream()
                .filter(site -> site.id == prioritySiteId && visibleSites.contains(site))
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

        refreshSummaryLabels();
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
            Label starLabel = new Label("\u2605");
            starLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2E6F40;");
            card.getChildren().add(starLabel);
        }

        VBox infoBox = new VBox(2);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(site.name + (prioritized ? " - \u0110ang \u01b0u ti\u00ean" : ""));
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (prioritized ? "#2E6F40" : "#1a2e22") + ";");

        Label codeLabel = new Label(site.siteCode + " | T\u00e0u: " + site.shipDays + " ng\u00e0y | Bay: " + site.airDays + " ng\u00e0y");
        codeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8FA899;");

        infoBox.getChildren().addAll(nameLabel, codeLabel);

        Button priorityButton;
        if (prioritized) {
            priorityButton = buildChipButton("B\u1ecf \u01b0u ti\u00ean", "#FFF3E0", "#E65100");
            priorityButton.setOnAction(event -> {
                prioritySiteIds.remove(site.id);
                updatePriorityTagsUI();
                refreshVisibleSites();
                notifyFiltersChanged();
            });
        } else {
            priorityButton = buildChipButton("\u01afu ti\u00ean", "#E8F5E9", "#2E7D32");
            priorityButton.setOnAction(event -> {
                prioritySiteIds.add(site.id);
                excludedSiteIds.remove(site.id);
                updatePriorityTagsUI();
                updateExcludeTagsUI();
                refreshVisibleSites();
                notifyFiltersChanged();
            });
        }

        Button excludeButton = buildChipButton("Lo\u1ea1i b\u1ecf", "#FEE2E2", "#DC2626");
        excludeButton.setOnAction(event -> {
            excludedSiteIds.add(site.id);
            prioritySiteIds.remove(site.id);
            updateExcludeTagsUI();
            updatePriorityTagsUI();
            refreshVisibleSites();
            notifyFiltersChanged();
        });

        card.getChildren().addAll(infoBox, priorityButton, excludeButton);
        return card;
    }

    private void updatePriorityTagsUI() {
        if (priorityTagsBox == null) {
            return;
        }

        priorityTagsBox.getChildren().clear();
        if (prioritySiteIds.isEmpty()) {
            Label placeholder = new Label("Ch\u01b0a ch\u1ecdn site \u01b0u ti\u00ean");
            placeholder.setStyle("-fx-background-color: #F0F7F2; -fx-border-color: #D0E4D6; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 4 14; -fx-font-size: 12px; -fx-text-fill: #8FA899;");
            priorityTagsBox.getChildren().add(placeholder);
            refreshSummaryLabels();
            return;
        }

        for (int prioritySiteId : prioritySiteIds) {
            allSites.stream()
                .filter(site -> site.id == prioritySiteId)
                .findFirst()
                .ifPresent(site -> {
                    Label tag = new Label("\u2605 " + site.name + "  \u2715");
                    tag.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-background-radius: 14; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
                    tag.setOnMouseClicked(event -> {
                        prioritySiteIds.remove(prioritySiteId);
                        updatePriorityTagsUI();
                        refreshVisibleSites();
                        notifyFiltersChanged();
                    });
                    priorityTagsBox.getChildren().add(tag);
                });
        }

        refreshSummaryLabels();
    }

    private void updateExcludeTagsUI() {
        if (excludeTagsBox == null) {
            return;
        }

        excludeTagsBox.getChildren().clear();
        if (excludedSiteIds.isEmpty()) {
            Label placeholder = new Label("Ch\u01b0a lo\u1ea1i b\u1ecf site n\u00e0o");
            placeholder.setStyle("-fx-background-color: #F0F7F2; -fx-border-color: #D0E4D6; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 4 14; -fx-font-size: 12px; -fx-text-fill: #8FA899;");
            excludeTagsBox.getChildren().add(placeholder);
            refreshSummaryLabels();
            return;
        }

        for (int excludedSiteId : excludedSiteIds) {
            allSites.stream()
                .filter(site -> site.id == excludedSiteId)
                .findFirst()
                .ifPresent(site -> {
                    Label tag = new Label("\u2715 " + site.name + "  \u2715");
                    tag.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 14; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
                    tag.setOnMouseClicked(event -> {
                        excludedSiteIds.remove(excludedSiteId);
                        updateExcludeTagsUI();
                        refreshVisibleSites();
                        notifyFiltersChanged();
                    });
                    excludeTagsBox.getChildren().add(tag);
                });
        }

        refreshSummaryLabels();
    }

    private void refreshSummaryLabels() {
        String summaryText = visibleSites.size() + "/" + allSites.size() + " site | "
            + prioritySiteIds.size() + " \u01b0u ti\u00ean | "
            + excludedSiteIds.size() + " lo\u1ea1i b\u1ecf";

        if (countLabel != null) {
            countLabel.setText(visibleSites.size() + "/" + allSites.size() + " site");
        }

        if (toggleSummaryLabel != null) {
            toggleSummaryLabel.setText(summaryText);
        }
    }

    private void setExpanded(boolean expanded) {
        this.expanded = expanded;

        if (filterContent != null) {
            filterContent.setManaged(expanded);
            filterContent.setVisible(expanded);
        }

        if (toggleChevronLabel != null) {
            toggleChevronLabel.setText(expanded ? "\u25be" : "\u25b8");
        }

        if (toggleButton != null) {
            toggleButton.setStyle(expanded ? expandedButtonStyle() : collapsedButtonStyle());
        }
    }

    private String collapsedButtonStyle() {
        return "-fx-background-color: #F5F9F6; "
            + "-fx-border-color: #D0DAD5; "
            + "-fx-border-radius: 12; "
            + "-fx-background-radius: 12; "
            + "-fx-padding: 12 16; "
            + "-fx-cursor: hand;";
    }

    private String expandedButtonStyle() {
        return "-fx-background-color: #EDF6F0; "
            + "-fx-border-color: #BCD4C4; "
            + "-fx-border-radius: 12; "
            + "-fx-background-radius: 12; "
            + "-fx-padding: 12 16; "
            + "-fx-cursor: hand;";
    }

    private Button buildChipButton(String text, String background, String foreground) {
        Button button = new Button(text);
        button.getStyleClass().add("forest-chip-button");
        button.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + foreground + ";");
        return button;
    }

    private void notifyFiltersChanged() {
        if (onFiltersChanged != null) {
            onFiltersChanged.run();
        }
    }
}
