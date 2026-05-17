package org.itss.prj_itss.request.presentation.ordering.process.preview;

import org.itss.prj_itss.request.presentation.ordering.process.RequestProcessingController;
import org.itss.prj_itss.request.business.service.RequestProcessingPreviewBuilder.PreviewOrder;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public final class RequestProcessingPreviewDialogController {

    private final RequestProcessingController requestProcessingController;
    private final List<PreviewOrder> previewOrders;

    public RequestProcessingPreviewDialogController(
        RequestProcessingController requestProcessingController,
        List<PreviewOrder> previewOrders
    ) {
        this.requestProcessingController = Objects.requireNonNull(
            requestProcessingController,
            "requestProcessingController"
        );
        this.previewOrders = previewOrders == null ? List.of() : List.copyOf(previewOrders);
    }

    public List<PreviewOrder> previewOrders() {
        return previewOrders;
    }

    public SubmitResult submit() {
        try {
            requestProcessingController.submitAllocatedOrders();
            return SubmitResult.succeeded();
        } catch (SQLException exception) {
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

