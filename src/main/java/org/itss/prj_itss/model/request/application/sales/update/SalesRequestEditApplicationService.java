package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.merchandise.application.MerchandiseUseCase;
import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandPort;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryPort;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.SalesRequestItemSubmission;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class SalesRequestEditApplicationService implements SalesRequestEditUseCase {

    private final SalesRequestQueryPort queryPort;
    private final SalesRequestCommandPort commandPort;
    private final MerchandiseUseCase merchandiseUseCase;
    private final SalesRequestEditMapper mapper;
    private final SalesRequestEditValidator validator;
    private final Clock clock;

    public SalesRequestEditApplicationService(
            SalesRequestQueryPort queryPort,
            SalesRequestCommandPort commandPort,
            MerchandiseUseCase merchandiseUseCase,
            SalesRequestEditMapper mapper,
            SalesRequestEditValidator validator,
            Clock clock
    ) {
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort");
        this.commandPort = Objects.requireNonNull(commandPort, "commandPort");
        this.merchandiseUseCase = Objects.requireNonNull(merchandiseUseCase, "merchandiseUseCase");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public SalesRequestEditSession openSession(int requestId) {
        Request request = queryPort.findById(requestId);
        if (request == null) {
            return null;
        }
        List<MerchandiseOption> merchandiseOptions = findMerchandiseOptions();
        SalesRequestEditState state = mapper.toState(
            request,
            queryPort.findItemsByRequestId(requestId),
            merchandiseOptions
        );
        return new EditSession(state, merchandiseOptions);
    }

    private List<MerchandiseOption> findMerchandiseOptions() {
        return merchandiseUseCase.findActive().stream()
            .map(this::toMerchandiseOption)
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

    private final class EditSession implements SalesRequestEditSession {

        private final SalesRequestEditState state;
        private final List<MerchandiseOption> merchandiseOptions;

        private EditSession(SalesRequestEditState state, List<MerchandiseOption> merchandiseOptions) {
            this.state = Objects.requireNonNull(state, "state");
            this.merchandiseOptions = List.copyOf(merchandiseOptions);
        }

        @Override
        public SalesRequestEditLoadResult currentView() {
            SalesRequestEditDraft draft = state.snapshot();
            return new SalesRequestEditLoadResult(draft, merchandiseOptions, validate(draft));
        }

        @Override
        public SalesRequestEditCommandResult addBlankItem() {
            state.addBlankItem();
            return currentCommandResult();
        }

        @Override
        public SalesRequestEditCommandResult removeItem(int lineId) {
            state.removeItem(lineId);
            return currentCommandResult();
        }

        @Override
        public SalesRequestEditCommandResult removeItems(List<Integer> lineIds) {
            if (lineIds == null || lineIds.isEmpty()) {
                return currentCommandResult();
            }
            Set<Integer> selectedLineIds = lineIds.stream().collect(Collectors.toSet());
            state.removeItems(selectedLineIds);
            return currentCommandResult();
        }

        @Override
        public SalesRequestEditCommandResult changeMerchandise(int lineId, Integer merchandiseId) {
            state.changeMerchandise(lineId, findMerchandiseOption(merchandiseId));
            return currentCommandResult();
        }

        @Override
        public SalesRequestEditCommandResult changeQuantity(int lineId, BigDecimal quantity) {
            state.changeQuantity(lineId, quantity);
            return currentCommandResult();
        }

        @Override
        public SalesRequestEditCommandResult changeDesiredDate(int lineId, LocalDate desiredDate) {
            state.changeDesiredDate(lineId, desiredDate);
            return currentCommandResult();
        }

        @Override
        public SalesRequestEditSaveResult save() {
            SalesRequestEditDraft draft = state.snapshot();
            SalesRequestEditValidationResult validationResult = validate(draft);
            if (!validationResult.validForm()) {
                return SalesRequestEditSaveResult.validationFailed(draft, validationResult);
            }

            try {
                commandPort.updateRequestItems(draft.requestId(), toDomainItems(draft), null);
                return SalesRequestEditSaveResult.saved(draft, "Cập nhật yêu cầu đặt hàng thành công");
            } catch (Exception exception) {
                return SalesRequestEditSaveResult.failed(draft, validationResult, exception.getMessage());
            }
        }

        @Override
        public SalesRequestEditDraft snapshot() {
            return state.snapshot();
        }

        private SalesRequestEditCommandResult currentCommandResult() {
            SalesRequestEditDraft draft = state.snapshot();
            return new SalesRequestEditCommandResult(draft, validate(draft));
        }

        private SalesRequestEditValidationResult validate(SalesRequestEditDraft draft) {
            return validator.validate(draft, LocalDate.now(clock));
        }

        private List<RequestMerchandise> toDomainItems(SalesRequestEditDraft draft) {
            return mapper.toInput(draft).stream()
                .map(item -> toDomainItem(draft.requestId(), item))
                .toList();
        }

        private RequestMerchandise toDomainItem(int requestId, SalesRequestItemSubmission item) {
            return new RequestMerchandise(
                requestId,
                item.merchandiseId(),
                item.quantityOrdered(),
                item.desiredDeliveryDate()
            );
        }

        private MerchandiseOption findMerchandiseOption(Integer merchandiseId) {
            if (merchandiseId == null) {
                return null;
            }
            return merchandiseOptions.stream()
                .filter(option -> option.id() == merchandiseId)
                .findFirst()
                .orElse(null);
        }
    }
}
