package org.itss.prj_itss.model.request.application.sales.create;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class SalesRequestCreationValidator {

    public SalesRequestCreationValidationResult validate(
            List<SalesRequestCreationItemDraft> items,
            Function<String, MerchandiseOption> merchandiseResolver,
            LocalDate today
    ) {
        List<SalesRequestCreationFieldViolation> violations = new ArrayList<>();
        List<SalesRequestCreationValidatedItem> validItems = new ArrayList<>();

        if (items == null || items.isEmpty()) {
            violations.add(new SalesRequestCreationFieldViolation(0, "items", "Cần ít nhất một mặt hàng để tạo yêu cầu."));
            return new SalesRequestCreationValidationResult(violations, validItems);
        }

        Set<Integer> seenMerchandiseIds = new HashSet<>();
        for (int index = 0; index < items.size(); index++) {
            SalesRequestCreationItemDraft item = items.get(index);
            validateItem(index + 1, item, merchandiseResolver, today, seenMerchandiseIds, violations, validItems);
        }
        return new SalesRequestCreationValidationResult(violations, validItems);
    }

    private void validateItem(
            int lineNumber,
            SalesRequestCreationItemDraft item,
            Function<String, MerchandiseOption> merchandiseResolver,
            LocalDate today,
            Set<Integer> seenMerchandiseIds,
            List<SalesRequestCreationFieldViolation> violations,
            List<SalesRequestCreationValidatedItem> validItems
    ) {
        MerchandiseOption merchandise = resolveMerchandise(lineNumber, item, merchandiseResolver, seenMerchandiseIds, violations);
        BigDecimal quantity = parseQuantity(lineNumber, item, violations);
        LocalDate desiredDate = validateDesiredDate(lineNumber, item, today, violations);

        if (merchandise == null || quantity == null || desiredDate == null) {
            return;
        }
        validItems.add(new SalesRequestCreationValidatedItem(merchandise, quantity, desiredDate));
    }

    private MerchandiseOption resolveMerchandise(
            int lineNumber,
            SalesRequestCreationItemDraft item,
            Function<String, MerchandiseOption> merchandiseResolver,
            Set<Integer> seenMerchandiseIds,
            List<SalesRequestCreationFieldViolation> violations
    ) {
        String code = item == null || item.merchandiseCode() == null ? "" : item.merchandiseCode().trim();
        if (code.isBlank()) {
            violations.add(new SalesRequestCreationFieldViolation(lineNumber, "merchandiseCode", "Vui lòng nhập mã hàng cho tất cả các dòng."));
            return null;
        }

        MerchandiseOption merchandise = merchandiseResolver.apply(code);
        if (merchandise == null) {
            violations.add(new SalesRequestCreationFieldViolation(lineNumber, "merchandiseCode", "Mã hàng \"" + code + "\" không tồn tại trong hệ thống."));
            return null;
        }

        if (!seenMerchandiseIds.add(merchandise.id())) {
            violations.add(new SalesRequestCreationFieldViolation(lineNumber, "merchandiseCode", "Không được chọn trùng mặt hàng."));
            return null;
        }
        return merchandise;
    }

    private BigDecimal parseQuantity(
            int lineNumber,
            SalesRequestCreationItemDraft item,
            List<SalesRequestCreationFieldViolation> violations
    ) {
        String rawQuantity = item == null ? null : item.quantityText();
        if (rawQuantity == null || rawQuantity.isBlank()) {
            violations.add(new SalesRequestCreationFieldViolation(lineNumber, "quantity", "Số lượng phải là số lớn hơn 0."));
            return null;
        }

        try {
            BigDecimal quantity = new BigDecimal(rawQuantity.trim());
            if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                return quantity;
            }
        } catch (NumberFormatException exception) {
            // Handled below with the common validation message.
        }

        violations.add(new SalesRequestCreationFieldViolation(lineNumber, "quantity", "Số lượng phải là số lớn hơn 0."));
        return null;
    }

    private LocalDate validateDesiredDate(
            int lineNumber,
            SalesRequestCreationItemDraft item,
            LocalDate today,
            List<SalesRequestCreationFieldViolation> violations
    ) {
        LocalDate desiredDate = item == null ? null : item.desiredDate();
        if (desiredDate == null || desiredDate.isBefore(today)) {
            violations.add(new SalesRequestCreationFieldViolation(lineNumber, "desiredDate", "Ngày nhận không hợp lệ."));
            return null;
        }
        return desiredDate;
    }
}
