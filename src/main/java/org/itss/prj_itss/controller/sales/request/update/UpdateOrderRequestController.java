package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.controller.shared.ActionResult;
import org.itss.prj_itss.controller.sales.request.shared.OrderRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.shared.OrderRequestUpdatedEvent;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.create.RequestItemInput;
import org.itss.prj_itss.model.request.application.sales.RequestSalesApplicationService;
import org.itss.prj_itss.model.request.application.sales.update.UpdateOrderRequestDraft;
import org.itss.prj_itss.model.request.application.sales.update.UpdateOrderRequestFormMapper;
import org.itss.prj_itss.model.request.application.sales.update.UpdateOrderRequestFormState;
import org.itss.prj_itss.model.request.application.sales.update.UpdateOrderRequestValidator;
import org.itss.prj_itss.model.request.application.sales.update.ValidationResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class UpdateOrderRequestController {

    private final RequestSalesApplicationService salesService;
    private final UpdateOrderRequestFormMapper formMapper;
    private final UpdateOrderRequestValidator validator;

    public UpdateOrderRequestController(RequestSalesApplicationService salesService) {
        this(salesService, new UpdateOrderRequestFormMapper(), new UpdateOrderRequestValidator());
    }

    UpdateOrderRequestController(
            RequestSalesApplicationService salesService,
            UpdateOrderRequestFormMapper formMapper,
            UpdateOrderRequestValidator validator
    ) {
        this.salesService = salesService;
        this.formMapper = formMapper;
        this.validator = validator;
    }

    public void start(
            UpdateOrderRequestScreen screen,
            UpdateOrderRequestDialogInput input,
            OrderRequestDialogListener listener
    ) {
        Objects.requireNonNull(screen, "screen");
        Objects.requireNonNull(input, "input");

        RequestFormView form = loadRequest(input.requestId());
        if (form == null) {
            screen.showError("Không tìm thấy yêu cầu cần cập nhật.");
            screen.close();
            return;
        }

        UpdateSession session = new UpdateSession(
            screen,
            listener,
            formMapper.toState(form),
            getMerchandiseOptions()
        );
        screen.bindEvents(session);
        session.render();
    }

    public RequestFormView loadRequest(int requestId) {
        return salesService.findFormView(requestId);
    }

    public List<MerchandiseOption> getMerchandiseOptions() {
        return salesService.findMerchandiseOptions();
    }

    public ActionResult updateRequest(int requestId, List<RequestItemInput> items) {
        try {
            salesService.updateRequest(requestId, items, null);
            return new ActionResult(true, "Cập nhật yêu cầu đặt hàng thành công");
        } catch (Exception e) {
            return new ActionResult(false, e.getMessage());
        }
    }

    private final class UpdateSession implements UpdateOrderRequestEvents {

        private final UpdateOrderRequestScreen screen;
        private final OrderRequestDialogListener listener;
        private final UpdateOrderRequestFormState state;
        private final List<MerchandiseOption> merchandiseOptions;

        private UpdateSession(
                UpdateOrderRequestScreen screen,
                OrderRequestDialogListener listener,
                UpdateOrderRequestFormState state,
                List<MerchandiseOption> merchandiseOptions
        ) {
            this.screen = screen;
            this.listener = listener;
            this.state = state;
            this.merchandiseOptions = List.copyOf(merchandiseOptions);
        }

        @Override
        public void addItemRequested() {
            state.addBlankItem();
            render();
        }

        @Override
        public void deleteItemRequested(int lineId) {
            state.removeItem(lineId);
            render();
        }

        @Override
        public void deleteItemsRequested(List<Integer> lineIds) {
            if (lineIds == null || lineIds.isEmpty()) {
                return;
            }
            state.removeItems(lineIds.stream().collect(Collectors.toSet()));
            render();
        }

        @Override
        public void merchandiseChanged(int lineId, Integer merchandiseId) {
            state.changeMerchandise(lineId, findMerchandiseOption(merchandiseId));
            render();
        }

        @Override
        public void quantityChanged(int lineId, BigDecimal quantity) {
            state.changeQuantity(lineId, quantity);
            render();
        }

        @Override
        public void desiredDateChanged(int lineId, LocalDate desiredDate) {
            state.changeDesiredDate(lineId, desiredDate);
            render();
        }

        @Override
        public void saveRequested() {
            UpdateOrderRequestDraft draft = state.snapshot();
            ValidationResult validationResult = validator.validate(draft, LocalDate.now());
            if (!validationResult.validForm()) {
                screen.renderValidation(validationResult);
                screen.focusFirstViolation(validationResult.violations());
                return;
            }

            ActionResult result = updateRequest(draft.requestId(), formMapper.toInput(draft));
            if (!result.success()) {
                screen.showError("Có lỗi xảy ra: " + result.message());
                return;
            }

            screen.showSuccess(result.message());
            if (listener != null) {
                listener.onOrderRequestUpdated(new OrderRequestUpdatedEvent(draft.requestId(), draft.requestCode()));
            }
            screen.close();
        }

        @Override
        public void cancelRequested() {
            if (listener != null) {
                listener.onOrderRequestCancelled(state.snapshot().requestId());
            }
            screen.close();
        }

        private void render() {
            UpdateOrderRequestDraft draft = state.snapshot();
            ValidationResult validationResult = validator.validate(draft, LocalDate.now());
            screen.render(new UpdateOrderRequestViewModel(
                draft.requestCode(),
                draft.createdAt(),
                draft.status(),
                merchandiseOptions,
                draft,
                validationResult
            ));
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
