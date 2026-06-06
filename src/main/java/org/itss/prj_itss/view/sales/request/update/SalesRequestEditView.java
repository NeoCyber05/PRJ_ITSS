package org.itss.prj_itss.view.sales.request.update;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditActionHandler;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditViewState;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditViewPort;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.List;

public final class SalesRequestEditView implements ViewLifecycle, SalesRequestEditViewPort {

    private SalesRequestEditActionHandler actionHandler;
    private Runnable closeHandler;
    private SalesRequestEditHeaderPanel headerPanel;
    private SalesRequestEditSearchFilterBar searchFilterBar;
    private SalesRequestEditTableComponent tableComponent;
    private ValidationMessageDispatcher validationMessageDispatcher;

    @FXML private Label headerTitle;
    @FXML private Button closeButton;
    @FXML private Button cancelButton;
    @FXML private Label requestCodeLabel;
    @FXML private Label createdAtLabel;
    @FXML private HBox statusBadge;
    @FXML private ScrollPane mainScrollPane;

    @FXML private TextField searchField;
    @FXML private Button addItemButton;
    @FXML private Label selectedCountLabel;
    @FXML private Button bulkDeleteButton;

    @FXML private TableView<SalesRequestEditItemRow> itemsTable;
    @FXML private TableColumn<SalesRequestEditItemRow, Boolean> checkboxColumn;
    @FXML private TableColumn<SalesRequestEditItemRow, MerchandiseOption> merchandiseCodeColumn;
    @FXML private TableColumn<SalesRequestEditItemRow, MerchandiseOption> merchandiseNameColumn;
    @FXML private TableColumn<SalesRequestEditItemRow, SalesRequestEditItemRow> quantityColumn;
    @FXML private TableColumn<SalesRequestEditItemRow, String> unitColumn;
    @FXML private TableColumn<SalesRequestEditItemRow, SalesRequestEditItemRow> desiredDateColumn;
    @FXML private TableColumn<SalesRequestEditItemRow, SalesRequestEditItemRow> actionColumn;

    @FXML private Label itemCountLabel;
    @FXML private HBox paginationBox;
    @FXML private Label errorLabel;
    @FXML private Button updateButton;

    @FXML
    private void initialize() {
        headerPanel = new SalesRequestEditHeaderPanel(requestCodeLabel, createdAtLabel, statusBadge);
        searchFilterBar = new SalesRequestEditSearchFilterBar(searchField);
        tableComponent = new SalesRequestEditTableComponent(
            bulkDeleteButton,
            selectedCountLabel,
            itemsTable,
            checkboxColumn,
            merchandiseCodeColumn,
            merchandiseNameColumn,
            quantityColumn,
            unitColumn,
            desiredDateColumn,
            actionColumn,
            itemCountLabel,
            paginationBox
        );
        validationMessageDispatcher = new ValidationMessageDispatcher(errorLabel, updateButton, itemsTable);

        closeButton.setOnAction(event -> cancelRequested());
        cancelButton.setOnAction(event -> cancelRequested());
        updateButton.setOnAction(event -> {
            if (actionHandler != null) {
                actionHandler.saveRequested();
            }
        });
        addItemButton.setOnAction(event -> {
            if (actionHandler != null) {
                actionHandler.addItemRequested();
            }
        });

        searchFilterBar.bind(tableComponent::applySearchFilter);
        tableComponent.initialize(new SalesRequestEditTableActions(
            () -> {
                if (actionHandler != null) {
                    actionHandler.addItemRequested();
                }
            },
            lineId -> {
                if (actionHandler != null) {
                    actionHandler.deleteItemRequested(lineId);
                }
            },
            lineIds -> {
                if (actionHandler != null) {
                    actionHandler.deleteItemsRequested(lineIds);
                }
            },
            (lineId, merchandiseId) -> {
                if (actionHandler != null) {
                    actionHandler.merchandiseChanged(lineId, merchandiseId);
                }
            },
            (lineId, quantity) -> {
                if (actionHandler != null) {
                    actionHandler.quantityChanged(lineId, quantity);
                }
            },
            (lineId, desiredDate) -> {
                if (actionHandler != null) {
                    actionHandler.desiredDateChanged(lineId, desiredDate);
                }
            }
        ));
    }

    public void setActionHandler(SalesRequestEditActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    public void render(SalesRequestEditViewState viewModel) {
        headerPanel.render(viewModel);
        tableComponent.renderItems(
            viewModel.items(),
            viewModel.availableOptionsByLineId(),
            searchFilterBar.keyword()
        );
        renderValidation(viewModel.validation());
    }

    public void renderValidation(SalesRequestEditViewState.Validation validationResult) {
        validationMessageDispatcher.render(validationResult);
        tableComponent.renderValidation(validationResult);
    }

    public void focusFirstViolation(List<SalesRequestEditViewState.FieldViolation> violations) {
        tableComponent.focusFirstViolation(violations);
    }

    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        Window owner = ownerWindow();
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    public void showError(String message) {
        validationMessageDispatcher.showError(message);
    }

    public void close() {
        if (closeHandler != null) {
            closeHandler.run();
            return;
        }
        Window owner = ownerWindow();
        if (owner != null) {
            owner.hide();
        }
    }

    private void cancelRequested() {
        if (actionHandler != null) {
            actionHandler.cancelRequested();
            return;
        }
        close();
    }

    private Window ownerWindow() {
        Scene scene = itemsTable == null ? null : itemsTable.getScene();
        return scene == null ? null : scene.getWindow();
    }
}
