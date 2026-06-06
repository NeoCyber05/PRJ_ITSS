package org.itss.prj_itss.model.site.application;

import org.itss.prj_itss.model.site.domain.Site;
import org.itss.prj_itss.model.site.application.port.InventoryRepository;
import org.itss.prj_itss.model.site.application.port.SiteRepository;

import java.util.List;
import java.util.Map;

public final class SiteUseCase {

    private final SiteRepository siteRepository;
    private final InventoryRepository inventoryRepository;

    public SiteUseCase(SiteRepository siteRepository, InventoryRepository inventoryRepository) {
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

    public Map<Integer, Integer> countMerchandiseGroupedBySiteId() {
        return inventoryRepository.countMerchandiseGroupedBySiteId();
    }
}
