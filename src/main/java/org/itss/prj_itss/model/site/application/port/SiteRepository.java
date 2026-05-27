package org.itss.prj_itss.model.site.application.port;

import org.itss.prj_itss.model.site.domain.Site;

import java.util.List;

public interface SiteRepository {
    List<Site> findAll();
    List<Site> findAvailableForMerchandiseIds(List<Integer> merchandiseIds);
    Site findById(int id);
    Site findBySiteCode(String siteCode);
    int countAll();
}
