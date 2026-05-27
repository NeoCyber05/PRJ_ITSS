package org.itss.prj_itss.view.ordering.request.detail;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderDetailController;
import org.itss.prj_itss.controller.ordering.order.OrderManagementController;
import org.itss.prj_itss.controller.ordering.request.RequestDetailPopupController;
import org.itss.prj_itss.model.request.application.sales.detail.AllocatedOrderRow;
import org.itss.prj_itss.model.request.application.sales.detail.RequestDetailViewModel;
import org.itss.prj_itss.view.ordering.order.OrderDetailView;
import org.itss.prj_itss.view.shared.ViewLifecycle;
import org.itss.prj_itss.view.shared.ui.StatusBadgeFactory;

public final class RequestDetailPopupView implements ViewLifecycle {

    @FXML
    private StackPane scrollContent;

    @FXML
    private StackPane dialogShell;

    @FXML
    private VBox requestCard;

    @FXML
    private StackPane orderDetailContainer;

    @FXML
    private Label titleLabel;

    @FXML
    private Button closeButton;

    @FXML
    private Label requestCodeValueLabel;

    @FXML
    private Label createdDateValueLabel;

    @FXML
    private Label earliestDeadlineValueLabel;

    @FXML
    private HBox statusContainer;

    @FXML
    private VBox requestItemsTable;

    @FXML
    private VBox allocatedOrdersTable;

    private RequestDetailPopupController controller;
    private OrderDetailController orderDetailController;
    private OrderManagementController orderManagementController;
    private Navigator navigator;

    private double requestCollapsedWidth;
    private double requestExpandedWidth;
    private double orderPanelWidth;

    private HBox currentSelectedRow;
    private AllocatedOrderRow currentSelectedOrder;
    private RequestDetailViewModel currentViewModel;

    public void init(
        Stage dialog,
        String requestCode,
        RequestDetailPopupController controller,
        OrderDetailController orderDetailController,
        OrderManagementController orderManagementController,
        Navigator navigator,
        RequestDetailViewModel viewModel,
        double sceneWidth,
        double requestCollapsedWidth,
        double requestExpandedWidth,
        double orderPanelWidth
    ) {
        this.controller = controller;
        this.orderDetailController = orderDetailController;
        this.orderManagementController = orderManagementController;
        this.navigator = navigator;
        this.requestCollapsedWidth = requestCollapsedWidth;
        this.requestExpandedWidth = requestExpandedWidth;
        this.orderPanelWidth = orderPanelWidth;
        this.currentViewModel = viewModel;

        closeButton.setOnAction(event -> dialog.close());
        StackPane.setAlignment(dialogShell, Pos.TOP_CENTER);
        scrollContent.setMinWidth(Math.max(0, sceneWidth - 1));
        setPanelWidth(requestCard, requestExpandedWidth);
        setPanelWidth(orderDetailContainer, requestExpandedWidth);
        hideOrderDetail();

        titleLabel.setText("Chi tiết " + requestCode);
        requestCodeValueLabel.setText(requestCode);
        createdDateValueLabel.setText(
            viewModel.createdAt() != null && !viewModel.createdAt().isBlank()
                ? viewModel.createdAt()
                : "N/A"
        );
        earliestDeadlineValueLabel.setText(viewModel.earliestDeadline() != null && !viewModel.earliestDeadline().isBlank()
            ? viewModel.earliestDeadline()
            : "N/A"
        );
        statusContainer.getChildren().setAll(StatusBadgeFactory.statusBadge(viewModel.status(), true));

        requestItemsTable.getChildren().setAll(RequestItemTableView.load(viewModel.requestItems()));
        allocatedOrdersTable.getChildren().setAll(AllocatedOrderTableView.load(
            viewModel.allocatedOrders(),
            controller,
            (selectedOrder, selectedRow) -> {
                this.currentSelectedOrder = selectedOrder;
                this.currentSelectedRow = selectedRow;
                showOrderDetail(selectedOrder.orderId());
            }
        ));
    }

    private void showOrderDetail(int orderId) {
        OrderDetailView orderDetailView = new OrderDetailView();
        orderDetailView.init(navigator, orderDetailController, orderManagementController, String.valueOf(orderId));
        orderDetailContainer.getChildren().setAll(orderDetailView.getView());

        requestCard.setVisible(false);
        requestCard.setManaged(false);

        orderDetailContainer.setManaged(true);
        orderDetailContainer.setVisible(true);
    }

    private void hideOrderDetail() {
        orderDetailContainer.getChildren().clear();
        orderDetailContainer.setManaged(false);
        orderDetailContainer.setVisible(false);

        requestCard.setVisible(true);
        requestCard.setManaged(true);

        if (currentSelectedOrder != null && currentSelectedRow != null && controller != null) {
            AllocatedOrderRow refreshed = controller.findOrderRow(currentSelectedOrder.orderId());
            if (refreshed != null && "cancelled".equalsIgnoreCase(refreshed.status())) {
                HBox statusBox = (HBox) currentSelectedRow.getChildren().get(4);
                statusBox.getChildren().setAll(StatusBadgeFactory.statusBadge("cancelled", false));
                
                HBox actionBox = (HBox) currentSelectedRow.getChildren().get(5);
                if (actionBox.getChildren().size() > 1) {
                    actionBox.getChildren().remove(0);
                }
            }
        }
    }

    private void setPanelWidth(Region panel, double width) {
        panel.setMinWidth(width);
        panel.setPrefWidth(width);
        panel.setMaxWidth(width);
    }
}
