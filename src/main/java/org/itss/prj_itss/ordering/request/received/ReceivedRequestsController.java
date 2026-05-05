package org.itss.prj_itss.ordering.request.received;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;
import org.itss.prj_itss.ordering.request.detail.RequestDetailPopup;
import org.itss.prj_itss.service.RequestService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReceivedRequestsController implements IViewController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObservableList<RequestRow> rows = FXCollections.observableArrayList();
    private final FilteredList<RequestRow> filteredRows = new FilteredList<>(rows, row -> true);
    private final ObservableList<RequestRow> paginatedRows = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int pageSize = 10;

    private INavigator navigator;
    private ApplicationContext context;
    private RequestService requestService;

    @FXML
    private TableView<RequestRow> requestTable;

    @FXML
    private TableColumn<RequestRow, String> requestCodeColumn;

    @FXML
    private TableColumn<RequestRow, String> createdAtColumn;

    @FXML
    private TableColumn<RequestRow, String> itemCountColumn;

    @FXML
    private TableColumn<RequestRow, String> deadlineColumn;

    @FXML
    private TableColumn<RequestRow, String> statusColumn;

    @FXML
    private TableColumn<RequestRow, RequestRow> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private Label paginationInfoLabel;

    @FXML
    private ComboBox<Integer> pageSizeComboBox;

    @FXML
    private Button prevPageButton;

    @FXML
    private Button nextPageButton;

    @FXML
    private Label pageIndicatorLabel;

    @FXML
    private void initialize() {
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Ép chiều cao bảng tự động co giãn theo số lượng bản ghi thực tế đang hiển thị
        requestTable.setFixedCellSize(46); // Khớp với thuộc tính -fx-fixed-cell-size: 46 trong file CSS
        requestTable.prefHeightProperty().bind(
            Bindings.max(1, Bindings.size(paginatedRows)).multiply(requestTable.getFixedCellSize()).add(36) // 36px để triệt tiêu hoàn toàn sai số phần header
        );
        requestTable.minHeightProperty().bind(requestTable.prefHeightProperty());
        requestTable.maxHeightProperty().bind(requestTable.prefHeightProperty());

        requestCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().requestCode()));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().createdAt()));
        itemCountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().itemCount()));
        deadlineColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().deadline()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    HBox badge = buildStatusDot(status);
                    badge.setMinWidth(160);
                    setGraphic(badge);
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

                HBox actions = new HBox(8);
                actions.setAlignment(Pos.CENTER_LEFT);

                Button detailButton = new Button("Chi tiết");
                detailButton.setOnAction(event -> RequestDetailPopup.show(
                    requestTable.getScene() == null ? null : requestTable.getScene().getWindow(),
                    row.requestCode(),
                    context,
                    navigator
                ));
                actions.getChildren().add(detailButton);

                if ("pending".equals(normalizeStatusKey(row.status()))) {
                    Button processButton = new Button("Xử lý");
                    processButton.setStyle("-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14;");
                    processButton.setOnAction(event -> navigator.showView("request-processing:" + row.request().getId()));
                    actions.getChildren().add(processButton);
                }

                setGraphic(actions);
                setText(null);
            }
        });

        requestTable.setItems(paginatedRows);

        statusFilter.getItems().addAll(
            "all",
            "pending",
            "processing",
            "shipping",
            "completed",
            "cancelled"
        );
        statusFilter.setValue("all");

        pageSizeComboBox.getItems().addAll(5, 10, 20, 50);
        pageSizeComboBox.setValue(pageSize);
        pageSizeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                pageSize = newValue;
                currentPage = 0;
                updatePagination();
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    @Override
    public void init(INavigator navigator, ApplicationContext context) {
        this.navigator = navigator;
        this.context = context;
        this.requestService = context.requestService();
        reload();
    }

    private void reload() {
        List<RequestRow> requestRows = requestService.findAll().stream()
            .map(this::toRow)
            .toList();
        rows.setAll(requestRows);
        applyFilters();
    }

    private RequestRow toRow(Request request) {
        LocalDate earliestDelivery = requestService.getEarliestDeliveryDate(request.getId());
        String createdAt = request.getCreatedAt() == null ? "" : request.getCreatedAt().toLocalDate().format(DATE_FORMAT);
        String deadline = earliestDelivery == null ? "N/A" : earliestDelivery.format(DATE_FORMAT);
        return new RequestRow(
            request,
            String.format("YC-2026-%03d", request.getId()),
            createdAt,
            requestService.countItemTypes(request.getId()) + " loại",
            deadline,
            request.getStatus() == null ? "N/A" : request.getStatus()
        );
    }

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

        currentPage = 0;
        updatePagination();
    }

    private void updatePagination() {
        int totalItems = filteredRows.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        if (fromIndex <= totalItems && fromIndex < toIndex) {
            paginatedRows.setAll(filteredRows.subList(fromIndex, toIndex));
        } else {
            paginatedRows.clear();
        }

        int displayFrom = totalItems == 0 ? 0 : fromIndex + 1;
        paginationInfoLabel.setText(totalItems == 0
            ? "Không có yêu cầu phù hợp"
            : String.format("Hiển thị %d - %d của %d yêu cầu", displayFrom, toIndex, totalItems));

        pageIndicatorLabel.setText(String.format("Trang %d / %d", currentPage + 1, totalPages));

        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    private void goToPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePagination();
        }
    }

    @FXML
    private void goToNextPage() {
        int totalItems = filteredRows.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePagination();
        }
    }

    private HBox buildStatusDot(String status) {
        HBox box = new HBox(7);
        box.setAlignment(Pos.CENTER_LEFT);

        String[] colors = resolveStatusDotColors(status);
        Circle dot = new Circle(5);
        dot.setFill(Color.web(colors[0]));

        Label label = new Label(status);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + colors[1] + ";");

        box.getChildren().addAll(dot, label);
        return box;
    }

    private String[] resolveStatusDotColors(String status) {
        return switch (normalizeStatusKey(status)) {
            case "pending" -> new String[]{"#F59E0B", "#B45309"};
            case "processing" -> new String[]{"#3B82F6", "#1D4ED8"};
            case "shipping" -> new String[]{"#A855F7", "#7E22CE"};
            case "completed" -> new String[]{"#22C55E", "#15803D"};
            case "cancelled" -> new String[]{"#EF4444", "#B91C1C"};
            default -> new String[]{"#9CA3AF", "#6B7280"};
        };
    }

    private String normalizeStatusKey(String status) {
        if (status == null) {
            return "other";
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "other";
        }
        return normalized;
    }

    private record RequestRow(
        Request request,
        String requestCode,
        String createdAt,
        String itemCount,
        String deadline,
        String status
    ) { }
}
