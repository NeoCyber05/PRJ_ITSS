package org.itss.prj_itss.request.presentation.ordering.process.site;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.request.business.model.SiteStockOption;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class SiteFilterView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/request/presentation/ordering/process/site/site-filter-view.fxml";
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

    private final SiteFilterController controller = new SiteFilterController();

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

    private VBox root;
    private boolean expanded;
    private Runnable onFiltersChanged = () -> {};

    public static SiteFilterView load(List<SiteStockOption> allSites, Runnable onFiltersChanged) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                SiteFilterView.class.getResource(VIEW_RESOURCE),
                "Missing site filter section FXML"
            ));
            VBox root = loader.load();
            SiteFilterView view = loader.getController();
            view.root = root;
            view.init(allSites, onFiltersChanged);
            return view;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load site filter section", exception);
        }
    }

    public VBox root() {
        return root;
    }

    public Set<Integer> getPrioritySiteIds() {
        return controller.prioritySiteIds();
    }

    public Set<Integer> getExcludedSiteIds() {
        return controller.excludedSiteIds();
    }

    @FXML
    private void initialize() {
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        toggleGraphic.setMaxWidth(Double.MAX_VALUE);
        toggleButton.setOnAction(event -> {
            expanded = !expanded;
            renderUi();
        });
        clearAllButton.setOnAction(event -> clearAllFilters());
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
            controller.search(newValue);
            renderUi();
        });
    }

    private void init(List<SiteStockOption> allSites, Runnable onFiltersChanged) {
        controller.init(allSites);
        this.onFiltersChanged = onFiltersChanged == null ? () -> {} : onFiltersChanged;
        expanded = false;
        renderUi();
    }

    private void clearAllFilters() {
        controller.clearAllFilters();
        if (searchBox != null && searchBox.getText() != null && !searchBox.getText().isEmpty()) {
            searchBox.clear();
        }
        renderUi();
        notifyFiltersChanged();
    }

    private void prioritizeSite(SiteStockOption site) {
        controller.prioritizeSite(site);
        renderUi();
        notifyFiltersChanged();
    }

    private void unprioritizeSite(SiteStockOption site) {
        controller.unprioritizeSite(site);
        renderUi();
        notifyFiltersChanged();
    }

    private void excludeSite(SiteStockOption site) {
        controller.excludeSite(site);
        renderUi();
        notifyFiltersChanged();
    }

    private void renderUi() {
        renderSiteList();
        renderPriorityTags();
        renderExcludeTags();
        renderSummary();
        renderExpandedState();
    }

    private void renderSiteList() {
        if (siteListContainer == null) {
            return;
        }

        siteListContainer.getChildren().clear();
        for (SiteStockOption site : controller.visibleSites()) {
            boolean prioritized = controller.isPriority(site);

            HBox card = new HBox(12);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("site-filter-site-card");
            card.getStyleClass().removeAll(SITE_CARD_STATE_CLASSES);
            card.getStyleClass().add(prioritized ? "site-filter-site-card-priority" : "site-filter-site-card-normal");

            if (prioritized) {
                Label starLabel = new Label("â˜…");
                starLabel.getStyleClass().add("site-filter-star");
                card.getChildren().add(starLabel);
            }

            VBox infoBox = buildSiteInfo(site, prioritized);
            Button priorityButton = buildPriorityButton(site, prioritized);
            Button excludeButton = buildExcludeButton(site);

            card.getChildren().addAll(infoBox, priorityButton, excludeButton);
            siteListContainer.getChildren().add(card);
        }
    }

    private VBox buildSiteInfo(SiteStockOption site, boolean prioritized) {
        VBox infoBox = new VBox(2);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(siteName(site) + (prioritized ? " - Äang Æ°u tiÃªn" : ""));
        nameLabel.getStyleClass().add("site-filter-site-name");
        nameLabel.getStyleClass().removeAll(SITE_NAME_STATE_CLASSES);
        nameLabel.getStyleClass().add(prioritized ? "site-filter-site-name-priority" : "site-filter-site-name-normal");

        Label codeLabel = new Label(siteCode(site) + " | TÃ u: " + site.shipDays + " ngÃ y | Bay: " + site.airDays + " ngÃ y");
        codeLabel.getStyleClass().add("site-filter-site-code");
        infoBox.getChildren().addAll(nameLabel, codeLabel);
        return infoBox;
    }

    private Button buildPriorityButton(SiteStockOption site, boolean prioritized) {
        Button priorityButton = new Button(prioritized ? "Bá» Æ°u tiÃªn" : "Æ¯u tiÃªn");
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
        return priorityButton;
    }

    private Button buildExcludeButton(SiteStockOption site) {
        Button excludeButton = new Button("Loáº¡i bá»");
        excludeButton.getStyleClass().addAll("forest-chip-button", "site-filter-exclude-button");
        excludeButton.setOnAction(event -> excludeSite(site));
        return excludeButton;
    }

    private void renderPriorityTags() {
        if (priorityTagsBox == null) {
            return;
        }

        priorityTagsBox.getChildren().clear();
        if (controller.prioritySiteIds().isEmpty()) {
            Label placeholderLabel = new Label("ChÆ°a chá»n site Æ°u tiÃªn");
            placeholderLabel.getStyleClass().add("site-filter-tag-placeholder");
            priorityTagsBox.getChildren().add(placeholderLabel);
            return;
        }

        for (SiteStockOption site : controller.prioritySites()) {
            Label tag = new Label("â˜… " + siteName(site) + "  âœ•");
            tag.getStyleClass().addAll("site-filter-tag", "site-filter-priority-tag");
            tag.setOnMouseClicked(event -> {
                controller.removePriority(site.id);
                renderUi();
                notifyFiltersChanged();
            });
            priorityTagsBox.getChildren().add(tag);
        }
    }

    private void renderExcludeTags() {
        if (excludeTagsBox == null) {
            return;
        }

        excludeTagsBox.getChildren().clear();
        if (controller.excludedSiteIds().isEmpty()) {
            Label placeholderLabel = new Label("ChÆ°a loáº¡i bá» site nÃ o");
            placeholderLabel.getStyleClass().add("site-filter-tag-placeholder");
            excludeTagsBox.getChildren().add(placeholderLabel);
            return;
        }

        for (SiteStockOption site : controller.excludedSites()) {
            Label tag = new Label("âœ• " + siteName(site) + "  âœ•");
            tag.getStyleClass().addAll("site-filter-tag", "site-filter-exclude-tag");
            tag.setOnMouseClicked(event -> {
                controller.removeExcluded(site.id);
                renderUi();
                notifyFiltersChanged();
            });
            excludeTagsBox.getChildren().add(tag);
        }
    }

    private void renderSummary() {
        String countText = controller.visibleSites().size() + "/" + controller.allSites().size() + " site";
        String summaryText = countText + " | "
            + controller.prioritySiteIds().size() + " Æ°u tiÃªn | "
            + controller.excludedSiteIds().size() + " loáº¡i bá»";

        if (countLabel != null) {
            countLabel.setText(countText);
        }
        if (toggleSummaryLabel != null) {
            toggleSummaryLabel.setText(summaryText);
        }
    }

    private void renderExpandedState() {
        if (filterContent != null) {
            filterContent.setManaged(expanded);
            filterContent.setVisible(expanded);
        }
        if (toggleChevronLabel != null) {
            toggleChevronLabel.setText(expanded ? "â–¾" : "â–¸");
        }
        if (toggleButton != null) {
            toggleButton.getStyleClass().removeAll(TOGGLE_STATE_CLASSES);
            toggleButton.getStyleClass().add(expanded ? "site-filter-toggle-expanded" : "site-filter-toggle-collapsed");
        }
    }

    private void notifyFiltersChanged() {
        onFiltersChanged.run();
    }

    private static String siteName(SiteStockOption site) {
        return site.name == null ? "" : site.name;
    }

    private static String siteCode(SiteStockOption site) {
        return site.siteCode == null ? "" : site.siteCode;
    }
}

