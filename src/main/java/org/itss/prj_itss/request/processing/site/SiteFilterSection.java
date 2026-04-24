package org.itss.prj_itss.request.processing.site;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.dto.SiteStockOption;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SiteFilterSection {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/request/processing/site/site-filter-section.fxml";

    private final List<SiteStockOption> allSites;
    private SiteFilterSectionController controller;
    private VBox root;
    private Runnable onFiltersChanged;

    public SiteFilterSection(List<SiteStockOption> allSites) {
        this.allSites = allSites;
    }

    public Set<Integer> getPrioritySiteIds() {
        return ensureController().getPrioritySiteIds();
    }

    public Set<Integer> getExcludedSiteIds() {
        return ensureController().getExcludedSiteIds();
    }

    public void setOnFiltersChanged(Runnable onFiltersChanged) {
        this.onFiltersChanged = onFiltersChanged;
        if (controller != null) {
            controller.setOnFiltersChanged(onFiltersChanged);
        }
    }

    public VBox build() {
        ensureController();
        return root;
    }

    private SiteFilterSectionController ensureController() {
        if (controller != null) {
            return controller;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                SiteFilterSection.class.getResource(VIEW_RESOURCE),
                "Missing site filter section FXML"
            ));
            root = loader.load();
            controller = loader.getController();
            controller.init(allSites, onFiltersChanged);
            return controller;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load site filter section", exception);
        }
    }
}
