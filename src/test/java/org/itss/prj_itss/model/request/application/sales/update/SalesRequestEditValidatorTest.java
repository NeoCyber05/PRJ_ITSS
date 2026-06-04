package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesRequestEditValidatorTest {

    private final SalesRequestEditValidator validator = new SalesRequestEditValidator();

    @Test
    void validDraftHasNoViolations() {
        LocalDate today = LocalDate.of(2026, 5, 25);
        SalesRequestEditDraft draft = new SalesRequestEditDraft(
            1,
            LocalDateTime.of(2026, 5, 25, 0, 0),
            "pending",
            List.of(new SalesRequestEditItemDraft(
                1,
                new MerchandiseOption(10, "MH-001", "Item 1", "box"),
                new BigDecimal("2"),
                today
            ))
        );

        SalesRequestEditValidationResult result = validator.validate(draft, today);

        assertTrue(result.validForm());
        assertTrue(result.violations().isEmpty());
    }

    @Test
    void duplicateMerchandiseInvalidQuantityAndPastDateProduceViolations() {
        LocalDate today = LocalDate.of(2026, 5, 25);
        MerchandiseOption option = new MerchandiseOption(10, "MH-001", "Item 1", "box");
        SalesRequestEditDraft draft = new SalesRequestEditDraft(
            1,
            LocalDateTime.of(2026, 5, 25, 0, 0),
            "pending",
            List.of(
                new SalesRequestEditItemDraft(1, option, BigDecimal.ONE, today),
                new SalesRequestEditItemDraft(2, option, BigDecimal.ZERO, today.minusDays(1))
            )
        );

        SalesRequestEditValidationResult result = validator.validate(draft, today);

        assertFalse(result.validForm());
        assertTrue(result.hasViolation(2));
        assertTrue(result.violations().stream().anyMatch(v -> v.field().equals("merchandise")));
        assertTrue(result.violations().stream().anyMatch(v -> v.field().equals("quantity")));
        assertTrue(result.violations().stream().anyMatch(v -> v.field().equals("desiredDate")));
    }
}
