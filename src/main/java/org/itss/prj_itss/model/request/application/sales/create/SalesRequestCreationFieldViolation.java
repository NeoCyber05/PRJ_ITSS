package org.itss.prj_itss.model.request.application.sales.create;

public record SalesRequestCreationFieldViolation(int lineNumber, String field, String message) {
}
