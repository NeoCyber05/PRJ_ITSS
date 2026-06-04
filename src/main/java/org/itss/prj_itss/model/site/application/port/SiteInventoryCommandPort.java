package org.itss.prj_itss.model.site.application.port;

public interface SiteInventoryCommandPort {
    void upsertInventoryItem(int siteId, int merchandiseId, int stockQuantity);
    void removeInventoryItem(int siteId, int merchandiseId);
}
