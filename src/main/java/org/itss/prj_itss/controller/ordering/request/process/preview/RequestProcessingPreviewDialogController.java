package org.itss.prj_itss.controller.ordering.request.process.preview;

import org.itss.prj_itss.controller.ordering.request.process.RequestProcessingLayoutController;
import org.itss.prj_itss.model.request.application.processing.RequestProcessingException;
import org.itss.prj_itss.view.ordering.request.process.state.ProcessingPreviewOrderView;

import java.util.List;
import java.util.Objects;

public final class RequestProcessingPreviewDialogController {

    private final RequestProcessingLayoutController requestProcessingLayoutController;
    private final List<ProcessingPreviewOrderView> previewOrders;

    public RequestProcessingPreviewDialogController(
        RequestProcessingLayoutController requestProcessingLayoutController,
        List<ProcessingPreviewOrderView> previewOrders
    ) {
        this.requestProcessingLayoutController = Objects.requireNonNull(
            requestProcessingLayoutController,
            "requestProcessingLayoutController"
        );
        this.previewOrders = previewOrders == null ? List.of() : List.copyOf(previewOrders);
    }

    public List<ProcessingPreviewOrderView> previewOrders() {
        return previewOrders;
    }

    public SubmitResult submit() {
        try {
            requestProcessingLayoutController.submitAllocatedOrders();
            return SubmitResult.succeeded();
        } catch (RequestProcessingException exception) {
            return SubmitResult.failed();
        }
    }

    public record SubmitResult(boolean success) {
        public static SubmitResult succeeded() {
            return new SubmitResult(true);
        }

        public static SubmitResult failed() {
            return new SubmitResult(false);
        }
    }
}
