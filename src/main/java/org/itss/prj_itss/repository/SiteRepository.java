package org.itss.prj_itss.repository;

import org.itss.prj_itss.entity.Site;

import java.util.List;

public interface SiteRepository {
    List<Site> findAll();
    Site findById(int id);
    Site findBySiteCode(String siteCode);
    int countAll();
}
