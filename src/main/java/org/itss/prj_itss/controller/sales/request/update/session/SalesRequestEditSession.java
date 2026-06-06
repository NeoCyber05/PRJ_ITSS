package org.itss.prj_itss.controller.sales.request.update.session;

import org.itss.prj_itss.controller.sales.request.update.SalesRequestDialogListener;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditDialogInput;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestEditViewState;
import org.itss.prj_itss.controller.sales.request.update.SalesRequestSavedEvent;
import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.shared.RequestFormView;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditData;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditDraft;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditException;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditItemDraft;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditState;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditUseCase;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidationException;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidationResult;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SalesRequestEditSession {

    private final SalesRequestEditUseCase useCase;

    private SalesRequestDialogListener listener;
    private SalesRequestEditState state;
    private List<MerchandiseOption> merchandiseOptions = List.of();

    public SalesRequestEditSession(SalesRequestEditUseCase useCase) {
        this.useCase = Objects.requireNonNull(useCase, "useCase");
    }

    public StartResult start(
            SalesRequestEditDialogInput input,
            SalesRequestDialogListener listener) {
        Objects.requireNonNull(input, "input");
        SalesRequestEditData data;
        try {
            data = useCase.loadEditData(input.requestId());
        } catch (SalesRequestEditException exception) {
            reset();
            return StartResult.failed("Có lỗi xảy ra khi tải yêu cầu cần cập nhật.");
        }
        if (!data.found()) {
            reset();
            return StartResult.failed("Không tìm thấy yêu cầu cần cập nhật.");
        }

        this.listener = listener;
        this.state = useCase.buildEditState(data.form());
        this.merchandiseOptions = data.merchandiseOptions();
        return StartResult.success();
    }

    public RequestFormView loadRequest(int requestId) {
        return useCase.loadRequest(requestId);
    }

    public List<MerchandiseOption> findMerchandiseOptions() {
        return useCase.findMerchandiseOptions();
    }

    public SalesRequestEditViewState buildViewModel() {
        SalesRequestEditDraft draft = currentDraft();
        SalesRequestEditValidationResult validationResult = new SalesRequestEditValidationResult(List.of());
        return SalesRequestEditViewState.from(
                draft,
                availableOptionsByLineId(draft),
                validationResult);
    }

    public void handleAddItem() {
        state.addBlankItem();
    }

    public void handleDeleteItem(int lineId) {
        state.removeItem(lineId);
    }

    public void handleDeleteItems(List<Integer> lineIds) {
        if (lineIds == null || lineIds.isEmpty()) {
            return;
        }
        state.removeItems(new LinkedHashSet<>(lineIds));
    }

    public void handleMerchandiseChanged(int lineId, Integer merchandiseId) {
        state.changeMerchandise(lineId, findMerchandiseOption(merchandiseId));
    }

    public void handleQuantityChanged(int lineId, String rawQuantity) {
        state.changeQuantity(lineId, rawQuantity);
    }

    public void handleDesiredDateChanged(int lineId, LocalDate desiredDate) {
        state.changeDesiredDate(lineId, desiredDate);
    }

    public SaveResult handleSave() throws SalesRequestEditException {
        SalesRequestEditDraft draft = currentDraft();
        try {
            useCase.updateRequest(draft, LocalDate.now());
        } catch (SalesRequestEditValidationException exception) {
            return SaveResult.invalid(exception.validationResult());
        }

        if (listener != null) {
            listener.onSalesRequestSaved(new SalesRequestSavedEvent(draft.requestId(), draft.requestCode()));
        }
        return SaveResult.saved("Cập nhật yêu cầu đặt hàng thành công");
    }

    public void handleCancel() {
        if (listener != null && state != null) {
            listener.onSalesRequestEditCancelled(currentDraft().requestId());
        }
    }

    private SalesRequestEditDraft currentDraft() {
        return state.snapshot();
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

    private Map<Integer, List<MerchandiseOption>> availableOptionsByLineId(SalesRequestEditDraft draft) {
        Map<Integer, List<MerchandiseOption>> optionsByLineId = new LinkedHashMap<>();
        for (SalesRequestEditItemDraft item : draft.items()) {
            optionsByLineId.put(
                    item.lineId(),
                    useCase.availableOptions(item.lineId(), draft, merchandiseOptions));
        }
        return optionsByLineId;
    }

    private void reset() {
        listener = null;
        state = null;
        merchandiseOptions = List.of();
    }

    public record SaveResult(
            boolean saved,
            String message,
            SalesRequestEditValidationResult validationResult) {
        public static SaveResult saved(String message) {
            return new SaveResult(true, message, null);
        }

        public static SaveResult invalid(SalesRequestEditValidationResult validationResult) {
            return new SaveResult(false, null, validationResult);
        }
    }

    public record StartResult(boolean started, String message) {
        public static StartResult success() {
            return new StartResult(true, null);
        }

        public static StartResult failed(String message) {
            return new StartResult(false, message);
        }
    }
}
