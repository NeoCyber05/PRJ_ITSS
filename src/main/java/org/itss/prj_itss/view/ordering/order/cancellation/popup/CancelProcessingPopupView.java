package org.itss.prj_itss.view.ordering.order.cancellation.popup;

import javafx.fxml.FXML;
import org.itss.prj_itss.view.ordering.order.cancellation.OrderCancellationLayoutView;

public final class CancelProcessingPopupView {

    private OrderCancellationLayoutView layoutView;

    public void init(OrderCancellationLayoutView layoutView) {
        this.layoutView = layoutView;
    }

    @FXML
    private void handleCancelAction() {
        if (layoutView != null) {
            layoutView.closePopup();
        }
    }

    @FXML
    private void handleConfirmAction() {
        if (layoutView != null) {
            layoutView.closePopup();
            layoutView.showFeedback("Đã hủy xử lý đơn hàng.", OrderCancellationLayoutView.FeedbackKind.WARNING);
            if (layoutView.getNavigator() != null) {
                layoutView.getNavigator().showView("orders");
            }
        }
    }
}
