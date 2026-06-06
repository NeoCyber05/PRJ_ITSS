package org.itss.prj_itss.view.ordering.request.process.site;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.controller.ordering.request.process.state.ProcessingSiteState;
import org.itss.prj_itss.controller.ordering.request.process.site.SiteFilterController;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.setStateClass;

public final class SiteFilterView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/view/ordering/request/process/site/site-filter-view.fxml";
    private static final List<String> TOGGLE_STATE_CLASSES = List.of(
        "site-filter-toggle-collapsed",
        "site-filter-toggle-expanded"
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

    public static SiteFilterView load(List<ProcessingSiteState> allSites, Runnable onFiltersChanged) {
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

    public Set<Integer> getSelectedSiteIds() {
        return controller.selectedSiteIds();
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

    private void init(List<ProcessingSiteState> allSites, Runnable onFiltersChanged) {
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

    private void onSelectionToggled(ProcessingSiteState site) {
        if (controller.isSelected(site)) {
            controller.deselectSite(site);
        } else {
            controller.selectSite(site);
        }
        renderUi();
        notifyFiltersChanged();
    }

    private void onExclude(ProcessingSiteState site) {
        controller.excludeSite(site);
        renderUi();
        notifyFiltersChanged();
    }

    private void renderUi() {
        renderSiteList();
        renderSelectedTags();
        renderExcludeTags();
        renderSummary();
        renderExpandedState();
    }

    private void renderSiteList() {
        if (siteListContainer == null) {
            return;
        }

        siteListContainer.getChildren().clear();
        Set<Integer> selected = controller.selectedSiteIds();
        for (ProcessingSiteState site : controller.visibleSites()) {
            boolean isSelected = controller.isSelected(site);
            boolean dimmed = !selected.isEmpty() && !isSelected;
            siteListContainer.getChildren().add(SiteFilterCardView.load(
                site,
                isSelected,
                dimmed,
                this::onSelectionToggled,
                this::onExclude
            ));
        }
    }

    private void renderSelectedTags() {
        if (priorityTagsBox == null) {
            return;
        }

        priorityTagsBox.getChildren().clear();
        if (controller.selectedSiteIds().isEmpty()) {
            Label placeholderLabel = new Label("Chưa chọn site");
            placeholderLabel.getStyleClass().add("site-filter-tag-placeholder");
            priorityTagsBox.getChildren().add(placeholderLabel);
            return;
        }

        for (ProcessingSiteState site : controller.selectedSites()) {
            Label tag = new Label("● " + siteName(site) + "  ✕");
            tag.getStyleClass().addAll("site-filter-tag", "site-filter-priority-tag");
            tag.setOnMouseClicked(event -> {
                controller.removeSelected(site.id());
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
            Label placeholderLabel = new Label("Chưa loại bỏ site nào");
            placeholderLabel.getStyleClass().add("site-filter-tag-placeholder");
            excludeTagsBox.getChildren().add(placeholderLabel);
            return;
        }

        for (ProcessingSiteState site : controller.excludedSites()) {
            Label tag = new Label("✕ " + siteName(site) + "  ✕");
            tag.getStyleClass().addAll("site-filter-tag", "site-filter-exclude-tag");
            tag.setOnMouseClicked(event -> {
                controller.removeExcluded(site.id());
                renderUi();
                notifyFiltersChanged();
            });
            excludeTagsBox.getChildren().add(tag);
        }
    }

    private void renderSummary() {
        String countText = controller.visibleSites().size() + "/" + controller.allSites().size() + " site";
        StringBuilder summaryText = new StringBuilder(countText);
        summaryText.append(" | ")
            .append(controller.selectedSiteIds().size()).append(" đã chọn")
            .append(" | ")
            .append(controller.excludedSiteIds().size()).append(" loại bỏ");
        if (!controller.selectedSiteIds().isEmpty()) {
            summaryText.append(" | Chỉ dùng site đã chọn");
        }

        if (countLabel != null) {
            countLabel.setText(countText);
        }
        if (toggleSummaryLabel != null) {
            toggleSummaryLabel.setText(summaryText.toString());
        }
    }

    private void renderExpandedState() {
        if (filterContent != null) {
            filterContent.setManaged(expanded);
            filterContent.setVisible(expanded);
        }
        if (toggleChevronLabel != null) {
            toggleChevronLabel.setText(expanded ? "▾" : "▸");
        }
        if (toggleButton != null) {
            setStateClass(toggleButton, TOGGLE_STATE_CLASSES,
                expanded ? "site-filter-toggle-expanded" : "site-filter-toggle-collapsed");
        }
    }

    private void notifyFiltersChanged() {
        onFiltersChanged.run();
    }

    private static String siteName(ProcessingSiteState site) {
        return site.name() == null ? "" : site.name();
    }
}
