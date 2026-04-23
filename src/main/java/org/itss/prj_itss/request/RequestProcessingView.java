package org.itss.prj_itss.request;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.itss.prj_itss.dao.DAOFactory;
import org.itss.prj_itss.dao.IInventoryDAO;
import org.itss.prj_itss.dao.IMerchandiseDAO;
import org.itss.prj_itss.dao.IOrderDAO;
import org.itss.prj_itss.dao.IRequestDAO;
import org.itss.prj_itss.dao.ISiteDAO;
import org.itss.prj_itss.db.DatabaseConnection;
import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.entity.Order;
import org.itss.prj_itss.entity.OrderMerchandise;
import org.itss.prj_itss.entity.RequestMerchandise;
import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.layout.Navigator;
import org.itss.prj_itss.request.RequestModels.Allocation;
import org.itss.prj_itss.request.RequestModels.ItemReq;
import org.itss.prj_itss.request.RequestModels.SiteInfo;
import org.itss.prj_itss.ui.Notifications;
import org.itss.prj_itss.ui.StatusNodes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RequestProcessingView {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BorderPane view;
    private final Navigator navigator;
    private final int requestId;

    private final IRequestDAO requestDAO;
    private final IOrderDAO orderDAO;
    private final ISiteDAO siteDAO;
    private final IInventoryDAO inventoryDAO;
    private final IMerchandiseDAO merchandiseDAO;

    private final List<ItemReq> items = new ArrayList<>();
    private final List<SiteInfo> allSites = new ArrayList<>();
    private final Map<Integer, Map<Integer, Allocation>> allocations = new LinkedHashMap<>();
    private final Map<Integer, LocalDate> desiredDeliveryDates = new LinkedHashMap<>();

    private SiteFilterSection siteFilter;
    private AllocationSection allocationSection;
    private Label[] allocStatusLabels = new Label[0];
    private Label[] allocFractionLabels = new Label[0];
    private VBox itemsTableContainer;
    private LocalDate earliestDeliveryDate;
    private int deadlineDays = 14;
    private int expandedItemIndex = -1;

    public RequestProcessingView(Navigator navigator, DAOFactory daoFactory, int requestId) {
        this.navigator = navigator;
        this.requestId = requestId;
        this.requestDAO = daoFactory.getRequestDAO();
        this.orderDAO = daoFactory.getOrderDAO();
        this.siteDAO = daoFactory.getSiteDAO();
        this.inventoryDAO = daoFactory.getInventoryDAO();
        this.merchandiseDAO = daoFactory.getMerchandiseDAO();

        this.view = new BorderPane();
        this.view.setStyle("-fx-background-color: #F5F9F6;");

        loadDataFromDatabase();
        buildView();
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
                desiredDeliveryDates.put(merchandise.getId(), requestItem.getDesiredDeliveryDate());
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
                site.getShipDeliveryDays() != null ? site.getShipDeliveryDays() : 999,
                site.getAirDeliveryDays() != null ? site.getAirDeliveryDays() : 999,
                inventory
            ));
        }

        for (ItemReq item : items) {
            allocations.put(item.merchandiseId, new LinkedHashMap<>());
        }
    }

    private void buildView() {
        view.setTop(buildHeader());

        siteFilter = new SiteFilterSection(allSites);
        siteFilter.setOnFiltersChanged(this::handleSiteFilterChanged);

        allocationSection = new AllocationSection(
            items,
            allSites,
            siteFilter.getExcludedSiteIds(),
            siteFilter.getPrioritySiteIds(),
            allocations,
            deadlineDays
        );
        allocationSection.setOnAllocationChanged(this::refreshAllocationLabels);
        allocationSection.setOnPlanApplied(this::rebuildItemsTable);

        itemsTableContainer = new VBox();
        rebuildItemsTable();

        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 28, 20, 28));
        content.setStyle("-fx-background-color: #F5F9F6;");
        content.getChildren().addAll(siteFilter.build(), itemsTableContainer);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F5F9F6; -fx-background: #F5F9F6;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        view.setCenter(scrollPane);
        view.setBottom(buildBottomBar());
    }

    private void rebuildItemsTable() {
        allocStatusLabels = new Label[items.size()];
        allocFractionLabels = new Label[items.size()];
        allocationSection.setAllocFractionLabels(allocFractionLabels);
        itemsTableContainer.getChildren().setAll(buildItemsTable());
        refreshAllocationLabels();
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 28, 18, 28));
        header.setStyle(
            "-fx-background-color:white;"
                + "-fx-border-color:transparent transparent #D8E8DD transparent;"
                + "-fx-border-width:0 0 1 0;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),4,0,0,2);"
        );

        VBox left = new VBox(3);
        Label sectionLabel = new Label("XỬ LÝ YÊU CẦU");
        sectionLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#2E6F40;");

        String requestCode = String.format("YC-2026-%03d", requestId);
        Label titleLabel = new Label("Yêu cầu " + requestCode);
        titleLabel.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");

        int totalQuantity = items.stream().mapToInt(item -> item.required).sum();
        Label descriptionLabel = new Label(
            "Ngày cần giao: " + (earliestDeliveryDate != null ? earliestDeliveryDate.format(DATE_FMT) : "N/A")
                + "  •  " + items.size() + " mặt hàng"
                + "  •  " + totalQuantity + " chiếc"
        );
        descriptionLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#6B7C72;");

        left.getChildren().addAll(sectionLabel, titleLabel, descriptionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label("Chờ xử lý");
        statusBadge.setStyle(
            "-fx-background-color:#FFF3E0;"
                + "-fx-text-fill:#E65100;"
                + "-fx-background-radius:8;"
                + "-fx-padding:6 14;"
                + "-fx-font-size:13px;"
                + "-fx-font-weight:bold;"
        );

        header.getChildren().addAll(left, spacer, statusBadge);
        return header;
    }

    private VBox buildItemsTable() {
        VBox card = new VBox(0);
        card.setStyle(cardStyle());
        card.setPadding(Insets.EMPTY);

        card.getChildren().add(buildTableToolbar());

        HBox header = new HBox();
        header.setPadding(new Insets(11, 20, 11, 20));
        header.setStyle("-fx-background-color:#F5F9F6;-fx-border-color:transparent transparent #E8EEEA transparent;-fx-border-width:0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(
            columnHeader("MÃ HÀNG", 200),
            columnHeader("SỐ LƯỢNG YÊU CẦU", 170),
            columnHeader("NGÀY CẦN GIAO", 150),
            columnHeader("PHÂN BỔ HIỆN TẠI", 180),
            spacer,
            columnHeader("TỒN KHO SITE", 160)
        );

        card.getChildren().add(header);
        for (int index = 0; index < items.size(); index++) {
            card.getChildren().add(buildItemBlock(items.get(index), index));
        }

        return card;
    }

    private HBox buildTableToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(18, 20, 18, 20));
        toolbar.setStyle("-fx-border-color:transparent transparent #EEF3EF transparent;-fx-border-width:0 0 1 0;");

        VBox titleBox = new VBox(3);
        Label sectionLabel = new Label("PHÂN BỔ THEO MẶT HÀNG");
        sectionLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#2E6F40;");
        Label titleLabel = new Label("Điều chỉnh tồn kho theo từng yêu cầu");
        titleLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        titleBox.getChildren().addAll(sectionLabel, titleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button optimizeButton = new Button("Gợi ý tối ưu");
        optimizeButton.getStyleClass().add("request-toolbar-secondary-button");
        optimizeButton.setOnAction(event -> handleOptimizeAllocation());

        Button showAllButton = new Button("Xem tất cả phương án");
        showAllButton.getStyleClass().add("request-toolbar-primary-button");
        showAllButton.setOnAction(event -> allocationSection.showAllAllocationsDialog());

        toolbar.getChildren().addAll(titleBox, spacer, optimizeButton, showAllButton);
        return toolbar;
    }

    private VBox buildItemBlock(ItemReq item, int index) {
        VBox block = new VBox(0);
        block.getChildren().add(buildItemRow(item, index));
        if (expandedItemIndex == index) {
            block.getChildren().add(allocationSection.buildInlineEditor(item, index));
        }
        return block;
    }

    private HBox buildItemRow(ItemReq item, int index) {
        boolean expanded = expandedItemIndex == index;

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle(
            "-fx-border-color:transparent transparent #F0F4F2 transparent;"
                + "-fx-border-width:0 0 1 0;"
        );

        VBox codeColumn = new VBox(4);
        codeColumn.setMinWidth(200);

        Label codeLabel = new Label(item.code);
        codeLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");
        Label nameLabel = new Label(item.name);
        nameLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#6B7C72;");
        codeColumn.getChildren().addAll(codeLabel, nameLabel);

        Label requiredLabel = new Label(item.required + " chiếc");
        requiredLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;");
        requiredLabel.setMinWidth(170);

        Label deadlineLabel = new Label(earliestDeliveryDate != null ? earliestDeliveryDate.format(DATE_FMT) : "N/A");
        deadlineLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#1a2e22;");
        deadlineLabel.setMinWidth(150);

        VBox allocationColumn = new VBox(4);
        allocationColumn.setMinWidth(180);

        Label allocationStatusLabel = new Label();
        Label allocationFractionLabel = new Label();
        allocStatusLabels[index] = allocationStatusLabel;
        allocFractionLabels[index] = allocationFractionLabel;
        allocationColumn.getChildren().addAll(allocationStatusLabel, allocationFractionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int totalStock = allSites.stream()
            .filter(site -> !siteFilter.getExcludedSiteIds().contains(site.id))
            .mapToInt(site -> site.stock.getOrDefault(item.merchandiseId, 0))
            .sum();

        VBox stockColumn = new VBox(10);
        stockColumn.setAlignment(Pos.CENTER_RIGHT);
        stockColumn.setMinWidth(160);

        Label stockValueLabel = new Label(String.valueOf(totalStock));
        stockValueLabel.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1a2e22;");

        Button toggleButton = new Button(expanded ? "Ẩn tồn kho" : "Hiện tồn kho");
        toggleButton.getStyleClass().add(expanded ? "forest-dark-button" : "forest-outline-button");
        toggleButton.setOnAction(event -> toggleExpandedItem(index));

        stockColumn.getChildren().addAll(stockValueLabel, toggleButton);

        row.getChildren().addAll(codeColumn, requiredLabel, deadlineLabel, allocationColumn, spacer, stockColumn);
        updateAllocationLabels(item, index);
        return row;
    }

    private void toggleExpandedItem(int index) {
        expandedItemIndex = expandedItemIndex == index ? -1 : index;
        rebuildItemsTable();
    }

    private void handleOptimizeAllocation() {
        allocationSection.applyOptimalAllocation();
        rebuildItemsTable();
    }

    private void handleSiteFilterChanged() {
        pruneExcludedAllocations();
        rebuildItemsTable();
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
        if (index >= allocStatusLabels.length || index >= allocFractionLabels.length) {
            return;
        }

        Label stateLabel = allocStatusLabels[index];
        Label fractionLabel = allocFractionLabels[index];
        if (stateLabel == null || fractionLabel == null) {
            return;
        }

        int allocated = allocationSection.getAllocated(item.merchandiseId);
        if (allocated > item.required) {
            stateLabel.setText("Vượt mức");
            stateLabel.setStyle("-fx-background-color:#FEE2E2;-fx-text-fill:#B91C1C;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
            fractionLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#B91C1C;-fx-font-weight:bold;");
        } else if (allocated == item.required) {
            stateLabel.setText("Đủ");
            stateLabel.setStyle("-fx-background-color:#E8F5E9;-fx-text-fill:#2E7D32;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
            fractionLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#2E6F40;-fx-font-weight:bold;");
        } else if (allocated > 0) {
            stateLabel.setText("Chưa đủ");
            stateLabel.setStyle("-fx-background-color:#FFF3E0;-fx-text-fill:#E65100;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
            fractionLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#E65100;-fx-font-weight:bold;");
        } else {
            stateLabel.setText("Chưa có phương án");
            stateLabel.setStyle("-fx-background-color:#F0F4F2;-fx-text-fill:#6B7C72;-fx-background-radius:10;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
            fractionLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#6B7C72;-fx-font-weight:bold;");
        }

        fractionLabel.setText(allocated + "/" + item.required);
    }

    private HBox buildBottomBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(16, 28, 16, 28));
        bar.setStyle("-fx-background-color:white;-fx-border-color:#D8E8DD transparent transparent transparent;-fx-border-width:1 0 0 0;");

        Button backButton = new Button("Danh sách yêu cầu");
        backButton.setStyle(secondaryButtonStyle());
        backButton.setOnAction(event -> navigator.showView("received-requests"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button confirmButton = new Button("Xác nhận và gửi");
        confirmButton.getStyleClass().add("request-confirm-button");
        confirmButton.setOnAction(event -> handleConfirm());

        bar.getChildren().addAll(backButton, spacer, confirmButton);
        return bar;
    }

    private void handleConfirm() {
        String validationMessage = validateSubmission();
        if (validationMessage != null) {
            showValidationError(validationMessage);
            return;
        }

        showAllocationPreviewDialog();
    }

    private String validateSubmission() {
        for (ItemReq item : items) {
            int allocated = allocationSection.getAllocated(item.merchandiseId);
            if (allocated < item.required) {
                return "Chưa đủ số lượng hàng cần";
            }
            if (allocated > item.required) {
                return "Số lượng phân bổ vượt yêu cầu";
            }
        }

        for (ItemReq item : items) {
            LocalDate desiredDate = desiredDeliveryDates.get(item.merchandiseId);
            int itemDeadlineDays = desiredDate == null
                ? deadlineDays
                : Math.max(1, (int) ChronoUnit.DAYS.between(LocalDate.now(), desiredDate));

            Map<Integer, Allocation> itemAllocations = allocations.getOrDefault(item.merchandiseId, Map.of());
            for (Allocation allocation : itemAllocations.values()) {
                SiteInfo site = findSiteInfo(allocation.siteId);
                if (site == null) {
                    return "Không đáp ứng ngày nhận mong muốn";
                }

                int deliveryDays = getDeliveryDays(site, allocation.transport);
                if (deliveryDays >= 999 || deliveryDays > itemDeadlineDays) {
                    return "Không đáp ứng ngày nhận mong muốn";
                }
            }
        }

        return null;
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Không hợp lệ");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Notifications.styleDialog(alert);
        alert.showAndWait();
    }

    private void createAllocatedOrders() throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            for (Map.Entry<Integer, List<Allocation>> siteEntry : groupAllocationsBySite().entrySet()) {
                Order order = new Order();
                order.setRequestId(requestId);
                order.setSiteId(siteEntry.getKey());
                order.setStatus("Chờ xác nhận");

                int orderId = orderDAO.create(order);
                if (orderId <= 0) {
                    throw new SQLException("Cannot create order for site " + siteEntry.getKey());
                }

                for (Allocation allocation : siteEntry.getValue()) {
                    OrderMerchandise orderItem = new OrderMerchandise(
                        orderId,
                        allocation.merchandiseId,
                        BigDecimal.valueOf(allocation.qty.get()),
                        toStoredDeliveryMethod(allocation.transport)
                    );
                    if (!orderDAO.addItem(orderItem)) {
                        throw new SQLException("Cannot create order line for order " + orderId);
                    }
                }
            }

            if (!requestDAO.updateStatus(requestId, "Đang xử lý")) {
                throw new SQLException("Cannot update request status " + requestId);
            }

            connection.commit();
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private void showAllocationPreviewDialog() {
        List<PreviewOrder> previewOrders = buildPreviewOrders();

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (view.getScene() != null && view.getScene().getWindow() != null) {
            dialog.initOwner(view.getScene().getWindow());
        }
        dialog.setTitle("Chi tiết phân bổ đơn hàng");
        dialog.setResizable(true);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color:#F5F9F6;");
        root.setPrefWidth(980);
        root.setPrefHeight(760);

        Label titleLabel = new Label("Chi tiết phân bổ đơn hàng");
        titleLabel.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#1a2e22;");

        int totalQuantity = previewOrders.stream()
            .flatMap(order -> order.lines().stream())
            .mapToInt(PreviewLine::quantity)
            .sum();
        int totalLines = previewOrders.stream()
            .mapToInt(order -> order.lines().size())
            .sum();

        Label subtitleLabel = new Label(
            previewOrders.size() + " đơn hàng dự kiến"
                + " • " + totalLines + " dòng phân bổ"
                + " • " + totalQuantity + " chiếc"
        );
        subtitleLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#6B7C72;");

        VBox ordersBox = new VBox(14);
        for (int index = 0; index < previewOrders.size(); index++) {
            ordersBox.getChildren().add(buildPreviewOrderCard(previewOrders.get(index), index + 1));
        }

        ScrollPane scrollPane = new ScrollPane(ordersBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(620);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button backButton = new Button("Quay lại");
        backButton.getStyleClass().add("forest-secondary-button");
        backButton.setOnAction(event -> dialog.close());

        Button sendButton = new Button("Gửi đơn hàng");
        sendButton.getStyleClass().add("request-confirm-button");
        sendButton.setOnAction(event -> {
            try {
                createAllocatedOrders();
                dialog.close();
                navigator.showView("orders");
                Notifications.showToast("Đã tạo đơn hàng thành công.");
            } catch (SQLException exception) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Không thể tạo đơn");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Không thể tạo các đơn hàng đã phân bổ.");
                Notifications.styleDialog(errorAlert);
                errorAlert.showAndWait();
            }
        });

        HBox footer = new HBox(12, backButton, sendButton);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titleLabel, subtitleLabel, scrollPane, footer);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private Map<Integer, List<Allocation>> groupAllocationsBySite() {
        Map<Integer, List<Allocation>> groupedAllocations = new LinkedHashMap<>();
        for (Map<Integer, Allocation> itemAllocations : allocations.values()) {
            for (Allocation allocation : itemAllocations.values()) {
                if (allocation.qty.get() <= 0) {
                    continue;
                }
                groupedAllocations
                    .computeIfAbsent(allocation.siteId, key -> new ArrayList<>())
                    .add(allocation);
            }
        }
        return groupedAllocations;
    }

    private List<PreviewOrder> buildPreviewOrders() {
        List<PreviewOrder> previewOrders = new ArrayList<>();
        int sequence = 1;

        for (Map.Entry<Integer, List<Allocation>> siteEntry : groupAllocationsBySite().entrySet()) {
            SiteInfo site = findSiteInfo(siteEntry.getKey());
            if (site == null) {
                continue;
            }

            List<PreviewLine> lines = new ArrayList<>();
            for (Allocation allocation : siteEntry.getValue()) {
                ItemReq item = findItem(allocation.merchandiseId);
                if (item == null) {
                    continue;
                }

                LocalDate desiredDate = desiredDeliveryDates.get(item.merchandiseId);
                int deliveryDays = getDeliveryDays(site, allocation.transport);
                LocalDate estimatedDate = LocalDate.now().plusDays(deliveryDays);
                lines.add(new PreviewLine(
                    item,
                    allocation.qty.get(),
                    allocation.transport,
                    desiredDate,
                    estimatedDate
                ));
            }

            lines.sort((left, right) -> left.item().code.compareToIgnoreCase(right.item().code));
            previewOrders.add(new PreviewOrder(sequence++, site, lines));
        }

        return previewOrders;
    }

    private SiteInfo findSiteInfo(int siteId) {
        for (SiteInfo site : allSites) {
            if (site.id == siteId) {
                return site;
            }
        }
        return null;
    }

    private ItemReq findItem(int merchandiseId) {
        for (ItemReq item : items) {
            if (item.merchandiseId == merchandiseId) {
                return item;
            }
        }
        return null;
    }

    private int getDeliveryDays(SiteInfo site, String transport) {
        return isAirTransport(transport) ? site.airDays : site.shipDays;
    }

    private boolean isAirTransport(String transport) {
        if (transport == null) {
            return false;
        }
        String normalized = transport.trim().toLowerCase();
        return normalized.contains("air")
            || normalized.contains("hàng không")
            || normalized.contains("hang khong")
            || normalized.contains("máy")
            || normalized.contains("may");
    }

    private String toStoredDeliveryMethod(String transport) {
        return isAirTransport(transport) ? "Hàng không" : "Đường biển";
    }

    private VBox buildPreviewOrderCard(PreviewOrder order, int index) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color:white;"
                + "-fx-background-radius:14;"
                + "-fx-border-radius:14;"
                + "-fx-border-color:#D8E8DD;"
                + "-fx-border-width:1;"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label orderLabel = new Label("Đơn hàng dự kiến " + index);
        orderLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1a2e22;");
        Label siteLabel = new Label(order.site().name + " • " + order.site().siteCode);
        siteLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#6B7C72;");
        titleBox.getChildren().addAll(orderLabel, siteLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int totalQuantity = order.lines().stream().mapToInt(PreviewLine::quantity).sum();
        Label qtyBadge = new Label(totalQuantity + " chiếc");
        qtyBadge.setStyle(
            "-fx-background-color:#EEF4FF;"
                + "-fx-text-fill:#2456C2;"
                + "-fx-background-radius:999;"
                + "-fx-padding:6 12;"
                + "-fx-font-size:11px;"
                + "-fx-font-weight:bold;"
        );

        header.getChildren().addAll(titleBox, spacer, qtyBadge);

        VBox table = new VBox(0);
        table.setStyle(
            "-fx-background-color:#FCFEFD;"
                + "-fx-background-radius:12;"
                + "-fx-border-radius:12;"
                + "-fx-border-color:#E5ECE7;"
                + "-fx-border-width:1;"
        );
        table.getChildren().add(buildPreviewTableHeader());
        for (PreviewLine line : order.lines()) {
            table.getChildren().add(buildPreviewTableRow(line));
        }

        card.getChildren().addAll(header, table);
        return card;
    }

    private HBox buildPreviewTableHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setStyle("-fx-background-color:#F7FAF8; -fx-background-radius:12 12 0 0;");
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

    private HBox buildPreviewTableRow(PreviewLine line) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle("-fx-border-color:transparent transparent #EEF3EF transparent; -fx-border-width:0 0 1 0;");

        row.getChildren().add(previewValueCell(line.item().code, 120, true));
        row.getChildren().add(previewValueCell(line.item().name, 250, false));
        row.getChildren().add(previewValueCell(String.valueOf(line.quantity()), 110, true));

        HBox transportBox = new HBox(StatusNodes.buildTransportBadgeCompact(toStoredDeliveryMethod(line.transport())));
        transportBox.setAlignment(Pos.CENTER_LEFT);
        transportBox.setMinWidth(150);
        transportBox.setPrefWidth(150);
        row.getChildren().add(transportBox);

        row.getChildren().add(previewValueCell(line.estimatedDate().format(DATE_FMT), 140, false));
        row.getChildren().add(previewValueCell(
            line.desiredDate() == null ? "N/A" : line.desiredDate().format(DATE_FMT),
            140,
            false
        ));
        return row;
    }

    private Label previewHeaderCell(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#6B7F95;");
        return label;
    }

    private Label previewValueCell(String text, double width, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(
            "-fx-font-size:12px;"
                + "-fx-text-fill:#1a2e22;"
                + (bold ? "-fx-font-weight:bold;" : "")
        );
        return label;
    }

    private String cardStyle() {
        return "-fx-background-color: white; -fx-background-radius: 12;"
            + "-fx-border-radius: 12; -fx-border-color: #E0EBE4; -fx-border-width: 1;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);";
    }

    private Label columnHeader(String text, double width) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        label.setMinWidth(width);
        label.setPrefWidth(width);
        return label;
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: white; -fx-text-fill: #2E6F40;"
            + "-fx-background-radius: 7; -fx-border-color: #2E6F40; -fx-border-radius: 7;"
            + "-fx-border-width: 1.5; -fx-cursor: hand; -fx-font-size: 13px;"
            + "-fx-font-weight: bold; -fx-padding: 9 20;";
    }

    public Node getView() {
        return view;
    }

    private record PreviewOrder(int index, SiteInfo site, List<PreviewLine> lines) {
    }

    private record PreviewLine(
        ItemReq item,
        int quantity,
        String transport,
        LocalDate desiredDate,
        LocalDate estimatedDate
    ) {
    }
}
