package org.itss.prj_itss.model.site.application.self;

import org.itss.prj_itss.model.catalog.domain.Merchandise;
import org.itss.prj_itss.model.site.domain.Site;

import java.util.List;

public record SiteWorkspaceSnapshot(
    boolean available,
    String message,
    Site site,
    List<Merchandise> merchandiseOptions,
    List<SiteInventoryRow> inventoryRows,
    List<SiteOrderRow> orders
) {
    public SiteWorkspaceSnapshot {
        merchandiseOptions = merchandiseOptions == null ? List.of() : List.copyOf(merchandiseOptions);
        inventoryRows = inventoryRows == null ? List.of() : List.copyOf(inventoryRows);
        orders = orders == null ? List.of() : List.copyOf(orders);
    }

    public static SiteWorkspaceSnapshot unavailable(String message) {
        return new SiteWorkspaceSnapshot(false, message, null, List.of(), List.of(), List.of());
    }
}
