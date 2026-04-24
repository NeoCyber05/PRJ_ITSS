package org.itss.prj_itss.dto;

import java.util.List;

public record AllocationResult(List<String> validationErrors) {
    public boolean isValid() {
        return validationErrors.isEmpty();
    }
}
