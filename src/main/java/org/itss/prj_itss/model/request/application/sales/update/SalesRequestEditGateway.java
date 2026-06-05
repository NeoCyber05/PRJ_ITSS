package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;

import java.util.List;

public interface SalesRequestEditGateway {

    RequestFormView loadRequest(int requestId) throws SalesRequestEditGatewayException;

    List<MerchandiseOption> findMerchandiseOptions() throws SalesRequestEditGatewayException;

    void updateRequest(
            int requestId,
            List<SalesRequestItemSubmission> items
    ) throws SalesRequestEditGatewayException;
}
