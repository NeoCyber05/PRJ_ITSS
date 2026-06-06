package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.controller.sales.request.update.session.SalesRequestEditSession;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryService;
import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandService;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditException;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditMapper;
import org.itss.prj_itss.model.request.application.lock.RequestLockUseCase;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditUseCase;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidator;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class SalesRequestEditController implements SalesRequestEditActionHandler, SalesRequestEditScreenStarter {

    private final SalesRequestEditSession session;
    private SalesRequestEditViewPort view;

    public SalesRequestEditController(
            SalesRequestEditUseCase useCase,
            RequestLockUseCase lockUseCase,
            Supplier<LockOwner> lockOwnerSupplier
    ) {
        this.session = new SalesRequestEditSession(
            Objects.requireNonNull(useCase, "useCase"),
            Objects.requireNonNull(lockUseCase, "lockUseCase"),
            Objects.requireNonNull(lockOwnerSupplier, "lockOwnerSupplier")
        );
    }

    public boolean start(
            SalesRequestEditViewPort view,
            SalesRequestEditDialogInput input,
            SalesRequestDialogListener listener
    ) {
        this.view = Objects.requireNonNull(view, "view");
        this.view.setActionHandler(this);
        SalesRequestEditSession.StartResult startResult = session.start(input, listener);
        if (!startResult.started()) {
            view.showAlertError(startResult.message());
            return false;
        }
        render();
        view.startHeartbeat(this::renewLock);
        return true;
    }

    public RequestFormView loadRequest(int requestId) {
        return session.loadRequest(requestId);
    }

    public List<MerchandiseOption> getMerchandiseOptions() {
        return session.findMerchandiseOptions();
    }

    public void addItemRequested() {
        session.handleAddItem();
        render();
    }

    public void deleteItemRequested(int lineId) {
        SalesRequestEditSession.DeleteResult result = session.handleDeleteItem(lineId);
        if (!result.deleted()) {
            view.showAlertError(result.message());
            return;
        }
        render();
    }

    public void deleteItemsRequested(List<Integer> lineIds) {
        SalesRequestEditSession.DeleteResult result = session.handleDeleteItems(lineIds);
        if (!result.deleted()) {
            view.showAlertError(result.message());
            return;
        }
        render();
    }

    public void merchandiseChanged(int lineId, Integer merchandiseId) {
        session.handleMerchandiseChanged(lineId, merchandiseId);
        render();
    }

    public void quantityChanged(int lineId, String rawQuantity) {
        session.handleQuantityChanged(lineId, rawQuantity);
        render();
    }

    public void desiredDateChanged(int lineId, LocalDate desiredDate) {
        session.handleDesiredDateChanged(lineId, desiredDate);
        render();
    }

    public void saveRequested() {
        SalesRequestEditSession.SaveResult result;
        try {
            result = session.handleSave();
        } catch (SalesRequestEditException exception) {
            view.showError("Có lỗi xảy ra: " + exception.getMessage());
            return;
        }
        if (!result.saved()) {
            SalesRequestEditViewState.Validation validation =
                SalesRequestEditViewState.validationFrom(result.validationResult());
            view.renderValidation(validation);
            view.focusFirstViolation(validation.violations());
            return;
        }
        if (result.message() != null && !result.message().isBlank()) {
            view.showSuccess(result.message());
        }
        view.close();
    }

    public void cancelRequested() {
        session.handleCancel();
        view.close();
    }

    public void renewLock() {
        session.renewLock();
    }

    private void render() {
        view.render(session.buildViewModel());
    }
}
