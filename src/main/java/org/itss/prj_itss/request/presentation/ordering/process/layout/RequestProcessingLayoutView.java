package org.itss.prj_itss.request.presentation.ordering.process.layout;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;
import org.itss.prj_itss.request.presentation.ordering.process.RequestProcessingController;
import org.itss.prj_itss.request.presentation.ordering.process.RequestProcessingController.ConfirmResult;
import org.itss.prj_itss.request.presentation.ordering.process.RequestProcessingController.ProcessingSnapshot;
import org.itss.prj_itss.request.business.allocation.algo.AllSuggestAlgo.SuggestedPlan;
import org.itss.prj_itss.request.presentation.ordering.process.items.ItemsSectionView;
import org.itss.prj_itss.request.business.service.RequestProcessingPreviewBuilder.PreviewOrder;
import org.itss.prj_itss.request.presentation.ordering.process.preview.RequestProcessingPreviewDialog;
import org.itss.prj_itss.request.presentation.ordering.process.preview.RequestProcessingPreviewDialogController;
import org.itss.prj_itss.request.presentation.ordering.process.site.SiteFilterView;
import org.itss.prj_itss.request.presentation.ordering.process.suggest.AllSuggestPopupView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class RequestProcessingLayoutView implements IViewController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private INavigator navigator;
    private RequestProcessingController controller;
    private SiteFilterView siteFilterView;

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

    @Override
    public void init(INavigator navigator, ApplicationContext context) {
        this.navigator = navigator;
        this.controller = new RequestProcessingController(
            context.requestProcessingUseCaseV2()
        );
    }

    public void setRequestId(int requestId) {
        controller.setRequestId(requestId);
        renderProcessingScreen();
    }

    @FXML
    private void goBack() {
        if (navigator != null) {
            navigator.showView("received-requests");
        }
    }

    @FXML
    private void handleConfirm() {
        ConfirmResult result = controller.handleConfirm();
        if (!result.valid()) {
            showValidationError(result.validationMessage());
            return;
        }

        showPreviewDialog(result.previewOrders());
    }

    private void showPreviewDialog(List<PreviewOrder> previewOrders) {
        new RequestProcessingPreviewDialog(
            navigator,
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
        ProcessingSnapshot snapshot = controller.snapshot();
        int totalQuantity = snapshot.items().stream().mapToInt(item -> item.required).sum();
        requestCodeLabel.setText("YÃªu cáº§u " + String.format("YC-2026-%03d", snapshot.requestId()));
        requestSummaryLabel.setText(
            "NgÃ y cáº§n giao: "
                + (snapshot.earliestDeliveryDate() == null ? "N/A" : snapshot.earliestDeliveryDate().format(DATE_FORMAT))
                + "  â€¢  " + snapshot.items().size() + " máº·t hÃ ng"
                + "  â€¢  " + totalQuantity + " chiáº¿c"
        );
        requestStatusLabel.setText("Chá» xá»­ lÃ½");
    }

    private void renderSiteFilterSection() {
        ProcessingSnapshot snapshot = controller.snapshot();
        siteFilterView = SiteFilterView.load(snapshot.allSites(), this::handleSiteFilterChanged);
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
        ProcessingSnapshot snapshot = controller.snapshot();
        ItemsSectionView itemsSection = ItemsSectionView.load(
            snapshot.items(),
            snapshot.allSites(),
            snapshot.excludedSiteIds(),
            snapshot.allocationControl(),
            snapshot.earliestDeliveryDate(),
            snapshot.expandedItemIndex(),
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

    private void applySelectedPlan(SuggestedPlan plan) {
        controller.applySelectedPlan(plan);
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
        alert.setTitle("KhÃ´ng há»£p lá»‡");
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

