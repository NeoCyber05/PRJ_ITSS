package org.itss.prj_itss.model.request.domain.processing;

import java.util.Map;

public final class SiteStockOption {
    public final int id;
    public final String siteCode;
    public final String name;
    public final String description;
    public final Integer shipDays;
    public final Integer airDays;
    public final Map<Integer, Integer> stock;

    public SiteStockOption(
        int id,
        String siteCode,
        String name,
        String description,
        Integer shipDays,
        Integer airDays,
        Map<Integer, Integer> stock
    ) {
        this.id = id;
        this.siteCode = siteCode;
        this.name = name;
        this.description = description;
        this.shipDays = shipDays;
        this.airDays = airDays;
        this.stock = stock;
    }
}

