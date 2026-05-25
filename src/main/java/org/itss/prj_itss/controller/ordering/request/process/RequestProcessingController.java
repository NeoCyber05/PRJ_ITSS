package org.itss.prj_itss.controller.ordering.request.process;

import org.itss.prj_itss.model.request.application.processing.AllocationChangeCommand;
import org.itss.prj_itss.model.request.application.processing.AllocationChangeResultView;
import org.itss.prj_itss.model.request.application.processing.ProcessingPreviewOrderView;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingSession;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingViewModel;
import org.itss.prj_itss.model.request.application.processing.SuggestedPlanView;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingUseCase;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingException;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class RequestProcessingController {

    private final RequestProcessingSession session;

    public RequestProcessingController(RequestProcessingUseCase requestProcessingUseCase) {
        this.session = new RequestProcessingSession(Objects.requireNonNull(requestProcessingUseCase, "requestProcessingUseCase"));
    }

    public void setRequestId(int requestId) {
        session.start(requestId);
    }

    public RequestProcessingViewModel snapshot() {
        return session.buildViewModel();
    }

    public void handleSiteFilterChanged(Set<Integer> excludedSiteIds, Set<Integer> prioritySiteIds) {
        session.handleSiteFilterChanged(excludedSiteIds, prioritySiteIds);
    }

    public void handleOptimizeAllocation() {
        session.handleOptimizeAllocation();
    }

    public List<SuggestedPlanView> handleShowAllPlans() {
        return session.handleShowAllPlans();
    }

    public void applySelectedPlan(String signature) {
        session.applySelectedPlanBySignature(signature);
    }

    public AllocationChangeResultView handleAllocationInputChanged(AllocationChangeCommand request) {
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

    public List<ProcessingPreviewOrderView> buildPreviewOrders() {
        return session.buildPreviewOrderViews();
    }

    public void submitAllocatedOrders() throws RequestProcessingException {
        session.submitAllocatedOrders();
    }

    public record ConfirmResult(String validationMessage, List<ProcessingPreviewOrderView> previewOrders) {
        public static ConfirmResult invalid(String validationMessage) {
            return new ConfirmResult(validationMessage, List.of());
        }

        public static ConfirmResult valid(List<ProcessingPreviewOrderView> previewOrders) {
            return new ConfirmResult(null, List.copyOf(previewOrders));
        }

        public boolean valid() {
            return validationMessage == null;
        }
    }
}
