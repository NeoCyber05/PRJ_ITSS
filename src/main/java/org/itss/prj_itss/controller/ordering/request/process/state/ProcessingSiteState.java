package org.itss.prj_itss.controller.ordering.request.process.state;

import java.util.Map;

public record ProcessingSiteState(
    int id,
    String siteCode,
    String name,
    String description,
    Integer shipDays,
    Integer airDays,
    Map<Integer, Integer> stock
) {
}

