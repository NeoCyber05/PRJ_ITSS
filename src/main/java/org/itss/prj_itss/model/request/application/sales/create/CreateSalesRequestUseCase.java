package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import java.util.List;

public interface CreateSalesRequestUseCase {
    int createRequest(List<SalesRequestItemSubmission> items, String note) throws Exception;
}
