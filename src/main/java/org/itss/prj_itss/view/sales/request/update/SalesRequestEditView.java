package org.itss.prj_itss.view.sales.request.update;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.controller.sales.request.update.ISalesRequestEditActions;
import org.itss.prj_itss.controller.sales.request.update.ISalesRequestEditViewPort;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditFieldViolationView;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditItemView;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditMerchandiseOptionView;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditValidationView;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditViewState;
import org.itss.prj_itss.view.sales.request.shared.ItemRow;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

public final class SalesRequestEditView implements ViewLifecycle, ISalesRequestEditViewPort {

    private static final int PAGE_SIZE = 10;

    private ISalesRequestEditActions events;
    private Runnable closeHandler;
    private SalesRequestEditValidationView validation = SalesRequestEditValidationView.valid();
    private SalesRequestEditPaginationController paginationController;

    private final ObservableList<ItemRow> allItems = FXCollections.observableArrayList();
    private final ObservableList<ItemRow> pageItems = FXCollections.observableArrayList();
    private FilteredList<ItemRow> filteredItems;
    private List<SalesRequestEditMerchandiseOptionView> merchandiseOptions = List.of();

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

    @FXML private TableView<ItemRow> itemsTable;
    @FXML private TableColumn<ItemRow, Boolean> checkboxColumn;
    @FXML private TableColumn<ItemRow, SalesRequestEditMerchandiseOptionView> merchandiseCodeColumn;
    @FXML private TableColumn<ItemRow, SalesRequestEditMerchandiseOptionView> merchandiseNameColumn;
    @FXML private TableColumn<ItemRow, ItemRow> quantityColumn;
    @FXML private TableColumn<ItemRow, String> unitColumn;
    @FXML private TableColumn<ItemRow, ItemRow> desiredDateColumn;
    @FXML private TableColumn<ItemRow, ItemRow> actionColumn;

    @FXML private Label itemCountLabel;
    @FXML private HBox paginationBox;
    @FXML private Label errorLabel;
    @FXML private Button updateButton;

    @FXML
    private void initialize() {
        filteredItems = new FilteredList<>(allItems, row -> true);
        paginationController = new SalesRequestEditPaginationController(
            PAGE_SIZE,
            filteredItems,
            pageItems,
            itemCountLabel,
            paginationBox
        );

        closeButton.setOnAction(event -> cancelRequested());
        cancelButton.setOnAction(event -> cancelRequested());
        updateButton.setOnAction(event -> {
            if (events != null) {
                events.saveRequested();
            }
        });
        addItemButton.setOnAction(event -> {
            if (events != null) {
                events.addItemRequested();
            }
        });
        bulkDeleteButton.setOnAction(event -> deleteSelectedRows());

        setupSearch();
        setupTable();
    }

    @Override
    public void bindEvents(ISalesRequestEditActions events) {
        this.events = events;
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    @Override
    public void render(SalesRequestEditViewState viewModel) {
        merchandiseOptions = viewModel.merchandiseOptions();
        requestCodeLabel.setText(viewModel.requestCode());
        createdAtLabel.setText(viewModel.createdAt() != null && !viewModel.createdAt().isBlank()
            ? viewModel.createdAt()
            : "N/A");
        buildStatusBadge(viewModel.status());
        renderItems(viewModel.items());
        renderValidation(viewModel.validation());
    }

    @Override
    public void renderItems(List<SalesRequestEditItemView> items) {
        allItems.setAll(items.stream().map(ItemRow::new).toList());
        applySearchFilter(searchField == null ? "" : searchField.getText());
    }

    @Override
    public void renderValidation(SalesRequestEditValidationView validation) {
        this.validation = validation;
        if (validation.validForm()) {
            errorLabel.setVisible(false);
            updateButton.setDisable(false);
        } else {
            showError(validation.firstMessage());
            updateButton.setDisable(true);
        }
        itemsTable.refresh();
    }

    @Override
    public void focusFirstViolation(List<SalesRequestEditFieldViolationView> violations) {
        if (violations == null || violations.isEmpty()) {
            return;
        }

        for (SalesRequestEditFieldViolationView violation : violations) {
            if (violation.lineId() <= 0) {
                return;
            }
            ItemRow row = findRow(violation.lineId());
            if (row == null) {
                continue;
            }
            int filteredIndex = filteredItems.indexOf(row);
            if (filteredIndex < 0) {
                continue;
            }
            paginationController.showFilteredIndex(filteredIndex);
            int pageIndex = paginationController.pageIndex(filteredIndex);
            itemsTable.scrollTo(pageIndex);
            itemsTable.getSelectionModel().select(pageIndex);
            return;
        }
    }

    @Override
    public void showSuccess(String message) {
        SalesRequestEditAlertHelper.showInfo(ownerWindow(), message);
    }

    @Override
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @Override
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
        if (events != null) {
            events.cancelRequested();
            return;
        }
        close();
    }

    private void setupSearch() {
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            debounce.setOnFinished(event -> applySearchFilter(newVal));
            debounce.playFromStart();
        });
    }

    private void applySearchFilter(String text) {
        String lower = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        filteredItems.setPredicate(row -> {
            if (lower.isEmpty()) {
                return true;
            }
            SalesRequestEditMerchandiseOptionView merchandise = row.merchandise();
            if (merchandise == null) {
                return true;
            }
            return contains(merchandise.code(), lower) || contains(merchandise.name(), lower);
        });
        paginationController.updatePageView();
    }

    private void setupTable() {
        itemsTable.setEditable(true);
        itemsTable.setFixedCellSize(48);
        itemsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        itemsTable.setItems(pageItems);

        itemsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && events != null) {
                events.addItemRequested();
            }
        });

        itemsTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(ItemRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("error-row");
                if (!empty && item != null && validation.hasViolation(item.lineId())) {
                    getStyleClass().add("error-row");
                }
            }
        });

        allItems.addListener((ListChangeListener<ItemRow>) change -> {
            while (change.next()) {
                for (ItemRow row : change.getAddedSubList()) {
                    row.selectedProperty().addListener((obs, oldValue, newValue) -> refreshBulkDelete());
                }
            }
            refreshBulkDelete();
        });

        setupCheckboxColumn();
        setupMerchandiseCodeColumn();
        setupMerchandiseNameColumn();
        setupUnitColumn();
        setupQuantityColumn();
        setupDesiredDateColumn();
        setupActionColumn();
    }

    private void setupCheckboxColumn() {
        checkboxColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        checkboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkboxColumn));
    }

    private void setupMerchandiseCodeColumn() {
        merchandiseCodeColumn.setCellValueFactory(data -> data.getValue().merchandiseProperty());
        merchandiseCodeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SalesRequestEditMerchandiseOptionView item, boolean empty) {
                super.updateItem(item, empty);
                ItemRow row = currentRow();
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                if (row.merchandise() != null) {
                    Label label = new Label(row.merchandise().code());
                    label.setStyle("-fx-text-fill: #1F2937; -fx-font-weight: bold;");
                    setGraphic(label);
                    return;
                }

                ComboBox<SalesRequestEditMerchandiseOptionView> comboBox = createSearchableComboBox(true, row);
                comboBox.valueProperty().addListener((obs, oldValue, newValue) -> merchandiseChanged(row, newValue));
                setGraphic(comboBox);
            }

            private ItemRow currentRow() {
                return getTableRow() == null ? null : getTableRow().getItem();
            }
        });
    }

    private void setupMerchandiseNameColumn() {
        merchandiseNameColumn.setCellValueFactory(data -> data.getValue().merchandiseProperty());
        merchandiseNameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SalesRequestEditMerchandiseOptionView item, boolean empty) {
                super.updateItem(item, empty);
                ItemRow row = currentRow();
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                if (row.merchandise() != null) {
                    Label label = new Label(row.merchandise().name());
                    label.setStyle("-fx-text-fill: #4B5563;");
                    setGraphic(label);
                    return;
                }

                ComboBox<SalesRequestEditMerchandiseOptionView> comboBox = createSearchableComboBox(false, row);
                comboBox.valueProperty().addListener((obs, oldValue, newValue) -> merchandiseChanged(row, newValue));
                setGraphic(comboBox);
            }

            private ItemRow currentRow() {
                return getTableRow() == null ? null : getTableRow().getItem();
            }
        });
    }

    private void setupUnitColumn() {
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(unitOf(data.getValue().merchandise())));
    }

    private void setupQuantityColumn() {
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        quantityColumn.setCellFactory(column -> new TableCell<>() {
            private final TextField textField = new TextField();
            private ItemRow currentRow;
            private boolean internalUpdate;

            {
                textField.setPrefWidth(100);
                textField.setAlignment(Pos.CENTER_LEFT);
                textField.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (internalUpdate || currentRow == null) {
                        return;
                    }
                    if (!newValue.matches("\\d*(\\.\\d*)?")) {
                        internalUpdate = true;
                        textField.setText(oldValue);
                        internalUpdate = false;
                        return;
                    }
                    BigDecimal quantity = parseQuantity(newValue);
                    currentRow.setQuantity(quantity);
                    if (events != null) {
                        events.quantityChanged(currentRow.lineId(), quantity);
                    }
                });
            }

            @Override
            protected void updateItem(ItemRow row, boolean empty) {
                super.updateItem(row, empty);
                currentRow = row;
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                internalUpdate = true;
                textField.setText(row.quantity() != null ? OrderingFormatters.formatQuantity(row.quantity()) : "");
                internalUpdate = false;
                applyInputStyle(textField, hasViolation(row.lineId(), "quantity"));
                setGraphic(textField);
            }
        });
    }

    private void setupDesiredDateColumn() {
        desiredDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        desiredDateColumn.setCellFactory(column -> new TableCell<>() {
            private final DatePicker datePicker = new DatePicker();
            private ItemRow currentRow;
            private boolean internalUpdate;

            {
                datePicker.setPrefWidth(165);
                datePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
                    if (internalUpdate || currentRow == null) {
                        return;
                    }
                    currentRow.setDesiredDate(newValue);
                    if (events != null) {
                        events.desiredDateChanged(currentRow.lineId(), newValue);
                    }
                });
                datePicker.setDayCellFactory(SalesRequestEditDateCellFactory.disablePastDates(datePicker));
            }

            @Override
            protected void updateItem(ItemRow row, boolean empty) {
                super.updateItem(row, empty);
                currentRow = row;
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                internalUpdate = true;
                datePicker.setValue(row.desiredDate());
                internalUpdate = false;
                applyInputStyle(datePicker, hasViolation(row.lineId(), "desiredDate"));
                setGraphic(datePicker);
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(ItemRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                Button deleteButton = new Button();
                deleteButton.setGraphic(SalesRequestEditIcons.trashIcon());
                deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
                deleteButton.setOnAction(event -> {
                    if (events != null) {
                        events.deleteItemRequested(row.lineId());
                    }
                });

                HBox box = new HBox(deleteButton);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
    }

    private void refreshBulkDelete() {
        long count = allItems.stream().filter(ItemRow::selected).count();
        boolean show = count > 0;
        bulkDeleteButton.setVisible(show);
        bulkDeleteButton.setManaged(show);
        selectedCountLabel.setVisible(show);
        selectedCountLabel.setManaged(show);
        if (show) {
            selectedCountLabel.setText("Đã chọn " + count + " dòng");
        }
    }

    private void deleteSelectedRows() {
        if (events == null) {
            return;
        }
        List<Integer> lineIds = allItems.stream()
            .filter(ItemRow::selected)
            .map(ItemRow::lineId)
            .toList();
        events.deleteItemsRequested(lineIds);
    }

    private void merchandiseChanged(ItemRow row, SalesRequestEditMerchandiseOptionView merchandise) {
        row.setMerchandise(merchandise);
        if (events != null) {
            events.merchandiseChanged(row.lineId(), merchandise == null ? null : merchandise.id());
        }
    }

    private void buildStatusBadge(String status) {
        String normalized = OrderingFormatters.normalizeStatusKey(status);
        String[] colors = switch (normalized) {
            case OrderingFormatters.STATUS_PENDING -> new String[]{"#FFF4E5", "#D97706"};
            case OrderingFormatters.STATUS_PROCESSING -> new String[]{"#E8F1FF", "#2563EB"};
            case OrderingFormatters.STATUS_SHIPPING -> new String[]{"#F2EAFF", "#7C3AED"};
            case OrderingFormatters.STATUS_COMPLETED -> new String[]{"#EAF8EF", "#15803D"};
            case OrderingFormatters.STATUS_CANCELLED -> new String[]{"#FEE2E2", "#B91C1C"};
            default -> new String[]{"#F3F4F6", "#6B7280"};
        };

        Label badge = new Label("● " + OrderingFormatters.requestStatusText(status));
        badge.setStyle("-fx-background-color:" + colors[0] + "; -fx-text-fill:" + colors[1]
            + "; -fx-background-radius:999; -fx-padding:4 12; -fx-font-size:12px; -fx-font-weight:bold;");
        statusBadge.getChildren().setAll(badge);
    }

    private ComboBox<SalesRequestEditMerchandiseOptionView> createSearchableComboBox(boolean useCode, ItemRow currentRow) {
        ComboBox<SalesRequestEditMerchandiseOptionView> comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        comboBox.setPrefWidth(useCode ? 110 : 240);

        Supplier<ObservableList<SalesRequestEditMerchandiseOptionView>> available = () -> {
            Set<Integer> usedMerchandiseIds = new HashSet<>();
            for (ItemRow row : allItems) {
                if (row != currentRow && row.merchandise() != null) {
                    usedMerchandiseIds.add(row.merchandise().id());
                }
            }
            return FXCollections.observableArrayList(
                merchandiseOptions.stream()
                    .filter(option -> !usedMerchandiseIds.contains(option.id()))
                    .toList()
            );
        };

        comboBox.setItems(available.get());
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SalesRequestEditMerchandiseOptionView merchandise) {
                return merchandise == null ? "" : (useCode ? merchandise.code() : merchandise.name());
            }

            @Override
            public SalesRequestEditMerchandiseOptionView fromString(String text) {
                return comboBox.getItems().stream()
                    .filter(merchandise -> String.valueOf(useCode ? merchandise.code() : merchandise.name()).equals(text))
                    .findFirst()
                    .orElse(null);
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            SalesRequestEditMerchandiseOptionView selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected != null && String.valueOf(useCode ? selected.code() : selected.name()).equals(newValue)) {
                return;
            }

            ObservableList<SalesRequestEditMerchandiseOptionView> current = available.get();
            if (newValue == null || newValue.isBlank()) {
                comboBox.setItems(current);
            } else {
                String lower = newValue.toLowerCase(Locale.ROOT);
                comboBox.setItems(FXCollections.observableArrayList(
                    current.stream()
                        .filter(merchandise -> contains(useCode ? merchandise.code() : merchandise.name(), lower))
                        .toList()
                ));
            }
            if (comboBox.getItems().isEmpty()) {
                comboBox.hide();
            } else {
                comboBox.show();
            }
        });

        comboBox.setOnShowing(event -> {
            if (comboBox.getEditor().getText() == null || comboBox.getEditor().getText().isBlank()) {
                comboBox.setItems(available.get());
            }
        });
        return comboBox;
    }

    private ItemRow findRow(int lineId) {
        return allItems.stream()
            .filter(row -> row.lineId() == lineId)
            .findFirst()
            .orElse(null);
    }

    private boolean hasViolation(int lineId, String field) {
        return validation.violations().stream()
            .anyMatch(violation -> violation.lineId() == lineId && violation.field().equals(field));
    }

    private String unitOf(SalesRequestEditMerchandiseOptionView merchandise) {
        return merchandise == null ? "" : merchandise.unit();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private BigDecimal parseQuantity(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(rawValue);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static void applyInputStyle(Node node, boolean invalid) {
        node.setStyle(invalid ? "-fx-border-color: #EF4444;" : "");
    }

    private Window ownerWindow() {
        Scene scene = itemsTable == null ? null : itemsTable.getScene();
        return scene == null ? null : scene.getWindow();
    }
}
