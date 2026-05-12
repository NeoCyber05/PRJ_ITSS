package org.itss.prj_itss.ordering.request.process;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.RequestProcessingData;
import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;
import org.itss.prj_itss.ordering.request.process.allocation.AllocationControl;
import org.itss.prj_itss.ordering.request.process.model.AllocationPlan;
import org.itss.prj_itss.ordering.request.process.preview.RequestProcessingPreviewBuilder;
import org.itss.prj_itss.ordering.request.process.preview.RequestProcessingPreviewDialog;
import org.itss.prj_itss.ordering.request.process.site.SiteFilterController;
import org.itss.prj_itss.ordering.request.process.ui.RequestProcessingItemsView;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RequestProcessingController implements IViewController {

    private static final String SITE_FILTER_RESOURCE =
        "/org/itss/prj_itss/ordering/request/process/site/site-filter-view.fxml";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RequestProcessingPreviewBuilder previewBuilder = new RequestProcessingPreviewBuilder();
    private final List<ItemRequirement> items = new ArrayList<>();
    private final List<SiteStockOption> allSites = new ArrayList<>();
    private final Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();
    private final Map<Integer, LocalDate> desiredDeliveryDates = new LinkedHashMap<>();

    private INavigator navigator;
    private int requestId = -1;
    private RequestProcessingService requestProcessingService;
    private SiteFilterController siteFilter;
    private AllocationControl allocationSection;
    private RequestProcessingItemsView itemsSection;
    private int deadlineDays = 14;
    private int expandedItemIndex = -1;
    private LocalDate earliestDeliveryDate;

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
        this.requestProcessingService = context.requestProcessingService();
    }

    public void setRequestId(int requestId) {
        if (requestId <= 0) return;
        this.requestId = requestId;
        resetState();
        loadDataFromDatabase();
        updateUI();
    }

    @FXML
    private void goBack() {
        if (navigator != null) {
            navigator.showView("received-requests");
        }
    }

    @FXML
    private void handleConfirm() {
        String validationMessage = requestProcessingService.validateSubmission(
            items,
            allSites,
            allocations,
            desiredDeliveryDates,
            deadlineDays
        );
        if (validationMessage != null) {
            showValidationError(validationMessage);
            return;
        }

        new RequestProcessingPreviewDialog(navigator, requestProcessingService, requestId, allocations)
            .show(itemsTableContainer, previewBuilder.build(items, allSites, allocations, desiredDeliveryDates));
    }

    private void resetState() {
        items.clear();
        allSites.clear();
        allocations.clear();
        desiredDeliveryDates.clear();
        earliestDeliveryDate = null;
        deadlineDays = 14;
        expandedItemIndex = -1;
    }

    private void loadDataFromDatabase() {
        RequestProcessingData data = requestProcessingService.loadProcessingData(requestId);
        items.addAll(data.items());
        allSites.addAll(data.sites());
        desiredDeliveryDates.putAll(data.desiredDeliveryDates());
        earliestDeliveryDate = data.earliestDeliveryDate();
        deadlineDays = data.deadlineDays();

        for (ItemRequirement item : items) {
            allocations.put(item.merchandiseId, new LinkedHashMap<>());
        }
    }

    private void updateUI() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                RequestProcessingController.class.getResource(SITE_FILTER_RESOURCE),
                "Missing site filter section FXML"
            ));
            VBox root = loader.load();
            siteFilter = loader.getController();
            siteFilter.init(allSites, this::handleSiteFilterChanged);
            siteFilterContainer.getChildren().setAll(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load site filter section", exception);
        }

        int totalQuantity = items.stream().mapToInt(item -> item.required).sum();
        requestCodeLabel.setText("Yêu cầu " + String.format("YC-2026-%03d", requestId));
        requestSummaryLabel.setText(
            "Ngày cần giao: " + (earliestDeliveryDate == null ? "N/A" : earliestDeliveryDate.format(DATE_FORMAT))
                + "  •  " + items.size() + " mặt hàng"
                + "  •  " + totalQuantity + " chiếc"
        );
        requestStatusLabel.setText("Chờ xử lý");

        if (allocationContainer != null) {
            allocationContainer.getChildren().clear();
            allocationContainer.setManaged(false);
            allocationContainer.setVisible(false);
        }

        allocationSection = new AllocationControl(
            items,
            allSites,
            siteFilter.getExcludedSiteIds(),
            siteFilter.getPrioritySiteIds(),
            allocations,
            deadlineDays
        );
        allocationSection.setOnAllocationChanged(this::refreshAllocationLabels);
        allocationSection.setOnPlanApplied(this::renderItemsViewSection);
        renderItemsViewSection();
    }

    private void renderItemsViewSection() {
        itemsSection = new RequestProcessingItemsView(
            items,
            allSites,
            siteFilter.getExcludedSiteIds(),
            allocationSection,
            earliestDeliveryDate,
            expandedItemIndex,
            this::handleOptimizeAllocation,
            this::handleShowAllPlans,
            this::toggleExpandedItem
        );

        itemsTableContainer.getChildren().setAll(itemsSection.build());
        allocationSection.setAllocFractionLabels(itemsSection.getAllocationFractionLabels());
        refreshAllocationLabels();
    }

    private void handleOptimizeAllocation() {
        allocationSection.applyOptimalAllocation();
        renderItemsViewSection();
    }

    private void handleShowAllPlans() {
        if (allocationSection != null) {
            allocationSection.showAllAllocationsDialog();
        }
    }

    private void handleSiteFilterChanged() {
        AllocationPlan.using(allocations).removeSites(siteFilter.getExcludedSiteIds());
        allocationSection = new AllocationControl(
            items,
            allSites,
            siteFilter.getExcludedSiteIds(),
            siteFilter.getPrioritySiteIds(),
            allocations,
            deadlineDays
        );
        allocationSection.setOnAllocationChanged(this::refreshAllocationLabels);
        allocationSection.setOnPlanApplied(this::renderItemsViewSection);
        renderItemsViewSection();
    }

    private void toggleExpandedItem(int index) {
        expandedItemIndex = expandedItemIndex == index ? -1 : index;
        renderItemsViewSection();
    }

    private void refreshAllocationLabels() {
        if (itemsSection != null) {
            itemsSection.refreshAllocationLabels();
        }
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
