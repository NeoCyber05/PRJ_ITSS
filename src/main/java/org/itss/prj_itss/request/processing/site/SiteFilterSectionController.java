package org.itss.prj_itss.request.processing.site;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.ui.RequestProcessingUiSupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SiteFilterSectionController {

    private static final List<String> TOGGLE_STATE_CLASSES = List.of(
        "site-filter-toggle-collapsed",
        "site-filter-toggle-expanded"
    );
    private static final List<String> SITE_CARD_STATE_CLASSES = List.of(
        "site-filter-site-card-normal",
        "site-filter-site-card-priority"
    );
    private static final List<String> SITE_NAME_STATE_CLASSES = List.of(
        "site-filter-site-name-normal",
        "site-filter-site-name-priority"
    );

    private final ObservableList<SiteStockOption> visibleSites = FXCollections.observableArrayList();
    private final Set<Integer> prioritySiteIds = new LinkedHashSet<>();
    private final Set<Integer> excludedSiteIds = new LinkedHashSet<>();

    @FXML
    private VBox siteListContainer;

    @FXML
    private VBox filterContent;

    @FXML
    private HBox toggleGraphic;

    @FXML
    private HBox priorityTagsBox;

    @FXML
    private HBox excludeTagsBox;

    @FXML
    private TextField searchBox;

    @FXML
    private Label countLabel;

    @FXML
    private Label toggleSummaryLabel;

    @FXML
    private Label toggleChevronLabel;

    @FXML
    private Button toggleButton;

    @FXML
    private Button clearAllButton;

    private List<SiteStockOption> allSites = List.of();
    private boolean expanded;
    private Runnable onFiltersChanged;

    @FXML
    private void initialize() {
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        toggleGraphic.setMaxWidth(Double.MAX_VALUE);
        toggleButton.setOnAction(event -> setExpanded(!expanded));
        clearAllButton.setOnAction(event -> clearAllFilters());
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> refreshVisibleSites());
    }

    public void init(List<SiteStockOption> allSites, Runnable onFiltersChanged) {
        this.allSites = allSites;
        this.onFiltersChanged = onFiltersChanged;
        visibleSites.setAll(allSites);

        updatePriorityTagsUI();
        updateExcludeTagsUI();
        refreshVisibleSites();
        setExpanded(false);
    }

    public Set<Integer> getPrioritySiteIds() {
        return prioritySiteIds;
    }

    public Set<Integer> getExcludedSiteIds() {
        return excludedSiteIds;
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

        for (SiteStockOption site : allSites) {
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
        List<SiteStockOption> sortedSites = new ArrayList<>();

        for (int prioritySiteId : prioritySiteIds) {
            allSites.stream()
                .filter(site -> site.id == prioritySiteId && visibleSites.contains(site))
                .findFirst()
                .ifPresent(sortedSites::add);
        }

        for (SiteStockOption site : visibleSites) {
            if (!prioritySiteIds.contains(site.id) && !excludedSiteIds.contains(site.id)) {
                sortedSites.add(site);
            }
        }

        for (SiteStockOption site : sortedSites) {
            siteListContainer.getChildren().add(buildSiteCard(site));
        }

        refreshSummaryLabels();
    }

    private HBox buildSiteCard(SiteStockOption site) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        RequestProcessingUiSupport.addStyleClass(card, "site-filter-site-card");

        boolean prioritized = prioritySiteIds.contains(site.id);
        RequestProcessingUiSupport.setStateClass(
            card,
            SITE_CARD_STATE_CLASSES,
            prioritized ? "site-filter-site-card-priority" : "site-filter-site-card-normal"
        );

        if (prioritized) {
            Label starLabel = new Label("★");
            RequestProcessingUiSupport.addStyleClass(starLabel, "site-filter-star");
            card.getChildren().add(starLabel);
        }

        VBox infoBox = new VBox(2);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(site.name + (prioritized ? " - Đang ưu tiên" : ""));
        RequestProcessingUiSupport.addStyleClass(nameLabel, "site-filter-site-name");
        RequestProcessingUiSupport.setStateClass(
            nameLabel,
            SITE_NAME_STATE_CLASSES,
            prioritized ? "site-filter-site-name-priority" : "site-filter-site-name-normal"
        );

        Label codeLabel = new Label(site.siteCode + " | Tàu: " + site.shipDays + " ngày | Bay: " + site.airDays + " ngày");
        RequestProcessingUiSupport.addStyleClass(codeLabel, "site-filter-site-code");

        infoBox.getChildren().addAll(nameLabel, codeLabel);

        Button priorityButton;
        if (prioritized) {
            priorityButton = buildChipButton("Bỏ ưu tiên", "site-filter-unprioritize-button");
            priorityButton.setOnAction(event -> {
                prioritySiteIds.remove(site.id);
                updatePriorityTagsUI();
                refreshVisibleSites();
                notifyFiltersChanged();
            });
        } else {
            priorityButton = buildChipButton("Ưu tiên", "site-filter-priority-button");
            priorityButton.setOnAction(event -> {
                prioritySiteIds.add(site.id);
                excludedSiteIds.remove(site.id);
                updatePriorityTagsUI();
                updateExcludeTagsUI();
                refreshVisibleSites();
                notifyFiltersChanged();
            });
        }

        Button excludeButton = buildChipButton("Loại bỏ", "site-filter-exclude-button");
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
            Label placeholder = new Label("Chưa chọn site ưu tiên");
            RequestProcessingUiSupport.addStyleClass(placeholder, "site-filter-tag-placeholder");
            priorityTagsBox.getChildren().add(placeholder);
            refreshSummaryLabels();
            return;
        }

        for (int prioritySiteId : prioritySiteIds) {
            allSites.stream()
                .filter(site -> site.id == prioritySiteId)
                .findFirst()
                .ifPresent(site -> {
                    Label tag = new Label("★ " + site.name + "  ✕");
                    RequestProcessingUiSupport.addStyleClass(tag, "site-filter-tag", "site-filter-priority-tag");
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
            Label placeholder = new Label("Chưa loại bỏ site nào");
            RequestProcessingUiSupport.addStyleClass(placeholder, "site-filter-tag-placeholder");
            excludeTagsBox.getChildren().add(placeholder);
            refreshSummaryLabels();
            return;
        }

        for (int excludedSiteId : excludedSiteIds) {
            allSites.stream()
                .filter(site -> site.id == excludedSiteId)
                .findFirst()
                .ifPresent(site -> {
                    Label tag = new Label("✕ " + site.name + "  ✕");
                    RequestProcessingUiSupport.addStyleClass(tag, "site-filter-tag", "site-filter-exclude-tag");
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
            + prioritySiteIds.size() + " ưu tiên | "
            + excludedSiteIds.size() + " loại bỏ";

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
            toggleChevronLabel.setText(expanded ? "▾" : "▸");
        }

        if (toggleButton != null) {
            RequestProcessingUiSupport.addStyleClass(toggleButton, "site-filter-toggle-button");
            RequestProcessingUiSupport.setStateClass(
                toggleButton,
                TOGGLE_STATE_CLASSES,
                expanded ? "site-filter-toggle-expanded" : "site-filter-toggle-collapsed"
            );
        }
    }

    private Button buildChipButton(String text, String styleClass) {
        Button button = new Button(text);
        RequestProcessingUiSupport.addStyleClass(button, "forest-chip-button", styleClass);
        return button;
    }

    private void notifyFiltersChanged() {
        if (onFiltersChanged != null) {
            onFiltersChanged.run();
        }
    }
}
