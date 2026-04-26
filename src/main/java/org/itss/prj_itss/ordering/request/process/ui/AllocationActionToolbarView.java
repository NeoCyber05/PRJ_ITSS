package org.itss.prj_itss.ordering.request.process.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Objects;

public final class AllocationActionToolbarView {

    private static final String VIEW_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/ui/allocation-action-toolbar-view.fxml";

    @FXML
    private HBox root;

    @FXML
    private Label sectionLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Button optimizeButton;

    @FXML
    private Button showAllButton;

    public AllocationActionToolbarView() {
    }

    public static HBox build(
        String sectionText,
        String titleText,
        Runnable onOptimizeRequested,
        Runnable onShowAllRequested,
        String optimizeButtonStyleClass,
        String showAllButtonStyleClass,
        String containerStyle
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                AllocationActionToolbarView.class.getResource(VIEW_RESOURCE),
                "Missing allocation action toolbar FXML"
            ));
            Parent rootNode = loader.load();
            AllocationActionToolbarView controller = loader.getController();
            return controller.init(
                sectionText,
                titleText,
                onOptimizeRequested,
                onShowAllRequested,
                optimizeButtonStyleClass,
                showAllButtonStyleClass,
                containerStyle
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load allocation action toolbar view", exception);
        }
    }

    private HBox init(
        String sectionText,
        String titleText,
        Runnable onOptimizeRequested,
        Runnable onShowAllRequested,
        String optimizeButtonStyleClass,
        String showAllButtonStyleClass,
        String containerStyle
    ) {
        sectionLabel.setText(sectionText);
        titleLabel.setText(titleText);
        optimizeButton.getStyleClass().add(optimizeButtonStyleClass);
        showAllButton.getStyleClass().add(showAllButtonStyleClass);
        optimizeButton.setOnAction(event -> onOptimizeRequested.run());
        showAllButton.setOnAction(event -> onShowAllRequested.run());
        if (containerStyle != null && !containerStyle.isBlank()) {
            root.setStyle(containerStyle);
        }
        return root;
    }
}
