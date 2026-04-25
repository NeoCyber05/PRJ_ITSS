package org.itss.prj_itss.request.received;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;
import org.itss.prj_itss.request.detail.RequestDetailPopup;
import org.itss.prj_itss.service.RequestService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReceivedRequestsController implements IViewController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObservableList<RequestRow> rows = FXCollections.observableArrayList();
    private final FilteredList<RequestRow> filteredRows = new FilteredList<>(rows, row -> true);

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
    private void initialize() {
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

                Button detailButton = new Button("Chi tiết");
                detailButton.setOnAction(event -> RequestDetailPopup.show(
                    requestTable.getScene() == null ? null : requestTable.getScene().getWindow(),
                    row.requestCode(),
                    context,
                    navigator
                ));
                actions.getChildren().add(detailButton);

                if ("Chờ xử lý".equals(row.status())) {
                    Button processButton = new Button("Xử lý");
                    processButton.setStyle("-fx-background-color: #253D2C; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14;");
                    processButton.setOnAction(event -> navigator.showView("request-processing:" + row.request().getId()));
                    actions.getChildren().add(processButton);
                }

                setGraphic(actions);
                setText(null);
            }
        });

        requestTable.setItems(filteredRows);

        statusFilter.getItems().addAll(
            "Mọi trạng thái",
            "Chờ xử lý",
            "Đang xử lý",
            "Đang giao",
            "Đã hoàn thành",
            "Đã hủy"
        );
        statusFilter.setValue("Mọi trạng thái");

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
                || "Mọi trạng thái".equals(selectedStatus)
                || selectedStatus.equals(row.status());
            return matchesKeyword && matchesStatus;
        });

        int size = filteredRows.size();
        paginationInfoLabel.setText(size == 0
            ? "Không có yêu cầu phù hợp"
            : "Hiển thị 1 - " + size + " của " + size + " yêu cầu");
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
        return switch (status.trim()) {
            case "Cho xu ly", "Cho xac nhan",
                "\u0043h\u1edd x\u1eed l\u00fd", "\u0043h\u1edd x\u00e1c nh\u1eadn" -> "pending";
            case "Dang xu ly", "\u0110ang x\u1eed l\u00fd" -> "processing";
            case "Dang giao", "\u0110ang giao" -> "shipping";
            case "Da hoan thanh", "Hoan thanh",
                "\u0110\u00e3 ho\u00e0n th\u00e0nh", "Ho\u00e0n th\u00e0nh" -> "completed";
            case "Da huy", "\u0110\u00e3 h\u1ee7y" -> "cancelled";
            default -> "other";
        };
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
