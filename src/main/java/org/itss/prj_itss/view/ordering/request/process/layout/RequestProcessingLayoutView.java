package org.itss.prj_itss.view.ordering.request.process.layout;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.model.request.application.processing.RequestProcessingViewModel;
import org.itss.prj_itss.model.request.application.processing.SuggestedPlanView;
import org.itss.prj_itss.model.request.application.processing.ProcessingPreviewOrderView;
import org.itss.prj_itss.controller.ordering.request.RequestProcessingController;
import org.itss.prj_itss.view.ordering.request.process.items.ItemsSectionView;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.view.ordering.request.process.preview.RequestProcessingPreviewDialog;
import org.itss.prj_itss.controller.ordering.request.process.preview.RequestProcessingPreviewDialogController;
import org.itss.prj_itss.view.ordering.request.process.site.SiteFilterView;
import org.itss.prj_itss.view.ordering.request.process.suggest.AllSuggestPopupView;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class RequestProcessingLayoutView {

    private RequestProcessingController controller;
    private SiteFilterView siteFilterView;
    private Consumer<String> navigateToView = viewId -> {};

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

    public void init(RequestProcessingUseCase requestProcessingUseCase, Consumer<String> navigateToView) {
        this.controller = new RequestProcessingController(
            Objects.requireNonNull(requestProcessingUseCase, "requestProcessingUseCase")
        );
        this.navigateToView = navigateToView == null ? viewId -> {} : navigateToView;
    }

    public void setRequestId(int requestId) {
        controller.setRequestId(requestId);
        renderProcessingScreen();
    }

    @FXML
    private void goBack() {
        navigateToView.accept("received-requests");
    }

    @FXML
    private void handleConfirm() {
        RequestProcessingController.ConfirmResult result = controller.handleConfirm();
        if (!result.valid()) {
            showValidationError(result.validationMessage());
            return;
        }

        showPreviewDialog(result.previewOrders());
    }

    private void showPreviewDialog(List<ProcessingPreviewOrderView> previewOrders) {
        new RequestProcessingPreviewDialog(
            () -> navigateToView.accept("orders"),
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
        RequestProcessingViewModel vm = controller.snapshot();
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
        RequestProcessingViewModel vm = controller.snapshot();
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
        RequestProcessingViewModel vm = controller.snapshot();
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
        controller.handleOptimizeAllocation();
        renderItemsViewSection();
    }

    private void handleShowAllPlans() {
        AllSuggestPopupView.show(controller.handleShowAllPlans(), this::applySelectedPlan);
    }

    private void applySelectedPlan(SuggestedPlanView plan) {
        controller.applySelectedPlan(plan.signature());
        renderItemsViewSection();
    }

    private void handleSiteFilterChanged() {
        controller.handleSiteFilterChanged(
            siteFilterView.getExcludedSiteIds(),
            siteFilterView.getPrioritySiteIds()
        );
        renderItemsViewSection();
    }

    private void toggleExpandedItem(int index) {
        controller.toggleExpandedItem(index);
        renderItemsViewSection();
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Không hợp lệ");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    private void styleDialog(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-size: 13px;");
    }
}
