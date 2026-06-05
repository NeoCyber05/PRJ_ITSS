package org.itss.prj_itss.view.ordering.request.process.state;

import java.util.Map;

public record ProcessingSiteView(
    int id,
    String siteCode,
    String name,
    String description,
    Integer shipDays,
    Integer airDays,
    Map<Integer, Integer> stock
) {
}
