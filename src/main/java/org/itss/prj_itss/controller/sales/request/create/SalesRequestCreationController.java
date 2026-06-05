package org.itss.prj_itss.controller.sales.request.create;

import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.controller.shared.MerchandiseOptionDTO;
import org.itss.prj_itss.controller.shared.SalesRequestItemInput;
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

    public MerchandiseOptionDTO getMerchandiseOptionByCode(String code) {
        MerchandiseOption m = queryService.findMerchandiseOptionByCode(code);
        if (m == null) return null;
        return toDTO(m);
    }

    public int getAvailableStock(String code) {
        return queryService.getAvailableStock(code);
    }

    public List<String> getAllMerchandiseCodes() {
        return queryService.findMerchandiseOptions().stream()
            .map(MerchandiseOption::code)
            .toList();
    }

    public List<String> suggestMerchandiseCodes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        String lowerKeyword = keyword.toLowerCase();
        return queryService.findMerchandiseOptions().stream()
            .map(MerchandiseOption::code)
            .filter(code -> code.toLowerCase().contains(lowerKeyword))
            .limit(5)
            .toList();
    }

    public ActionResult createRequest(List<SalesRequestItemInput> items) {
        try {
            List<SalesRequestItemSubmission> submissions = items.stream()
                .map(i -> new SalesRequestItemSubmission(i.merchandiseId(), i.quantity(), i.desiredDate()))
                .toList();
            createUseCase.createRequest(submissions, "");
            return new ActionResult(true, "Yêu cầu nhập hàng đã được gửi thành công.");
        } catch (Exception e) {
            return new ActionResult(false, e.getMessage());
        }
    }

    private MerchandiseOptionDTO toDTO(MerchandiseOption m) {
        return new MerchandiseOptionDTO(m.id(), m.code(), m.name(), m.unit());
    }
}

