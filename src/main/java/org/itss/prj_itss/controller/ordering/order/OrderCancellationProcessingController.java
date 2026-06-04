package org.itss.prj_itss.controller.ordering.order;

import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingException;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingSession;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingViewModel;
import org.itss.prj_itss.view.ordering.request.process.state.AllocationChangeCommand;
import org.itss.prj_itss.view.ordering.request.process.state.AllocationChangeResultView;

import java.util.Objects;

public final class OrderCancellationProcessingController {

    private final CancelledOrderProcessingSession session;

    public OrderCancellationProcessingController(CancelledOrderProcessingSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    public void start(int cancelledOrderId) {
        session.start(cancelledOrderId);
    }

    public CancelledOrderProcessingViewModel buildViewModel() {
        return session.buildViewModel();
    }

    public void handleSuggestAllocation(int optionId) {
        session.handleSuggestAllocation(optionId);
    }

    public AllocationChangeResultView handleAllocationInputChanged(AllocationChangeCommand command) {
        CancelledOrderProcessingSession.AllocationChangeResult result = session.handleAllocationInputChanged(
            new CancelledOrderProcessingSession.AllocationChangeCommand(
                command.itemMerchandiseId(),
                command.siteId(),
                command.quantityText(),
                command.transportLabel()
            )
        );
        return new AllocationChangeResultView(
            result.applied(),
            result.errorType(),
            result.stock(),
            result.deliveryDays(),
            result.dayDelta(),
            result.deliveryAvailable(),
            result.deliveryStatusText(),
            result.deliveryStatusClass()
        );
    }

    public ConfirmResult handleConfirm() {
        CancelledOrderProcessingSession.ConfirmResult result = session.handleConfirm();
        if (!result.valid()) {
            return ConfirmResult.invalid(result.validationMessage());
        }
        return ConfirmResult.valid(result.previewOrders().stream()
            .map(order -> new org.itss.prj_itss.view.ordering.request.process.state.ProcessingPreviewOrderView(
                order.siteName(),
                order.siteCode(),
                order.lines().stream()
                    .map(line -> new org.itss.prj_itss.view.ordering.request.process.state.ProcessingPreviewOrderView.ProcessingPreviewLineView(
                        line.merchandiseCode(),
                        line.merchandiseName(),
                        line.quantity(),
                        line.transport(),
                        line.desiredDate(),
                        line.estimatedDate()
                    ))
                    .toList()
            ))
            .toList());
    }

    public void handleSubmit() throws CancelledOrderProcessingException {
        session.submitAllocatedOrders();
    }

    public void toggleExpandedItem(int index) {
        session.toggleExpandedItem(index);
    }

    public record ConfirmResult(
        String validationMessage,
        java.util.List<org.itss.prj_itss.view.ordering.request.process.state.ProcessingPreviewOrderView> previewOrders
    ) {
        public static ConfirmResult invalid(String validationMessage) {
            return new ConfirmResult(validationMessage, java.util.List.of());
        }

        public static ConfirmResult valid(
            java.util.List<org.itss.prj_itss.view.ordering.request.process.state.ProcessingPreviewOrderView> previewOrders
        ) {
            return new ConfirmResult(null, java.util.List.copyOf(previewOrders));
        }

        public boolean valid() {
            return validationMessage == null;
        }
    }
}
