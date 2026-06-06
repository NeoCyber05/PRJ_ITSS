package org.itss.prj_itss.controller.ordering.request.process;

import org.itss.prj_itss.controller.ordering.request.process.session.RequestProcessingSession;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingException;
import org.itss.prj_itss.controller.ordering.request.process.state.AllocationChangeCommand;
import org.itss.prj_itss.controller.ordering.request.process.state.AllocationChangeResult;
import org.itss.prj_itss.controller.ordering.request.process.state.ProcessingPreviewOrder;
import org.itss.prj_itss.controller.ordering.request.process.state.RequestProcessingState;
import org.itss.prj_itss.controller.ordering.request.process.state.SuggestedPlanState;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class RequestProcessingLayoutController {

    private final RequestProcessingSession session;

    public RequestProcessingLayoutController(RequestProcessingUseCase requestProcessingUseCase) {
        this.session = new RequestProcessingSession(Objects.requireNonNull(requestProcessingUseCase, "requestProcessingUseCase"));
    }

    public void setRequestId(int requestId) {
        session.start(requestId);
    }

    public RequestProcessingState snapshot() {
        return session.buildState();
    }

    public void handleSiteFilterChanged(Set<Integer> excludedSiteIds, Set<Integer> selectedSiteIds) {
        session.handleSiteFilterChanged(excludedSiteIds, selectedSiteIds);
    }

    public boolean handleOptimizeAllocation() {
        return session.handleOptimizeAllocation();
    }

    public List<SuggestedPlanState> handleShowAllPlans() {
        return session.handleShowAllPlans();
    }

    public void applySelectedPlan(String signature) {
        session.applySelectedPlanBySignature(signature);
    }

    public AllocationChangeResult handleAllocationInputChanged(AllocationChangeCommand request) {
        return session.handleAllocationInputChanged(request);
    }

    public void toggleExpandedItem(int index) {
        session.toggleExpandedItem(index);
    }

    public ConfirmResult handleConfirm() {
        RequestProcessingSession.ConfirmResult sessionResult = session.handleConfirm();
        if (!sessionResult.valid()) {
            return ConfirmResult.invalid(sessionResult.validationMessage());
        }
        return ConfirmResult.valid(sessionResult.previewOrders());
    }

    public String validateCurrentSubmission() {
        return session.validateCurrentSubmission();
    }

    public List<ProcessingPreviewOrder> buildPreviewOrders() {
        return session.buildPreviewOrders();
    }

    public void submitAllocatedOrders() throws RequestProcessingException {
        session.submitAllocatedOrders();
    }

    public record ConfirmResult(String validationMessage, List<ProcessingPreviewOrder> previewOrders) {
        public static ConfirmResult invalid(String validationMessage) {
            return new ConfirmResult(validationMessage, List.of());
        }

        public static ConfirmResult valid(List<ProcessingPreviewOrder> previewOrders) {
            return new ConfirmResult(null, List.copyOf(previewOrders));
        }

        public boolean valid() {
            return validationMessage == null;
        }
    }
}
