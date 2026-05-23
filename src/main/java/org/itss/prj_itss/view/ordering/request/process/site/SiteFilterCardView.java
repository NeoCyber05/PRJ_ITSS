package org.itss.prj_itss.view.ordering.request.process.site;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import org.itss.prj_itss.model.request.application.processing.ProcessingSiteView;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.itss.prj_itss.view.ordering.request.process.shared.AllocationViewSupport.setStateClass;

public final class SiteFilterCardView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/site/site-filter-card.fxml";

    private static final List<String> SITE_CARD_STATE_CLASSES = List.of(
        "site-filter-site-card-normal",
        "site-filter-site-card-priority"
    );
    private static final List<String> SITE_NAME_STATE_CLASSES = List.of(
        "site-filter-site-name-normal",
        "site-filter-site-name-priority"
    );

    @FXML
    private Label starLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label codeLabel;
    @FXML
    private Button priorityButton;
    @FXML
    private Button excludeButton;

    private ProcessingSiteView site;
    private boolean prioritized;
    private Consumer<ProcessingSiteView> onPriorityToggle = ignored -> {};
    private Consumer<ProcessingSiteView> onExclude = ignored -> {};

    public static HBox load(
        ProcessingSiteView site,
        boolean prioritized,
        Consumer<ProcessingSiteView> onPriorityToggle,
        Consumer<ProcessingSiteView> onExclude
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                SiteFilterCardView.class.getResource(VIEW_RESOURCE),
                "Missing site filter card FXML"
            ));
            HBox root = loader.load();
            SiteFilterCardView controller = loader.getController();
            controller.init(root, site, prioritized, onPriorityToggle, onExclude);
            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load site filter card view", exception);
        }
    }

    @FXML
    private void handlePriorityToggle() {
        onPriorityToggle.accept(site);
    }

    @FXML
    private void handleExclude() {
        onExclude.accept(site);
    }

    private void init(
        HBox root,
        ProcessingSiteView site,
        boolean prioritized,
        Consumer<ProcessingSiteView> onPriorityToggle,
        Consumer<ProcessingSiteView> onExclude
    ) {
        this.site = Objects.requireNonNull(site, "site");
        this.prioritized = prioritized;
        this.onPriorityToggle = onPriorityToggle == null ? ignored -> {} : onPriorityToggle;
        this.onExclude = onExclude == null ? ignored -> {} : onExclude;

        setStateClass(root, SITE_CARD_STATE_CLASSES,
            prioritized ? "site-filter-site-card-priority" : "site-filter-site-card-normal");

        starLabel.setVisible(prioritized);
        starLabel.setManaged(prioritized);

        nameLabel.setText(siteName(site) + (prioritized ? " - Đang ưu tiên" : ""));
        setStateClass(nameLabel, SITE_NAME_STATE_CLASSES,
            prioritized ? "site-filter-site-name-priority" : "site-filter-site-name-normal");

        codeLabel.setText(siteCode(site) + " | Tàu: " + site.shipDays() + " ngày | Bay: " + site.airDays() + " ngày");

        priorityButton.setText(prioritized ? "Bỏ ưu tiên" : "Ưu tiên");
        priorityButton.getStyleClass().removeAll("site-filter-priority-button", "site-filter-unprioritize-button");
        priorityButton.getStyleClass().add(prioritized ? "site-filter-unprioritize-button" : "site-filter-priority-button");
    }

    private static String siteName(ProcessingSiteView site) {
        return site.name() == null ? "" : site.name();
    }

    private static String siteCode(ProcessingSiteView site) {
        return site.siteCode() == null ? "" : site.siteCode();
    }
}
