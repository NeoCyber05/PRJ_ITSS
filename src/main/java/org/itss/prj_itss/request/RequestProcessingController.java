package org.itss.prj_itss.request;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.itss.prj_itss.common.AppStyles;
import org.itss.prj_itss.common.ToastHelper;
import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.dao.IInventoryDAO;
import org.itss.prj_itss.dao.IMerchandiseDAO;
import org.itss.prj_itss.dao.IRequestDAO;
import org.itss.prj_itss.dao.ISiteDAO;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.RequestMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.layout.Navigator;
import org.itss.prj_itss.layout.ViewController;
import org.itss.prj_itss.request.RequestModels.Allocation;
import org.itss.prj_itss.request.RequestModels.ItemReq;
import org.itss.prj_itss.request.RequestModels.SiteInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RequestProcessingController implements ViewController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Navigator navigator;
    private int requestId = -1;

    private IRequestDAO requestDAO;
    private ISiteDAO siteDAO;
    private IInventoryDAO inventoryDAO;
    private IMerchandiseDAO merchandiseDAO;

    private final List<ItemReq> items = new ArrayList<>();
    private final List<SiteInfo> allSites = new ArrayList<>();
    private final Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();

    private SiteFilterSection siteFilter;
    private AllocationSection allocationSection;
    private Label[] allocationStatusLabels = new Label[0];
    private Label[] allocationFractionLabels = new Label[0];
    private int deadlineDays = 14;
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
    public void init(Navigator navigator, DAOFactory daoFactory) {
        this.navigator = navigator;
        this.requestDAO = daoFactory.getRequestDAO();
        this.siteDAO = daoFactory.getSiteDAO();
        this.inventoryDAO = daoFactory.getInventoryDAO();
        this.merchandiseDAO = daoFactory.getMerchandiseDAO();
        loadIfReady();
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
        loadIfReady();
    }

    @FXML
    private void goBack() {
        if (navigator != null) {
            navigator.showView("received-requests");
        }
    }

    @FXML
    private void handleConfirm() {
        List<String> errors = new ArrayList<>();
        for (ItemReq item : items) {
            int allocated = getAllocated(item.merchandiseId);
            if (allocated < item.required) {
                errors.add("• " + item.code + " chỉ phân bổ " + allocated + "/" + item.required);
            }
            if (allocated > item.required) {
                errors.add("• " + item.code + " phân bổ vượt " + allocated + "/" + item.required);
            }
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Chưa hoàn tất");
            alert.setHeaderText("Vui lòng kiểm tra:");
            alert.setContentText(String.join("\n", errors));
            ToastHelper.styleDialog(alert);
            alert.showAndWait();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Bạn có chắc muốn tạo đơn?");
        int totalQuantity = items.stream().mapToInt(item -> item.required).sum();
        long siteCount = allocations.values().stream().flatMap(map -> map.keySet().stream()).distinct().count();
        confirmAlert.setContentText("Tổng: " + totalQuantity + " chiếc | " + siteCount + " site");
        ToastHelper.styleDialog(confirmAlert);
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            navigator.showView("orders");
            ToastHelper.showToast("Đã tạo đơn hàng thành công.");
        }
    }

    private void loadIfReady() {
        if (navigator == null || requestDAO == null || requestId <= 0) {
            return;
        }

        resetState();
        loadDataFromDatabase();
        render();
    }

    private void resetState() {
        items.clear();
        allSites.clear();
        allocations.clear();
        earliestDeliveryDate = null;
        deadlineDays = 14;
    }

    private void loadDataFromDatabase() {
        List<RequestMerchandise> requestItems = requestDAO.findItemsByRequestId(requestId);
        for (RequestMerchandise requestItem : requestItems) {
            Merchandise merchandise = merchandiseDAO.findById(requestItem.getMerchandiseId());
            if (merchandise != null) {
                items.add(new ItemReq(
                    merchandise.getId(),
                    merchandise.getCode(),
                    merchandise.getName(),
                    requestItem.getQuantityOrdered().intValue()
                ));
            }
        }

        earliestDeliveryDate = requestDAO.getEarliestDeliveryDate(requestId);
        if (earliestDeliveryDate != null) {
            deadlineDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), earliestDeliveryDate);
            if (deadlineDays < 1) {
                deadlineDays = 1;
            }
        }

        for (Site site : siteDAO.findAll()) {
            Map<Integer, Integer> inventory = inventoryDAO.getInventoryBySiteId(site.getId());
            allSites.add(new SiteInfo(
                site.getId(),
                site.getSiteCode(),
                site.getName(),
                site.getDescription(),
                site.getShipDeliveryDays() == null ? 999 : site.getShipDeliveryDays(),
                site.getAirDeliveryDays() == null ? 999 : site.getAirDeliveryDays(),
                inventory
            ));
        }

        for (ItemReq item : items) {
            allocations.put(item.merchandiseId, new LinkedHashMap<>());
        }
    }

    private void render() {
        int totalQuantity = items.stream().mapToInt(item -> item.required).sum();
        requestCodeLabel.setText("Yêu cầu " + String.format("YC-2026-%03d", requestId));
        requestSummaryLabel.setText(
            "Ngày cần giao: " + (earliestDeliveryDate == null ? "N/A" : earliestDeliveryDate.format(DATE_FORMAT))
                + "  •  " + items.size() + " mặt hàng"
                + "  •  " + totalQuantity + " chiếc"
        );
        requestStatusLabel.setText("Chờ xử lý");

        siteFilter = new SiteFilterSection(allSites);
        siteFilter.setOnFiltersChanged(this::handleSiteFilterChanged);
        siteFilterContainer.getChildren().setAll(siteFilter.build());

        rebuildItemsAndAllocationSections();
    }

    private void rebuildItemsAndAllocationSections() {
        allocationStatusLabels = new Label[items.size()];
        allocationFractionLabels = new Label[items.size()];

        itemsTableContainer.getChildren().setAll(buildItemsTable());

        allocationSection = new AllocationSection(
            items,
            allSites,
            siteFilter.getExcludedSiteIds(),
            siteFilter.getPrioritySiteIds(),
            allocations,
            deadlineDays
        );
        allocationSection.setAllocFractionLabels(allocationFractionLabels);
        allocationSection.setOnAllocationChanged(this::refreshAllocationLabels);

        allocationContainer.getChildren().setAll(allocationSection.buildWrapper());
        refreshAllocationLabels();
    }

    private VBox buildItemsTable() {
        VBox card = new VBox(0);
        card.setStyle(AppStyles.cardStyle());
        card.setPadding(Insets.EMPTY);

        HBox header = new HBox();
        header.setPadding(new Insets(11, 20, 11, 20));
        header.setStyle("-fx-background-color:#F5F9F6;-fx-border-color:transparent transparent #E8EEEA transparent;-fx-border-width:0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(
            AppStyles.colHeader("MÃ HÀNG", 140),
            AppStyles.colHeader("SỐ LƯỢNG YÊU CẦU", 150),
            AppStyles.colHeader("NGÀY CẦN GIAO", 140),
            AppStyles.colHeader("ĐÃ PHÂN BỔ", 160),
            spacer,
            AppStyles.colHeader("TỔNG TỒN KHO", 120)
        );

        card.getChildren().add(header);

        for (int index = 0; index < items.size(); index++) {
            card.getChildren().add(buildItemRow(items.get(index), index));
        }

        return card;
    }

    private HBox buildItemRow(ItemReq item, int index) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setStyle("-fx-border-color:transparent transparent #F0F4F2 transparent;-fx-border-width:0 0 1 0;");

        VBox codeColumn = new VBox(2);
        codeColumn.setMinWidth(140);

        Label codeLabel = new Label(item.code);
        codeLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        Label nameLabel = new Label(item.name);
        nameLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#6B7C72;");
        codeColumn.getChildren().addAll(codeLabel, nameLabel);

        Label requiredLabel = new Label(item.required + " chiếc");
        requiredLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;");
        requiredLabel.setMinWidth(150);

        Label deadlineLabel = new Label(earliestDeliveryDate == null ? "N/A" : earliestDeliveryDate.format(DATE_FORMAT));
        deadlineLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;");
        deadlineLabel.setMinWidth(140);

        VBox allocationColumn = new VBox(4);
        allocationColumn.setMinWidth(160);

        Label stateLabel = new Label();
        Label fractionLabel = new Label();
        allocationStatusLabels[index] = stateLabel;
        allocationFractionLabels[index] = fractionLabel;
        allocationColumn.getChildren().addAll(stateLabel, fractionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int totalStock = allSites.stream()
            .filter(site -> !siteFilter.getExcludedSiteIds().contains(site.id))
            .mapToInt(site -> site.stock.getOrDefault(item.merchandiseId, 0))
            .sum();

        VBox stockColumn = new VBox(2);
        stockColumn.setAlignment(Pos.CENTER_RIGHT);
        stockColumn.setMinWidth(120);

        Label stockValueLabel = new Label(String.valueOf(totalStock));
        stockValueLabel.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        Label stockTextLabel = new Label("Hiện tồn kho");
        stockTextLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#8FA899;");
        stockColumn.getChildren().addAll(stockValueLabel, stockTextLabel);

        row.getChildren().addAll(codeColumn, requiredLabel, deadlineLabel, allocationColumn, spacer, stockColumn);
        updateAllocationLabels(item, index);
        return row;
    }

    private void handleSiteFilterChanged() {
        pruneExcludedAllocations();
        rebuildItemsAndAllocationSections();
    }

    private void pruneExcludedAllocations() {
        for (Map<Integer, Allocation> itemAllocations : allocations.values()) {
            itemAllocations.keySet().removeIf(siteFilter.getExcludedSiteIds()::contains);
        }
    }

    private void refreshAllocationLabels() {
        for (int index = 0; index < items.size(); index++) {
            updateAllocationLabels(items.get(index), index);
        }
    }

    private void updateAllocationLabels(ItemReq item, int index) {
        if (index >= allocationStatusLabels.length || index >= allocationFractionLabels.length) {
            return;
        }

        Label stateLabel = allocationStatusLabels[index];
        Label fractionLabel = allocationFractionLabels[index];
        if (stateLabel == null || fractionLabel == null) {
            return;
        }

        int allocated = getAllocated(item.merchandiseId);
        boolean done = allocated >= item.required;

        if (done) {
            stateLabel.setText("Đủ");
            stateLabel.setStyle("-fx-background-color:#E8F5E9;-fx-text-fill:#2E7D32;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        } else if (allocated > 0) {
            stateLabel.setText("Chưa đủ");
            stateLabel.setStyle("-fx-background-color:#FFF3E0;-fx-text-fill:#E65100;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        } else {
            stateLabel.setText("Chưa phân bổ");
            stateLabel.setStyle("-fx-background-color:#F0F4F2;-fx-text-fill:#8FA899;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        }

        fractionLabel.setText(allocated + "/" + item.required + " chiếc");
        fractionLabel.setStyle("-fx-font-size:13px;-fx-text-fill:" + (done ? "#2E6F40" : "#E65100") + ";-fx-font-weight:bold;");
    }

    private int getAllocated(int merchandiseId) {
        return allocations.getOrDefault(merchandiseId, Collections.emptyMap())
            .values()
            .stream()
            .mapToInt(allocation -> allocation.qty.get())
            .sum();
    }
}
