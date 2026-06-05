package org.itss.prj_itss.view.sales.request.update;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditViewState;

final class SalesRequestEditHeaderPanel {

    private final Label requestCodeLabel;
    private final Label createdAtLabel;
    private final HBox statusBadge;

    SalesRequestEditHeaderPanel(Label requestCodeLabel, Label createdAtLabel, HBox statusBadge) {
        this.requestCodeLabel = requestCodeLabel;
        this.createdAtLabel = createdAtLabel;
        this.statusBadge = statusBadge;
    }

    void render(SalesRequestEditViewState viewModel) {
        requestCodeLabel.setText(viewModel.requestCode());
        createdAtLabel.setText(viewModel.createdAt() != null && !viewModel.createdAt().isBlank()
            ? viewModel.createdAt()
            : "N/A");
        statusBadge.getChildren().setAll(SalesRequestEditStatusBadgeFactory.create(viewModel.status()));
    }
}
