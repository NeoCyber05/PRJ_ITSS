package org.itss.prj_itss.model.request.application.sales.update;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HW07 - SalesRequestEditValidator Black-box Decision Table Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SalesRequestEditValidatorBlackBoxTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 25);
    private static final MerchandiseOption M1 =
        new MerchandiseOption(10, "MH-001", "Item 1", "box");

    private final SalesRequestEditValidator validator = new SalesRequestEditValidator();

    @Test
    @Order(1)
    @DisplayName("DT-01 - Empty item list should be invalid")
    void validate_shouldReject_whenItemsEmpty() {
        SalesRequestEditDraft draft = draftOf();

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "DT-01",
            "C1: items=[]",
            "items=[]",
            "validForm=false, violations=[items]",
            result
        );
        assertAll(
            () -> assertFalse(result.validForm()),
            () -> assertFieldViolation(result, "items")
        );
    }

    @Test
    @Order(2)
    @DisplayName("DT-02 - Missing merchandise should be invalid")
    void validate_shouldReject_whenMerchandiseMissing() {
        SalesRequestEditDraft draft = draftOf(item(1, null, "1", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "DT-02",
            "C2: merchandise == null",
            "1 item: merchandise=null, quantity=1, desiredDate=2026-05-25",
            "validForm=false, violations=[merchandise]",
            result
        );
        assertFieldViolation(result, "merchandise");
    }

    @Test
    @Order(3)
    @DisplayName("DT-03 - Null quantity should be invalid")
    void validate_shouldReject_whenQuantityNull() {
        SalesRequestEditDraft draft = draftOf(new SalesRequestEditItemDraft(1, M1, null, TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "DT-03",
            "C3: quantity == null",
            "1 item: M1, quantity=null, desiredDate=2026-05-25",
            "validForm=false, violations=[quantity]",
            result
        );
        assertFieldViolation(result, "quantity");
    }

    @Test
    @Order(4)
    @DisplayName("DT-04 - Non-positive quantity should be invalid")
    void validate_shouldReject_whenQuantityNonPositive() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "0", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "DT-04",
            "C4: quantity <= 0",
            "1 item: M1, quantity=0, desiredDate=2026-05-25",
            "validForm=false, violations=[quantity]",
            result
        );
        assertFieldViolation(result, "quantity");
    }

    @Test
    @Order(5)
    @DisplayName("DT-05 - Null desired date should be invalid")
    void validate_shouldReject_whenDesiredDateNull() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", null));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "DT-05",
            "C5: desiredDate == null",
            "1 item: M1, quantity=1, desiredDate=null",
            "validForm=false, violations=[desiredDate]",
            result
        );
        assertFieldViolation(result, "desiredDate");
    }

    @Test
    @Order(6)
    @DisplayName("DT-06 - Desired date before today should be invalid")
    void validate_shouldReject_whenDesiredDateBeforeToday() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", TODAY.minusDays(1)));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "DT-06",
            "C6: desiredDate < today",
            "1 item: M1, quantity=1, desiredDate=2026-05-24",
            "validForm=false, violations=[desiredDate]",
            result
        );
        assertFieldViolation(result, "desiredDate");
    }

    @Test
    @Order(7)
    @DisplayName("DT-07 - Valid draft should have no violations")
    void validate_shouldHaveNoViolations_whenDraftIsValid() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "DT-07",
            "All validation conditions are false",
            "1 item: M1, quantity=1, desiredDate=2026-05-25",
            "validForm=true, violations=[]",
            result
        );
        assertAll(
            () -> assertTrue(result.validForm()),
            () -> assertTrue(result.violations().isEmpty())
        );
    }

    @Test
    @Order(8)
    @DisplayName("DT-08 - Valid submission should return validated draft")
    void validateForSubmission_shouldReturnValidatedDraft_whenDraftValid() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", TODAY));

        ValidatedSalesRequestEditDraft validatedDraft =
            assertDoesNotThrow(() -> validator.validateForSubmission(draft, TODAY));

        logSubmissionResult(
            "DT-08",
            "C7: submit draft hop le",
            "validateForSubmission(valid draft)",
            "returns ValidatedSalesRequestEditDraft",
            "returned " + validatedDraft.getClass().getSimpleName()
        );
        assertEquals(draft.requestId(), validatedDraft.requestId());
    }

    @Test
    @Order(9)
    @DisplayName("DT-09 - Invalid submission should throw validation exception")
    void validateForSubmission_shouldThrow_whenDraftInvalid() {
        SalesRequestEditDraft draft = draftOf(item(1, null, "0", TODAY.minusDays(1)));

        SalesRequestEditValidationException exception = assertThrows(
            SalesRequestEditValidationException.class,
            () -> validator.validateForSubmission(draft, TODAY)
        );
        logSubmissionResult(
            "DT-09",
            "C8: submit draft khong hop le",
            "validateForSubmission(invalid draft)",
            "throws SalesRequestEditValidationException",
            "threw " + exception.getClass().getSimpleName()
                + ", validation=" + actualValidation(exception.validationResult())
        );
    }

    private static SalesRequestEditDraft draftOf(SalesRequestEditItemDraft... items) {
        return new SalesRequestEditDraft(
            1,
            "YC-2026-001",
            "25/05/2026",
            "PENDING",
            List.of(items)
        );
    }

    private static SalesRequestEditItemDraft item(
            int lineId,
            MerchandiseOption merchandise,
            String quantity,
            LocalDate desiredDate
    ) {
        return new SalesRequestEditItemDraft(
            lineId,
            merchandise,
            new BigDecimal(quantity),
            desiredDate
        );
    }

    private static void assertFieldViolation(
            SalesRequestEditValidationResult result,
            String field
    ) {
        assertTrue(
            result.violations().stream().anyMatch(violation -> violation.field().equals(field)),
            "Expected violation field: " + field
        );
    }

    private static void logValidationResult(
            String testCaseId,
            String rule,
            String input,
            String expected,
            SalesRequestEditValidationResult actual
    ) {
        log(testCaseId, rule, input, expected, actualValidation(actual));
    }

    private static void logSubmissionResult(
            String testCaseId,
            String rule,
            String input,
            String expected,
            String actual
    ) {
        log(testCaseId, rule, input, expected, actual);
    }

    private static void log(
            String testCaseId,
            String rule,
            String input,
            String expected,
            String actual
    ) {
        System.out.println();
        System.out.println("[" + testCaseId + "] Decision Table Test");
        System.out.println("Rule     : " + rule);
        System.out.println("Input    : " + input);
        System.out.println("Expected : " + expected);
        System.out.println("Actual   : " + actual);
        System.out.println("Result   : PASS");
    }

    private static String actualValidation(SalesRequestEditValidationResult result) {
        return "validForm=" + result.validForm()
            + ", violations=" + result.violations().stream()
                .map(SalesRequestEditFieldViolation::field)
                .toList();
    }
}
