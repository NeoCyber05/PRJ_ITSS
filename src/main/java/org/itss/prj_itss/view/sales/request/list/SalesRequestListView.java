package org.itss.prj_itss.view.sales.request.list;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.sales.request.shared.ISalesRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.shared.SalesRequestSavedEvent;
import org.itss.prj_itss.controller.sales.request.list.SalesRequestListController;
import org.itss.prj_itss.controller.sales.request.create.SalesRequestCreationController;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditDialogInput;
import org.itss.prj_itss.controller.sales.request.view.ViewOrderRequestController;
import org.itss.prj_itss.model.request.application.listing.RequestRow;
import org.itss.prj_itss.view.sales.request.create.SalesRequestCreationDialog;
import org.itss.prj_itss.view.sales.request.shared.ISalesRequestEditDialogLauncher;
import org.itss.prj_itss.view.sales.request.view.ViewOrderRequestPopup;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SalesRequestListView implements ViewLifecycle {

    private static final int PAGE_SIZE = 10;

    private static final Map<String, String> STATUS_DISPLAY = Map.of(
        "all", "Tất cả trạng thái",
        "pending", "Chờ xử lý",
        "processing", "Đang xử lý",
        "shipping", "Đang giao",
        "completed", "Đã hoàn thành",
        "cancelled", "Đã hủy"
    );

    private final ObservableList<RequestRow> allRows = FXCollections.observableArrayList();
    private final FilteredList<RequestRow> filteredRows = new FilteredList<>(allRows, row -> true);

    private Navigator navigator;
    private SalesRequestListController controller;
    private SalesRequestCreationController createController;
    private ISalesRequestEditDialogLauncher editDialogLauncher;
    private ViewOrderRequestController viewController;
    private int currentPage = 1;

    @FXML private TableView<RequestRow> requestTable;
    @FXML private TableColumn<RequestRow, String> requestCodeColumn;
    @FXML private TableColumn<RequestRow, String> createdAtColumn;
    @FXML private TableColumn<RequestRow, String> itemCountColumn;
    @FXML private TableColumn<RequestRow, String> deadlineColumn;
    @FXML private TableColumn<RequestRow, String> statusColumn;
    @FXML private TableColumn<RequestRow, RequestRow> actionsColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label paginationInfoLabel;
    @FXML private HBox paginationBox;
    @FXML private Button createRequestButton;

    @FXML
    private void initialize() {
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        requestCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().requestCode()));
        requestCodeColumn.setCellFactory(column -> createStyledCell("-fx-font-weight: bold; -fx-text-fill: #1E3A5F;"));

        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().createdAt()));
        itemCountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().itemCount()));

        deadlineColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().deadline()));
        deadlineColumn.setCellFactory(column -> createStyledCell("-fx-font-weight: bold;"));

        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    setGraphic(buildStatusBadge(status));
                }
                setText(null);
            }
        });

        actionsColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(RequestRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(buildActionButtons(row));
                setText(null);
            }
        });

        setupStatusFilter();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; applyFilters(); });
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; applyFilters(); });

        createRequestButton.getStyleClass().add("btn-create-request");
        createRequestButton.setOnAction(event ->
            SalesRequestCreationDialog.show(requestTable.getScene().getWindow(), createController, this::reload)
        );
    }

    public void init(
            Navigator navigator,
            SalesRequestListController controller,
            SalesRequestCreationController createController,
            ISalesRequestEditDialogLauncher editDialogLauncher,
            ViewOrderRequestController viewController
    ) {
        this.navigator = navigator;
        this.controller = controller;
        this.createController = createController;
        this.editDialogLauncher = editDialogLauncher;
        this.viewController = viewController;
        reload();
    }

    @Override
    public void onViewShown() {
        reload();
    }

    private void reload() {
        if (controller == null) return;
        List<RequestRow> requestRows = controller.getRequests();
        allRows.setAll(requestRows);
        applyFilters();
    }

    // ── Filters & Pagination ──────────────────────────────────────────

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilter.getValue();

        filteredRows.setPredicate(row -> {
            boolean matchesKeyword = keyword.isBlank()
                || row.requestCode().toLowerCase(Locale.ROOT).contains(keyword);
            boolean matchesStatus = selectedStatus == null
                || "all".equalsIgnoreCase(selectedStatus)
                || selectedStatus.equalsIgnoreCase(normalizeStatusKey(row.status()));
            return matchesKeyword && matchesStatus;
        });

        updatePagination();
    }

    private void updatePagination() {
        int totalItems = filteredRows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = (currentPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);

        ObservableList<RequestRow> pageItems = FXCollections.observableArrayList(
            filteredRows.subList(fromIndex, toIndex)
        );
        requestTable.setItems(pageItems);

        paginationInfoLabel.setText(totalItems == 0
            ? "Không có yêu cầu phù hợp"
            : "Hiển thị " + (fromIndex + 1) + " - " + toIndex + " của " + totalItems + " yêu cầu");

        buildPaginationButtons(totalPages);
    }

    private void buildPaginationButtons(int totalPages) {
        paginationBox.getChildren().clear();
        if (totalPages <= 1) {
            return;
        }

        Button prev = new Button("<");
        prev.setStyle(paginationButtonStyle(false));
        prev.setDisable(currentPage <= 1);
        prev.setOnAction(e -> { currentPage--; updatePagination(); });
        paginationBox.getChildren().add(prev);

        for (int i = 1; i <= totalPages; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            boolean isActive = (i == currentPage);
            pageBtn.setStyle(paginationButtonStyle(isActive));
            int page = i;
            pageBtn.setOnAction(e -> { currentPage = page; updatePagination(); });
            paginationBox.getChildren().add(pageBtn);
        }

        Button next = new Button(">");
        next.setStyle(paginationButtonStyle(false));
        next.setDisable(currentPage >= totalPages);
        next.setOnAction(e -> { currentPage++; updatePagination(); });
        paginationBox.getChildren().add(next);
    }

    private String paginationButtonStyle(boolean active) {
        if (active) {
            return "-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; " +
                   "-fx-min-width: 32; -fx-min-height: 32; -fx-font-size: 12px; -fx-cursor: hand;";
        }
        return "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-background-radius: 6; " +
               "-fx-min-width: 32; -fx-min-height: 32; -fx-font-size: 12px; -fx-cursor: hand;";
    }

    // ── Status ────────────────────────────────────────────────────────

    private void setupStatusFilter() {
        statusFilter.getItems().addAll("all", "pending", "processing", "shipping", "completed", "cancelled");
        statusFilter.setValue("all");

        statusFilter.setButtonCell(createStatusListCell());
        statusFilter.setCellFactory(listView -> createStatusListCell());
    }

    private ListCell<String> createStatusListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : STATUS_DISPLAY.getOrDefault(item, item));
            }
        };
    }

    private HBox buildStatusBadge(String status) {
        HBox box = new HBox(7);
        box.setAlignment(Pos.CENTER_LEFT);

        String[] colors = resolveStatusColors(status);
        Circle dot = new Circle(5);
        dot.setFill(Color.web(colors[0]));

        String displayText = STATUS_DISPLAY.getOrDefault(normalizeStatusKey(status), status);
        Label label = new Label(displayText);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + colors[1] + ";");

        box.getChildren().addAll(dot, label);
        return box;
    }

    private String[] resolveStatusColors(String status) {
        return switch (normalizeStatusKey(status)) {
            case "pending"    -> new String[]{"#F59E0B", "#B45309"};
            case "processing" -> new String[]{"#3B82F6", "#1D4ED8"};
            case "shipping"   -> new String[]{"#A855F7", "#7E22CE"};
            case "completed"  -> new String[]{"#22C55E", "#15803D"};
            case "cancelled"  -> new String[]{"#EF4444", "#B91C1C"};
            default           -> new String[]{"#9CA3AF", "#6B7280"};
        };
    }

    // ── Action Buttons ────────────────────────────────────────────────

    private HBox buildActionButtons(RequestRow row) {
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        boolean isPending = row.processable();

        // ── Xem ──────────────────────────────────────────────────────────────
        Button viewBtn = new Button("Xem");
        viewBtn.setOnAction(event ->
            ViewOrderRequestPopup.show(
                requestTable.getScene().getWindow(),
                row.requestId(),
                viewController
            )
        );
        actions.getChildren().add(viewBtn);

        // ── Sửa ──────────────────────────────────────────────────────────────
        if (isPending) {
            Button editBtn = new Button("Sửa");
            editBtn.setStyle("-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
            editBtn.setOnAction(event -> {
                editDialogLauncher.showEdit(
                    requestTable.getScene().getWindow(),
                    new SalesRequestEditDialogInput(row.requestId()),
                    new ISalesRequestDialogListener() {
                        @Override
                        public void onSalesRequestSaved(SalesRequestSavedEvent event) {
                            reload();
                        }
                    }
                );
            });
            actions.getChildren().add(editBtn);
        }

        // ── Xóa ──────────────────────────────────────────────────────────────
        Button deleteBtn = new Button("Xóa");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        deleteBtn.setOnAction(event -> {
            if (!isPending) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.initOwner(requestTable.getScene().getWindow());
                warn.setTitle("Không thể xóa");
                warn.setHeaderText(null);
                warn.setContentText(
                    "Chỉ có thể xóa yêu cầu ở trạng thái \"Chờ xử lý\".\n" +
                    "Yêu cầu đang ở trạng thái: " +
                    STATUS_DISPLAY.getOrDefault(normalizeStatusKey(row.status()), row.status()) + "."
                );
                warn.showAndWait();
                return;
            }

            // Confirm before deleting
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(requestTable.getScene().getWindow());
            confirm.setTitle("Xác nhận xóa");
            confirm.setHeaderText("Xóa yêu cầu " +
                String.format("YC-2026-%03d", row.requestId()) + "?");
            confirm.setContentText(
                "Hành động này sẽ xóa vĩnh viễn yêu cầu và toàn bộ mặt hàng đi kèm.\n" +
                "Bạn có chắc chắn muốn tiếp tục?"
            );

            confirm.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    boolean success = controller.deleteRequest(row.requestId());
                    if (success) {
                        reload();
                    } else {
                        Alert err = new Alert(Alert.AlertType.ERROR);
                        err.initOwner(requestTable.getScene().getWindow());
                        err.setTitle("Lỗi");
                        err.setHeaderText(null);
                        err.setContentText("Xóa yêu cầu thất bại. Vui lòng thử lại.");
                        err.showAndWait();
                    }
                }
            });
        });
        actions.getChildren().add(deleteBtn);

        return actions;
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private String normalizeStatusKey(String status) {
        if (status == null) return "other";
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? "other" : normalized;
    }

    private <T> TableCell<RequestRow, String> createStyledCell(String style) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(style);
                }
            }
        };
    }
}
