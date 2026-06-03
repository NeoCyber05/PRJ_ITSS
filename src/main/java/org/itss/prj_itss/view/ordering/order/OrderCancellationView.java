package org.itss.prj_itss.view.ordering.order;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderCancellationProcessingController;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingViewModel;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingSession;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeCommand;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeResultView;
import org.itss.prj_itss.model.request.application.processing.ProcessingPreviewOrderView;
import org.itss.prj_itss.model.shared.formatting.OrderingFormatters;
import org.itss.prj_itss.view.shared.ViewLifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OrderCancellationView implements ViewLifecycle {

    private Navigator navigator;
    private OrderCancellationProcessingController controller;
    private int cancelledOrderId = -1;
    private CancelledOrderProcessingViewModel currentViewModel;
    private List<ProcessingPreviewOrderView> currentPreviewOrders = new ArrayList<>();
    private ScreenMode screenMode = ScreenMode.ALLOCATION;
    private Runnable modalConfirmAction;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private HBox suggestBar;

    @FXML
    private Button cancelProcessingButton;

    @FXML
    private Label feedbackLabel;

    @FXML
    private ScrollPane contentScrollPane;

    @FXML
    private VBox contentBox;

    @FXML
    private HBox footerSummaryBox;

    @FXML
    private Button secondaryActionButton;

    @FXML
    private Button primaryActionButton;

    @FXML
    private StackPane modalOverlay;

    @FXML
    private Label modalIconLabel;

    @FXML
    private Label modalTitleLabel;

    @FXML
    private Label modalSubtitleLabel;

    @FXML
    private VBox modalBody;

    @FXML
    private HBox modalFooter;

    @FXML
    private Button modalCancelButton;

    @FXML
    private Button modalConfirmButton;

    @FXML
    private void initialize() {
        showAllocationScreen();
    }

    public void init(Navigator navigator, OrderCancellationProcessingController controller) {
        this.navigator = navigator;
        this.controller = controller;
        if (cancelledOrderId > 0 && controller != null) {
            controller.start(cancelledOrderId);
            showAllocationScreen();
        }
    }

    public void setCancelledOrderId(int id) {
        this.cancelledOrderId = id;
        if (controller != null) {
            controller.start(id);
            showAllocationScreen();
        }
    }

    @Override
    public void onViewShown() {
        if (cancelledOrderId > 0 && controller != null) {
            controller.start(cancelledOrderId);
            showAllocationScreen();
        }
    }

    @FXML
    private void handleCancelProcessingAction() {
        showConfirmModal(
            "Hủy xử lý",
            "Bạn có chắc chắn muốn hủy xử lý phân bổ đơn hàng này? Dữ liệu đang nhập sẽ không được lưu.",
            "Xác nhận hủy",
            "⚠",
            () -> {
                closeModal();
                showFeedback("Đã hủy xử lý đơn hàng.", FeedbackKind.WARNING);
                if (navigator != null) {
                    navigator.showView("orders");
                }
            }
        );
    }

    @FXML
    private void handleSuggestAction() {
        if (controller != null) {
            controller.handleSuggestAllocation();
            showFeedback("Đã tự động tính toán phương án phân bổ tối ưu.", FeedbackKind.SUCCESS);
            showAllocationScreen();
        }
    }

    @FXML
    private void handlePrimaryAction() {
        if (screenMode == ScreenMode.ALLOCATION) {
            createPreviewOrders();
            return;
        }
        if (screenMode == ScreenMode.RESULT) {
            showConfirmModal(
                "Gửi yêu cầu",
                "Xác nhận gửi yêu cầu xử lý đơn hàng?",
                "Ok",
                "✓",
                () -> {
                    closeModal();
                    submitAllocatedOrders();
                }
            );
            return;
        }
        if (navigator != null) {
            navigator.showView("orders");
        }
    }

    @FXML
    private void handleSecondaryAction() {
        if (screenMode == ScreenMode.RESULT) {
            showAllocationScreen();
            return;
        }
        if (screenMode == ScreenMode.SUBMITTED && navigator != null) {
            navigator.showView("orders");
        }
    }

    @FXML
    private void handleModalCloseAction() {
        closeModal();
    }

    private void showAllocationScreen() {
        screenMode = ScreenMode.ALLOCATION;
        titleLabel.setText("Phân bổ đơn hàng bị hủy");
        
        if (controller == null || cancelledOrderId <= 0) {
            subtitleLabel.setText("Chưa tải được dữ liệu đơn hàng.");
            primaryActionButton.setText("Tạo các đơn hàng");
            primaryActionButton.setDisable(true);
            return;
        }

        currentViewModel = controller.buildViewModel();
        subtitleLabel.setText("Đơn hàng: " + currentViewModel.cancelledOrderCode() + "  •  Mã yêu cầu: " + currentViewModel.requestCode());
        suggestBar.setManaged(true);
        suggestBar.setVisible(true);
        cancelProcessingButton.setManaged(true);
        cancelProcessingButton.setVisible(true);
        secondaryActionButton.setManaged(false);
        secondaryActionButton.setVisible(false);
        primaryActionButton.setText("Tạo các đơn hàng");
        primaryActionButton.setDisable(false);
        
        contentBox.getChildren().setAll(currentViewModel.allocationItems().stream().map(this::buildItemGroupCard).toList());
        updateFooterSummary();
        contentScrollPane.setVvalue(0);
    }

    private void showResultScreen() {
        screenMode = ScreenMode.RESULT;
        titleLabel.setText("Kết quả các đơn hàng mới");
        subtitleLabel.setText("Đơn hàng: " + currentViewModel.cancelledOrderCode() + " - Gom nhóm theo Site");
        suggestBar.setManaged(false);
        suggestBar.setVisible(false);
        cancelProcessingButton.setManaged(false);
        cancelProcessingButton.setVisible(false);
        secondaryActionButton.setText("← Quay lại");
        secondaryActionButton.setManaged(true);
        secondaryActionButton.setVisible(true);
        primaryActionButton.setText("Gửi yêu cầu");
        primaryActionButton.setDisable(currentPreviewOrders.isEmpty());
        contentBox.getChildren().setAll(currentPreviewOrders.stream().map(this::buildPreviewOrderCard).toList());
        updateFooterSummary();
        contentScrollPane.setVvalue(0);
    }

    private void showSubmittedScreen() {
        screenMode = ScreenMode.SUBMITTED;
        titleLabel.setText("Chi tiết yêu cầu đặt hàng");
        subtitleLabel.setText("Đơn hàng: " + currentViewModel.cancelledOrderCode());
        suggestBar.setManaged(false);
        suggestBar.setVisible(false);
        cancelProcessingButton.setManaged(false);
        cancelProcessingButton.setVisible(false);
        secondaryActionButton.setText("Về danh sách đơn hàng");
        secondaryActionButton.setManaged(true);
        secondaryActionButton.setVisible(true);
        primaryActionButton.setText("Hoàn tất");
        primaryActionButton.setDisable(false);

        List<Node> nodes = new ArrayList<>();
        nodes.add(buildSuccessBanner());
        nodes.addAll(currentPreviewOrders.stream().map(this::buildSubmittedOrderCard).toList());
        contentBox.getChildren().setAll(nodes);
        updateFooterSummary();
        contentScrollPane.setVvalue(0);
        showFeedback("Xử lý gửi đơn thành công.", FeedbackKind.SUCCESS);
    }

    private VBox buildItemGroupCard(CancelledOrderProcessingViewModel.AllocationItemViewModel group) {
        VBox card = new VBox(0);
        card.getStyleClass().add("cancelled-order-section-card");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("cancelled-order-section-header");
        header.setPadding(new Insets(15, 18, 15, 18));

        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(group.name());
        nameLabel.getStyleClass().add("cancelled-order-section-title");
        
        String desiredDateText = currentViewModel.desiredDeliveryDates().get(group.merchandiseId());
        Label desiredDateLabel = new Label("Ngày cần giao: " + (desiredDateText == null ? "" : desiredDateText));
        desiredDateLabel.getStyleClass().add("cancelled-order-deadline-badge");
        Label metaLabel = new Label(group.siteRows().size() + " site  •  Tổng tồn: " + group.totalStock());
        metaLabel.getStyleClass().add("cancelled-order-muted-text");
        titleRow.getChildren().addAll(nameLabel, desiredDateLabel);
        titleBox.getChildren().addAll(titleRow, metaLabel);

        Label statusBadge = new Label(group.allocationFractionText());
        boolean fulfilled = group.allocated() >= group.required();
        statusBadge.getStyleClass().addAll("cancelled-order-pill", fulfilled ? "cancelled-order-pill-success" : "cancelled-order-pill-warning");

        Button toggleButton = new Button(group.expanded() ? "⌃" : "⌄");
        toggleButton.getStyleClass().add("cancelled-order-icon-button");
        toggleButton.setOnAction(event -> {
            controller.toggleExpandedItem(currentViewModel.allocationItems().indexOf(group));
            showAllocationScreen();
        });

        header.getChildren().addAll(titleBox, statusBadge, toggleButton);
        card.getChildren().add(header);
        if (group.expanded()) {
            card.getChildren().add(buildAllocationTable(group));
        }
        return card;
    }

    private VBox buildAllocationTable(CancelledOrderProcessingViewModel.AllocationItemViewModel group) {
        VBox table = new VBox(0);
        table.getStyleClass().add("cancelled-order-table");
        table.getChildren().add(buildAllocationStatusHeaderRow());
        for (CancelledOrderProcessingViewModel.AllocationSiteRowViewModel site : group.siteRows()) {
            table.getChildren().add(buildAllocationStatusRow(group, site));
        }
        return table;
    }

    private HBox buildAllocationStatusHeaderRow() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cancelled-order-table-header");
        row.getChildren().addAll(
            headerCell("MÃ SITE", 92),
            headerCell("TÊN SITE", 150),
            headerCell("TỒN KHO", 90),
            headerCell("VẬN CHUYỂN", 150),
            headerCell("TRẠNG THÁI", 150),
            headerCell("SL ĐẶT", 100)
        );
        return row;
    }

    private HBox buildAllocationStatusRow(
        CancelledOrderProcessingViewModel.AllocationItemViewModel group,
        CancelledOrderProcessingViewModel.AllocationSiteRowViewModel site
    ) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cancelled-order-table-row");

        ComboBox<String> transportBox = new ComboBox<>();
        transportBox.getItems().setAll(site.transportLabels());
        transportBox.setValue(site.selectedTransportLabel());
        transportBox.setPrefWidth(128);
        transportBox.getStyleClass().add("cancelled-order-combo");
        transportBox.setDisable(site.transportDisabled());
        transportBox.setOnAction(event -> {
            if (transportBox.getValue() != null && !transportBox.getValue().equals(site.selectedTransportLabel())) {
                handleAllocationChanged(group, site, String.valueOf(site.quantity()), transportBox.getValue());
            }
        });

        TextField quantityField = new TextField(site.quantity() == 0 ? "" : String.valueOf(site.quantity()));
        quantityField.setPromptText("0");
        quantityField.setPrefWidth(78);
        quantityField.getStyleClass().add("cancelled-order-quantity-field");
        
        quantityField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Focus lost
                String currentText = quantityField.getText().trim();
                handleAllocationChanged(group, site, currentText, transportBox.getValue());
            }
        });
        quantityField.setOnAction(event -> {
            String currentText = quantityField.getText().trim();
            handleAllocationChanged(group, site, currentText, transportBox.getValue());
        });

        row.getChildren().addAll(
            valueCell(site.siteDetail().split(" - ")[0], 92, "cancelled-order-code-badge"),
            valueCell(site.siteName(), 150, null),
            valueCell(String.valueOf(site.stock()), 90, "cancelled-order-stock-badge"),
            wrappedCell(transportBox, 150),
            valueCell(site.deliveryStatusText(), 150, site.deliveryStatusClass()),
            wrappedCell(quantityField, 100)
        );
        return row;
    }

    private void handleAllocationChanged(
        CancelledOrderProcessingViewModel.AllocationItemViewModel group,
        CancelledOrderProcessingViewModel.AllocationSiteRowViewModel site,
        String quantityText,
        String transportLabel
    ) {
        AllocationChangeCommand command = new AllocationChangeCommand(
            group.merchandiseId(),
            site.siteId(),
            quantityText,
            transportLabel
        );
        AllocationChangeResultView result = controller.handleAllocationInputChanged(command);
        if (!result.applied()) {
            String message = switch (result.errorType()) {
                case "INVALID_INTEGER" -> "Vui lòng nhập số nguyên hợp lệ.";
                case "NEGATIVE_QUANTITY" -> "Số lượng không được âm.";
                case "EXCEEDS_STOCK" -> "Số lượng vượt quá tồn kho khả dụng (" + result.stock() + ").";
                default -> "Lỗi nhập liệu.";
            };
            showFeedback(message, FeedbackKind.ERROR);
        } else {
            showFeedback("Đã cập nhật phân bổ thành công.", FeedbackKind.SUCCESS);
        }
        showAllocationScreen();
    }

    private void createPreviewOrders() {
        if (controller == null) return;
        CancelledOrderProcessingSession.ConfirmResult result = controller.handleConfirm();
        if (!result.valid()) {
            showFeedback(result.validationMessage(), FeedbackKind.ERROR);
            return;
        }

        currentPreviewOrders = result.previewOrders();
        showFeedback("Đã tạo bản xem trước các đơn hàng mới.", FeedbackKind.SUCCESS);
        showResultScreen();
    }

    private void submitAllocatedOrders() {
        if (controller == null) return;
        try {
            controller.handleSubmit();
            showSubmittedScreen();
        } catch (Exception exception) {
            showFeedback(exception.getMessage(), FeedbackKind.ERROR);
        }
    }

    private VBox buildPreviewOrderCard(ProcessingPreviewOrderView order) {
        return buildOrderCard(order, false);
    }

    private VBox buildSubmittedOrderCard(ProcessingPreviewOrderView order) {
        return buildOrderCard(order, true);
    }

    private VBox buildOrderCard(ProcessingPreviewOrderView order, boolean includeStatus) {
        VBox card = new VBox(0);
        card.getStyleClass().add("cancelled-order-section-card");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 18, 15, 18));
        header.getStyleClass().add("cancelled-order-section-header");

        Label title = new Label("Đơn hàng " + order.siteName());
        title.getStyleClass().add("cancelled-order-section-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label code = new Label(order.siteCode());
        code.getStyleClass().add("cancelled-order-muted-text");
        header.getChildren().addAll(title, code);
        if (includeStatus) {
            Label status = new Label("● Chờ xác nhận");
            status.getStyleClass().addAll("cancelled-order-pill", "cancelled-order-pill-warning");
            header.getChildren().add(status);
        }

        VBox table = new VBox(0);
        table.getStyleClass().add("cancelled-order-table");
        table.getChildren().add(buildPreviewHeaderRow(includeStatus));
        for (ProcessingPreviewOrderView.ProcessingPreviewLineView line : order.lines()) {
            table.getChildren().add(buildPreviewRow(line, includeStatus));
        }

        card.getChildren().addAll(header, table);
        return card;
    }

    private HBox buildPreviewHeaderRow(boolean includeStatus) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cancelled-order-table-header");
        row.getChildren().addAll(
            headerCell("MẶT HÀNG", 240),
            headerCell("SỐ LƯỢNG", 120),
            headerCell("VẬN CHUYỂN", 160)
        );
        if (includeStatus) {
            row.getChildren().add(headerCell("TRẠNG THÁI", 160));
        }
        return row;
    }

    private HBox buildPreviewRow(ProcessingPreviewOrderView.ProcessingPreviewLineView line, boolean includeStatus) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cancelled-order-table-row");
        row.getChildren().addAll(
            valueCell(line.merchandiseName(), 240, null),
            valueCell(String.valueOf(line.quantity()), 120, "cancelled-order-stock-badge"),
            valueCell(line.transport(), 160, null)
        );
        if (includeStatus) {
            row.getChildren().add(valueCell("● Chờ xác nhận", 160, "cancelled-order-date-sea"));
        }
        return row;
    }

    private Node buildSuccessBanner() {
        HBox banner = new HBox(10);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.getStyleClass().add("cancelled-order-success-banner");
        banner.getChildren().addAll(
            new Label("●"),
            new Label("Yêu cầu đã được gửi thành công - Đang chờ xác nhận từ các Site.")
        );
        return banner;
    }

    private void showConfirmModal(String title, String message, String confirmText, String icon, Runnable confirmAction) {
        modalIconLabel.setText(icon);
        modalTitleLabel.setText(title);
        modalSubtitleLabel.setText(message);
        modalBody.getChildren().clear();
        modalConfirmAction = confirmAction;
        modalCancelButton.setText("Cancel");
        modalConfirmButton.setText(confirmText);
        modalConfirmButton.setManaged(true);
        modalConfirmButton.setVisible(true);
        modalConfirmButton.setOnAction(event -> {
            if (modalConfirmAction != null) {
                modalConfirmAction.run();
            }
        });
        modalFooter.setManaged(true);
        modalFooter.setVisible(true);
        showModal();
    }

    private void showModal() {
        modalOverlay.setManaged(true);
        modalOverlay.setVisible(true);
    }

    private void closeModal() {
        modalOverlay.setManaged(false);
        modalOverlay.setVisible(false);
        modalBody.getChildren().clear();
        modalConfirmAction = null;
    }

    private void updateFooterSummary() {
        footerSummaryBox.getChildren().clear();
        if (screenMode == ScreenMode.RESULT || screenMode == ScreenMode.SUBMITTED) {
            footerSummaryBox.getChildren().add(new Label(currentPreviewOrders.size() + " đơn hàng mới theo site"));
            footerSummaryBox.getChildren().get(0).getStyleClass().add("cancelled-order-muted-text");
            return;
        }
    }

    private void showFeedback(String message, FeedbackKind kind) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().removeAll(
            "cancelled-order-feedback-info",
            "cancelled-order-feedback-success",
            "cancelled-order-feedback-warning",
            "cancelled-order-feedback-error"
        );
        feedbackLabel.getStyleClass().add(kind.cssClass());
        feedbackLabel.setManaged(true);
        feedbackLabel.setVisible(true);
    }

    private Label headerCell(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.getStyleClass().add("cancelled-order-table-header-cell");
        return label;
    }

    private StackPane valueCell(String text, double width, String valueStyleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("cancelled-order-table-cell-text");
        if (valueStyleClass != null) {
            label.getStyleClass().add(valueStyleClass);
        }
        return wrappedCell(label, width);
    }

    private StackPane wrappedCell(Node child, double width) {
        StackPane wrapper = new StackPane(child);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setMinWidth(width);
        wrapper.setPrefWidth(width);
        wrapper.setPadding(new Insets(0, 12, 0, 12));
        return wrapper;
    }

    private enum ScreenMode {
        ALLOCATION,
        RESULT,
        SUBMITTED
    }

    private enum FeedbackKind {
        INFO("cancelled-order-feedback-info"),
        SUCCESS("cancelled-order-feedback-success"),
        WARNING("cancelled-order-feedback-warning"),
        ERROR("cancelled-order-feedback-error");

        private final String cssClass;

        FeedbackKind(String cssClass) {
            this.cssClass = cssClass;
        }

        private String cssClass() {
            return cssClass;
        }
    }
}
