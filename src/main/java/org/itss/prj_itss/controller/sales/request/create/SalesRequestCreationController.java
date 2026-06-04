package org.itss.prj_itss.controller.sales.request.create;

import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryService;
import org.itss.prj_itss.model.request.application.sales.create.CreateSalesRequestUseCase;

import java.util.List;

public final class SalesRequestCreationController {

    private final SalesRequestQueryService queryService;
    private final CreateSalesRequestUseCase createUseCase;

    public SalesRequestCreationController(SalesRequestQueryService queryService, CreateSalesRequestUseCase createUseCase) {
        this.queryService = queryService;
        this.createUseCase = createUseCase;
    }

    public MerchandiseOption getMerchandiseOptionByCode(String code) {
        return queryService.findMerchandiseOptionByCode(code);
    }

    public ActionResult createRequest(List<SalesRequestItemSubmission> items) {
        try {
            createUseCase.createRequest(items, "");
            return new ActionResult(true, "Yêu cầu nhập hàng đã được gửi thành công.");
        } catch (Exception e) {
            return new ActionResult(false, e.getMessage());
        }
    }
}
