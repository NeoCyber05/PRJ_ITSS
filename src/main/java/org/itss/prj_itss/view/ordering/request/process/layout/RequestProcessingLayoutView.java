package org.itss.prj_itss.view.ordering.request.process.layout;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.itss.prj_itss.controller.ordering.request.process.state.LockOutcome;
import org.itss.prj_itss.controller.ordering.request.process.state.RequestProcessingState;
import org.itss.prj_itss.controller.ordering.request.process.state.SuggestedPlanState;
import org.itss.prj_itss.controller.ordering.request.process.state.ProcessingPreviewOrder;
import org.itss.prj_itss.controller.ordering.request.process.RequestProcessingLayoutController;
import org.itss.prj_itss.view.ordering.request.process.items.ItemsSectionView;
import org.itss.prj_itss.view.ordering.request.process.preview.RequestProcessingPreviewDialog;
import org.itss.prj_itss.controller.ordering.request.process.preview.RequestProcessingPreviewDialogController;
import org.itss.prj_itss.view.ordering.request.process.site.SiteFilterView;
import org.itss.prj_itss.view.ordering.request.process.suggest.AllSuggestPopupView;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class RequestProcessingLayoutView {

    private RequestProcessingLayoutController controller;
    private SiteFilterView siteFilterView;
    private Consumer<String> navigateToView = viewId -> {};
    private Timeline heartbeatTimeline;

    @FXML
    private Label requestCodeLabel;

    @FXML
    private Label requestSummaryLabel;

    @FXML
    private Label requestStatusLabel;

    @FXML
    private VBox siteFilterContainer;

    @FXML
    private VBox itemsTableContainer;

    @FXML
    private VBox allocationContainer;

    public void init(RequestProcessingLayoutController controller, Consumer<String> navigateToView) {
        this.controller = Objects.requireNonNull(controller, "controller");
        this.navigateToView = navigateToView == null ? viewId -> {} : navigateToView;
    }

    public void setRequestId(int requestId) {
        LockOutcome outcome = controller.setRequestId(requestId);
        if (outcome.blocked()) {
            showLockError(outcome.blockedMessage());
            goBack();
            return;
        }
        renderProcessingScreen();
        startHeartbeat();
    }

    private void showLockError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Không thể xử lý");
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "Yêu cầu đang bị khóa.");
        alert.showAndWait();
    }

    private void startHeartbeat() {
        heartbeatTimeline = new Timeline(
            new KeyFrame(Duration.seconds(300), e -> {
                Thread t = new Thread(controller::renewLock, "lock-heartbeat");
                t.setDaemon(true);
                t.start();
            })
        );
        heartbeatTimeline.setCycleCount(Animation.INDEFINITE);
        heartbeatTimeline.play();
    }

    private void stopHeartbeatAndRelease() {
        if (heartbeatTimeline != null) {
            heartbeatTimeline.stop();
            heartbeatTimeline = null;
        }
        Thread t = new Thread(controller::releaseLock, "lock-release");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void goBack() {
        stopHeartbeatAndRelease();
        navigateToView.accept("received-requests");
    }

    @FXML
    private void handleConfirm() {
        RequestProcessingLayoutController.ConfirmResult result = controller.handleConfirm();
        if (!result.valid()) {
            showValidationError(result.validationMessage());
            return;
        }

        showPreviewDialog(result.previewOrders());
    }

    private void showPreviewDialog(List<ProcessingPreviewOrder> previewOrders) {
        new RequestProcessingPreviewDialog(
            () -> {
                stopHeartbeatAndRelease();
                navigateToView.accept("received-requests");
            },
            new RequestProcessingPreviewDialogController(controller, previewOrders)
        ).show(itemsTableContainer);
    }

    private void renderProcessingScreen() {
        renderHeader();
        renderSiteFilterSection();
        hideLegacyAllocationContainer();
        renderItemsViewSection();
    }

    private void renderHeader() {
        RequestProcessingState vm = controller.snapshot();
        int totalQuantity = vm.items().stream().mapToInt(item -> item.required()).sum();
        requestCodeLabel.setText("Yêu cầu " + vm.requestCode());
        requestSummaryLabel.setText(
            "Ngày cần giao: "
                + (vm.earliestDeliveryDate() == null || vm.earliestDeliveryDate().isBlank() ? "N/A" : vm.earliestDeliveryDate())
                + "  •  " + vm.items().size() + " mặt hàng"
                + "  •  " + totalQuantity + " chiếc"
        );
        requestStatusLabel.setText("Chờ xử lý");
    }

    private void renderSiteFilterSection() {
        RequestProcessingState vm = controller.snapshot();
        siteFilterView = SiteFilterView.load(vm.sites(), this::handleSiteFilterChanged);
        siteFilterContainer.getChildren().setAll(siteFilterView.root());
    }

    private void hideLegacyAllocationContainer() {
        if (allocationContainer == null) {
            return;
        }
        allocationContainer.getChildren().clear();
        allocationContainer.setManaged(false);
        allocationContainer.setVisible(false);
    }

    private void renderItemsViewSection() {
        RequestProcessingState vm = controller.snapshot();
        ItemsSectionView itemsSection = ItemsSectionView.load(
            vm,
            this::handleOptimizeAllocation,
            this::handleShowAllPlans,
            this::toggleExpandedItem,
            controller::handleAllocationInputChanged
        );

        itemsTableContainer.getChildren().setAll(itemsSection.root());
        itemsSection.refreshAllocationLabels();
    }

    private void handleOptimizeAllocation() {
        boolean success = controller.handleOptimizeAllocation();
        if (!success) {
            showWarningAlert("Thông báo", "Không tìm được phương án đáp ứng đủ số lượng và thời hạn hiện tại.");
        }
        renderItemsViewSection();
    }

    private void handleShowAllPlans() {
        AllSuggestPopupView.show(controller.handleShowAllPlans(), this::applySelectedPlan);
    }

    private void applySelectedPlan(SuggestedPlanState plan) {
        controller.applySelectedPlan(plan.signature());
        renderItemsViewSection();
    }

    private void handleSiteFilterChanged() {
        controller.handleSiteFilterChanged(
            siteFilterView.getExcludedSiteIds(),
            siteFilterView.getSelectedSiteIds()
        );
        renderItemsViewSection();
    }

    private void toggleExpandedItem(int index) {
        controller.toggleExpandedItem(index);
        renderItemsViewSection();
    }

    private void showValidationError(String message) {
        showWarningAlert("Không hợp lệ", message);
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    private void styleDialog(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/org/itss/prj_itss/styles/main-style.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog-custom");
    }
}
