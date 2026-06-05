package org.itss.prj_itss.view.ordering.order.cancellation.suggestion;

import javafx.fxml.FXML;
import org.itss.prj_itss.view.ordering.order.cancellation.OrderCancellationLayoutView;

public final class OrderSuggestionPopupView {

    private OrderCancellationLayoutView layoutView;

    public void init(OrderCancellationLayoutView layoutView) {
        this.layoutView = layoutView;
    }

    @FXML
    private void handleCloseAction() {
        if (layoutView != null) {
            layoutView.closePopup();
        }
    }

    @FXML
    private void handleApplyOption1() {
        applyOption(1);
    }

    @FXML
    private void handleApplyOption2() {
        applyOption(2);
    }

    @FXML
    private void handleApplyOption3() {
        applyOption(3);
    }

    @FXML
    private void handleApplyOption4() {
        applyOption(4);
    }

    @FXML
    private void handleApplyOption5() {
        applyOption(5);
    }

    @FXML
    private void handleApplyOption6() {
        applyOption(6);
    }

    private void applyOption(int optionId) {
        if (layoutView == null) return;
        layoutView.closePopup();
        layoutView.getController().handleSuggestAllocation(optionId);
        layoutView.showFeedback("Đã áp dụng Option " + optionId + " thành công.", OrderCancellationLayoutView.FeedbackKind.SUCCESS);
        layoutView.showAllocationScreen();
    }
}
