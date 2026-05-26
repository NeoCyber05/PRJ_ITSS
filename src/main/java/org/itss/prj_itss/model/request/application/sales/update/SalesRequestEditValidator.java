package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SalesRequestEditValidator {

    public SalesRequestEditValidationResult validate(SalesRequestEditDraft draft, LocalDate today) {
        List<SalesRequestEditFieldViolation> violations = new ArrayList<>();
        if (draft.items().isEmpty()) {
            violations.add(new SalesRequestEditFieldViolation(0, "items", "Phải có ít nhất 1 mặt hàng."));
            return new SalesRequestEditValidationResult(violations);
        }

        Set<Integer> seenMerchandiseIds = new HashSet<>();
        for (SalesRequestEditItemDraft item : draft.items()) {
            MerchandiseOption merchandise = item.merchandise();
            if (merchandise == null) {
                violations.add(new SalesRequestEditFieldViolation(item.lineId(), "merchandise", "Vui lòng chọn mặt hàng cho tất cả các dòng."));
            } else if (!seenMerchandiseIds.add(merchandise.id())) {
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
}
