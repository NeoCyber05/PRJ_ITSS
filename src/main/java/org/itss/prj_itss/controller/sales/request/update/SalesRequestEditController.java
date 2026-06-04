package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.controller.sales.request.shared.ISalesRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.shared.SalesRequestSavedEvent;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditCommandResult;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditDraft;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditLoadResult;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditSaveResult;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditSession;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditUseCase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class SalesRequestEditController {

    private final SalesRequestEditUseCase editUseCase;
    private final SalesRequestEditPresenter presenter;

    public SalesRequestEditController(SalesRequestEditUseCase editUseCase, SalesRequestEditPresenter presenter) {
        this.editUseCase = Objects.requireNonNull(editUseCase, "editUseCase");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
    }

    public void start(
            ISalesRequestEditViewPort screen,
            SalesRequestEditDialogInput input,
            ISalesRequestDialogListener listener
    ) {
        Objects.requireNonNull(screen, "screen");
        Objects.requireNonNull(input, "input");

        SalesRequestEditSession editSession = editUseCase.openSession(input.requestId());
        if (editSession == null) {
            screen.showError("Không tìm thấy yêu cầu cần cập nhật.");
            screen.close();
            return;
        }

        SalesRequestEditLoadResult initialView = editSession.currentView();
        EditActionsAdapter actions = new EditActionsAdapter(
            screen,
            listener,
            editSession,
            initialView.merchandiseOptions()
        );
        screen.bindEvents(actions);
        actions.render(initialView);
    }

    private final class EditActionsAdapter implements ISalesRequestEditActions {

        private final ISalesRequestEditViewPort screen;
        private final ISalesRequestDialogListener listener;
        private final SalesRequestEditSession editSession;
        private final List<MerchandiseOption> merchandiseOptions;

        private EditActionsAdapter(
                ISalesRequestEditViewPort screen,
                ISalesRequestDialogListener listener,
                SalesRequestEditSession editSession,
                List<MerchandiseOption> merchandiseOptions
        ) {
            this.screen = screen;
            this.listener = listener;
            this.editSession = editSession;
            this.merchandiseOptions = List.copyOf(merchandiseOptions);
        }

        @Override
        public void addItemRequested() {
            render(editSession.addBlankItem());
        }

        @Override
        public void deleteItemRequested(int lineId) {
            render(editSession.removeItem(lineId));
        }

        @Override
        public void deleteItemsRequested(List<Integer> lineIds) {
            render(editSession.removeItems(lineIds));
        }

        @Override
        public void merchandiseChanged(int lineId, Integer merchandiseId) {
            render(editSession.changeMerchandise(lineId, merchandiseId));
        }

        @Override
        public void quantityChanged(int lineId, BigDecimal quantity) {
            render(editSession.changeQuantity(lineId, quantity));
        }

        @Override
        public void desiredDateChanged(int lineId, LocalDate desiredDate) {
            render(editSession.changeDesiredDate(lineId, desiredDate));
        }

        @Override
        public void saveRequested() {
            SalesRequestEditSaveResult result = editSession.save();
            if (!result.success() && !result.validationResult().validForm()) {
                SalesRequestEditValidationView validation = presenter.presentValidation(result);
                screen.renderValidation(validation);
                screen.focusFirstViolation(validation.violations());
                return;
            }

            if (!result.success()) {
                screen.showError("Có lỗi xảy ra: " + result.message());
                return;
            }

            screen.showSuccess(result.message());
            if (listener != null) {
                SalesRequestEditDraft draft = result.draft();
                listener.onSalesRequestSaved(new SalesRequestSavedEvent(
                    draft.requestId(),
                    presenter.presentRequestCode(draft)
                ));
            }
            screen.close();
        }

        @Override
        public void cancelRequested() {
            if (listener != null) {
                listener.onSalesRequestEditCancelled(editSession.snapshot().requestId());
            }
            screen.close();
        }

        private void render(SalesRequestEditLoadResult result) {
            screen.render(presenter.present(result));
        }

        private void render(SalesRequestEditCommandResult result) {
            screen.render(presenter.present(new SalesRequestEditLoadResult(
                result.draft(),
                merchandiseOptions,
                result.validationResult()
            )));
        }
    }
}
