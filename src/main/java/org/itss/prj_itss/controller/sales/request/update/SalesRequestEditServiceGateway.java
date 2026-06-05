package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandService;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryService;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditGateway;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditGatewayException;

import java.util.List;
import java.util.Objects;

public final class SalesRequestEditServiceGateway implements SalesRequestEditGateway {

    private final SalesRequestQueryService queryService;
    private final SalesRequestCommandService commandService;

    public SalesRequestEditServiceGateway(
            SalesRequestQueryService queryService,
            SalesRequestCommandService commandService
    ) {
        this.queryService = Objects.requireNonNull(queryService, "queryService");
        this.commandService = Objects.requireNonNull(commandService, "commandService");
    }

    @Override
    public RequestFormView loadRequest(int requestId) throws SalesRequestEditGatewayException {
        try {
            return queryService.findFormView(requestId);
        } catch (RuntimeException exception) {
            throw new SalesRequestEditGatewayException("Cannot load sales request " + requestId, exception);
        }
    }

    @Override
    public List<MerchandiseOption> findMerchandiseOptions() throws SalesRequestEditGatewayException {
        try {
            return queryService.findMerchandiseOptions();
        } catch (RuntimeException exception) {
            throw new SalesRequestEditGatewayException("Cannot load merchandise options", exception);
        }
    }

    @Override
    public void updateRequest(
            int requestId,
            List<SalesRequestItemSubmission> items
    ) throws SalesRequestEditGatewayException {
        try {
            commandService.updateRequest(requestId, items, null);
        } catch (Exception exception) {
            throw new SalesRequestEditGatewayException("Cannot update sales request " + requestId, exception);
        }
    }
}
