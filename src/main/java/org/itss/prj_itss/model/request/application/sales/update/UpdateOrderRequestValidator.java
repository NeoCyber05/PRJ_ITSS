package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class UpdateOrderRequestValidator {

    public ValidationResult validate(UpdateOrderRequestDraft draft, LocalDate today) {
        List<FieldViolation> violations = new ArrayList<>();
        if (draft.items().isEmpty()) {
            violations.add(new FieldViolation(0, "items", "Phải có ít nhất 1 mặt hàng."));
            return new ValidationResult(violations);
        }

        Set<Integer> seenMerchandiseIds = new HashSet<>();
        for (UpdateOrderRequestItemDraft item : draft.items()) {
            MerchandiseOption merchandise = item.merchandise();
            if (merchandise == null) {
                violations.add(new FieldViolation(item.lineId(), "merchandise", "Vui lòng chọn mặt hàng cho tất cả các dòng."));
            } else if (!seenMerchandiseIds.add(merchandise.id())) {
                violations.add(new FieldViolation(item.lineId(), "merchandise", "Không được chọn trùng mặt hàng."));
            }

            BigDecimal quantity = item.quantity();
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                violations.add(new FieldViolation(item.lineId(), "quantity", "Số lượng phải là số lớn hơn 0."));
            }

            LocalDate desiredDate = item.desiredDate();
            if (desiredDate == null || desiredDate.isBefore(today)) {
                violations.add(new FieldViolation(item.lineId(), "desiredDate", "Ngày nhận không hợp lệ."));
            }
        }
        return new ValidationResult(violations);
    }
}
