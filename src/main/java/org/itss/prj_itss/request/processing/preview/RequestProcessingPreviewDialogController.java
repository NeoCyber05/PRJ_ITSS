package org.itss.prj_itss.request.processing.preview;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.layout.Navigator;
import org.itss.prj_itss.request.processing.RequestProcessingUiSupport;
import org.itss.prj_itss.request.processing.allocation.RequestProcessingAllocationSupport;
import org.itss.prj_itss.service.RequestProcessingService;
import org.itss.prj_itss.ui.Notifications;
import org.itss.prj_itss.ui.StatusNodes;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class RequestProcessingPreviewDialogController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox ordersBox;

    @FXML
    private Button backButton;

    @FXML
    private Button sendButton;

    private Stage dialog;
    private Navigator navigator;
    private RequestProcessingService requestProcessingService;
    private int requestId;
    private Map<Integer, Map<Integer, Allocation>> allocations;

    void init(
        Stage dialog,
        Navigator navigator,
        RequestProcessingService requestProcessingService,
        int requestId,
        Map<Integer, Map<Integer, Allocation>> allocations,
        List<RequestProcessingPreviewBuilder.PreviewOrder> previewOrders
    ) {
        this.dialog = dialog;
        this.navigator = navigator;
        this.requestProcessingService = requestProcessingService;
        this.requestId = requestId;
        this.allocations = allocations;

        render(previewOrders);
        backButton.setOnAction(event -> dialog.close());
        sendButton.setOnAction(event -> submit());
    }

    private void render(List<RequestProcessingPreviewBuilder.PreviewOrder> previewOrders) {
        int totalQuantity = previewOrders.stream()
            .flatMap(order -> order.lines().stream())
            .mapToInt(RequestProcessingPreviewBuilder.PreviewLine::quantity)
            .sum();
        int totalLines = previewOrders.stream()
            .mapToInt(order -> order.lines().size())
            .sum();

        subtitleLabel.setText(
            previewOrders.size() + " đơn hàng dự kiến"
                + " • " + totalLines + " dòng phân bổ"
                + " • " + totalQuantity + " chiếc"
        );

        ordersBox.getChildren().clear();
        for (int index = 0; index < previewOrders.size(); index++) {
            ordersBox.getChildren().add(buildPreviewOrderCard(previewOrders.get(index), index + 1));
        }
    }

    private void submit() {
        try {
            requestProcessingService.createAllocatedOrders(requestId, allocations);
            dialog.close();
            navigator.showView("orders");
            Notifications.showToast("Đã tạo đơn hàng thành công.");
        } catch (SQLException exception) {
            showCreationError();
        }
    }

    private VBox buildPreviewOrderCard(RequestProcessingPreviewBuilder.PreviewOrder order, int index) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        RequestProcessingUiSupport.addStyleClass(card, "request-preview-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label orderLabel = new Label("Đơn hàng dự kiến " + index);
        RequestProcessingUiSupport.addStyleClass(orderLabel, "request-preview-card-title");
        Label siteLabel = new Label(order.site().name + " • " + order.site().siteCode);
        RequestProcessingUiSupport.addStyleClass(siteLabel, "request-preview-subtitle");
        titleBox.getChildren().addAll(orderLabel, siteLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int totalQuantity = order.lines().stream().mapToInt(RequestProcessingPreviewBuilder.PreviewLine::quantity).sum();
        Label qtyBadge = new Label(totalQuantity + " chiếc");
        RequestProcessingUiSupport.addStyleClass(qtyBadge, "request-preview-quantity-badge");

        header.getChildren().addAll(titleBox, spacer, qtyBadge);

        VBox table = new VBox(0);
        RequestProcessingUiSupport.addStyleClass(table, "request-preview-table");
        table.getChildren().add(buildPreviewTableHeader());
        for (RequestProcessingPreviewBuilder.PreviewLine line : order.lines()) {
            table.getChildren().add(buildPreviewTableRow(line));
        }

        card.getChildren().addAll(header, table);
        return card;
    }

    private HBox buildPreviewTableHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 14, 10, 14));
        RequestProcessingUiSupport.addStyleClass(header, "request-preview-table-header");
        header.getChildren().addAll(
            previewHeaderCell("MÃ HÀNG", 120),
            previewHeaderCell("TÊN MẶT HÀNG", 250),
            previewHeaderCell("SỐ LƯỢNG", 110),
            previewHeaderCell("VẬN CHUYỂN", 150),
            previewHeaderCell("DỰ KIẾN NHẬN", 140),
            previewHeaderCell("HẠN NHẬN", 140)
        );
        return header;
    }

    private HBox buildPreviewTableRow(RequestProcessingPreviewBuilder.PreviewLine line) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        RequestProcessingUiSupport.addStyleClass(row, "request-preview-table-row");

        row.getChildren().add(previewValueCell(line.item().code, 120, true));
        row.getChildren().add(previewValueCell(line.item().name, 250, false));
        row.getChildren().add(previewValueCell(String.valueOf(line.quantity()), 110, true));

        HBox transportBox = new HBox(StatusNodes.buildTransportBadgeCompact(
            RequestProcessingAllocationSupport.toDisplayDeliveryMethod(line.transport())
        ));
        transportBox.setAlignment(Pos.CENTER_LEFT);
        transportBox.setMinWidth(150);
        transportBox.setPrefWidth(150);
        row.getChildren().add(transportBox);

        row.getChildren().add(previewValueCell(line.estimatedDate().format(DATE_FORMAT), 140, false));
        row.getChildren().add(previewValueCell(
            line.desiredDate() == null ? "N/A" : line.desiredDate().format(DATE_FORMAT),
            140,
            false
        ));
        return row;
    }

    private Label previewHeaderCell(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        RequestProcessingUiSupport.addStyleClass(label, "request-preview-header-cell");
        return label;
    }

    private Label previewValueCell(String text, double width, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        RequestProcessingUiSupport.addStyleClass(label, "request-preview-value-cell");
        if (bold) {
            RequestProcessingUiSupport.addStyleClass(label, "request-preview-value-cell-strong");
        }
        return label;
    }

    private void showCreationError() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Không thể tạo đơn");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText("Không thể tạo các đơn hàng đã phân bổ.");
        Notifications.styleDialog(errorAlert);
        errorAlert.showAndWait();
    }
}
