package org.itss.prj_itss.model.site.application.port;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public interface InventoryRepository {
    Map<Integer, Integer> getInventoryBySiteId(int siteId);
    int getStockQuantity(int siteId, int merchandiseId);
    int getTotalStock(int merchandiseId);
    int countMerchandiseAtSite(int siteId);
    Map<Integer, Integer> countMerchandiseGroupedBySiteId();

    default Map<Integer, Map<Integer, Integer>> getInventoryBySiteIds(Collection<Integer> siteIds) {
        Map<Integer, Map<Integer, Integer>> inventories = new LinkedHashMap<>();
        if (siteIds == null) {
            return inventories;
        }
        for (Integer siteId : siteIds) {
            if (siteId != null) {
                inventories.put(siteId, getInventoryBySiteId(siteId));
            }
        }
        return inventories;
    }

    default Map<Integer, Integer> getTotalStockByMerchandiseIds(Collection<Integer> merchandiseIds) {
        Map<Integer, Integer> stockByMerchandiseId = new LinkedHashMap<>();
        if (merchandiseIds == null) {
            return stockByMerchandiseId;
        }
        for (Integer merchandiseId : merchandiseIds) {
            if (merchandiseId != null) {
                stockByMerchandiseId.put(merchandiseId, getTotalStock(merchandiseId));
            }
        }
        return stockByMerchandiseId;
    }
}
