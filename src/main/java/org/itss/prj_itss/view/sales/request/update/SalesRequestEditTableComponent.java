package org.itss.prj_itss.view.sales.request.update;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditViewState;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

final class SalesRequestEditTableComponent {

    private final Button bulkDeleteButton;
    private final Label selectedCountLabel;
    private final TableView<SalesRequestEditItemRow> itemsTable;
    private final TableColumn<SalesRequestEditItemRow, Boolean> checkboxColumn;
    private final TableColumn<SalesRequestEditItemRow, MerchandiseOption> merchandiseCodeColumn;
    private final TableColumn<SalesRequestEditItemRow, MerchandiseOption> merchandiseNameColumn;
    private final TableColumn<SalesRequestEditItemRow, SalesRequestEditItemRow> quantityColumn;
    private final TableColumn<SalesRequestEditItemRow, String> unitColumn;
    private final TableColumn<SalesRequestEditItemRow, SalesRequestEditItemRow> desiredDateColumn;
    private final TableColumn<SalesRequestEditItemRow, SalesRequestEditItemRow> actionColumn;
    private final Label itemCountLabel;
    private final HBox paginationBox;
    private final SalesRequestEditPaginator paginator = new SalesRequestEditPaginator();
    private final ObservableList<SalesRequestEditItemRow> allItems = FXCollections.observableArrayList();
    private final ObservableList<SalesRequestEditItemRow> pageItems = FXCollections.observableArrayList();
    private final FilteredList<SalesRequestEditItemRow> filteredItems = new FilteredList<>(allItems, row -> true);

    private SalesRequestEditTableActions actions;
    private SalesRequestEditViewState.Validation validationResult = new SalesRequestEditViewState.Validation(List.of());
    private Map<Integer, List<MerchandiseOption>> availableOptionsByLineId = Map.of();

    SalesRequestEditTableComponent(
            Button bulkDeleteButton,
            Label selectedCountLabel,
            TableView<SalesRequestEditItemRow> itemsTable,
            TableColumn<SalesRequestEditItemRow, Boolean> checkboxColumn,
            TableColumn<SalesRequestEditItemRow, MerchandiseOption> merchandiseCodeColumn,
            TableColumn<SalesRequestEditItemRow, MerchandiseOption> merchandiseNameColumn,
            TableColumn<SalesRequestEditItemRow, SalesRequestEditItemRow> quantityColumn,
            TableColumn<SalesRequestEditItemRow, String> unitColumn,
            TableColumn<SalesRequestEditItemRow, SalesRequestEditItemRow> desiredDateColumn,
            TableColumn<SalesRequestEditItemRow, SalesRequestEditItemRow> actionColumn,
            Label itemCountLabel,
            HBox paginationBox) {
        this.bulkDeleteButton = bulkDeleteButton;
        this.selectedCountLabel = selectedCountLabel;
        this.itemsTable = itemsTable;
        this.checkboxColumn = checkboxColumn;
        this.merchandiseCodeColumn = merchandiseCodeColumn;
        this.merchandiseNameColumn = merchandiseNameColumn;
        this.quantityColumn = quantityColumn;
        this.unitColumn = unitColumn;
        this.desiredDateColumn = desiredDateColumn;
        this.actionColumn = actionColumn;
        this.itemCountLabel = itemCountLabel;
        this.paginationBox = paginationBox;
    }

    void initialize(SalesRequestEditTableActions actions) {
        this.actions = actions;
        bulkDeleteButton.setOnAction(event -> deleteSelectedRows());
        setupTable();
    }

    void renderItems(
            List<SalesRequestEditViewState.Item> items,
            Map<Integer, List<MerchandiseOption>> availableOptionsByLineId,
            String searchText) {
        this.availableOptionsByLineId = copyOptionsByLineId(availableOptionsByLineId);
        allItems.setAll(items.stream().map(SalesRequestEditItemRow::new).toList());
        applySearchFilter(searchText);
    }

    void renderValidation(SalesRequestEditViewState.Validation validationResult) {
        this.validationResult = validationResult;
        itemsTable.refresh();
    }

    void applySearchFilter(String text) {
        String lower = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        filteredItems.setPredicate(row -> {
            if (lower.isEmpty()) {
                return true;
            }
            MerchandiseOption merchandise = row.merchandise();
            if (merchandise == null) {
                return true;
            }
            return SalesRequestEditViewSupport.contains(merchandise.code(), lower)
                    || SalesRequestEditViewSupport.contains(merchandise.name(), lower);
        });
        paginator.keepCurrentPageValid(filteredItems.size());
        refreshPage();
    }

    void focusFirstViolation(List<SalesRequestEditViewState.FieldViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            return;
        }

        for (SalesRequestEditViewState.FieldViolation violation : violations) {
            if (violation.lineId() <= 0) {
                return;
            }
            SalesRequestEditItemRow row = findRow(violation.lineId());
            if (row == null) {
                continue;
            }
            int filteredIndex = filteredItems.indexOf(row);
            int pageIndex = paginator.focusPageForIndex(filteredIndex);
            if (pageIndex < 0) {
                continue;
            }
            refreshPage();
            itemsTable.scrollTo(pageIndex);
            itemsTable.getSelectionModel().select(pageIndex);
            return;
        }
    }

    private void setupTable() {
        itemsTable.setEditable(true);
        itemsTable.setFixedCellSize(74);
        itemsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        itemsTable.setItems(pageItems);

        itemsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && actions != null) {
                actions.addItem().run();
            }
        });

        itemsTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(SalesRequestEditItemRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("error-row");
                if (!empty && item != null && validationResult.hasViolation(item.lineId())) {
                    getStyleClass().add("error-row");
                }
            }
        });

        allItems.addListener((ListChangeListener<SalesRequestEditItemRow>) change -> {
            while (change.next()) {
                for (SalesRequestEditItemRow row : change.getAddedSubList()) {
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
            protected void updateItem(MerchandiseOption item, boolean empty) {
                super.updateItem(item, empty);
                SalesRequestEditItemRow row = currentRow();
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                if (row.merchandise() != null) {
                    Label label = new Label(row.merchandise().code());
                    String validationMessage = violationMessage(row.lineId(), "merchandise");
                    label.setStyle(validationMessage == null
                            ? "-fx-text-fill: #1F2937; -fx-font-weight: bold;"
                            : "-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                    label.setTooltip(validationMessage == null ? null : new Tooltip(validationMessage));
                    setGraphic(label);
                    return;
                }

                ComboBox<MerchandiseOption> comboBox = createMerchandiseComboBox(true, row);
                SalesRequestEditViewSupport.applyValidationFeedback(
                        comboBox,
                        violationMessage(row.lineId(), "merchandise"));
                setGraphic(comboBox);
            }

            private SalesRequestEditItemRow currentRow() {
                return getTableRow() == null ? null : getTableRow().getItem();
            }
        });
    }

    private void setupMerchandiseNameColumn() {
        merchandiseNameColumn.setCellValueFactory(data -> data.getValue().merchandiseProperty());
        merchandiseNameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(MerchandiseOption item, boolean empty) {
                super.updateItem(item, empty);
                SalesRequestEditItemRow row = currentRow();
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                if (row.merchandise() != null) {
                    Label label = new Label(row.merchandise().name());
                    String validationMessage = violationMessage(row.lineId(), "merchandise");
                    label.setStyle(validationMessage == null ? "-fx-text-fill: #4B5563;" : "-fx-text-fill: #DC2626;");
                    label.setTooltip(validationMessage == null ? null : new Tooltip(validationMessage));
                    setGraphic(label);
                    return;
                }

                ComboBox<MerchandiseOption> comboBox = createMerchandiseComboBox(false, row);
                SalesRequestEditViewSupport.applyValidationFeedback(
                        comboBox,
                        violationMessage(row.lineId(), "merchandise"));
                setGraphic(comboBox);
            }

            private SalesRequestEditItemRow currentRow() {
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
            private SalesRequestEditItemRow currentRow;

            {
                textField.setPrefWidth(100);
                textField.setAlignment(Pos.CENTER_LEFT);
                textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused && currentRow != null) {
                        commitValue();
                    }
                });
                textField.setOnAction(event -> {
                    if (currentRow != null) {
                        commitValue();
                    }
                });
            }

            private void commitValue() {
                if (currentRow == null) {
                    return;
                }
                String rawQuantity = textField.getText();
                if (Objects.equals(currentRow.rawQuantity(), rawQuantity)) {
                    return;
                }
                BigDecimal quantity = SalesRequestEditViewSupport.parseQuantity(rawQuantity);
                currentRow.setRawQuantity(rawQuantity);
                currentRow.setQuantity(quantity);
                if (actions != null) {
                    actions.quantityChanged().accept(currentRow.lineId(), rawQuantity);
                }
            }

            @Override
            protected void updateItem(SalesRequestEditItemRow row, boolean empty) {
                super.updateItem(row, empty);
                currentRow = row;
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                textField.setText(row.rawQuantity());
                String validationMessage = quantityValidationMessage(row);
                SalesRequestEditViewSupport.applyValidationFeedback(textField, validationMessage);
                setGraphic(inputWithValidationMessage(textField, validationMessage));
            }
        });
    }

    private void setupDesiredDateColumn() {
        desiredDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        desiredDateColumn.setCellFactory(column -> new TableCell<>() {
            private final DatePicker datePicker = new DatePicker();
            private SalesRequestEditItemRow currentRow;
            private boolean internalUpdate;

            {
                datePicker.setPrefWidth(165);
                datePicker.setEditable(false);
                datePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
                    if (internalUpdate || currentRow == null) {
                        return;
                    }
                    currentRow.setDesiredDate(newValue);
                    if (actions != null) {
                        actions.desiredDateChanged().accept(currentRow.lineId(), newValue);
                    }
                });
                datePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (empty || date == null) {
                            setDisable(false);
                            setTooltip(null);
                            setStyle("");
                            return;
                        }
                        setDisable(false);
                        if (isBlockedDesiredDate(date)) {
                            setDisable(true);
                            setTooltip(new Tooltip("Chỉ được chọn ngày sau hôm nay."));
                            setStyle(
                                    "-fx-background-color: #F3F4F6; -fx-text-fill: #9CA3AF; -fx-border-color: transparent;");
                        } else if (datePicker.getValue() != null && date.equals(datePicker.getValue())) {
                            setTooltip(null);
                            setStyle(
                                    "-fx-background-color: #bfdbfe; -fx-text-fill: #1e3a8a; -fx-font-weight: bold; -fx-border-color: transparent;");
                        } else {
                            setTooltip(null);
                            setStyle("");
                        }
                    }
                });
            }

            @Override
            protected void updateItem(SalesRequestEditItemRow row, boolean empty) {
                super.updateItem(row, empty);
                currentRow = row;
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                internalUpdate = true;
                datePicker.setValue(row.desiredDate());
                internalUpdate = false;
                String validationMessage = desiredDateValidationMessage(row);
                SalesRequestEditViewSupport.applyValidationFeedback(datePicker, validationMessage);
                setGraphic(inputWithValidationMessage(datePicker, validationMessage));
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SalesRequestEditItemRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                Button deleteButton = new Button();
                deleteButton.setGraphic(SalesRequestEditViewSupport.trashIcon());
                deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
                deleteButton.setOnAction(event -> {
                    if (actions != null) {
                        actions.deleteItem().accept(row.lineId());
                    }
                });

                HBox box = new HBox(deleteButton);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
    }

    private ComboBox<MerchandiseOption> createMerchandiseComboBox(
            boolean useCode,
            SalesRequestEditItemRow row) {
        return SalesRequestEditMerchandiseComboBoxFactory.create(
                useCode,
                availableOptionsByLineId.getOrDefault(row.lineId(), List.of()),
                selected -> merchandiseChanged(row, selected));
    }

    private Map<Integer, List<MerchandiseOption>> copyOptionsByLineId(
            Map<Integer, List<MerchandiseOption>> optionsByLineId) {
        return optionsByLineId.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())));
    }

    private void refreshPage() {
        SalesRequestPaginationModel model = paginator.model(filteredItems.size());
        pageItems.setAll(filteredItems.subList(model.fromIndex(), model.toIndex()));
        itemCountLabel.setText(filteredItems.isEmpty()
                ? "Không có mặt hàng"
                : "Hiển thị " + (model.fromIndex() + 1) + " - " + model.toIndex() + " / " + filteredItems.size()
                        + " mặt hàng");
        renderPaginationButtons(model);
    }

    private void renderPaginationButtons(SalesRequestPaginationModel model) {
        paginationBox.getChildren().clear();
        if (model.totalPages() <= 1) {
            return;
        }

        Button previous = pageButton("<", false);
        previous.setDisable(!model.hasPrevious());
        previous.setOnAction(event -> {
            paginator.previousPage();
            refreshPage();
        });
        paginationBox.getChildren().add(previous);

        for (Integer pageNumber : model.pageNumbers()) {
            int pageIndex = pageNumber - 1;
            Button button = pageButton(String.valueOf(pageNumber), pageIndex == model.currentPage());
            button.setOnAction(event -> {
                paginator.goToPage(pageIndex);
                refreshPage();
            });
            paginationBox.getChildren().add(button);
        }

        Button next = pageButton(">", false);
        next.setDisable(!model.hasNext());
        next.setOnAction(event -> {
            paginator.nextPage();
            refreshPage();
        });
        paginationBox.getChildren().add(next);
    }

    private void refreshBulkDelete() {
        long count = allItems.stream().filter(SalesRequestEditItemRow::selected).count();
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
        if (actions == null) {
            return;
        }
        List<Integer> lineIds = allItems.stream()
                .filter(SalesRequestEditItemRow::selected)
                .map(SalesRequestEditItemRow::lineId)
                .toList();
        actions.deleteItems().accept(lineIds);
    }

    private void merchandiseChanged(SalesRequestEditItemRow row, MerchandiseOption merchandise) {
        row.setMerchandise(merchandise);
        if (actions != null) {
            actions.merchandiseChanged().accept(row.lineId(), merchandise == null ? null : merchandise.id());
        }
    }

    private SalesRequestEditItemRow findRow(int lineId) {
        return allItems.stream()
                .filter(row -> row.lineId() == lineId)
                .findFirst()
                .orElse(null);
    }

    private String violationMessage(int lineId, String field) {
        return validationResult.violations().stream()
                .filter(violation -> violation.lineId() == lineId && violation.field().equals(field))
                .map(SalesRequestEditViewState.FieldViolation::message)
                .findFirst()
                .orElse(null);
    }

    private String quantityValidationMessage(SalesRequestEditItemRow row) {
        String message = violationMessage(row.lineId(), "quantity");
        if (message == null) {
            return null;
        }
        if (row.rawQuantity() == null || row.rawQuantity().isBlank()) {
            return "Nhập số lượng.";
        }
        return "Số lượng > 0.";
    }

    private String desiredDateValidationMessage(SalesRequestEditItemRow row) {
        String message = violationMessage(row.lineId(), "desiredDate");
        if (message == null) {
            return null;
        }
        if (row.desiredDate() == null) {
            return "Chọn ngày nhận.";
        }
        if (isBlockedDesiredDate(row.desiredDate())) {
            return "Chọn sau hôm nay.";
        }
        return message;
    }

    private Node inputWithValidationMessage(Control input, String message) {
        if (message == null || message.isBlank()) {
            return input;
        }
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(input.getPrefWidth());
        messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 10px; -fx-font-weight: bold;");

        VBox box = new VBox(2, input, messageLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private boolean isBlockedDesiredDate(LocalDate date) {
        return !date.isAfter(LocalDate.now());
    }

    private String unitOf(MerchandiseOption merchandise) {
        return merchandise == null ? "" : merchandise.unit();
    }

    private Button pageButton(String text, boolean active) {
        Button button = new Button(text);
        button.setStyle(active
                ? "-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;"
                : "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-background-radius: 6; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;");
        return button;
    }
}
