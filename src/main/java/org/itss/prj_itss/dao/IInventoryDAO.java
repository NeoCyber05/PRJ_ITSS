package org.itss.prj_itss.dao;

import java.util.Map;

public interface IInventoryDAO {
    Map<Integer, Integer> getInventoryBySiteId(int siteId);
    int getStockQuantity(int siteId, int merchandiseId);
    int getTotalStock(int merchandiseId);
    int countMerchandiseAtSite(int siteId);
}
