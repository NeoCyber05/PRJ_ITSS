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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HW07 - SalesRequestEditValidator White-box C1 Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SalesRequestEditValidatorWhiteBoxC1Test {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 25);
    private static final MerchandiseOption M1 =
        new MerchandiseOption(10, "MH-001", "Item 1", "box");

    private final SalesRequestEditValidator validator = new SalesRequestEditValidator();

    @Test
    @Order(1)
    @DisplayName("WB-01 - Cover B1 true: empty items")
    void validate_shouldCoverEmptyItemsBranch() {
        SalesRequestEditDraft draft = draftOf();

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "WB-01",
            "Cover B1=True",
            "items=[]",
            "validForm=false, violations=[items]",
            result
        );
        assertFieldViolation(result, "items");
    }

    @Test
    @Order(2)
    @DisplayName("WB-02 - Cover all valid false branches and valid submission")
    void validate_shouldCoverValidBranches() {
        SalesRequestEditDraft draft = draftOf(item(1, M1, "1", TODAY));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);
        ValidatedSalesRequestEditDraft validatedDraft =
            validator.validateForSubmission(draft, TODAY);

        logValidationResult(
            "WB-02",
            "Cover B1=False, B2=False, B3=False, B4a=False, B4b=False, B5a=False, B5b=False, B6=False",
            "1 valid item: M1, quantity=1, desiredDate=2026-05-25",
            "validForm=true, submission returns ValidatedSalesRequestEditDraft",
            result
        );
        System.out.println("Submission actual: returned " + validatedDraft.getClass().getSimpleName());
        assertAll(
            () -> assertTrue(result.validForm()),
            () -> assertEquals(1, validatedDraft.items().size())
        );
    }

    @Test
    @Order(3)
    @DisplayName("WB-03 - Cover null merchandise, null quantity, null desiredDate and invalid submission")
    void validate_shouldCoverNullBranchesAndInvalidSubmission() {
        SalesRequestEditDraft draft =
            draftOf(new SalesRequestEditItemDraft(1, null, null, null));

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        SalesRequestEditValidationException exception = assertThrows(
            SalesRequestEditValidationException.class,
            () -> validator.validateForSubmission(draft, TODAY)
        );
        logValidationResult(
            "WB-03",
            "Cover B2=True, B4a=True, B5a=True, B6=True",
            "1 item: merchandise=null, quantity=null, desiredDate=null",
            "validForm=false, violations=[merchandise, quantity, desiredDate], submission throws exception",
            result
        );
        System.out.println("Submission actual: threw " + exception.getClass().getSimpleName());
        assertAll(
            () -> assertFieldViolation(result, "merchandise"),
            () -> assertFieldViolation(result, "quantity"),
            () -> assertFieldViolation(result, "desiredDate")
        );
    }

    @Test
    @Order(4)
    @DisplayName("WB-04 - Cover duplicate merchandise, zero quantity and past desiredDate")
    void validate_shouldCoverDuplicateAndNonNullInvalidBranches() {
        SalesRequestEditDraft draft = draftOf(
            item(1, M1, "1", TODAY),
            item(2, M1, "0", TODAY.minusDays(1))
        );

        SalesRequestEditValidationResult result = validator.validate(draft, TODAY);

        logValidationResult(
            "WB-04",
            "Cover B3=True, B4b=True, B5b=True",
            "2 items: line 1 M1 valid, line 2 M1 quantity=0 desiredDate=2026-05-24",
            "validForm=false, violations contain merchandise, quantity, desiredDate",
            result
        );
        assertAll(
            () -> assertFieldViolation(result, "merchandise"),
            () -> assertFieldViolation(result, "quantity"),
            () -> assertFieldViolation(result, "desiredDate")
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
            String path,
            String input,
            String expected,
            SalesRequestEditValidationResult actual
    ) {
        System.out.println();
        System.out.println("[" + testCaseId + "] " + path);
        System.out.println("Input    : " + input);
        System.out.println("Expected : " + expected);
        System.out.println("Actual   : " + actualValidation(actual));
    }

    private static String actualValidation(SalesRequestEditValidationResult result) {
        return "validForm=" + result.validForm()
            + ", violations=" + result.violations().stream()
                .map(SalesRequestEditFieldViolation::field)
                .toList();
    }
}
