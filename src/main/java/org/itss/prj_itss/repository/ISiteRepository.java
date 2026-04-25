package org.itss.prj_itss.repository;

import org.itss.prj_itss.entity.Site;

import java.util.List;

public interface ISiteRepository {
    List<Site> findAll();
    List<Site> findAvailableForMerchandiseIds(List<Integer> merchandiseIds);
    Site findById(int id);
    Site findBySiteCode(String siteCode);
    int countAll();
}
