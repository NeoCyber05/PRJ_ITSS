package org.itss.prj_itss.service;

import org.itss.prj_itss.entity.Site;
import org.itss.prj_itss.repository.IInventoryRepository;
import org.itss.prj_itss.repository.ISiteRepository;

import java.util.List;
import java.util.Map;

public final class SiteService {

    private final ISiteRepository siteRepository;
    private final IInventoryRepository inventoryRepository;

    public SiteService(ISiteRepository siteRepository, IInventoryRepository inventoryRepository) {
        this.siteRepository = siteRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<Site> findAll() {
        return siteRepository.findAll();
    }

    public Site findById(int id) {
        return siteRepository.findById(id);
    }

    public int countAll() {
        return siteRepository.countAll();
    }

    public Map<Integer, Integer> getInventoryBySiteId(int siteId) {
        return inventoryRepository.getInventoryBySiteId(siteId);
    }

    public int getTotalStock(int merchandiseId) {
        return inventoryRepository.getTotalStock(merchandiseId);
    }

    public int countMerchandiseAtSite(int siteId) {
        return inventoryRepository.countMerchandiseAtSite(siteId);
    }
}
