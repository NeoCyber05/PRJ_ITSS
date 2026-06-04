package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandPort;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class SalesRequestCreationApplicationService {

    private final SalesRequestCommandPort commandPort;
    private final MerchandiseUseCase merchandiseUseCase;
    private final SalesRequestCreationValidator validator;
    private final Clock clock;

    public SalesRequestCreationApplicationService(
            SalesRequestCommandPort commandPort,
            MerchandiseUseCase merchandiseUseCase,
            SalesRequestCreationValidator validator,
            Clock clock
    ) {
        this.commandPort = Objects.requireNonNull(commandPort, "commandPort");
        this.merchandiseUseCase = Objects.requireNonNull(merchandiseUseCase, "merchandiseUseCase");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public MerchandiseOption findMerchandiseOptionByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        Merchandise merchandise = merchandiseUseCase.findByCode(code.trim());
        if (merchandise == null) {
            return null;
        }
        return toMerchandiseOption(merchandise);
    }

    public List<MerchandiseOption> findMerchandiseOptions() {
        return merchandiseUseCase.findActive().stream()
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
            Request request = new Request(note);
            toDomainItems(validationResult.validItems()).forEach(item ->
                request.addItem(
                    item.getMerchandiseId(),
                    item.getQuantityOrdered(),
                    item.getDesiredDeliveryDate()
                )
            );
            int requestId = commandPort.createRequest(request);
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
