package org.itss.prj_itss.controller.sales.request.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditDraft;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditFieldViolation;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditItemDraft;
import org.itss.prj_itss.model.request.application.sales.update.SalesRequestEditValidationResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record SalesRequestEditViewState(
        String requestCode,
        String createdAt,
        String status,
        List<Item> items,
        Map<Integer, List<MerchandiseOption>> availableOptionsByLineId,
        Validation validation
) {

    public SalesRequestEditViewState {
        items = List.copyOf(items);
        availableOptionsByLineId = availableOptionsByLineId.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> List.copyOf(entry.getValue())
            ));
    }

    public static SalesRequestEditViewState from(
            SalesRequestEditDraft draft,
            Map<Integer, List<MerchandiseOption>> availableOptionsByLineId,
            SalesRequestEditValidationResult validationResult
    ) {
        return new SalesRequestEditViewState(
            draft.requestCode(),
            draft.createdAt(),
            draft.status(),
            draft.items().stream().map(SalesRequestEditViewState::itemFrom).toList(),
            availableOptionsByLineId,
            validationFrom(validationResult)
        );
    }

    public static Validation validationFrom(SalesRequestEditValidationResult validationResult) {
        return new Validation(validationResult.violations().stream()
            .map(SalesRequestEditViewState::violationFrom)
            .toList());
    }

    private static Item itemFrom(SalesRequestEditItemDraft item) {
        return new Item(item.lineId(), item.merchandise(), item.quantity(), item.rawQuantity(), item.desiredDate());
    }

    private static FieldViolation violationFrom(SalesRequestEditFieldViolation violation) {
        return new FieldViolation(violation.lineId(), violation.field(), violation.message());
    }

    public record Item(
            int lineId,
            MerchandiseOption merchandise,
            BigDecimal quantity,
            String rawQuantity,
            LocalDate desiredDate
    ) {
    }

    public record Validation(List<FieldViolation> violations) {

        public Validation {
            violations = List.copyOf(violations);
        }

        public boolean validForm() {
            return violations.isEmpty();
        }

        public String firstMessage() {
            return violations.isEmpty() ? "" : violations.get(0).message();
        }

        public boolean hasViolation(int lineId) {
            return violations.stream().anyMatch(violation -> violation.lineId() == lineId);
        }

        public boolean hasViolation(int lineId, String field) {
            return violations.stream()
                .anyMatch(violation -> violation.lineId() == lineId && violation.field().equals(field));
        }
    }

    public record FieldViolation(int lineId, String field, String message) {
    }
}
