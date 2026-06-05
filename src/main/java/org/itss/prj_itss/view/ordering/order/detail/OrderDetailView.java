package org.itss.prj_itss.view.ordering.order.detail;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.itss.prj_itss.App;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderDetailController;
import org.itss.prj_itss.controller.ordering.order.OrderManagementController;
import org.itss.prj_itss.model.order.application.OrderDetailViewModel;
import org.itss.prj_itss.view.shared.ViewLifecycle;
import org.itss.prj_itss.view.ordering.order.detail.components.OrderCancellationHandler;
import org.itss.prj_itss.view.ordering.order.detail.components.OrderDetailCardBuilder;
import org.itss.prj_itss.view.ordering.order.detail.components.OrderStatusRenderer;
import org.itss.prj_itss.view.ordering.order.management.OrderManagementView;

public final class OrderDetailView implements ViewLifecycle {

    private Navigator navigator;
    private OrderDetailController controller;
    private OrderManagementController managementController;

    private String orderIdRaw;
    private Runnable onBackAction;
    private Runnable onCloseAction;
    private boolean isEmbedded = false;

    @FXML
    private StackPane rootPane;

    @FXML
    private StackPane backgroundContainer;

    @FXML
    private Region backdrop;

    @FXML
    private HBox cardWrapper;

    @FXML
    private VBox cardContainer;

    @FXML
    private BorderPane panelRoot;

    @FXML
    private Button backButton;

    @FXML
    private Label subtitleLabel;

    @FXML
    private HBox actionContainer;

    @FXML
    private HBox topStatusContainer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox contentBox;

    public OrderDetailView() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/view/ordering/order/detail/order-detail-view.fxml"));
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load order-detail-view.fxml", e);
        }
    }

    @FXML
    private void initialize() {
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

        // Reset elements to standard (popup card) layout
        rootPane.setStyle("-fx-background-color: #F3F7FB;");
        backgroundContainer.setVisible(true);
        backgroundContainer.setManaged(true);
        backdrop.setVisible(true);
        backdrop.setManaged(true);
        cardWrapper.setPadding(new Insets(30, 30, 30, 30));
        cardContainer.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-border-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.14), 20, 0, 0, 8);");
        cardContainer.setMinWidth(920);
        cardContainer.setPrefWidth(920);
        cardContainer.setMaxWidth(920);

        backgroundContainer.getChildren().clear();
        Node background = loadOrdersBackground();
        background.setEffect(new GaussianBlur(14));
        background.setOpacity(0.96);
        backgroundContainer.getChildren().add(background);

        buildPanelContent();
    }

    public void initAsEmbedded(OrderDetailController controller, String orderIdRaw, Runnable onBackAction, Runnable onCloseAction) {
        this.controller = controller;
        this.orderIdRaw = orderIdRaw;
        this.onBackAction = onBackAction;
        this.onCloseAction = onCloseAction;
        this.isEmbedded = true;
        this.navigator = null;
        this.managementController = null;

        // Reset elements for embedded transparent mode
        rootPane.setStyle("-fx-background-color: transparent;");
        backgroundContainer.setVisible(false);
        backgroundContainer.setManaged(false);
        backdrop.setVisible(false);
        backdrop.setManaged(false);
        cardWrapper.setPadding(Insets.EMPTY);
        cardContainer.setStyle("-fx-background-color: transparent;");
        cardContainer.setEffect(null);
        cardContainer.setMinWidth(Region.USE_COMPUTED_SIZE);
        cardContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        cardContainer.setMaxWidth(Double.MAX_VALUE);

        buildPanelContent();
    }

    @Override
    public void onViewShown() {
        buildPanelContent();
    }

    @FXML
    private void handleBackAction() {
        if (onBackAction != null) {
            onBackAction.run();
        }
    }

    @FXML
    private void handleBackdropClick() {
        if (onBackAction != null) {
            onBackAction.run();
        }
    }

    @FXML
    private void handleCloseAction() {
        if (onCloseAction != null) {
            onCloseAction.run();
        }
    }

    private Node loadOrdersBackground() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/view/ordering/order/management/order-management-view.fxml"));
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

        subtitleLabel.setText("Mã đơn hàng: " + vm.orderCode());

        // Build dynamic action buttons (Cancel, Close)
        actionContainer.getChildren().clear();

        if (vm.cancellable()) {
            Button cancelBtn = OrderCancellationHandler.createCancelButton(vm.orderId(), controller, onBackAction);
            actionContainer.getChildren().add(cancelBtn);
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
            actionContainer.getChildren().add(closeBtn);
        }

        Label topStatusBadge = OrderStatusRenderer.buildTopStatusBadge(vm.status());
        topStatusContainer.getChildren().setAll(topStatusBadge);

        contentBox.getChildren().setAll(
            OrderDetailCardBuilder.buildOverviewCard(vm),
            OrderStatusRenderer.buildProgressCard(vm.status()),
            OrderDetailCardBuilder.buildItemsCard(vm.items())
        );
    }

    private int parseOrderId(String orderIdRaw) {
        try {
            return Integer.parseInt(orderIdRaw.replaceAll("\\D+", ""));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    public Node getView() {
        return rootPane;
    }
}
