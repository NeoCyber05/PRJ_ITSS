package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class SalesRequestEditUseCase {

    private final SalesRequestEditGateway gateway;
    private final SalesRequestEditMapper mapper;
    private final SalesRequestEditValidator validator;
    private final SalesRequestEditSelectionPolicy selectionPolicy = new SalesRequestEditSelectionPolicy();

    public SalesRequestEditUseCase(
            SalesRequestEditGateway gateway,
            SalesRequestEditMapper mapper,
            SalesRequestEditValidator validator
    ) {
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public SalesRequestEditData loadEditData(int requestId) throws SalesRequestEditException {
        try {
            RequestFormView form = gateway.loadRequest(requestId);
            if (form == null) {
                return SalesRequestEditData.empty(requestId);
            }
            return new SalesRequestEditData(requestId, form, gateway.findMerchandiseOptions());
        } catch (SalesRequestEditGatewayException exception) {
            throw new SalesRequestEditException("Cannot load sales request " + requestId, exception);
        }
    }

    public RequestFormView loadRequest(int requestId) {
        return gateway.loadRequest(requestId);
    }

    public List<MerchandiseOption> findMerchandiseOptions() {
        return gateway.findMerchandiseOptions();
    }

    public SalesRequestEditState buildEditState(RequestFormView form) {
        return mapper.toState(form);
    }

    public SalesRequestEditValidationResult validateDraft(SalesRequestEditDraft draft, LocalDate today) {
        return validator.validate(draft, today);
    }

    public List<MerchandiseOption> availableOptions(
            int currentLineId,
            SalesRequestEditDraft draft,
            List<MerchandiseOption> merchandiseOptions
    ) {
        return selectionPolicy.availableOptions(currentLineId, draft.items(), merchandiseOptions);
    }

    public void updateRequest(SalesRequestEditDraft draft, LocalDate today) throws SalesRequestEditException {
        ValidatedSalesRequestEditDraft validatedDraft = validator.validateForSubmission(draft, today);
        try {
            gateway.updateRequest(validatedDraft.requestId(), mapper.toInput(validatedDraft));
        } catch (SalesRequestEditGatewayException exception) {
            throw new SalesRequestEditException(
                "Cannot update sales request " + validatedDraft.requestId(),
                exception
            );
        }
    }
}
