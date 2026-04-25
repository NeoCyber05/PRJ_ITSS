package org.itss.prj_itss.request.processing.site;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.dto.SiteStockOption;

import java.util.List;
import java.util.Set;

public final class SiteFilterController {

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

    private final SiteFilterModel model = new SiteFilterModel();

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

    private boolean expanded;
    private Runnable onFiltersChanged;

    @FXML
    private void initialize() {
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        toggleGraphic.setMaxWidth(Double.MAX_VALUE);
        toggleButton.setOnAction(event -> setExpanded(!expanded));
        clearAllButton.setOnAction(event -> clearAllFilters());
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> renderUi());
    }

    public void init(List<SiteStockOption> allSites, Runnable onFiltersChanged) {
        model.setSites(allSites);
        this.onFiltersChanged = onFiltersChanged;
        expanded = false;
        renderUi();
    }

    public Set<Integer> getPrioritySiteIds() {
        return model.prioritySiteIds();
    }

    public Set<Integer> getExcludedSiteIds() {
        return model.excludedSiteIds();
    }

    private void clearAllFilters() {
        model.clearFilters();
        if (searchBox != null && searchBox.getText() != null && !searchBox.getText().isEmpty()) {
            searchBox.clear();
        }

        renderUi();
        notifyFiltersChanged();
    }

    private void prioritizeSite(SiteStockOption site) {
        model.prioritize(site);
        renderUi();
        notifyFiltersChanged();
    }

    private void unprioritizeSite(SiteStockOption site) {
        model.unprioritize(site);
        renderUi();
        notifyFiltersChanged();
    }

    private void excludeSite(SiteStockOption site) {
        model.exclude(site);
        renderUi();
        notifyFiltersChanged();
    }

    private void setExpanded(boolean expanded) {
        this.expanded = expanded;
        renderUi();
    }

    private void renderUi() {
        model.refreshVisibleSites(searchBox == null ? "" : searchBox.getText());

        if (siteListContainer != null) {
            siteListContainer.getChildren().clear();
            for (SiteStockOption site : model.visibleSites()) {
                boolean prioritized = model.isPriority(site);

                HBox card = new HBox(12);
                card.setAlignment(Pos.CENTER_LEFT);
                card.getStyleClass().add("site-filter-site-card");
                card.getStyleClass().removeAll(SITE_CARD_STATE_CLASSES);
                card.getStyleClass().add(prioritized ? "site-filter-site-card-priority" : "site-filter-site-card-normal");

                if (prioritized) {
                    Label starLabel = new Label("★");
                    starLabel.getStyleClass().add("site-filter-star");
                    card.getChildren().add(starLabel);
                }

                VBox infoBox = new VBox(2);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                Label nameLabel = new Label(siteName(site) + (prioritized ? " - Đang ưu tiên" : ""));
                nameLabel.getStyleClass().add("site-filter-site-name");
                nameLabel.getStyleClass().removeAll(SITE_NAME_STATE_CLASSES);
                nameLabel.getStyleClass().add(prioritized ? "site-filter-site-name-priority" : "site-filter-site-name-normal");

                Label codeLabel = new Label(siteCode(site) + " | Tàu: " + site.shipDays + " ngày | Bay: " + site.airDays + " ngày");
                codeLabel.getStyleClass().add("site-filter-site-code");
                infoBox.getChildren().addAll(nameLabel, codeLabel);

                Button priorityButton = new Button(prioritized ? "Bỏ ưu tiên" : "Ưu tiên");
                priorityButton.getStyleClass().addAll(
                    "forest-chip-button",
                    prioritized ? "site-filter-unprioritize-button" : "site-filter-priority-button"
                );
                priorityButton.setOnAction(event -> {
                    if (prioritized) {
                        unprioritizeSite(site);
                    } else {
                        prioritizeSite(site);
                    }
                });

                Button excludeButton = new Button("Loại bỏ");
                excludeButton.getStyleClass().addAll("forest-chip-button", "site-filter-exclude-button");
                excludeButton.setOnAction(event -> excludeSite(site));

                card.getChildren().addAll(infoBox, priorityButton, excludeButton);
                siteListContainer.getChildren().add(card);
            }
        }

        if (priorityTagsBox != null) {
            priorityTagsBox.getChildren().clear();
            if (model.prioritySiteIds().isEmpty()) {
                Label placeholderLabel = new Label("Chưa chọn site ưu tiên");
                placeholderLabel.getStyleClass().add("site-filter-tag-placeholder");
                priorityTagsBox.getChildren().add(placeholderLabel);
            } else {
                for (SiteStockOption site : model.prioritySites()) {
                    Label tag = new Label("★ " + siteName(site) + "  ✕");
                    tag.getStyleClass().addAll("site-filter-tag", "site-filter-priority-tag");
                    tag.setOnMouseClicked(event -> {
                        model.removePriority(site.id);
                        renderUi();
                        notifyFiltersChanged();
                    });
                    priorityTagsBox.getChildren().add(tag);
                }
            }
        }

        if (excludeTagsBox != null) {
            excludeTagsBox.getChildren().clear();
            if (model.excludedSiteIds().isEmpty()) {
                Label placeholderLabel = new Label("Chưa loại bỏ site nào");
                placeholderLabel.getStyleClass().add("site-filter-tag-placeholder");
                excludeTagsBox.getChildren().add(placeholderLabel);
            } else {
                for (SiteStockOption site : model.excludedSites()) {
                    Label tag = new Label("✕ " + siteName(site) + "  ✕");
                    tag.getStyleClass().addAll("site-filter-tag", "site-filter-exclude-tag");
                    tag.setOnMouseClicked(event -> {
                        model.removeExcluded(site.id);
                        renderUi();
                        notifyFiltersChanged();
                    });
                    excludeTagsBox.getChildren().add(tag);
                }
            }
        }

        String countText = model.visibleSites().size() + "/" + model.allSites().size() + " site";
        String summaryText = countText + " | "
            + model.prioritySiteIds().size() + " ưu tiên | "
            + model.excludedSiteIds().size() + " loại bỏ";

        if (countLabel != null) {
            countLabel.setText(countText);
        }

        if (toggleSummaryLabel != null) {
            toggleSummaryLabel.setText(summaryText);
        }

        if (filterContent != null) {
            filterContent.setManaged(expanded);
            filterContent.setVisible(expanded);
        }

        if (toggleChevronLabel != null) {
            toggleChevronLabel.setText(expanded ? "▾" : "▸");
        }

        if (toggleButton != null) {
            toggleButton.getStyleClass().removeAll(TOGGLE_STATE_CLASSES);
            toggleButton.getStyleClass().add(expanded ? "site-filter-toggle-expanded" : "site-filter-toggle-collapsed");
        }
    }

    private static String siteName(SiteStockOption site) {
        return site.name == null ? "" : site.name;
    }

    private static String siteCode(SiteStockOption site) {
        return site.siteCode == null ? "" : site.siteCode;
    }

    private void notifyFiltersChanged() {
        if (onFiltersChanged != null) {
            onFiltersChanged.run();
        }
    }
}
