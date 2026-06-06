package org.itss.prj_itss.view.sales.request.update;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditViewState;

final class ValidationMessageDispatcher {

    private final Label errorLabel;
    private final Button updateButton;
    private final TableView<SalesRequestEditItemRow> itemsTable;

    ValidationMessageDispatcher(
            Label errorLabel,
            Button updateButton,
            TableView<SalesRequestEditItemRow> itemsTable) {
        this.errorLabel = errorLabel;
        this.updateButton = updateButton;
        this.itemsTable = itemsTable;
    }

    void render(SalesRequestEditViewState.Validation validationResult) {
        if (validationResult.validForm()) {
            errorLabel.setVisible(false);
            updateButton.setDisable(false);
        } else {
            showError(validationResult.firstMessage());
            updateButton.setDisable(true);
        }
        itemsTable.refresh();
    }

    void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
