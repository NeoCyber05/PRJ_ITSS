package org.itss.prj_itss.request.presentation.ordering.received;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;
import org.itss.prj_itss.request.presentation.ordering.detail.RequestDetailPopup;
import org.itss.prj_itss.request.application.listing.ReceivedRequestsApplicationService;
import org.itss.prj_itss.request.application.listing.RequestRow;
import org.itss.prj_itss.common.application.OrderingFormatters;
import org.itss.prj_itss.common.presentation.ui.StatusBadgeFactory;
import org.itss.prj_itss.common.presentation.ui.TableViewSupport;

import java.util.Locale;

public class ReceivedRequestsController implements IViewController {

    private final ObservableList<RequestRow> rows = FXCollections.observableArrayList();
    private final FilteredList<RequestRow> filteredRows = new FilteredList<>(rows, row -> true);
    private final ObservableList<RequestRow> paginatedRows = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int pageSize = 10;

    private INavigator navigator;
    private ApplicationContext context;
    private ReceivedRequestsApplicationService receivedRequestsApplicationService;

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
        TableViewSupport.useConstrainedResize(requestTable);

        requestTable.setFixedCellSize(46);
        requestTable.prefHeightProperty().bind(
            Bindings.max(1, Bindings.size(paginatedRows)).multiply(requestTable.getFixedCellSize()).add(36)
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
                    HBox badge = StatusBadgeFactory.statusDot(status, true);
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

                if ("pending".equals(OrderingFormatters.normalizeStatusKey(row.status()))) {
                    Button processButton = new Button("Xử lý");
                    processButton.setStyle("-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14;");
                    processButton.setOnAction(event -> navigator.showView("request-processing:" + row.requestId()));
                    actions.getChildren().add(processButton);
                }

                setGraphic(actions);
                setText(null);
            }
        });

        requestTable.setItems(paginatedRows);

        statusFilter.getItems().addAll(
            "Mọi trạng thái",
            "Chờ xử lý",
            "Đang xử lý",
            "Đang giao",
            "Đã hoàn thành",
            "Đã hủy"
        );
        statusFilter.setValue("Mọi trạng thái");

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
        this.receivedRequestsApplicationService = context.receivedRequestsApplicationService();
        reload();
    }

    private void reload() {
        rows.setAll(receivedRequestsApplicationService.findRows());
        applyFilters();
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilter.getValue();

        filteredRows.setPredicate(row -> {
            boolean matchesKeyword = keyword.isBlank()
                || row.requestCode().toLowerCase(Locale.ROOT).contains(keyword);
            String selectedStatusKey = OrderingFormatters.toRequestStatusKey(selectedStatus);
            boolean matchesStatus = selectedStatus == null
                || "all".equalsIgnoreCase(selectedStatusKey)
                || selectedStatusKey.equalsIgnoreCase(OrderingFormatters.normalizeStatusKey(row.status()));
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
}
