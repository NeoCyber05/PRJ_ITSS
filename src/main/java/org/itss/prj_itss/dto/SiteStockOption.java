package org.itss.prj_itss.dto;

import java.util.Map;

public final class SiteStockOption {
    public final int id;
    public final String siteCode;
    public final String name;
    public final String description;
    public final int shipDays;
    public final int airDays;
    public final Map<Integer, Integer> stock;

    public SiteStockOption(
        int id,
        String siteCode,
        String name,
        String description,
        int shipDays,
        int airDays,
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
