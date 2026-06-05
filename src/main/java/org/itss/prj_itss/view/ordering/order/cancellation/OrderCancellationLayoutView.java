package org.itss.prj_itss.view.ordering.order.cancellation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.itss.prj_itss.App;
import org.itss.prj_itss.controller.navigation.Navigator;
import org.itss.prj_itss.controller.ordering.order.OrderCancellationProcessingController;
import org.itss.prj_itss.view.ordering.order.cancellation.state.CancelledOrderProcessingSession;
import org.itss.prj_itss.view.ordering.order.cancellation.state.CancelledOrderProcessingViewModel;
import org.itss.prj_itss.view.ordering.request.process.state.ProcessingPreviewOrderView;
import org.itss.prj_itss.view.shared.ViewLifecycle;
import org.itss.prj_itss.view.ordering.order.cancellation.allocation.OrderAllocationView;
import org.itss.prj_itss.view.ordering.order.cancellation.preview.OrderPreviewView;
import org.itss.prj_itss.view.ordering.order.cancellation.popup.ConfirmSubmitPopupView;
import org.itss.prj_itss.view.ordering.order.cancellation.popup.CancelProcessingPopupView;
import org.itss.prj_itss.view.ordering.order.cancellation.popup.DetailNotificationPopupView;
import org.itss.prj_itss.view.ordering.order.cancellation.suggestion.OrderSuggestionPopupView;

import java.util.ArrayList;
import java.util.List;

public final class OrderCancellationLayoutView implements ViewLifecycle {

    private Navigator navigator;
    private OrderCancellationProcessingController controller;
    private int cancelledOrderId = -1;
    private CancelledOrderProcessingViewModel currentViewModel;
    private List<ProcessingPreviewOrderView> currentPreviewOrders = new ArrayList<>();
    private ScreenMode screenMode = ScreenMode.ALLOCATION;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private HBox suggestBar;

    @FXML
    private Button suggestButton;

    @FXML
    private Button cancelProcessingButton;

    @FXML
    private Label feedbackLabel;

    @FXML
    private ScrollPane contentScrollPane;

    @FXML
    private StackPane contentContainer;

    @FXML
    private HBox footerSummaryBox;

    @FXML
    private Button secondaryActionButton;

    @FXML
    private Button primaryActionButton;

    @FXML
    private StackPane modalOverlay;

    @FXML
    private StackPane modalContainer;

    @FXML
    private void initialize() {
        showAllocationScreen();
    }

    public void init(Navigator navigator, OrderCancellationProcessingController controller) {
        this.navigator = navigator;
        this.controller = controller;
        if (cancelledOrderId > 0 && controller != null) {
            controller.start(cancelledOrderId);
            showAllocationScreen();
        }
    }

    public void setCancelledOrderId(int id) {
        this.cancelledOrderId = id;
        if (controller != null) {
            controller.start(id);
            showAllocationScreen();
        }
    }

    @Override
    public void onViewShown() {
        if (cancelledOrderId > 0 && controller != null) {
            controller.start(cancelledOrderId);
            showAllocationScreen();
        }
    }

    @FXML
    private void handleCancelProcessingAction() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/view/ordering/order/cancellation/popup/cancel-processing-popup.fxml"));
            Node node = loader.load();
            CancelProcessingPopupView view = loader.getController();
            view.init(this);
            showPopup(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSuggestAction() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/view/ordering/order/cancellation/suggestion/order-suggestion-popup.fxml"));
            Node node = loader.load();
            OrderSuggestionPopupView view = loader.getController();
            view.init(this);
            showPopup(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePrimaryAction() {
        if (screenMode == ScreenMode.ALLOCATION) {
            createPreviewOrders();
            return;
        }
        if (screenMode == ScreenMode.PREVIEW) {
            showConfirmSubmitPopup();
        }
    }

    @FXML
    private void handleSecondaryAction() {
        if (screenMode == ScreenMode.PREVIEW) {
            showAllocationScreen();
        }
    }

    public void showAllocationScreen() {
        screenMode = ScreenMode.ALLOCATION;
        titleLabel.setText("Phân bổ đơn hàng bị hủy");

        if (controller == null || cancelledOrderId <= 0) {
            subtitleLabel.setText("Chưa tải được dữ liệu đơn hàng.");
            primaryActionButton.setText("Tạo các đơn hàng");
            primaryActionButton.setDisable(true);
            return;
        }

        currentViewModel = controller.buildViewModel();
        subtitleLabel.setText("Đơn hàng: " + currentViewModel.cancelledOrderCode() + "  •  Mã yêu cầu: " + currentViewModel.requestCode());
        suggestBar.setManaged(true);
        suggestBar.setVisible(true);
        cancelProcessingButton.setManaged(true);
        cancelProcessingButton.setVisible(true);
        secondaryActionButton.setManaged(false);
        secondaryActionButton.setVisible(false);
        primaryActionButton.setText("Tạo các đơn hàng");
        primaryActionButton.setDisable(false);

        refreshAllocationScreen();
        updateFooterSummary();
        contentScrollPane.setVvalue(0);
    }

    public void showPreviewScreen() {
        screenMode = ScreenMode.PREVIEW;
        titleLabel.setText("Kết quả các đơn hàng mới");
        subtitleLabel.setText("Đơn hàng: " + currentViewModel.cancelledOrderCode() + " - Gom nhóm theo Site");
        suggestBar.setManaged(false);
        suggestBar.setVisible(false);
        cancelProcessingButton.setManaged(false);
        cancelProcessingButton.setVisible(false);
        secondaryActionButton.setText("← Quay lại");
        secondaryActionButton.setManaged(true);
        secondaryActionButton.setVisible(true);
        primaryActionButton.setText("Gửi yêu cầu");
        primaryActionButton.setDisable(currentPreviewOrders.isEmpty());

        refreshPreviewScreen();
        updateFooterSummary();
        contentScrollPane.setVvalue(0);
    }

    public void refreshAllocationScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/view/ordering/order/cancellation/allocation/order-allocation-view.fxml"));
            Node node = loader.load();
            OrderAllocationView view = loader.getController();
            view.init(this);
            contentContainer.getChildren().setAll(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshPreviewScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/view/ordering/order/cancellation/preview/order-preview-view.fxml"));
            Node node = loader.load();
            OrderPreviewView view = loader.getController();
            view.init(this);
            contentContainer.getChildren().setAll(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPreviewOrders() {
        if (controller == null) return;
        CancelledOrderProcessingSession.ConfirmResult result = controller.handleConfirm();
        if (!result.valid()) {
            showFeedback(result.validationMessage(), FeedbackKind.ERROR);
            return;
        }

        currentPreviewOrders = result.previewOrders();
        showFeedback("Đã tạo bản xem trước các đơn hàng mới.", FeedbackKind.SUCCESS);
        showPreviewScreen();
    }

    private void showConfirmSubmitPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/view/ordering/order/cancellation/popup/confirm-submit-popup.fxml"));
            Node node = loader.load();
            ConfirmSubmitPopupView view = loader.getController();
            view.init(this);
            showPopup(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void submitAllocatedOrders() {
        if (controller == null) return;
        try {
            controller.handleSubmit();
            closePopup();
            showDetailNotificationPopup();
        } catch (Exception exception) {
            showFeedback(exception.getMessage(), FeedbackKind.ERROR);
        }
    }

    private void showDetailNotificationPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/itss/prj_itss/view/ordering/order/cancellation/popup/detail-notification-popup.fxml"));
            Node node = loader.load();
            DetailNotificationPopupView view = loader.getController();
            view.init(this);
            showPopup(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showPopup(Node popupNode) {
        modalContainer.getChildren().setAll(popupNode);
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    public void closePopup() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        modalContainer.getChildren().clear();
    }

    public void showFeedback(String message, FeedbackKind kind) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().removeAll(
            "cancelled-order-feedback-info",
            "cancelled-order-feedback-success",
            "cancelled-order-feedback-warning",
            "cancelled-order-feedback-error"
        );
        feedbackLabel.getStyleClass().add(kind.cssClass());
        feedbackLabel.setManaged(true);
        feedbackLabel.setVisible(true);
    }

    private void updateFooterSummary() {
        footerSummaryBox.getChildren().clear();
        if (screenMode == ScreenMode.PREVIEW) {
            Label label = new Label(currentPreviewOrders.size() + " đơn hàng mới theo site");
            label.getStyleClass().add("cancelled-order-muted-text");
            footerSummaryBox.getChildren().add(label);
        }
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public OrderCancellationProcessingController getController() {
        return controller;
    }

    public CancelledOrderProcessingViewModel getCurrentViewModel() {
        return currentViewModel;
    }

    public List<ProcessingPreviewOrderView> getCurrentPreviewOrders() {
        return currentPreviewOrders;
    }

    public ScreenMode getScreenMode() {
        return screenMode;
    }

    public enum ScreenMode {
        ALLOCATION,
        PREVIEW
    }

    public enum FeedbackKind {
        INFO("cancelled-order-feedback-info"),
        SUCCESS("cancelled-order-feedback-success"),
        WARNING("cancelled-order-feedback-warning"),
        ERROR("cancelled-order-feedback-error");

        private final String cssClass;

        FeedbackKind(String cssClass) {
            this.cssClass = cssClass;
        }

        public String cssClass() {
            return cssClass;
        }
    }
}
