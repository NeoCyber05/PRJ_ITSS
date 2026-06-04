package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.catalog.application.CatalogUseCase;
import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.request.application.RequestManagementUseCase;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class SalesRequestCreationApplicationService {

    private final RequestManagementUseCase requestService;
    private final CatalogUseCase catalogUseCase;
    private final SalesRequestCreationValidator validator;
    private final Clock clock;

    public SalesRequestCreationApplicationService(
            RequestManagementUseCase requestService,
            CatalogUseCase catalogUseCase,
            SalesRequestCreationValidator validator,
            Clock clock
    ) {
        this.requestService = Objects.requireNonNull(requestService, "requestService");
        this.catalogUseCase = Objects.requireNonNull(catalogUseCase, "catalogUseCase");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public MerchandiseOption findMerchandiseOptionByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        Merchandise merchandise = catalogUseCase.findByCode(code.trim());
        if (merchandise == null) {
            return null;
        }
        return toMerchandiseOption(merchandise);
    }

    public List<MerchandiseOption> findMerchandiseOptions() {
        return catalogUseCase.findAll().stream()
            .map(this::toMerchandiseOption)
            .toList();
    }

    public SalesRequestCreationResult createRequest(List<SalesRequestCreationItemDraft> items, String note) {
        SalesRequestCreationValidationResult validationResult =
            validator.validate(items, this::findMerchandiseOptionByCode, LocalDate.now(clock));
        if (!validationResult.validForm()) {
            return SalesRequestCreationResult.validationFailed(validationResult);
        }

        try {
            int requestId = requestService.createRequest(toDomainItems(validationResult.validItems()), note);
            return SalesRequestCreationResult.created(requestId);
        } catch (Exception exception) {
            return SalesRequestCreationResult.failed(exception.getMessage());
        }
    }

    private List<RequestMerchandise> toDomainItems(List<SalesRequestCreationValidatedItem> items) {
        return items.stream()
            .map(item -> new RequestMerchandise(
                0,
                item.merchandise().id(),
                item.quantity(),
                item.desiredDate()
            ))
            .toList();
    }

    private MerchandiseOption toMerchandiseOption(Merchandise merchandise) {
        return new MerchandiseOption(
            merchandise.getId(),
            merchandise.getCode(),
            merchandise.getName(),
            merchandise.getUnit()
        );
    }
}
