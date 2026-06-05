package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SalesRequestEditValidator {

    private final SalesRequestEditSelectionPolicy selectionPolicy = new SalesRequestEditSelectionPolicy();

    public SalesRequestEditValidationResult validate(SalesRequestEditDraft draft, LocalDate today) {
        List<SalesRequestEditFieldViolation> violations = new ArrayList<>();
        if (draft.items().isEmpty()) {
            violations.add(new SalesRequestEditFieldViolation(0, "items", "Phải có ít nhất 1 mặt hàng."));
            return new SalesRequestEditValidationResult(violations);
        }

        for (SalesRequestEditItemDraft item : draft.items()) {
            MerchandiseOption merchandise = item.merchandise();
            if (merchandise == null) {
                violations.add(new SalesRequestEditFieldViolation(item.lineId(), "merchandise", "Vui lòng chọn mặt hàng cho tất cả các dòng."));
            } else if (selectionPolicy.isDuplicateSelection(item.lineId(), merchandise.id(), draft.items())) {
                violations.add(new SalesRequestEditFieldViolation(item.lineId(), "merchandise", "Không được chọn trùng mặt hàng."));
            }

            BigDecimal quantity = item.quantity();
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                violations.add(new SalesRequestEditFieldViolation(item.lineId(), "quantity", "Số lượng phải là số lớn hơn 0."));
            }

            LocalDate desiredDate = item.desiredDate();
            if (desiredDate == null || desiredDate.isBefore(today)) {
                violations.add(new SalesRequestEditFieldViolation(item.lineId(), "desiredDate", "Ngày nhận không hợp lệ."));
            }
        }
        return new SalesRequestEditValidationResult(violations);
    }

    public ValidatedSalesRequestEditDraft validateForSubmission(SalesRequestEditDraft draft, LocalDate today) {
        SalesRequestEditValidationResult validationResult = validate(draft, today);
        if (!validationResult.validForm()) {
            throw new SalesRequestEditValidationException(validationResult);
        }
        return new ValidatedSalesRequestEditDraft(draft.requestId(), draft.requestCode(), draft.items());
    }
}
