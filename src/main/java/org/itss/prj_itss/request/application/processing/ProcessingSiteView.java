package org.itss.prj_itss.request.application.processing;

import java.util.Map;

public record ProcessingSiteView(
    int id,
    String siteCode,
    String name,
    String description,
    int shipDays,
    int airDays,
    Map<Integer, Integer> stock
) {
}
