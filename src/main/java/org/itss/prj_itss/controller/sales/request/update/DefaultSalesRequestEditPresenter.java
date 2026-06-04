package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.model.request.application.port.RequestDisplayFormatter;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditDraft;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditFieldViolation;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditItemDraft;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditLoadResult;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditSaveResult;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidationResult;

import java.util.List;
import java.util.Objects;

public final class DefaultSalesRequestEditPresenter implements SalesRequestEditPresenter {

    private final RequestDisplayFormatter formatter;

    public DefaultSalesRequestEditPresenter(RequestDisplayFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    @Override
    public SalesRequestEditViewState present(SalesRequestEditLoadResult result) {
        return toViewState(result.draft(), result.merchandiseOptions(), result.validationResult());
    }

    @Override
    public SalesRequestEditValidationView presentValidation(SalesRequestEditValidationResult validationResult) {
        return new SalesRequestEditValidationView(toViolationViews(validationResult.violations()));
    }

    @Override
    public SalesRequestEditValidationView presentValidation(SalesRequestEditSaveResult result) {
        return presentValidation(result.validationResult());
    }

    @Override
    public String presentRequestCode(SalesRequestEditDraft draft) {
        return formatter.formatRequestCode(draft.requestId());
    }

    SalesRequestEditViewState toViewState(
            SalesRequestEditDraft draft,
            List<MerchandiseOption> merchandiseOptions,
            SalesRequestEditValidationResult validationResult
    ) {
        return new SalesRequestEditViewState(
            presentRequestCode(draft),
            formatter.formatDateOrEmpty(draft.createdAt()),
            draft.status(),
            merchandiseOptions.stream().map(this::toMerchandiseView).toList(),
            draft.items().stream().map(this::toItemView).toList(),
            presentValidation(validationResult)
        );
    }

    private SalesRequestEditItemView toItemView(SalesRequestEditItemDraft item) {
        return new SalesRequestEditItemView(
            item.lineId(),
            toMerchandiseView(item.merchandise()),
            item.quantity(),
            item.desiredDate()
        );
    }

    private SalesRequestEditMerchandiseOptionView toMerchandiseView(MerchandiseOption merchandise) {
        if (merchandise == null) {
            return null;
        }
        return new SalesRequestEditMerchandiseOptionView(
            merchandise.id(),
            merchandise.code(),
            merchandise.name(),
            merchandise.unit()
        );
    }

    private List<SalesRequestEditFieldViolationView> toViolationViews(
            List<SalesRequestEditFieldViolation> violations
    ) {
        return violations.stream()
            .map(violation -> new SalesRequestEditFieldViolationView(
                violation.lineId(),
                violation.field(),
                violation.message()
            ))
            .toList();
    }
}
