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

@DisplayName("HW07 - SalesRequestEditValidator Black-box Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SalesRequestEditValidatorBlackBoxTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 25);
    private static final MerchandiseOption M1 =
        new MerchandiseOption(10, "MH-001", "Item 1", "box");

    private final SalesRequestEditValidator validator = new SalesRequestEditValidator();

    @Test
    @Order(1)
    @DisplayName("BB-01 - Valid draft should have no violations")
    void validate_shouldHaveNoViolations_whenDraftIsValid() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-01",
            "Vung hop le day du",
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
    @Order(2)
    @DisplayName("BB-02 - Empty item list should be invalid")
    void validate_shouldReject_whenItemsEmpty() {
        SalesRequestEditDraft draft = draftOf();

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-02",
            "Vung khong hop le cua danh sach item",
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
    @Order(3)
    @DisplayName("BB-03 - Missing merchandise should be invalid")
    void validate_shouldReject_whenMerchandiseMissing() {
        SalesRequestEditDraft draft = draftOf(item(1, null, "1", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-03",
            "Vung khong hop le cua merchandise",
            "1 item: merchandise=null, quantity=1, desiredDate=2026-05-25",
            "validForm=false, violations=[merchandise]",
            result
        );
        assertFieldViolation(result, "merchandise");
    }

    @Test
    @Order(4)
    @DisplayName("BB-04 - Duplicate merchandise should be invalid")
    void validate_shouldReject_whenMerchandiseDuplicated() {
        SalesRequestEditDraft draft = draftOf(
            item(1, M1, "1", TODAY),
            item(2, M1, "1", TODAY)
        );

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-04",
            "Vung khong hop le do trung merchandise",
            "2 items: line 1 M1, line 2 M1",
            "validForm=false, violations contains merchandise",
            result
        );
        assertFieldViolation(result, "merchandise");
    }

    @Test
    @Order(5)
    @DisplayName("BB-05 - Null quantity should be invalid")
    void validate_shouldReject_whenQuantityNull() {
        SalesRequestEditDraft draft = draftOf(new SalesRequestEditItemDraft(1, M1, null, TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-05",
            "Vung khong hop le: quantity null",
            "1 item: M1, quantity=null, desiredDate=2026-05-25",
            "validForm=false, violations=[quantity]",
            result
        );
        assertFieldViolation(result, "quantity");
    }

    @Test
    @Order(6)
    @DisplayName("BB-06 - Negative quantity should be invalid")
    void validate_shouldReject_whenQuantityNegative() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "-1", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-06",
            "Bien duoi khong hop le cua quantity",
            "1 item: M1, quantity=-1, desiredDate=2026-05-25",
            "validForm=false, violations=[quantity]",
            result
        );
        assertFieldViolation(result, "quantity");
    }

    @Test
    @Order(7)
    @DisplayName("BB-07 - Zero quantity should be invalid")
    void validate_shouldReject_whenQuantityZero() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "0", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-07",
            "Bien tai ranh gioi khong hop le cua quantity",
            "1 item: M1, quantity=0, desiredDate=2026-05-25",
            "validForm=false, violations=[quantity]",
            result
        );
        assertFieldViolation(result, "quantity");
    }

    @Test
    @Order(8)
    @DisplayName("BB-08 - Positive decimal quantity should be valid")
    void validate_shouldAcceptQuantity_whenQuantityPositiveDecimal() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "0.01", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-08",
            "Bien duoi hop le cua quantity",
            "1 item: M1, quantity=0.01, desiredDate=2026-05-25",
            "validForm=true, no quantity violation",
            result
        );
        assertNoFieldViolation(result, "quantity");
    }

    @Test
    @Order(9)
    @DisplayName("BB-09 - Null desired date should be invalid")
    void validate_shouldReject_whenDesiredDateNull() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", null));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-09",
            "Vung khong hop le: desiredDate null",
            "1 item: M1, quantity=1, desiredDate=null",
            "validForm=false, violations=[desiredDate]",
            result
        );
        assertFieldViolation(result, "desiredDate");
    }

    @Test
    @Order(10)
    @DisplayName("BB-10 - Desired date before today should be invalid")
    void validate_shouldReject_whenDesiredDateBeforeToday() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", TODAY.minusDays(1)));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-10",
            "Bien khong hop le truoc ngay hien tai",
            "1 item: M1, quantity=1, desiredDate=2026-05-24",
            "validForm=false, violations=[desiredDate]",
            result
        );
        assertFieldViolation(result, "desiredDate");
    }

    @Test
    @Order(11)
    @DisplayName("BB-11 - Desired date equal today should be valid")
    void validate_shouldAcceptDesiredDate_whenDateIsToday() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-11",
            "Bien hop le dung ngay hien tai",
            "1 item: M1, quantity=1, desiredDate=2026-05-25",
            "validForm=true, no desiredDate violation",
            result
        );
        assertNoFieldViolation(result, "desiredDate");
    }

    @Test
    @Order(12)
    @DisplayName("BB-12 - Desired date after today should be valid")
    void validate_shouldAcceptDesiredDate_whenDateAfterToday() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", TODAY.plusDays(1)));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-12",
            "Vung hop le sau ngay hien tai",
            "1 item: M1, quantity=1, desiredDate=2026-05-26",
            "validForm=true, no desiredDate violation",
            result
        );
        assertNoFieldViolation(result, "desiredDate");
    }

    @Test
    @Order(13)
    @DisplayName("BB-13 - Multiple invalid fields should produce multiple violations")
    void validate_shouldReturnMultipleViolations_whenMultipleFieldsInvalid() {
        SalesRequestEditDraft draft = draftOf(item(1, null, "0", TODAY.minusDays(1)));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "BB-13",
            "Ket hop nhieu vung khong hop le",
            "1 item: merchandise=null, quantity=0, desiredDate=2026-05-24",
            "validForm=false, violations=[merchandise, quantity, desiredDate]",
            result
        );
        assertAll(
            () -> assertFieldViolation(result, "merchandise"),
            () -> assertFieldViolation(result, "quantity"),
            () -> assertFieldViolation(result, "desiredDate")
        );
    }

    @Test
    @Order(14)
    @DisplayName("BB-14 - Valid submission should return validated draft")
    void validateForSubmission_shouldReturnValidatedDraft_whenDraftValid() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", TODAY));

        ValidatedSalesRequestEditDraft validatedDraft =
            assertDoesNotThrow(() -> validator.validateForSubmission(draft, TODAY));

        logSubmissionResult(
            "BB-14",
            "Submission hop le",
            "validateForSubmission(valid draft)",
            "returns ValidatedSalesRequestEditDraft",
            "returned " + validatedDraft.getClass().getSimpleName()
        );
        assertEquals(draft.requestId(), validatedDraft.requestId());
    }

    @Test
    @Order(15)
    @DisplayName("BB-15 - Invalid submission should throw validation exception")
    void validateForSubmission_shouldThrow_whenDraftInvalid() {
        SalesRequestEditDraft draft = draftOf(item(1, null, "0", TODAY.minusDays(1)));

        SalesRequestEditValidationException exception = assertThrows(
            SalesRequestEditValidationException.class,
            () -> validator.validateForSubmission(draft, TODAY)
        );
        logSubmissionResult(
            "BB-15",
            "Submission khong hop le",
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

    private static void assertNoFieldViolation(
            SalesRequestEditValidationResult result,
            String field
    ) {
        assertFalse(
            result.violations().stream().anyMatch(violation -> violation.field().equals(field)),
            "Expected no violation field: " + field
        );
    }

    private static void logValidationResult(
            String testCaseId,
            String description,
            String input,
            String expected,
            SalesRequestEditValidationResult actual
    ) {
        log(testCaseId, description, input, expected, actualValidation(actual));
    }

    private static void logSubmissionResult(
            String testCaseId,
            String description,
            String input,
            String expected,
            String actual
    ) {
        log(testCaseId, description, input, expected, actual);
    }

    private static void log(
            String testCaseId,
            String description,
            String input,
            String expected,
            String actual
    ) {
        System.out.println();
        System.out.println("[" + testCaseId + "] " + description);
        System.out.println("Input    : " + input);
        System.out.println("Expected : " + expected);
        System.out.println("Actual   : " + actual);
    }

    private static String actualValidation(SalesRequestEditValidationResult result) {
        return "validForm=" + result.validForm()
            + ", violations=" + result.violations().stream()
                .map(SalesRequestEditFieldViolation::field)
                .toList();
    }
}
