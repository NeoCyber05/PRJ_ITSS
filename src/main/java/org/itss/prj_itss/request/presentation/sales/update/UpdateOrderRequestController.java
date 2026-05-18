package org.itss.prj_itss.request.presentation.sales.update;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.request.application.sales.MerchandiseOption;
import org.itss.prj_itss.request.application.sales.RequestFormView;
import org.itss.prj_itss.request.application.sales.RequestItemInput;
import org.itss.prj_itss.request.application.sales.RequestSalesApplicationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

public final class UpdateOrderRequestController {

    private static final java.time.format.DateTimeFormatter DATE_FORMAT = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int PAGE_SIZE = 10;

    private RequestSalesApplicationService salesService;
    private Stage              stage;
    private Runnable           onUpdateSuccess;
    private int                currentRequestId = -1;
    private int                currentPage = 0;

    private final ObservableList<ItemRow> allItems     = FXCollections.observableArrayList();
    private FilteredList<ItemRow>         filteredItems;
    private final ObservableList<ItemRow> pageItems    = FXCollections.observableArrayList();

    @FXML private Label    headerTitle;
    @FXML private Button   closeButton;
    @FXML private Button   cancelButton;
    @FXML private Label    requestCodeLabel;
    @FXML private Label    createdAtLabel;
    @FXML private HBox     statusBadge;
    @FXML private ScrollPane mainScrollPane;

    @FXML private TextField searchField;
    @FXML private Button    addItemButton;
    @FXML private Label     selectedCountLabel;
    @FXML private Button    bulkDeleteButton;

    @FXML private TableView<ItemRow>           itemsTable;
    @FXML private TableColumn<ItemRow, Boolean>    checkboxColumn;
    @FXML private TableColumn<ItemRow, MerchandiseOption> merchandiseCodeColumn;
    @FXML private TableColumn<ItemRow, MerchandiseOption> merchandiseNameColumn;
    @FXML private TableColumn<ItemRow, ItemRow>     quantityColumn;
    @FXML private TableColumn<ItemRow, String>      unitColumn;
    @FXML private TableColumn<ItemRow, ItemRow>     desiredDateColumn;
    @FXML private TableColumn<ItemRow, ItemRow>     actionColumn;

    @FXML private Label itemCountLabel;
    @FXML private HBox  paginationBox;
    @FXML private Label errorLabel;
    @FXML private Button cancelFormButton;
    @FXML private Button updateButton;

    void init(Stage stage, int requestId, ApplicationContext context, Runnable onUpdateSuccess) {
        this.stage            = stage;
        this.currentRequestId = requestId;
        this.salesService     = context.requestSalesApplicationService();
        this.onUpdateSuccess  = onUpdateSuccess;
        setupUI();
        loadData();
    }

    private void setupUI() {
        filteredItems = new FilteredList<>(allItems, r -> true);

        closeButton.setOnAction(e -> goBack());
        cancelButton.setOnAction(e -> goBack());
        cancelFormButton.setOnAction(e -> goBack());
        updateButton.setOnAction(e -> saveChanges());
        addItemButton.setOnAction(e -> addNewRow());
        bulkDeleteButton.setOnAction(e -> bulkDelete());

        setupSearch();
        setupTable();
    }

    private void setupSearch() {
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            debounce.setOnFinished(e -> applySearchFilter(newVal));
            debounce.playFromStart();
        });
    }

    private void applySearchFilter(String text) {
        String lower = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        filteredItems.setPredicate(row -> {
            if (lower.isEmpty()) return true;
            MerchandiseOption m = row.merchandise.get();
            if (m == null) return true;
            return (m.code() != null && m.code().toLowerCase(Locale.ROOT).contains(lower))
                || (m.name() != null && m.name().toLowerCase(Locale.ROOT).contains(lower));
        });
        currentPage = 0;
        updatePageView();
    }

    private void setupTable() {
        itemsTable.setEditable(true);
        itemsTable.setFixedCellSize(48);
        itemsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        itemsTable.setItems(pageItems);

        itemsTable.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) addNewRow();
        });

        itemsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ItemRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { getStyleClass().remove("error-row"); setStyle(""); return; }
                refreshStyle(item);
                item.merchandise.addListener((o, a, b) -> refreshStyle(item));
                item.quantity.addListener((o, a, b)    -> refreshStyle(item));
                item.desiredDate.addListener((o, a, b) -> refreshStyle(item));
            }
            private void refreshStyle(ItemRow item) {
                if (isRowInvalid(item)) {
                    if (!getStyleClass().contains("error-row")) getStyleClass().add("error-row");
                } else {
                    getStyleClass().remove("error-row");
                }
            }
        });

        allItems.addListener((ListChangeListener<ItemRow>) change -> {
            while (change.next()) {
                for (ItemRow row : change.getAddedSubList()) {
                    row.selected.addListener((obs, o, n) -> refreshBulkDelete());
                }
            }
            refreshBulkDelete();
        });

        List<MerchandiseOption> allMerchandise = salesService.findMerchandiseOptions();
        setupCheckboxColumn();
        setupMerchandiseCodeColumn(allMerchandise);
        setupMerchandiseNameColumn(allMerchandise);
        setupUnitColumn();
        setupQuantityColumn();
        setupDesiredDateColumn();
        setupActionColumn();
    }

    private void setupCheckboxColumn() {
        checkboxColumn.setCellValueFactory(data -> data.getValue().selected);
        checkboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkboxColumn));
    }

    private void setupMerchandiseCodeColumn(List<MerchandiseOption> all) {
        merchandiseCodeColumn.setCellValueFactory(data -> data.getValue().merchandise);
        merchandiseCodeColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(MerchandiseOption item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                ItemRow row = getTableRow().getItem();
                
                if (row.merchandise.get() != null) {
                    Label label = new Label(row.merchandise.get().code());
                    label.setStyle("-fx-text-fill: #1F2937; -fx-font-weight: bold;");
                    setGraphic(label);
                } else {
                    ComboBox<MerchandiseOption> cb = createSearchableComboBox(all, true, row);
                    cb.valueProperty().addListener((obs, o, n) -> { row.merchandise.set(n); validateForm(); });
                    setGraphic(cb);
                }
            }
        });
    }

    private void setupMerchandiseNameColumn(List<MerchandiseOption> all) {
        merchandiseNameColumn.setCellValueFactory(data -> data.getValue().merchandise);
        merchandiseNameColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(MerchandiseOption item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                ItemRow row = getTableRow().getItem();

                if (row.merchandise.get() != null) {
                    Label label = new Label(row.merchandise.get().name());
                    label.setStyle("-fx-text-fill: #4B5563;");
                    setGraphic(label);
                } else {
                    ComboBox<MerchandiseOption> cb = createSearchableComboBox(all, false, row);
                    cb.valueProperty().addListener((obs, o, n) -> { row.merchandise.set(n); validateForm(); });
                    setGraphic(cb);
                }
            }
        });
    }

    private void setupUnitColumn() {
        unitColumn.setCellValueFactory(data -> {
            SimpleStringProperty prop = new SimpleStringProperty();
            MerchandiseOption m = data.getValue().merchandise.get();
            prop.set(m != null ? m.unit() : "");
            data.getValue().merchandise.addListener((obs, o, n) -> prop.set(n != null ? n.unit() : ""));
            return prop;
        });
    }

    private void setupQuantityColumn() {
        quantityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        quantityColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(ItemRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) { setGraphic(null); return; }
                TextField tf = new TextField(row.quantity.get() != null ? row.quantity.get().toPlainString() : "");
                tf.setPrefWidth(100);
                tf.setAlignment(Pos.CENTER_LEFT);
                tf.textProperty().addListener((obs, o, n) -> {
                    if (!n.matches("\\d*(\\.\\d*)?")) { tf.setText(o); return; }
                    try { row.quantity.set(n.isEmpty() ? null : new BigDecimal(n)); }
                    catch (NumberFormatException ex) { row.quantity.set(null); }
                    validateForm();
                });
                row.quantity.addListener((obs, o, n) ->
                    applyInputStyle(tf, n == null || n.compareTo(BigDecimal.ZERO) <= 0));
                applyInputStyle(tf, row.quantity.get() == null || row.quantity.get().compareTo(BigDecimal.ZERO) <= 0);
                setGraphic(tf);
            }
        });
    }

    private void setupDesiredDateColumn() {
        desiredDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        desiredDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(ItemRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) { setGraphic(null); return; }
                DatePicker dp = new DatePicker(row.desiredDate.get());
                dp.setPrefWidth(165);
                dp.valueProperty().addListener((obs, o, n) -> { row.desiredDate.set(n); validateForm(); });
                row.desiredDate.addListener((obs, o, n) ->
                    applyInputStyle(dp, n == null || n.isBefore(LocalDate.now())));
                applyInputStyle(dp, row.desiredDate.get() == null || row.desiredDate.get().isBefore(LocalDate.now()));
                setGraphic(dp);
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(ItemRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) { setGraphic(null); return; }
                
                SVGPath trashIcon = new SVGPath();
                trashIcon.setContent("M9 3v1H4v2h1v13a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V6h1V4h-5V3H9m2 2h2v1h-2V5m-4 3h2v10H7V8m4 0h2v10h-2V8m4 0h2v10h-2V8Z");
                trashIcon.setFill(javafx.scene.paint.Color.web("#EF4444"));
                trashIcon.setScaleX(0.8);
                trashIcon.setScaleY(0.8);

                Button del = new Button();
                del.setGraphic(trashIcon);
                del.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
                del.setOnAction(e -> { allItems.remove(row); updatePageView(); validateForm(); });
                
                HBox box = new HBox(del);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
    }

    private void updatePageView() {
        int total      = filteredItems.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0)          currentPage = 0;

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        pageItems.setAll(filteredItems.subList(from, to));

        itemCountLabel.setText(total == 0
            ? "Không có mặt hàng"
            : "Hiển thị " + (from + 1) + " – " + to + " / " + total + " mặt hàng");

        buildPaginationButtons(totalPages);
    }

    private void buildPaginationButtons(int totalPages) {
        paginationBox.getChildren().clear();
        if (totalPages <= 1) return;

        Button prev = pageBtn("<");
        prev.setDisable(currentPage <= 0);
        prev.setOnAction(e -> { currentPage--; updatePageView(); });
        paginationBox.getChildren().add(prev);

        for (int i = 0; i < totalPages; i++) {
            boolean active = (i == currentPage);
            Button b = pageBtn(String.valueOf(i + 1));
            b.setStyle(pageBtnStyle(active));
            int p = i;
            b.setOnAction(e -> { currentPage = p; updatePageView(); });
            paginationBox.getChildren().add(b);
        }

        Button next = pageBtn(">");
        next.setDisable(currentPage >= totalPages - 1);
        next.setOnAction(e -> { currentPage++; updatePageView(); });
        paginationBox.getChildren().add(next);
    }

    private Button pageBtn(String text) {
        Button b = new Button(text);
        b.setStyle(pageBtnStyle(false));
        return b;
    }

    private String pageBtnStyle(boolean active) {
        return active
            ? "-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;"
            : "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-background-radius: 6; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;";
    }

    private void refreshBulkDelete() {
        long count = allItems.stream().filter(r -> r.selected.get()).count();
        boolean show = count > 0;
        bulkDeleteButton.setVisible(show);
        bulkDeleteButton.setManaged(show);
        selectedCountLabel.setVisible(show);
        selectedCountLabel.setManaged(show);
        if (show) selectedCountLabel.setText("Đã chọn " + count + " dòng");
    }

    private void bulkDelete() {
        allItems.removeIf(r -> r.selected.get());
        currentPage = 0;
        updatePageView();
        validateForm();
    }

    private void loadData() {
        if (currentRequestId <= 0) return;
        RequestFormView form = salesService.findFormView(currentRequestId);
        if (form == null) return;

        requestCodeLabel.setText(form.requestCode());
        createdAtLabel.setText(form.createdAt() != null && !form.createdAt().isBlank()
            ? form.createdAt() : "N/A");
        buildStatusBadge(form.status());

        for (RequestFormView.RequestItemFormRow row : form.items()) {
            if (row.merchandise() == null) continue;
            BigDecimal qty = null;
            try { qty = new BigDecimal(row.quantity()); } catch (Exception ignored) {}
            LocalDate date = null;
            try { date = LocalDate.parse(row.desiredDate(), DATE_FORMAT); } catch (Exception ignored) {}
            allItems.add(new ItemRow(row.merchandise(), qty, date));
        }

        updatePageView();
        validateForm();
    }

    private void buildStatusBadge(String status) {
        String norm = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        String[] colors = switch (norm) {
            case "pending"    -> new String[]{"#FFF4E5", "#D97706"};
            case "processing" -> new String[]{"#E8F1FF", "#2563EB"};
            case "shipping"   -> new String[]{"#F2EAFF", "#7C3ED"};
            case "completed"  -> new String[]{"#EAF8EF", "#15803D"};
            case "cancelled"  -> new String[]{"#FEE2E2", "#B91C1C"};
            default           -> new String[]{"#F3F4F6", "#6B7280"};
        };
        String display = switch (norm) {
            case "pending"    -> "Chờ xử lý";
            case "processing" -> "Đang xử lý";
            case "shipping"   -> "Đang giao";
            case "completed"  -> "Đã hoàn thành";
            case "cancelled"  -> "Đã hủy";
            default           -> status != null ? status : "N/A";
        };
        Label badge = new Label("● " + display);
        badge.setStyle("-fx-background-color:" + colors[0] + "; -fx-text-fill:" + colors[1] +
            "; -fx-background-radius:999; -fx-padding:4 12; -fx-font-size:12px; -fx-font-weight:bold;");
        statusBadge.getChildren().setAll(badge);
    }

    private boolean isRowInvalid(ItemRow row) {
        if (row.merchandise.get() == null) return true;
        if (row.quantity.get() == null || row.quantity.get().compareTo(BigDecimal.ZERO) <= 0) return true;
        if (row.desiredDate.get() == null || row.desiredDate.get().isBefore(LocalDate.now())) return true;
        return false;
    }

    private void validateForm() {
        if (allItems.isEmpty()) { showError("Phải có ít nhất 1 mặt hàng."); updateButton.setDisable(true); return; }

        Set<Integer> seen = new HashSet<>();
        for (ItemRow row : allItems) {
            if (row.merchandise.get() == null) {
                showError("Vui lòng chọn mặt hàng cho tất cả các dòng."); updateButton.setDisable(true); return;
            }
            if (!seen.add(row.merchandise.get().id())) {
                showError("Không được chọn trùng mặt hàng."); updateButton.setDisable(true); return;
            }
            if (row.quantity.get() == null || row.quantity.get().compareTo(BigDecimal.ZERO) <= 0) {
                showError("Số lượng phải là số lớn hơn 0."); updateButton.setDisable(true); return;
            }
            if (row.desiredDate.get() == null || row.desiredDate.get().isBefore(LocalDate.now())) {
                showError("Ngày nhận không hợp lệ."); updateButton.setDisable(true); return;
            }
        }
        errorLabel.setVisible(false);
        updateButton.setDisable(false);
    }

    private void scrollToFirstError() {
        for (int i = 0; i < allItems.size(); i++) {
            if (isRowInvalid(allItems.get(i))) {
                int fi = filteredItems.indexOf(allItems.get(i));
                if (fi >= 0) {
                    currentPage = fi / PAGE_SIZE;
                    updatePageView();
                    itemsTable.scrollTo(fi % PAGE_SIZE);
                    itemsTable.getSelectionModel().select(fi % PAGE_SIZE);
                }
                return;
            }
        }
    }

    private void addNewRow() {
        ItemRow row = new ItemRow();
        row.selected.addListener((obs, o, n) -> refreshBulkDelete());
        allItems.add(row);
        int total = filteredItems.size();
        currentPage = Math.max(0, (int) Math.ceil((double) total / PAGE_SIZE) - 1);
        updatePageView();
        itemsTable.scrollTo(pageItems.size() - 1);
        validateForm();
    }

    private void saveChanges() {
        validateForm();
        if (updateButton.isDisabled()) { scrollToFirstError(); return; }

        List<RequestItemInput> requestItems = allItems.stream().map(row ->
            new RequestItemInput(
                row.merchandise.get().id(),
                row.quantity.get(),
                row.desiredDate.get()
            )
        ).toList();

        try {
            salesService.updateRequest(currentRequestId, requestItems, null);
        } catch (Exception ex) {
            showError("Có lỗi xảy ra: " + ex.getMessage());
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cập nhật yêu cầu đặt hàng thành công", ButtonType.OK);
        alert.setHeaderText(null);
        alert.initOwner(stage);
        alert.showAndWait();

        if (onUpdateSuccess != null) onUpdateSuccess.run();
        goBack();
    }

    private void goBack() {
        if (stage != null) stage.close();
    }

    private ComboBox<MerchandiseOption> createSearchableComboBox(List<MerchandiseOption> all, boolean useCode, ItemRow currentRow) {
        ComboBox<MerchandiseOption> cb = new ComboBox<>();
        cb.setEditable(true);
        cb.setPrefWidth(useCode ? 110 : 240);

        Supplier<ObservableList<MerchandiseOption>> available = () -> {
            ObservableList<MerchandiseOption> list = FXCollections.observableArrayList();
            for (MerchandiseOption m : all) {
                boolean used = allItems.stream()
                    .filter(r -> r != currentRow)
                    .anyMatch(r -> r.merchandise.get() != null && r.merchandise.get().id() == m.id());
                if (!used) list.add(m);
            }
            return list;
        };

        cb.setItems(available.get());
        cb.setConverter(new StringConverter<>() {
            @Override public String toString(MerchandiseOption m)   { return m == null ? "" : (useCode ? m.code() : m.name()); }
            @Override public MerchandiseOption fromString(String s) {
                return cb.getItems().stream()
                    .filter(m -> (useCode ? m.code() : m.name()).equals(s))
                    .findFirst().orElse(null);
            }
        });

        cb.getEditor().textProperty().addListener((obs, o, n) -> {
            MerchandiseOption sel = cb.getSelectionModel().getSelectedItem();
            if (sel != null && (useCode ? sel.code() : sel.name()).equals(n)) return;
            ObservableList<MerchandiseOption> cur = available.get();
            if (n == null || n.isEmpty()) {
                cb.setItems(cur);
            } else {
                String lower = n.toLowerCase(Locale.ROOT);
                cb.setItems(FXCollections.observableArrayList(
                    cur.stream().filter(m -> {
                        String v = useCode ? m.code() : m.name();
                        return v != null && v.toLowerCase(Locale.ROOT).contains(lower);
                    }).toList()
                ));
            }
            if (!cb.getItems().isEmpty()) cb.show(); else cb.hide();
        });

        cb.setOnShowing(e -> { if (cb.getEditor().getText() == null || cb.getEditor().getText().isEmpty()) cb.setItems(available.get()); });
        return cb;
    }

    private static void applyInputStyle(javafx.scene.Node node, boolean invalid) {
        node.setStyle(invalid ? "-fx-border-color: #EF4444;" : "");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
