package org.itss.prj_itss.controller.ordering.order;

import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingException;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingSession;
import org.itss.prj_itss.model.order.application.cancellation.CancelledOrderProcessingViewModel;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeCommand;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeResultView;

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

    public void handleSuggestAllocation() {
        session.handleSuggestAllocation();
    }

    public AllocationChangeResultView handleAllocationInputChanged(AllocationChangeCommand command) {
        return session.handleAllocationInputChanged(command);
    }

    public CancelledOrderProcessingSession.ConfirmResult handleConfirm() {
        return session.handleConfirm();
    }

    public void handleSubmit() throws CancelledOrderProcessingException {
        session.submitAllocatedOrders();
    }

    public void toggleExpandedItem(int index) {
        session.toggleExpandedItem(index);
    }
}
