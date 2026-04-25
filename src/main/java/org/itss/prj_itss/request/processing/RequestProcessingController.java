package org.itss.prj_itss.request.processing;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.dto.Allocation;
import org.itss.prj_itss.dto.ItemRequirement;
import org.itss.prj_itss.dto.RequestProcessingData;
import org.itss.prj_itss.dto.SiteStockOption;
import org.itss.prj_itss.layout.INavigator;
import org.itss.prj_itss.layout.IViewController;
import org.itss.prj_itss.request.processing.allocation.AllocationSection;
import org.itss.prj_itss.request.processing.allocation.RequestProcessingAllocationSupport;
import org.itss.prj_itss.request.processing.items.RequestProcessingItemsSection;
import org.itss.prj_itss.request.processing.preview.RequestProcessingPreviewBuilder;
import org.itss.prj_itss.request.processing.preview.RequestProcessingPreviewDialog;
import org.itss.prj_itss.request.processing.site.SiteFilterSectionController;
import org.itss.prj_itss.service.RequestProcessingService;
import org.itss.prj_itss.ui.Notifications;

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
        "/org/itss/prj_itss/request/processing/site/site-filter-section.fxml";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RequestProcessingPreviewBuilder previewBuilder = new RequestProcessingPreviewBuilder();
    private final List<ItemRequirement> items = new ArrayList<>();
    private final List<SiteStockOption> allSites = new ArrayList<>();
    private final Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();
    private final Map<Integer, LocalDate> desiredDeliveryDates = new LinkedHashMap<>();

    private INavigator navigator;
    private int requestId = -1;
    private RequestProcessingService requestProcessingService;
    private SiteFilterSectionController siteFilter;
    private AllocationSection allocationSection;
    private RequestProcessingItemsSection itemsSection;
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
        render();
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

    private void render() {
        updateHeader();

        loadSiteFilterSection();

        if (allocationContainer != null) {
            allocationContainer.getChildren().clear();
            allocationContainer.setManaged(false);
            allocationContainer.setVisible(false);
        }

        rebuildAllocationSection();
        rebuildItemsSection();
    }

    private void updateHeader() {
        int totalQuantity = items.stream().mapToInt(item -> item.required).sum();
        requestCodeLabel.setText("Yêu cầu " + String.format("YC-2026-%03d", requestId));
        requestSummaryLabel.setText(
            "Ngày cần giao: " + (earliestDeliveryDate == null ? "N/A" : earliestDeliveryDate.format(DATE_FORMAT))
                + "  •  " + items.size() + " mặt hàng"
                + "  •  " + totalQuantity + " chiếc"
        );
        requestStatusLabel.setText("Chờ xử lý");
    }

    private void loadSiteFilterSection() {
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
    }

    private void rebuildAllocationSection() {
        allocationSection = new AllocationSection(
            items,
            allSites,
            siteFilter.getExcludedSiteIds(),
            siteFilter.getPrioritySiteIds(),
            allocations,
            deadlineDays
        );
        allocationSection.setOnAllocationChanged(this::refreshAllocationLabels);
        allocationSection.setOnPlanApplied(this::rebuildItemsSection);
    }

    private void rebuildItemsSection() {
        itemsSection = new RequestProcessingItemsSection(
            items,
            allSites,
            siteFilter.getExcludedSiteIds(),
            allocationSection,
            earliestDeliveryDate,
            expandedItemIndex,
            this::handleOptimizeAllocation,
            allocationSection::showAllAllocationsDialog,
            this::toggleExpandedItem
        );

        itemsTableContainer.getChildren().setAll(itemsSection.build());
        allocationSection.setAllocFractionLabels(itemsSection.getAllocationFractionLabels());
        refreshAllocationLabels();
    }

    private void handleOptimizeAllocation() {
        allocationSection.applyOptimalAllocation();
        rebuildItemsSection();
    }

    private void handleSiteFilterChanged() {
        RequestProcessingAllocationSupport.pruneExcludedAllocations(allocations, siteFilter.getExcludedSiteIds());
        rebuildAllocationSection();
        rebuildItemsSection();
    }

    private void toggleExpandedItem(int index) {
        expandedItemIndex = expandedItemIndex == index ? -1 : index;
        rebuildItemsSection();
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
        Notifications.styleDialog(alert);
        alert.showAndWait();
    }
}
