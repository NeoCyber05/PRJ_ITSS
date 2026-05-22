package org.itss.prj_itss.controller.shared;

import java.util.List;

public record ValidationResult(boolean isValid, List<String> errors) {
}
