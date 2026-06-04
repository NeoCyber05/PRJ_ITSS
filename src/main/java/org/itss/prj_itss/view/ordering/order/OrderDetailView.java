package org.itss.prj_itss.view.ordering.order;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.input.ScrollEvent;
import org.itss.prj_itss.App;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderDetailController;
import org.itss.prj_itss.controller.ordering.order.OrderManagementController;
import org.itss.prj_itss.model.order.application.OrderDetailViewModel;
import org.itss.prj_itss.view.shared.ViewLifecycle;
import org.itss.prj_itss.view.ordering.order.components.OrderCancellationHandler;
import org.itss.prj_itss.view.ordering.order.components.OrderDetailCardBuilder;
import org.itss.prj_itss.view.ordering.order.components.OrderStatusRenderer;

public final class OrderDetailView implements ViewLifecycle {

    private final StackPane view;
    private Navigator navigator;
    private OrderDetailController controller;
    private OrderManagementController managementController;

    private String orderIdRaw;
    private BorderPane panelRoot;
    private Runnable onBackAction;
    private Runnable onCloseAction;
    private boolean isEmbedded = false;

    public OrderDetailView() {
        this.view = new StackPane();
        this.view.setStyle("-fx-background-color: #F3F7FB;");
    }

    public void init(Navigator navigator, OrderDetailController controller, OrderManagementController managementController, String orderIdRaw) {
        this.navigator = navigator;
        this.controller = controller;
        this.managementController = managementController;
        this.orderIdRaw = orderIdRaw;
        this.isEmbedded = false;
        
        this.onBackAction = () -> {
            if (this.navigator != null) {
                this.navigator.showView("orders");
            }
        };
        this.onCloseAction = null;

        this.view.getChildren().clear();

        Node background = loadOrdersBackground();
        background.setEffect(new GaussianBlur(14));
        background.setOpacity(0.96);

        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(15,23,42,0.34);");
        backdrop.setOnMouseClicked(event -> {
            if (this.navigator != null) {
                this.navigator.showView("orders");
            }
        });

        this.panelRoot = new BorderPane();
        this.panelRoot.setMaxWidth(Double.MAX_VALUE);
        this.panelRoot.setStyle("-fx-background-color: transparent;");

        buildPanelContent();

        VBox drawerContainer = new VBox(panelRoot);
        drawerContainer.setStyle("-fx-background-color: white; -fx-background-radius: 24 0 0 24; -fx-border-radius: 24 0 0 24;");
        drawerContainer.setPrefWidth(540);
        drawerContainer.setMinWidth(540);
        VBox.setVgrow(panelRoot, Priority.ALWAYS);

        HBox drawerLayer = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        drawerLayer.getChildren().addAll(spacer, drawerContainer);
        drawerLayer.setAlignment(Pos.CENTER_RIGHT);

        view.getChildren().addAll(background, backdrop, drawerLayer);
    }
    
    public void initAsEmbedded(OrderDetailController controller, String orderIdRaw, Runnable onBackAction, Runnable onCloseAction) {
        this.controller = controller;
        this.orderIdRaw = orderIdRaw;
        this.onBackAction = onBackAction;
        this.onCloseAction = onCloseAction;
        this.isEmbedded = true;
        this.navigator = null;
        this.managementController = null;
        
        this.view.getChildren().clear();
        this.view.setStyle("-fx-background-color: transparent;");
        
        this.panelRoot = new BorderPane();
        this.panelRoot.setMaxWidth(Double.MAX_VALUE);
        this.panelRoot.setStyle("-fx-background-color: transparent;"); // container handles background
        
        buildPanelContent();
        
        this.view.getChildren().add(this.panelRoot);
    }

    @Override
    public void onViewShown() {
        buildPanelContent();
    }

    private Node loadOrdersBackground() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/view/ordering/order/order-management-view.fxml"));
            Node background = loader.load();
            Object controllerObj = loader.getController();
            if (controllerObj instanceof OrderManagementView viewObj) {
                viewObj.init(navigator, managementController);
            }
            return background;
        } catch (Exception exception) {
            Label errorLabel = new Label("Không thể tải danh sách đơn hàng.");
            StackPane fallback = new StackPane(errorLabel);
            fallback.getStyleClass().add("content-area");
            return fallback;
        }
    }

    private void buildPanelContent() {
        if (controller == null) return;
        int orderId = parseOrderId(orderIdRaw);
        OrderDetailViewModel vm = controller.loadDetail(orderId);
        if (vm == null) {
            Label errorLabel = new Label("Không tìm thấy đơn hàng.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #DC2626; -fx-padding: 40;");
            panelRoot.setCenter(new StackPane(errorLabel));
            return;
        }

        VBox header = new VBox(18);
        header.setPadding(new Insets(28, 28, 22, 28));
        header.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #E7EDF5 transparent; -fx-border-width: 0 0 1 0;");

        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("‹");
        backButton.setOnAction(event -> {
            if (onBackAction != null) {
                onBackAction.run();
            }
        });
        backButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #D9E2EE;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-text-fill: #475569;" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-min-width: 38;" +
            "-fx-min-height: 38;" +
            "-fx-cursor: hand;"
        );

        VBox titleBox = new VBox(6);
        Label titleLabel = new Label("Chi tiết đơn hàng");
        titleLabel.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label subtitleLabel = new Label("Mã đơn hàng: " + vm.orderCode());
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #7B8DA6;");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        topRow.getChildren().addAll(backButton, titleBox);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        topRow.getChildren().add(topSpacer);

        if (vm.cancellable()) {
            Button cancelBtn = OrderCancellationHandler.createCancelButton(vm.orderId(), controller, onBackAction);
            topRow.getChildren().add(cancelBtn);
        }

        if (onCloseAction != null) {
            Button closeBtn = new Button("✕");
            closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #94A3B8;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            );
            closeBtn.setOnAction(e -> onCloseAction.run());
            topRow.getChildren().add(closeBtn);
        }

        Label topStatusBadge = OrderStatusRenderer.buildTopStatusBadge(vm.status());
        header.getChildren().addAll(topRow, topStatusBadge);

        VBox content = new VBox(22);
        content.setPadding(new Insets(16, 24, 16, 24));
        content.getChildren().addAll(
            OrderDetailCardBuilder.buildOverviewCard(vm),
            OrderStatusRenderer.buildProgressCard(vm.status()),
            OrderDetailCardBuilder.buildItemsCard(vm.items())
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double scrollRange = contentHeight - viewportHeight;
                if (scrollRange > 0) {
                    double deltaY = event.getDeltaY() * 3;
                    scrollPane.setVvalue(scrollPane.getVvalue() - deltaY / scrollRange);
                }
                event.consume();
            }
        });

        panelRoot.setTop(header);
        panelRoot.setCenter(scrollPane);
        
        BorderPane.setMargin(scrollPane, new Insets(0, 4, 24, 4));
    }

    private int parseOrderId(String orderIdRaw) {
        try {
            return Integer.parseInt(orderIdRaw.replaceAll("\\D+", ""));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    public javafx.scene.Node getView() {
        return view;
    }
}
