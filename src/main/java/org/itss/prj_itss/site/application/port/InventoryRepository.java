package org.itss.prj_itss.site.application.port;

import java.util.Map;

public interface InventoryRepository {
    Map<Integer, Integer> getInventoryBySiteId(int siteId);
    int getStockQuantity(int siteId, int merchandiseId);
    int getTotalStock(int merchandiseId);
    int countMerchandiseAtSite(int siteId);
}
